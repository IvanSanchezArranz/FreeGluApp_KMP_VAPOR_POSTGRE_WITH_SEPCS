# Tasks: Migración a Ejecución Local (Offline-first)

**Input**: `spec.md` y `plan.md` en `scripts/docs/018-local-execution-migration/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Configuración de dependencias de SQLDelight en KMP y preparación del entorno.

- [ ] T001 Agregar dependencias del plugin y drivers nativos de SQLDelight en `FreeGluKMP/shared/build.gradle.kts`.
- [ ] T002 Configurar el dialecto de SQLite en la sección `sqldelight` de gradle.
- [ ] T003 [P] Crear la estructura de directorios para SQLDelight en `FreeGluKMP/shared/src/commonMain/sqldelight/com/ivan/freeglukmp/data/db`.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Esquema base de la base de datos SQLDelight y factorías de drivers.

- [ ] T004 Crear el archivo `AppDatabase.sq` con el esquema de tablas: `Food`, `User`, `UserFavorite`, `UserFoodOverride`.
- [ ] T005 [P] Implementar la interfaz expect `DatabaseDriverFactory` en `commonMain`.
- [ ] T006 Implementar la actual `DatabaseDriverFactory` en `androidMain` (AndroidSqliteDriver) y preparar la copia del asset `freeglu.db` en el dispositivo.
- [ ] T007 Implementar la actual `DatabaseDriverFactory` en `iosMain` (NativeSqliteDriver) y preparar la copia del recurso de iOS.
- [ ] T008 [P] Configurar el mock/fallback de driver en `jsMain` y `wasmJsMain` (puede ser un driver en memoria para pruebas y evitar crash).
- [ ] T009 Generar el código de SQLDelight mediante Gradle compile (`./gradlew generateSqlDelightInterface`).

---

## Phase 3: User Story 1 - Búsqueda de Alimentos Offline (Priority: P1) 🎯 MVP

**Goal**: Permitir la búsqueda local de alimentos indexados usando el catálogo precargado `freeglu.db`.

**Independent Test**: Ejecutar la app con el Wi-Fi/Datos apagados, realizar búsquedas de texto e identificar que se cargan productos desde la DB de assets de forma instantánea.

### Implementation for User Story 1

- [ ] T010 Implementar queries de búsqueda (`getFoodByBarcode`, `searchFoodByName`) en `AppDatabase.sq`.
- [ ] T011 Crear la clase `LocalFoodRepositoryImpl` en `FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/data/local/LocalFoodRepositoryImpl.kt` utilizando las consultas generadas por SQLDelight.
- [ ] T012 Actualizar la inyección de dependencias en `AppModule.kt` para usar `LocalFoodRepositoryImpl` en lugar del antiguo `FoodRepositoryImpl` remoto.
- [ ] T013 Modificar el script de Python (`scripts/import_csv.py` u homólogo) para exportar el CSV de alimentos sin gluten de Open Food Facts filtrados directamente a una base de datos SQLite `freeglu.db` y copiarla a los assets de Android (`androidApp/src/main/assets`) y recursos de iOS.

---

## Phase 4: User Story 2 - Perfil y Favoritos Locales (Priority: P2)

**Goal**: Registrar y consultar favoritos de forma local con persistencia robusta SQLDelight.

**Independent Test**: Añadir productos como favoritos en la UI, reiniciar el dispositivo, abrir la app y verificar que se listan correctamente.

### Implementation for User Story 2

- [ ] T014 Implementar queries de favoritos (`insertFavorite`, `deleteFavorite`, `getFavoritesByUserId`) en `AppDatabase.sq`.
- [ ] T015 Crear la clase `LocalAuthRepositoryImpl` en `FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/data/local/LocalAuthRepositoryImpl.kt` que simule la sesión local del usuario por defecto (`local-user`).
- [ ] T016 Conectar los flujos de la pantalla de favoritos con `LocalFoodRepositoryImpl` para recuperar favoritos usando la relación local.
- [ ] T017 [P] Actualizar la inyección de dependencias de Koin en `AppModule.kt` para instanciar `LocalAuthRepositoryImpl`.

---

## Phase 5: User Story 3 - Personalización de Alimentos (Overrides) (Priority: P3)

**Goal**: Permitir al usuario modificar notas de un alimento a nivel local.

**Independent Test**: Escribir una nota personalizada en un alimento, reiniciar la app, y verificar que se recupera correctamente en la vista de detalle.

### Implementation for User Story 3

- [ ] T018 Implementar consultas de edición y sobreescritura (`insertFoodOverride`, `getFoodOverrideById`) en `AppDatabase.sq`.
- [ ] T019 Integrar la lectura de sobreescritura de notas locales en `LocalFoodRepositoryImpl.getFoodDetail`.
- [ ] T020 Integrar la actualización de la UI en la vista de detalle del producto para guardar la nota local.
