# Implementation Plan: Migración a Ejecución Local (Offline-first)

**Branch**: `018-local-execution-migration` | **Date**: 2026-07-23 | **Spec**: `spec.md`

## Summary

Migrar la arquitectura actual del monorepo a un modelo offline-first (ejecución totalmente local en el dispositivo) para eliminar la dependencia de servidores backend en la nube. Reemplazaremos la capa de red del cliente KMP con una base de datos local SQLite gestionada mediante **SQLDelight**. El catálogo de alimentos preexistente se exportará a SQLite usando un script ETL modificado y se empaquetará como un Asset nativo dentro de la app móvil.

## Technical Context

**Language/Version**: Kotlin 1.9.x / 2.0.x, Python 3.11+, SQLDelight 2.0.x

**Primary Dependencies**: `app.cash.sqldelight` (runtime, android-driver, native-driver), `sqlite3`

**Storage**: SQLite pre-empaquetado (`freeglu.db`) copiado a `/databases/` en Android y al contenedor de la sandbox de iOS.

**Testing**: JUnit local, SQLDelight in-memory SQLite driver tests.

**Target Platform**: Android, iOS, con fallback/mock en Web Wasm/JS.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Is standard KMP structure preserved?** Sí, todo se integra en `FreeGluKMP/shared/` usando la separación `commonMain`, `androidMain` e `iosMain`.
- **Is Room avoided for Wasm?** Sí, no utilizaremos Room. Usaremos SQLDelight.
- **Is Koin initialization synchronous?** Sí, se mantendrá síncrona en los puntos de entrada nativos de la plataforma.

## Project Structure

### Documentation

```text
scripts/docs/018-local-execution-migration/
├── spec.md              # Feature specification
├── plan.md              # This file
├── tasks.md             # Granular tasks
└── checklists/
    └── requirements.md  # Acceptance and quality checklists
```

### Source Code

```text
FreeGluKMP/
├── shared/
│   ├── src/
│   │   ├── commonMain/
│   │   │   ├── kotlin/com/ivan/freeglukmp/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── LocalFoodRepositoryImpl.kt   # NUEVO: Reemplaza a FoodRepositoryImpl anterior
│   │   │   │   │   │   └── LocalAuthRepositoryImpl.kt   # NUEVO: Reemplaza a AuthRepositoryImpl anterior
│   │   │   │   │   └── sqldelight/
│   │   │   │   │       └── Database.sq                  # NUEVO: Definición del esquema SQLite
│   │   │   │   └── di/
│   │   │   │       └── AppModule.kt                    # MODIFICADO: Inyección de SQLDelight
│   │   │   ├── sqldelight/
│   │   │   │   └── com/ivan/freeglukmp/data/db/
│   │   │   │       └── AppDatabase.sq                   # NUEVO: Queries SQLDelight
│   │   ├── androidMain/
│   │   │   └── kotlin/.../
│   │   │       └── data/
│   │   │           └── DatabaseDriverFactory.android.kt # NUEVO: Driver SQLite Android
│   │   └── iosMain/
│   │       └── kotlin/.../
│   │           └── data/
│   │               └── DatabaseDriverFactory.ios.kt     # NUEVO: Driver SQLite iOS
```

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Ninguna | N/A | N/A |
