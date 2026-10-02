# R8 release-size check

Measured 2026-10-02 against PR #11's baseline commit
`609a6d9f87b8c0e1c61a21c6d6882e9b59082641`.

Both measurements are unsigned release APKs built with JDK 17, Android SDK 35,
AGP 8.7.3, Kotlin 2.1.21, and Gradle 8.10.2. No signing environment variables
were supplied. The optimized build enables `minifyEnabled`, `shrinkResources`,
and `proguard-android-optimize.txt`; application source and dependencies are
unchanged. Sizes use decimal KB (1 KB = 1,000 bytes).

| Measurement | Unshrunk baseline | R8 + resource shrinking |
| --- | ---: | ---: |
| APK bytes | 669,284 | 46,392 |
| APK KB | 669.284 | 46.392 |
| Uncompressed classes.dex bytes | 2,125,704 | 57,060 |
| DEX method IDs | 13,023 | 440 |
| DEX class definitions | 1,136 | 52 |

Savings: **622,892 bytes (93.1%)**. These are download-file measurements, not
installed storage or RAM measurements. Signed APK size differs slightly.
Future refactor changes may change the retained code and final size.

## Verification

- Baseline `assembleRelease testDebugUnitTest lint`: passed; six tests passed.
- Optimized `assembleRelease testReleaseUnitTest lint`: passed; six tests passed.
- `assembleRelease assembleR8Smoke lintR8Smoke`: passed; release size unchanged.
- Smoke APK: 53,971 bytes, package suffix `.r8test`, version `2.1-r8-test`;
  APK signature verification passed (v1 and v2).
- Lint reported no errors; six existing debug lint warnings remain.
- Optimized APK retains package `io.github.buildsbyben.shoppinglistcalc`, version
  `2.1` / code `16`, minimum SDK 23, target SDK 35, and launcher activity.
- No production reflection-based serialization was found. Preferences and JSON
  use explicit keys; no blanket application/Kotlin keep rules were added.
- Unit tests exercise JVM classes, **not optimized DEX on Android**.
- Device smoke testing and in-place saved-data upgrade testing remain pending.
- Official F-Droid rebuild/reproducibility verification has not been performed.

## Reproduce

In a separate checkout of the baseline commit, run `./gradlew assembleRelease`
and preserve `app/build/outputs/apk/release/app-release-unsigned.apk`. In a checkout
with this optimization change, run the same command and compare file sizes.
Use identical toolchains and leave signing environment variables unset for both.
Do not compare a debug APK or signed APK against an unsigned release.

For device checks, follow the README's optimized release testing instructions.
The `r8Smoke` variant inherits release shrinking/optimization but uses an isolated
package and debug signing key. It is not a production update or proof that an
existing installation upgrades successfully. Keep the PR draft until device
validation and merge approval are complete.
