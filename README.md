# Nagare Jiu-Jitsu App

App mòbil per gestionar l'equip de Jiu-Jitsu Nagare: notícies, activitats i entrenos, amb inscripció de membres.

Plataformes: **Android** i **iOS** amb Kotlin Multiplatform + Compose Multiplatform.

---

## Requisits

- **macOS** amb Xcode 15+
- **Android Studio** (Meerkat o posterior)
- **JDK 21** (Corretto o OpenJDK)
- **Kotlin 2.4.0** (gestionat per Gradle)
- Projecte **Firebase** creat a la consola de Firebase

---

## Configuració de Firebase

### Android

Col·loca el fitxer `google-services.json` (descarregat des de la consola de Firebase → Configuració del projecte → Android) a:

```
androidApp/google-services.json
```

### iOS

Col·loca el fitxer `GoogleService-Info.plist` (descarregat des de la consola de Firebase → Configuració del projecte → iOS) a:

```
iosApp/iosApp/GoogleService-Info.plist
```

Per a iOS, cal inicialitzar Firebase via CocoaPods o SPM. Afegeix al `Podfile` de l'iOS target:

```ruby
pod 'FirebaseAuth'
pod 'FirebaseFirestore'
```

I assegura't que `iOSApp.swift` crida `MainViewControllerKt.doInitKoin()` (ja inclou la inicialització de Koin).

> **RGPD**: El DNI i la data de naixement dels membres es guarden a Firestore. Desplega les regles de seguretat (`firestore.rules`) **abans** de donar accés a l'equip.

---

## Executar a Android

```bash
# Compilar APK debug
export JAVA_HOME=~/.gradle/jdks/amazon_com_inc_-21-aarch64-os_x.2/amazon-corretto-21.jdk/Contents/Home
./gradlew :androidApp:assembleDebug

# Instal·lar en un dispositiu/emulador connectat
./gradlew :androidApp:installDebug
```

O obre el projecte a **Android Studio** i executa `androidApp`.

---

## Executar a iOS

1. Obre `iosApp/iosApp.xcodeproj` (o `.xcworkspace` si uses CocoaPods) amb Xcode.
2. Selecciona el simulador o dispositiu.
3. Compila i executa.

Si uses CocoaPods, executa `pod install` al directori `iosApp/` primer.

---

## Desplegament de les regles de Firestore

```bash
# Instal·la Firebase CLI si no el tens
npm install -g firebase-tools

# Autentifica't
firebase login

# Desplega les regles
firebase deploy --only firestore:rules
```

Alternativament, copia el contingut de `firestore.rules` directament a la consola de Firebase → Firestore → Regles.

---

## Estructura del projecte

```
NagareJiuJitsu/
├── androidApp/                 # App Android
│   ├── src/main/
│   │   ├── kotlin/org/nagare/project/
│   │   │   ├── MainActivity.kt
│   │   │   └── NagareApplication.kt   # Inicialitza Koin
│   │   ├── AndroidManifest.xml
│   │   └── google-services.json       # ← col·loca aquí
│   └── build.gradle.kts
│
├── shared/                     # Codi compartit (commonMain)
│   └── src/commonMain/kotlin/org/nagare/project/
│       ├── App.kt                     # Punt d'entrada Compose
│       ├── Strings.kt                 # Textos en català
│       ├── data/
│       │   ├── model/                 # Usuari, Noticia, Activitat, Entreno...
│       │   └── repository/            # AuthRepository, UsuariRepository...
│       ├── di/
│       │   └── AppModule.kt           # Mòdul Koin
│       └── ui/
│           ├── navigation/AppNavigation.kt
│           ├── screens/
│           │   ├── auth/              # Login, Register, CompletarPerfil
│           │   ├── main/MainScreen.kt # Drawer + NavHost
│           │   ├── noticies/
│           │   ├── activitats/
│           │   ├── entrenos/
│           │   └── perfil/
│           └── theme/Theme.kt         # Colors BJJ (blau/negre)
│
├── iosApp/                     # App iOS
│   └── iosApp/
│       ├── iOSApp.swift               # Punt d'entrada (crida initKoin)
│       ├── ContentView.swift
│       └── GoogleService-Info.plist   # ← col·loca aquí
│
├── firestore.rules             # Regles de seguretat Firestore (RGPD)
└── gradle/libs.versions.toml   # Versions de dependències
```

---

## Stack tecnològic

| Capa | Llibreria |
|---|---|
| UI | Compose Multiplatform 1.11.1 (Material 3) |
| Navegació | `org.jetbrains.androidx.navigation:navigation-compose` |
| ViewModel | `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` |
| DI | Koin 4.0.0 |
| Firebase | GitLive Firebase Kotlin SDK 2.0.0 |
| Dates | `kotlinx-datetime` 0.6.1 |
| Serialització | `kotlinx-serialization-json` 1.7.3 |

---

## Crear un ADMIN

El rol s'assigna manualment des de la consola de Firebase:

1. Ves a **Firestore → usuaris → {uid del membre}**
2. Edita el camp `rol` i canvia `"MEMBRE"` per `"ADMIN"`

---

## Funcionalitats v1

- Registre i login amb email/contrasenya (Firebase Auth)
- Perfil complet amb validació DNI espanyol
- Notícies en temps real per tipus (Entreno, Competició, Viatge, General)
- Activitats amb inscripció (admins veuen dades federatives dels inscrits)
- Entrenos amb control d'assistència
- Tema Material 3 en blau/negre (BJJ)
