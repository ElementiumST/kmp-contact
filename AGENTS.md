# kmp-contact Agent Guide

## Project Map

- `:kmp:domain`: shared domain models, repository contracts, and use cases.
- `:kmp:data`: shared repository implementation, DTOs, mappers, network/database abstractions.
- `:kmp:support`: shared support utilities such as paging.
- `:android:contacts`: Android feature module with Compose UI, presentation, DI, and Android executors.
- `:android:main`: thin Android app shell that boots the contacts feature.
- `:web:contacts`: JS/web entry point, DOM controller, and web platform executors.
- `:ios:contacts`: KMP helper module for iOS integration. This repo does not contain a full iOS app shell.

## Architecture Boundaries

- Keep dependency direction one-way: platform modules -> shared modules, `data` -> `domain`, `support` -> `domain`.
- `kmp/domain` must stay platform-agnostic. No DTOs, HTTP details, SQL details, Android APIs, JS DOM APIs, or platform storage.
- `kmp/data` owns repository orchestration, DTO/entity mapping, request payload building, cache persistence, and network-to-cache fallback behavior.
- `kmp/support` should contain reusable cross-platform helpers, not platform wiring.
- Prefer calling use cases or support abstractions from UI/presentation code. Do not bypass the domain contract to reach `ContactsRepositoryImpl` from feature code.

## Platform Notes

### Android

- Hilt is the DI mechanism. Keep Android wiring in `android/contacts/.../di`.
- Compose UI lives in `android/contacts/.../ui`; state and interaction logic live in `android/contacts/.../presentation`.
- Android-specific executors belong in `android/contacts/.../data/local` and `android/contacts/.../data/remote`.
- `android/main` should stay minimal: application/bootstrap code only.

### Web

- Web wiring is manual in `web/contacts/src/jsMain/kotlin/com/stark/kmpcontact/web/contacts/main.kt`.
- Keep browser-specific adapters in `web/contacts/.../platform`.
- Keep DOM coordination in `web/contacts/.../ui`, following the existing controller pattern.

### iOS

- Treat `ios/contacts` as a shared integration helper layer unless the repo later gains an actual iOS app target.
- Do not invent iOS platform executors or app bootstrap structure unless there is code in the repo that establishes that pattern.

## Naming And Placement

- Follow the existing root package prefix: `com.stark.kmpcontact`.
- Match the current package split by concern: `domain`, `data`, `support`, `ui`, `presentation`, `di`, `platform`, `local`, `remote`, `auth`.
- Keep `toDomain()`-style mapping extensions near the shared data layer instead of scattering conversions through UI code.
- Preserve the existing stable-key/cache-key behavior unless the change explicitly requires updating all related call sites.

## Change Guidelines

- Make changes in the narrowest layer that owns the behavior.
- Do not move platform-specific code into `commonMain`.
- Do not encode new API/storage conventions in UI code when the repository or executor layer is the actual owner.
- Treat current uncommitted feature work as local WIP unless the task explicitly asks to extend it.
- When adding new shared APIs, update all affected platform wiring paths deliberately rather than assuming Android-only usage.

## Verification

- Prefer targeted Gradle checks for the modules you touched.
- On Windows use `gradlew.bat`; on Unix-like shells use `./gradlew`.
- Useful commands:
  - `gradlew.bat :kmp:domain:test`
  - `gradlew.bat :kmp:data:test`
  - `gradlew.bat :kmp:support:test`
  - `gradlew.bat :android:contacts:compileDebugKotlin`
  - `gradlew.bat :android:main:assembleDebug`
  - `gradlew.bat :web:contacts:jsBrowserDevelopmentWebpack`
