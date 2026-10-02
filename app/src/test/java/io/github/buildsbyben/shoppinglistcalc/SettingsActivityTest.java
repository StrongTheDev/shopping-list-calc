package io.github.buildsbyben.shoppinglistcalc;

import static org.junit.Assert.*;

import android.content.Context;
import android.widget.EditText;
import android.widget.RadioGroup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class SettingsActivityTest {
    private ActivityController<SettingsActivity> controller;
    private SettingsActivity activity;
    private ShoppingListStore store;

    @Before public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("shopping_calc", Context.MODE_PRIVATE).edit().clear().commit();
        store = new ShoppingListStore(context.getSharedPreferences("shopping_calc", Context.MODE_PRIVATE));
        store.saveCurrencyFormat(new CurrencyFormat("$", false, '.', ',', 2));
        store.saveSettings(7.5, 100);
        controller = Robolectric.buildActivity(SettingsActivity.class).setup();
        activity = controller.get();
    }

    @After public void tearDown() {
        controller.pause().stop().destroy();
    }

    @Test public void editedBudgetSurvivesCurrencySwitchAndSave() throws Exception {
        input("budgetInput").setText("$1,234.56");
        select("currencyChoices", 1);
        assertEquals("€1.234,56", input("budgetInput").getText().toString());
        assertTrue(save());
        assertEquals(1234.56, store.budget(), 0.001);
    }

    @Test public void clearedBudgetDoesNotRestoreLoadedOrPreviouslyEnteredAmount() throws Exception {
        input("budgetInput").setText("");
        select("currencyChoices", 1);
        assertEquals("", input("budgetInput").getText().toString());
        input("budgetInput").setText("€250,00");
        select("currencyChoices", 2);
        input("budgetInput").setText("");
        select("currencyChoices", 0);
        assertEquals("", input("budgetInput").getText().toString());
        assertTrue(save());
        assertEquals(0, store.budget(), 0);
    }

    @Test public void customAfterAmountAndBlankGroupingArePreserved() throws Exception {
        input("budgetInput").setText("1234.56");
        input("symbol").setText("EUR");
        input("decimal").setText(",");
        input("grouping").setText("");
        select("symbolPosition", 1);
        select("currencyChoices", 4);
        assertEquals("1234,56EUR", input("budgetInput").getText().toString());
        assertTrue(save());
        assertTrue(store.currencyFormat().symbolAfter);
        assertEquals('\0', store.currencyFormat().groupingSeparator);
        assertEquals(1234.56, store.budget(), 0.001);
    }

    @Test public void literalSpaceGroupingIsPreserved() throws Exception {
        input("budgetInput").setText("1234.56");
        input("grouping").setText(" ");
        select("currencyChoices", 4);
        assertEquals("$1 234.56", input("budgetInput").getText().toString());
        assertTrue(save());
        assertEquals(' ', store.currencyFormat().groupingSeparator);
        assertEquals(1234.56, store.budget(), 0.001);
    }

    @Test public void matchingSeparatorsDoNotSaveAnySettings() throws Exception {
        select("currencyChoices", 4);
        input("decimal").setText(",");
        input("grouping").setText(",");
        input("taxInput").setText("9");
        assertFalse(save());
        assertNotNull(input("grouping").getError());
        assertEquals(7.5, store.taxRate(), 0);
        assertEquals('.', store.currencyFormat().decimalSeparator);
    }

    @Test public void allWeightUnitsAndEntryPreferencesRoundTrip() throws Exception {
        String[] units = {"lb", "kg", "oz", "g"};
        select("priceChoices", 1);
        select("flowChoices", 1);
        for (int i = 0; i < units.length; i++) {
            select("weightChoices", i);
            assertTrue(save());
            assertEquals(units[i], store.weightUnit());
        }
        assertTrue(store.quickCentsEntry());
        assertTrue(store.quickEntry());
        controller.pause().stop().destroy();
        controller = Robolectric.buildActivity(SettingsActivity.class).setup();
        activity = controller.get();
        assertEquals("$100.00", input("budgetInput").getText().toString());
        assertEquals(3, selected("weightChoices"));
        assertEquals(1, selected("priceChoices"));
        assertEquals(1, selected("flowChoices"));
    }

    private Object field(String name) throws Exception {
        Field field = SettingsActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(activity);
    }
    private EditText input(String name) throws Exception { return (EditText) field(name); }
    private void select(String name, int index) throws Exception {
        RadioGroup group = (RadioGroup) field(name);
        group.check(group.getChildAt(index).getId());
    }
    private int selected(String name) throws Exception {
        RadioGroup group = (RadioGroup) field(name);
        return group.indexOfChild(group.findViewById(group.getCheckedRadioButtonId()));
    }
    private boolean save() throws Exception {
        Method method = SettingsActivity.class.getDeclaredMethod("saveAll");
        method.setAccessible(true);
        return (boolean) method.invoke(activity);
    }
}
