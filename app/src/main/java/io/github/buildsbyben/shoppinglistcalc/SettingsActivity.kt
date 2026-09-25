package io.github.buildsbyben.shoppinglistcalc

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class SettingsActivity : Activity() {
    private val bg = Color.BLACK
    private val text = Color.WHITE
    private val muted = Color.WHITE
    private lateinit var store: ShoppingListStore
    private var rows: LinearLayout? = null
    private var customFormat: LinearLayout? = null
    private var symbol: EditText? = null
    private var decimal: EditText? = null
    private var grouping: EditText? = null
    private var digits: EditText? = null
    private var taxInput: EditText? = null
    private var budgetInput: EditText? = null
    private var symbolPosition: RadioGroup? = null
    private var currencyChoices: RadioGroup? = null
    private var priceChoices: RadioGroup? = null
    private var flowChoices: RadioGroup? = null
    private var weightChoices: RadioGroup? = null
    private var budgetFormat: CurrencyFormat? = null

    private var rawBudgetValue: Double = 0.0

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        getWindow().setStatusBarColor(bg)
        getWindow().setNavigationBarColor(bg)
        store = ShoppingListStore(getSharedPreferences(PREFS, MODE_PRIVATE))
        render()
    }

    private fun render() {
        val screen = column()
        screen.setBackgroundColor(bg)
        val header = LinearLayout(this)
        header.setGravity(Gravity.CENTER_VERTICAL)
        header.setPadding(dp(16), dp(16), dp(16), dp(8))
        val title = label("Settings", 29, text, true)
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        val save = primaryButton("Save")
        save.setOnClickListener(View.OnClickListener { v: View? -> if (saveAll()) finish() })
        header.addView(save, LinearLayout.LayoutParams(-2, -2))
        screen.addView(header)
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)
        rows = column()
        rows!!.setPadding(dp(16), dp(8), dp(16), dp(28))
        scroll.addView(rows)
        screen.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        section("Budget and tax")
        addBudgetAndTaxSettings()
        section("Money")
        addCurrencySettings()
        section("Price entry")
        priceChoices = radios()
        val quickCents = store.quickCentsEntry()
        addRadio(
            priceChoices!!,
            "Direct amount entry",
            "Type an amount normally, such as 12.50 or 12,50.",
            !quickCents
        )
        addRadio(
            priceChoices!!,
            "Quick cents entry",
            "Digits shift into cents as you type.",
            quickCents
        )
        rows!!.addView(priceChoices)

        section("New item focus")
        flowChoices = radios()
        val quickEntry = store.quickEntry()
        addRadio(
            flowChoices!!,
            "Name first",
            "New items start at the item name field.",
            !quickEntry
        )
        addRadio(
            flowChoices!!,
            "Price first",
            "New items start at the price field. Next adds another item.",
            quickEntry
        )
        rows!!.addView(flowChoices)
        section("Weight unit")
        weightChoices = radios()
        addRadio(
            weightChoices!!,
            "Pounds (lb)",
            "Common in the United States.",
            "lb" == store.weightUnit()
        )
        addRadio(
            weightChoices!!,
            "Kilograms (kg)",
            "Common in most countries.",
            "kg" == store.weightUnit()
        )
        addRadio(
            weightChoices!!,
            "Ounces (oz)",
            "Useful for smaller US measurements.",
            "oz" == store.weightUnit()
        )
        addRadio(
            weightChoices!!,
            "Grams (g)",
            "Useful for smaller metric measurements.",
            "g" == store.weightUnit()
        )
        rows!!.addView(weightChoices)
        section("About")
        val about = card()
        about.addView(label("Shopping List Calculator", 17, text, true))
        val version = label("Version " + appVersion(), 13, muted, false)
        version.setPadding(0, dp(3), 0, dp(9))
        about.addView(version)
        val github = button("GitHub repository")
        github.setOnClickListener(View.OnClickListener { v: View? -> openUrl("https://github.com/buildsbyben/shopping-list-calc") })
        about.addView(github)
        val issues = button("Report an issue")
        issues.setOnClickListener(View.OnClickListener { v: View? -> openUrl("https://github.com/buildsbyben/shopping-list-calc/issues") })
        about.addView(issues, top(8))
        val fdroid = button("Get updates on F-Droid")
        fdroid.setOnClickListener(View.OnClickListener { v: View? -> openUrl("https://f-droid.org/packages/io.github.buildsbyben.shoppinglistcalc/") })
        about.addView(fdroid, top(8))
        rows!!.addView(about)
        screen.setOnApplyWindowInsetsListener(View.OnApplyWindowInsetsListener { view: View?, insets: WindowInsets? ->
            header.setPadding(dp(16), dp(16) + insets!!.getSystemWindowInsetTop(), dp(16), dp(8))
            insets
        })
        setContentView(screen)
    }

    private fun addBudgetAndTaxSettings() {
        val settingsCard = card()
        val fields = LinearLayout(this)
        fields.setOrientation(LinearLayout.HORIZONTAL)

        val taxField = column()
        taxField.addView(label("Tax rate (%)", 16, text, true))
        taxInput = input("", trimNumber(store.taxRate()), true)
        taxField.addView(taxInput, top(5))

        budgetFormat = store.currencyFormat()
        rawBudgetValue = store.budget()

        val budgetField = column()
        budgetField.addView(label("Budget", 16, text, true))

        budgetInput = input(
            "",
            if (rawBudgetValue == 0.0) "" else formatBudget(rawBudgetValue, budgetFormat!!),
            false
        )
        budgetField.addView(budgetInput, top(5))

        val taxParams = LinearLayout.LayoutParams(0, -2, 1f)
        val budgetParams = LinearLayout.LayoutParams(0, -2, 1f)
        budgetParams.leftMargin = dp(10)

        fields.addView(taxField, taxParams)
        fields.addView(budgetField, budgetParams)
        settingsCard.addView(fields)
        rows!!.addView(settingsCard)
    }
    private fun addCurrencySettings() {
        val current = store.currencyFormat()
        val money = card()
        money.addView(label("Currency format", 17, text, true))
        val note = label(
            "Choose manually. This app does not read your device region or location.",
            13,
            muted,
            false
        )
        note.setPadding(0, dp(3), 0, dp(8))
        money.addView(note)
        currencyChoices = radios()
        addRadio(
            currencyChoices!!,
            "US Dollar",
            "$1,234.56",
            isFormat(current, "$", false, '.', ',', 2)
        )
        addRadio(currencyChoices!!, "Euro", "€1.234,56", isFormat(current, "€", false, ',', '.', 2))
        addRadio(
            currencyChoices!!,
            "British Pound",
            "£1,234.56",
            isFormat(current, "£", false, '.', ',', 2)
        )
        addRadio(
            currencyChoices!!,
            "Japanese Yen",
            "¥1,235",
            isFormat(current, "¥", false, '.', ',', 0)
        )
        val custom = !isPreset(current)
        addRadio(
            currencyChoices!!,
            "Custom format",
            "Choose your own symbol and separators.",
            custom
        )
        money.addView(currencyChoices)
        addCustomFormat(current, money, custom)
        currencyChoices!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { g: RadioGroup?, id: Int ->
            val selected = selectedIndex(currencyChoices!!)
            customFormat!!.setVisibility(if (selected == 4) View.VISIBLE else View.GONE)
            refreshBudgetFormat(if (selected < 4) presetFormat(selected) else customFormatPreview())
        })
        rows!!.addView(money)
    }

    private fun addCustomFormat(value: CurrencyFormat, parent: LinearLayout, visible: Boolean) {
        customFormat = column()
        customFormat!!.setPadding(0, dp(8), 0, 0)
        customFormat!!.setVisibility(if (visible) View.VISIBLE else View.GONE)
        customFormat!!.addView(label("Custom format", 16, text, true))
        symbol = input("", value.symbol, false)
        decimal = input("", value.decimalSeparator.toString(), false)
        grouping = input(
            "",
            if (value.groupingSeparator == '\u0000') "" else value.groupingSeparator.toString(),
            false
        )
        digits = input("", value.fractionDigits.toString(), true)
        customFormat!!.addView(inputRow("Currency symbol", symbol, 6))
        customFormat!!.addView(inputRow("Decimal separator (. or ,)", decimal, 8))
        customFormat!!.addView(inputRow("Thousands separator (, . space, or blank)", grouping, 8))
        customFormat!!.addView(inputRow("Decimal places (0–3)", digits, 8))
        customFormat!!.addView(label("Symbol placement", 14, text, true), top(10))
        symbolPosition = radios()
        addRadio(symbolPosition!!, "Before amount", "$1,234.56", !value.symbolAfter)
        addRadio(symbolPosition!!, "After amount", "1.234,56 €", value.symbolAfter)
        customFormat!!.addView(symbolPosition)
        parent.addView(customFormat)
    }

    private fun saveAll(): Boolean {
        val currency = selectedIndex(currencyChoices!!)
        val format: CurrencyFormat?
        if (currency < 4) format = presetFormat(currency)
        else {
            val d = separator(decimal!!.getText().toString(), '.')
            val g = if (grouping!!.getText().toString().trim { it <= ' ' }
                    .isEmpty()) '\u0000' else separator(grouping!!.getText().toString(), ',')
            if (d == g && g != '\u0000') {
                grouping!!.setError("Use a different separator than decimal.")
                return false
            }
            format = CurrencyFormat(
                symbol!!.getText().toString().trim { it <= ' ' },
                selectedIndex(symbolPosition!!) == 1,
                d,
                g,
                parseInt(digits!!.getText().toString(), 2)
            )
        }
        store.saveCurrencyFormat(format)
        store.saveQuickCentsEntry(selectedIndex(priceChoices!!) == 1)
        store.saveQuickEntry(selectedIndex(flowChoices!!) == 1)
        store.saveWeightUnit(
            arrayOf<String>("lb", "kg", "oz", "g")[max(
                0,
                selectedIndex(weightChoices!!)
            )]
        )
        store.saveSettings(
            parseDouble(taxInput!!.getText().toString()),
            parseBudget(budgetInput!!.getText().toString(), budgetFormat!!)
        )
        return true
    }

    private fun presetFormat(id: Int): CurrencyFormat {
        if (id == 0) return CurrencyFormat("$", false, '.', ',', 2)
        if (id == 1) return CurrencyFormat("€", false, ',', '.', 2)
        if (id == 2) return CurrencyFormat("£", false, '.', ',', 2)
        return CurrencyFormat("¥", false, '.', ',', 0)
    }

    private fun customFormatPreview(): CurrencyFormat {
        val d = separator(decimal!!.getText().toString(), '.')
        val g = if (grouping!!.getText().toString().trim { it <= ' ' }
                .isEmpty()) '\u0000' else separator(grouping!!.getText().toString(), ',')
        return CurrencyFormat(
            symbol!!.getText().toString().trim { it <= ' ' },
            selectedIndex(symbolPosition!!) == 1,
            d,
            g,
            parseInt(digits!!.getText().toString(), 2)
        )
    }

    private fun refreshBudgetFormat(format: CurrencyFormat) {
        budgetFormat = format
        if (rawBudgetValue != 0.0 || !budgetInput!!.text.toString().trim().isEmpty()) {
            budgetInput!!.setText(formatBudget(rawBudgetValue, format))
        }
    }

    private fun formatBudget(amount: Double, format: CurrencyFormat): String {
        val raw = String.format(Locale.US, "%." + format.fractionDigits + "f", amount)
        val point = raw.indexOf('.')
        var whole = if (point < 0) raw else raw.substring(0, point)
        val fraction = if (point < 0) "" else raw.substring(point + 1)
        if (format.groupingSeparator != '\u0000') {
            val grouped = StringBuilder()
            for (i in 0..<whole.length) {
                if (i > 0 && (whole.length - i) % 3 == 0) grouped.append(format.groupingSeparator)
                grouped.append(whole.get(i))
            }
            whole = grouped.toString()
        }
        val number =
            if (format.fractionDigits == 0) whole else whole + format.decimalSeparator + fraction
        return if (format.symbolAfter) number + format.symbol else format.symbol + number
    }

    private fun parseBudget(value: String?, format: CurrencyFormat): Double {
        if (value.isNullOrBlank()) return 0.0
        var raw = value.trim()
        if (format.symbol.isNotEmpty()) raw = raw.replace(format.symbol, "")
        if (format.groupingSeparator != '\u0000') {
            raw = raw.replace(format.groupingSeparator.toString(), "")
        }
        if (format.decimalSeparator != '.') {
            raw = raw.replace(format.decimalSeparator, '.')
        }
        // Remove any remaining non-numeric characters except standard decimal point
        raw = raw.replace(Regex("[^0-9.]"), "")
        return parseDouble(raw)
    }

    private fun isPreset(v: CurrencyFormat): Boolean {
        return isFormat(v, "$", false, '.', ',', 2) || isFormat(
            v,
            "€",
            false,
            ',',
            '.',
            2
        ) || isFormat(v, "£", false, '.', ',', 2) || isFormat(v, "¥", false, '.', ',', 0)
    }

    private fun isFormat(
        v: CurrencyFormat,
        s: String?,
        after: Boolean,
        d: Char,
        g: Char,
        places: Int
    ): Boolean {
        return v.symbol == s && v.symbolAfter == after && v.decimalSeparator == d && v.groupingSeparator == g && v.fractionDigits == places
    }

    private fun radios(): RadioGroup {
        val g = RadioGroup(this)
        g.setOrientation(LinearLayout.VERTICAL)
        return g
    }

    private fun addRadio(group: RadioGroup, title: String, summary: String?, checked: Boolean) {
        val radio = RadioButton(this)
        radio.setId(View.generateViewId())
        val content = SpannableString(title + "\n" + summary)
        content.setSpan(
            RelativeSizeSpan(.76f),
            title.length + 1,
            content.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        radio.setText(content)
        radio.setTextSize(17f)
        radio.setTextColor(text)
        radio.setButtonTintList(ColorStateList.valueOf(Color.WHITE))
        radio.setPadding(0, dp(6), 0, dp(6))
        radio.setChecked(checked)
        group.addView(radio)
    }

    private fun selectedIndex(group: RadioGroup): Int {
        val selected = group.findViewById<View?>(group.getCheckedRadioButtonId())
        return if (selected == null) -1 else group.indexOfChild(selected)
    }

    private fun section(title: String?) {
        val v = label(title, 16, text, true)
        v.setPadding(0, dp(24), 0, dp(7))
        rows!!.addView(v)
    }

    private fun card(): LinearLayout {
        val l = column()
        l.setPadding(0, 0, 0, 0)
        val p = LinearLayout.LayoutParams(-1, -2)
        p.bottomMargin = dp(8)
        l.setLayoutParams(p)
        return l
    }

    private fun button(value: String?): Button {
        val b = Button(this)
        b.setText(value)
        b.setTextColor(text)
        b.setGravity(Gravity.START or Gravity.CENTER_VERTICAL)
        b.setBackgroundColor(Color.TRANSPARENT)
        b.setPadding(dp(8), dp(6), dp(8), dp(6))
        return b
    }

    private fun primaryButton(value: String?): Button {
        val b = Button(this)
        b.setText(value)
        b.setTextColor(Color.BLACK)
        b.setTextSize(15f)
        b.setGravity(Gravity.CENTER)
        b.setBackgroundColor(Color.WHITE)
        b.setPadding(dp(16), dp(8), dp(16), dp(8))
        return b
    }

    private fun column(): LinearLayout {
        val l = LinearLayout(this)
        l.setOrientation(LinearLayout.VERTICAL)
        return l
    }

    private fun inputRow(title: String?, field: EditText?, marginTop: Int): LinearLayout {
        val row = LinearLayout(this)
        row.setOrientation(LinearLayout.HORIZONTAL)
        row.setGravity(Gravity.CENTER_VERTICAL)
        val name = label(title, 14, text, true)
        name.setPadding(0, 0, dp(12), 0)
        row.addView(name, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(field, LinearLayout.LayoutParams(dp(116), -2))
        val params = LinearLayout.LayoutParams(-1, -2)
        params.topMargin = dp(marginTop)
        row.setLayoutParams(params)
        return row
    }

    private fun label(value: String?, size: Int, color: Int, bold: Boolean): TextView {
        val v = TextView(this)
        v.setText(value)
        v.setTextSize(size.toFloat())
        v.setTextColor(color)
        if (bold) v.setTypeface(null, Typeface.BOLD)
        return v
    }

    private fun input(hint: String?, value: String?, number: Boolean): EditText {
        val e = EditText(this)
        e.setHint(hint)
        e.setText(value)
        e.setSingleLine(true)
        e.setTextColor(text)
        e.setHintTextColor(Color.LTGRAY)
        e.setPadding(dp(10), 0, dp(10), 0)
        val border = GradientDrawable()
        border.setColor(Color.TRANSPARENT)
        border.setStroke(dp(1), Color.WHITE)
        border.setCornerRadius(dp(2).toFloat())
        e.setBackground(border)
        if (number) e.setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        return e
    }

    private fun top(margin: Int): LinearLayout.LayoutParams {
        val p = LinearLayout.LayoutParams(-1, -2)
        p.topMargin = dp(margin)
        return p
    }

    private fun openUrl(url: String?) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun appVersion(): String? {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName
        } catch (e: Exception) {
            return ""
        }
    }

    private fun dp(value: Int): Int {
        return Math.round(value * getResources().getDisplayMetrics().density)
    }

    private fun separator(value: String?, fallback: Char): Char {
        val t = if (value == null) "" else value.trim { it <= ' ' }
        return if (t.isEmpty()) fallback else t.get(0)
    }

    private fun parseInt(value: String, fallback: Int): Int {
        try {
            return max(0, min(3, value.trim { it <= ' ' }.toInt()))
        } catch (e: Exception) {
            return fallback
        }
    }

    private fun parseDouble(value: String): Double {
        try {
            return if (value.trim { it <= ' ' }.isEmpty()) 0.0 else value.trim { it <= ' ' }
                .toDouble()
        } catch (e: Exception) {
            return 0.0
        }
    }

    private fun trimNumber(value: Double): String {
        return if (value == round(value)) value.toLong().toString() else value.toString()
    }

    companion object {
        private const val PREFS = "shopping_calc"
    }
}
