# Quality Checklist: Migración a Ejecución Local (Offline-first)

**Purpose**: Asegurar la calidad, estabilidad y paridad visual/funcional al migrar de backend remoto a ejecución local SQLite.
**Created**: 2026-07-23
**Feature**: `018-local-execution-migration`

## Arquitectura y Persistencia

- [ ] CHK001 El archivo `freeglu.db` pre-empaquetado se encuentra correctamente en los directorios de Assets (`androidApp/src/main/assets` e `iosApp/Resources`).
- [ ] CHK002 La copia de la base de datos de Assets a la ubicación local se realiza sólo en la primera ejecución de la app (o si se ha borrado), sin sobreescribir datos del usuario como favoritos.
- [ ] CHK003 Se ha implementado `DatabaseDriverFactory` utilizando `expect` / `actual` de KMP, garantizando que el driver correcto se inicializa en Android e iOS.
- [ ] CHK004 SQLDelight genera correctamente las interfaces de base de datos (`generateSqlDelightInterface` compila sin errores).
- [ ] CHK005 Se evitan ORMs incompatibles o inestables como Room para la compatibilidad Wasm (según la regla C de GEMINI.md).

## Capa de Red y Repositorios

- [ ] CHK002 Se reemplazan por completo las llamadas de `ApiService` de Ktor en `LocalFoodRepositoryImpl` y `LocalAuthRepositoryImpl`.
- [ ] CHK003 La inyección de dependencias con Koin se inicializa de forma síncrona en los puntos de entrada nativos (`MainActivity.onCreate`, `MainViewController.kt`, `main.kt`) antes de que Compose intente inyectar (según la regla A de GEMINI.md).

## Rendimiento y Experiencia de Usuario (UX)

- [ ] CHK004 El tiempo de carga de las búsquedas en listas densas es inferior a 50ms (ejecución síncrona indexada local).
- [ ] CHK005 El tamaño final del binario compilado de la aplicación no se infla excesivamente debido al archivo `freeglu.db` (máximo sugerido: 50MB).
- [ ] CHK006 Las listas de alimentos con imágenes optimizadas siguen cargándose fluidamente utilizando Coil 3 con el 25% de la RAM en caché (según la regla D de GEMINI.md).

## Higiene de Git y Entorno

- [ ] CHK007 No se borra ni destruye la base de datos PostgreSQL de desarrollo actual si se levanta el entorno de testeo antiguo (según la regla F de GEMINI.md).
- [ ] CHK008 El archivo `.gitignore` de la raíz continúa bloqueando la carpeta `/data/` masiva sin afectar al código de Kotlin (según la regla E de GEMINI.md).
- [ ] CHK009 Las pruebas unitarias locales en JVM (`./gradlew :shared:allTests`) pasan con éxito utilizando un driver de SQLDelight en memoria.
