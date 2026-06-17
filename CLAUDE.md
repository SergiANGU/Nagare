# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Mobile app (Android + iOS) for managing the Nagare Jiu-Jitsu team: news, activities, and training sessions, with member registration. Built with **Kotlin Multiplatform + Compose Multiplatform**.

Language of the UI is **Catalan**. All UI strings must go through `Strings.kt` (the `org.nagare.project.Strings` object in `commonMain`).

## Build commands

```bash
# Android — set JAVA_HOME before any Gradle command
# macOS (Apple Silicon): export JAVA_HOME=~/.gradle/jdks/amazon_com_inc_-21-aarch64-os_x.2/amazon-corretto-21.jdk/Contents/Home
# Windows: $env:JAVA_HOME = "$env:USERPROFILE\.gradle\jdks\amazon_com_inc_-21-amd64-windows.2"

# Build Android debug APK
./gradlew :androidApp:assembleDebug          # macOS/Linux
.\gradlew.bat :androidApp:assembleDebug      # Windows

# Install on connected device/emulator
./gradlew :androidApp:installDebug

# Run all shared tests
./gradlew :shared:allTests

# Run a single test class
./gradlew :shared:allTests --tests "org.nagare.project.SharedCommonTest"
```

For iOS: open `iosApp/iosApp.xcodeproj` in Xcode (macOS only) and run on simulator. `JAVA_HOME` is not needed for Xcode builds.

## Architecture

**MVVM — keep it simple.** No Clean Architecture or use cases. The pattern is:

```
Screen (Composable) → ViewModel (StateFlow<UiState>) → Repository → Firestore
```

Every `UiState` must have `Loading`, `Success(data)`, and `Error(message)` states.

All business logic and UI code lives in `shared/src/commonMain`. Platform-specific code (`androidMain`, `iosMain`) is only for `expect`/`actual` declarations.

### Key packages (`shared/src/commonMain/kotlin/org/nagare/project/`)

| Package | Contents |
|---|---|
| `data/model/` | `@Serializable` data classes: `Usuari`, `Noticia`, `Activitat`, `Entreno`, enums `Rol`, `TipusNoticia`, `TipusActivitat` |
| `data/repository/` | `AuthRepository` (Firebase Auth), `UsuariRepository`, `NoticiesRepository`, `ActivitatsRepository`, `EntrenosRepository` (all use GitLive Firestore) |
| `di/AppModule.kt` | Single Koin module — all repositories as `single`, all ViewModels as `viewModel` |
| `ui/navigation/AppNavigation.kt` | Root `NavHost`: Login → Register → CompletarPerfil → MainScreen |
| `ui/screens/main/MainScreen.kt` | `ModalNavigationDrawer` + inner `NavHost` with Noticies / Activitats / Entrenos / Perfil |
| `ui/screens/activitats/` | `ActivitatsScreen` + `ActivitatsViewModel` (list) and `DetallActivitatScreen` + `DetallActivitatViewModel` (detail with inscription) |
| `ui/screens/auth/` | Login, Register, CompletarPerfil screens + ViewModels; also contains `validaDni` and `validaDataNaixement` helpers reused by `PerfilScreen` |
| `ui/screens/perfil/PerfilScreen.kt` | Profile screen — **exception to the pattern**: `PerfilViewModel` is defined inline in this file and instantiated with `remember { PerfilViewModel(koinInject()) }` instead of being registered in Koin |
| `ui/theme/Theme.kt` | Material 3 theme (blue/black BJJ palette) |

### Firestore collections

```
usuaris/{uid}       — nom, cognoms, dni, dataNaixement (ISO-8601), email, rol ("ADMIN"|"MEMBRE"), creatEl
noticies/{id}       — titol, cos, tipus, data, autorUid
activitats/{id}     — titol, descripcio, tipus, dataInici, dataFi?, lloc, inscrits: [uid]
entrenos/{id}       — titol, data, lloc, notes, assistents: [uid]
```

### Roles

- `MEMBRE` — default for new users. Can read all content; can add/remove themselves from `inscrits` / `assistents`.
- `ADMIN` — must be set manually in Firestore console (`usuaris/{uid}.rol = "ADMIN"`). Can create/edit/delete news, activities, and training sessions; sees members' full federative data (DNI, birthdate) in activity details.

### Navigation flow

`AppNavigation` checks on startup: no session → Login; session but no profile doc → CompletarPerfil; session + profile → MainScreen. `MainScreen` uses a nested NavHost inside a `ModalNavigationDrawer` with four destinations: Noticies (default), Activitats, Entrenos, Perfil. The `DetallActivitatScreen` is also inside `MainScreen`'s NavHost at `detall_activitat/{activitatId}`.

### Koin initialization

- **Android**: `NagareApplication.onCreate()` calls `startKoin { modules(appModule) }`
- **iOS**: `iOSApp.swift` calls `MainViewControllerKt.doInitKoin()` which calls `initKoin()` in `MainViewController.kt`

## Firestore security rules

`firestore.rules` must be deployed before giving team access. Key rules:
- Users can only read/write their own profile; cannot change their own `rol`.
- Members can only modify the `inscrits`/`assistents` array on activities/entrenos to add or remove themselves.
- ADMIN can read all `usuaris` documents.

Deploy with: `firebase deploy --only firestore:rules`

## Dependency management

All versions in `gradle/libs.versions.toml`. Key versions: Kotlin 2.4.0, Compose Multiplatform 1.11.1, Koin 4.0.0, GitLive Firebase SDK 2.0.0, Material 3 1.11.0-alpha07.

Firebase is accessed via `dev.gitlive:firebase-auth` and `dev.gitlive:firebase-firestore` (the GitLive KMP wrapper, usable from `commonMain`). Do **not** use the Google Firebase Android SDK directly in shared code.
