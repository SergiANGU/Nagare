# App d'equip de Jiu-Jitsu — Pla de treball per a Claude Code

## Context

App mòbil (iOS + Android) amb **Kotlin Multiplatform + Compose Multiplatform** per organitzar un equip de jiu-jitsu: notícies/comunicacions, activitats (competicions, viatges, entrenos federatius) i entrenaments, amb inscripció dels membres.

**Estat actual:** projecte acabat de generar amb el [KMP Wizard](https://kmp.jetbrains.com) (Android + iOS, UI compartida amb Compose). El projecte de Firebase ja està creat per l'usuari; els fitxers `google-services.json` (Android) i `GoogleService-Info.plist` (iOS) ja estan (o estaran) col·locats als seus llocs. **No cal que generis aquests fitxers; si no hi són, atura't i demana'ls.**

**Entorn:** macOS amb Android Studio i Xcode. Prioritza que tot compili i funcioni primer a **Android**; iOS es verifica al final de cada fase.

## Stack i llibreries

| Capa | Llibreria |
|---|---|
| UI | Compose Multiplatform (Material 3) |
| Navegació | `org.jetbrains.androidx.navigation:navigation-compose` (Compose Navigation per a KMP) |
| ViewModel | `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` |
| DI | Koin (`koin-core`, `koin-compose-viewmodel`) |
| Firebase (Auth + Firestore) | **GitLive Firebase Kotlin SDK** (`dev.gitlive:firebase-auth`, `dev.gitlive:firebase-firestore`) — usable des de `commonMain` |
| Dates | `kotlinx-datetime` |
| Serialització | `kotlinx-serialization` |

Regles generals:
- Tot el codi de negoci i UI a `commonMain`. Codi específic de plataforma només quan sigui imprescindible (`expect`/`actual`).
- Idioma de la UI: **català**. Centralitza els strings (de moment n'hi ha prou amb un objecte `Strings.kt` a commonMain; no cal i18n completa).
- Arquitectura **MVVM senzilla**: `Screen (Composable) → ViewModel (StateFlow<UiState>) → Repository → Firestore`. No apliquis Clean Architecture completa ni use cases; és una app petita.
- Cada `UiState` ha de contemplar: `Loading`, `Success(data)`, `Error(missatge)`.

## Model de dades (Firestore)

Col·leccions:

```
usuaris/{uid}
  nom: String
  cognoms: String
  dni: String
  dataNaixement: String (ISO-8601, "1995-04-23")
  email: String
  rol: "ADMIN" | "MEMBRE"
  creatEl: Timestamp

noticies/{id}
  titol: String
  cos: String
  tipus: "ENTRENO" | "COMPETICIO" | "VIATGE" | "GENERAL"
  data: Timestamp
  autorUid: String

activitats/{id}
  titol: String
  descripcio: String
  tipus: "COMPETICIO" | "VIATGE" | "ENTRENO_FEDERATIU"
  dataInici: Timestamp
  dataFi: Timestamp?       // opcional (viatges de diversos dies)
  lloc: String
  inscrits: [uid]          // array d'uids

entrenos/{id}
  titol: String
  data: Timestamp
  lloc: String
  notes: String
  assistents: [uid]
```

Models Kotlin a `commonMain` amb `@Serializable`, en un paquet `data/model`.

## Estructura de navegació

- Després del login: **`ModalNavigationDrawer`** + `NavHost` amb 3 destinacions:
  1. **Notícies** (destinació inicial)
  2. **Activitats**
  3. **Entrenos**
- Capçalera del drawer: nom i cognoms de l'usuari + email.
- Peu del drawer: botó "Tancar sessió".
- `TopAppBar` amb icona hamburguesa que obre el drawer.

---

## Fases de treball

Treballa fase per fase. **Al final de cada fase: compila Android (`./gradlew :composeApp:assembleDebug`) i no passis a la següent fase si no compila.**

### Fase 0 — Configuració del projecte

1. Afegeix totes les dependències al `libs.versions.toml` i `build.gradle.kts` (versions estables més recents compatibles entre si).
2. Configura el plugin `com.google.gms.google-services` a l'app Android i la inicialització de Firebase:
   - Android: inicialització a la classe `Application` (Firebase.initialize amb GitLive).
   - iOS: `FirebaseApp.configure()` a l'entry point (via cocoapods o SPM segons com ho suporti GitLive a la versió triada; documenta-ho al README).
3. Munta Koin: mòdul amb repositoris i viewmodels, inicialitzat a Android (`Application`) i iOS (`MainViewController`).
4. Crea l'estructura de paquets: `data/model`, `data/repository`, `ui/screens`, `ui/components`, `ui/navigation`, `di`.

### Fase 1 — Autenticació i perfil

1. **Pantalla Login**: email + contrasenya, botons "Inicia sessió" i "Registra't". Gestió d'errors visible (credencials incorrectes, etc.).
2. **Registre**: després de crear l'usuari a Firebase Auth, navega a una pantalla **Completa el perfil**: nom, cognoms, DNI, data de naixement (date picker). En desar, crea el document a `usuaris/{uid}` amb rol `MEMBRE`.
3. **Validacions**: DNI espanyol (8 dígits + lletra de control correcta), camps no buits, data de naixement no futura.
4. **Flux d'arrencada**: si hi ha sessió activa i el perfil existeix → drawer; si hi ha sessió però no perfil → Completa el perfil; si no → Login.
5. `AuthRepository` i `UsuariRepository` amb Koin.

### Fase 2 — Drawer i pantalla de Notícies

1. Munta el `ModalNavigationDrawer` + `NavHost` amb les 3 rutes i el TopAppBar.
2. **Pantalla Notícies**:
   - Llista (LazyColumn) de `noticies` ordenades per `data` descendent, amb **escolta en temps real** (snapshots de Firestore com a `Flow`).
   - Cada targeta: chip de color segons `tipus` (ENTRENO / COMPETICIO / VIATGE / GENERAL), títol, cos (màx. 3 línies), data relativa ("fa 2 dies").
   - Estat buit ("Encara no hi ha notícies") i estat d'error amb botó de reintentar.
3. **Publicar notícia (només ADMIN)**: si `usuari.rol == ADMIN`, mostra un FAB "+" que obre un formulari (títol, cos, tipus) i crea el document.

### Fase 3 — Pantalla d'Activitats

1. Llista d'activitats ordenades per `dataInici` ascendent, separades en dues seccions: **Properes** i **Passades**.
2. Targeta: tipus (chip), títol, dates, lloc, nombre d'inscrits.
3. **Detall d'activitat** (nova ruta): descripció completa + botó **"M'apunto" / "Em desapunto"** (afegir/treure el meu uid de `inscrits` amb `arrayUnion`/`arrayRemove`).
4. Si sóc ADMIN, al detall veig la **llista d'inscrits** amb nom, cognoms, DNI i data de naixement (per a inscripcions federatives).
5. ADMIN: FAB per crear activitat (formulari amb tots els camps).

### Fase 4 — Pantalla d'Entrenos

1. Llista d'entrenos ordenats per data, mateixa separació Propers / Passats.
2. Botó "Hi assistiré" / "No hi aniré" a cada entreno proper (array `assistents`).
3. ADMIN: FAB per crear entreno + veure el recompte i la llista d'assistents.

### Fase 5 — Acabats

1. **Pantalla Perfil** accessible des del drawer: veure i editar les meves dades.
2. Tema Material 3 personalitzat (colors del club; de moment paleta blava/negra de BJJ, fàcil de canviar a `Theme.kt`).
3. Pull-to-refresh a les tres llistes.
4. **README.md** del projecte: requisits, com col·locar els fitxers de Firebase, com executar a Android i iOS, i estructura del projecte.
5. Verificació final a iOS (simulador): login, drawer, les 3 pantalles.

---

## Security Rules de Firestore (lliurar com a fitxer `firestore.rules`)

Genera el fitxer amb aquestes regles (l'usuari les desplegarà manualment):

- `usuaris/{uid}`: lectura i escriptura només pel propi usuari; lectura de tots els documents si el sol·licitant té rol ADMIN. Ningú pot canviar el seu propi `rol`.
- `noticies`, `activitats`, `entrenos`: lectura per a qualsevol usuari autenticat; creació/edició/esborrat només ADMIN.
- Excepció: un usuari autenticat pot actualitzar **només** els camps `inscrits`/`assistents` d'una activitat/entreno, i només per afegir-se o treure's a si mateix.

> ⚠️ Important (RGPD): el DNI i la data de naixement són dades personals. Les regles anteriors són imprescindibles abans de donar accés a l'equip.

## Fora de l'abast d'aquesta primera versió (no ho implementis)

- Notificacions push (FCM) — fase 2 del projecte.
- Pujada d'imatges/cartells (Storage).
- Recuperació de contrasenya, login amb Google.
- Tests (de moment prioritzem l'esquelet funcional).

## Criteris d'acceptació de la v1

- [ ] Compila i s'executa a Android i iOS.
- [ ] Em puc registrar, completar el perfil i tornar a entrar amb sessió persistent.
- [ ] Un ADMIN (rol canviat a mà a la consola de Firestore) pot publicar notícies, activitats i entrenos.
- [ ] Un MEMBRE veu les notícies en temps real i es pot apuntar/desapuntar d'activitats i entrenos.
- [ ] L'ADMIN veu les dades federatives (nom, cognoms, DNI, naixement) dels inscrits a una activitat.
- [ ] Existeix `firestore.rules` i el README explica com desplegar-lo.
