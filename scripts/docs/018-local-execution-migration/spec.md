# Feature Specification: Migración a Ejecución Local (Offline-first)

**Feature Branch**: `018-local-execution-migration`

**Created**: 2026-07-23

**Status**: Draft

**Input**: Migrar el backend a ejecución local para no depender de servidores externos, utilizando SQLDelight en KMP y un SQLite pre-empaquetado con el catálogo de alimentos sin gluten.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Búsqueda de Alimentos Offline (Priority: P1)

Como usuario celíaco, quiero buscar alimentos sin gluten en la aplicación incluso cuando no tengo conexión a internet, para poder hacer mi compra de forma segura en cualquier supermercado.

**Why this priority**: Es la funcionalidad principal de la aplicación. Al no depender de un servidor externo, la app debe responder de forma instantánea usando el catálogo local.

**Independent Test**: Desactivar el Wi-Fi/Datos del dispositivo de prueba (o simulador) y realizar búsquedas de alimentos. Los resultados deben retornar de forma inmediata.

**Acceptance Scenarios**:

1. **Given** El usuario abre la app sin conexión a internet por primera vez, **When** la base de datos se inicializa, **Then** el catálogo de alimentos precargados (`freeglu.db`) se copia al almacenamiento del dispositivo de forma transparente.
2. **Given** El usuario realiza una búsqueda de un producto por código de barras o nombre, **When** no hay conexión a internet, **Then** se obtienen los resultados correspondientes directamente desde la base de datos local SQLite instantáneamente.

---

### User Story 2 - Perfil y Favoritos Locales (Priority: P2)

Como usuario, quiero poder registrar favoritos y que se guarden permanentemente en mi dispositivo, sin necesidad de crear una cuenta en la nube.

**Why this priority**: Permite la personalización básica sin requerir una infraestructura de servidores costosa o procesos de registro con datos sensibles.

**Independent Test**: Agregar un alimento a favoritos, cerrar completamente la aplicación, volver a abrirla y comprobar que el alimento sigue listado como favorito.

**Acceptance Scenarios**:

1. **Given** El usuario está en la pantalla de detalle de un alimento, **When** hace clic en el botón de favoritos, **Then** la relación se guarda de forma persistente en la tabla `UserFavorite` local.
2. **Given** La aplicación no posee inicio de sesión en la nube, **When** se arranca la app por primera vez, **Then** se crea de forma automática un usuario/perfil local por defecto (`id: local-user`) para gestionar las preferencias.

---

### User Story 3 - Personalización de Alimentos (Overrides) (Priority: P3)

Como usuario, quiero poder editar notas o anular el estado de gluten de un alimento para mi uso personal local.

**Why this priority**: Da flexibilidad al usuario para agregar notas propias (ej. "Contiene trazas según la etiqueta nueva") y que esta personalización se mantenga localmente.

**Independent Test**: Editar las notas de un alimento, reiniciar la aplicación y verificar que la nota personalizada sigue apareciendo en el detalle de dicho alimento.

**Acceptance Scenarios**:

1. **Given** El usuario está en la vista de edición de un alimento, **When** guarda notas personalizadas, **Then** estas notas se guardan en la tabla local `UserFoodOverride`.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: La aplicación debe empaquetar un archivo de base de datos pre-generado SQLite (`freeglu.db`) que contenga el catálogo optimizado y filtrado de alimentos sin gluten.
- **FR-002**: En la primera ejecución de la app (o cuando no exista el archivo DB en el directorio privado de la app), el sistema debe copiar el archivo `freeglu.db` desde los assets nativos de la plataforma al directorio de datos privados del dispositivo.
- **FR-003**: El sistema debe configurar e inicializar **SQLDelight** utilizando el driver adecuado para cada plataforma (`AndroidSqliteDriver`, `NativeSqliteDriver` para iOS).
- **FR-004**: Los repositorios `FoodRepositoryImpl` y `AuthRepositoryImpl` deben sustituir el uso de llamadas de red (Ktor client `ApiService`) por consultas de base de datos directas mediante SQLDelight.
- **FR-005**: El sistema de autenticación debe simular un flujo síncrono local que mantenga una sesión persistente para un usuario local por defecto (`local-user`).

### Key Entities

- **Food**: Representa un alimento del catálogo local. Atributos clave: id, barcode, name, brand, category, isGlutenFree, ingredients.
- **User**: Representa el perfil de usuario local. Atributos clave: id, profileName, preferences.
- **UserFavorite**: Relación muchos a muchos para marcar alimentos favoritos a nivel local.
- **UserFoodOverride**: Contiene los campos personalizados de un alimento para un usuario concreto (ej. notas locales).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Las búsquedas de alimentos deben resolverse en menos de 50ms, al ejecutarse localmente de forma indexada mediante SQLite.
- **SC-002**: El tamaño del archivo de la base de datos pre-empaquetada en assets no debe superar los 50MB (catalogo filtrado de alimentos sin gluten de alta calidad, no el dataset completo de 12GB).
- **SC-003**: 100% de la funcionalidad básica (búsqueda, favoritos, historial) debe estar disponible de forma offline sin internet.

## Assumptions

- Se asume que el script de Python `scripts/import_csv.py` o similar puede adaptarse fácilmente para exportar a un formato compatible SQLite (`freeglu.db`).
- Se asume que la carga web (Wasm) puede tener limitaciones técnicas para la persistencia persistente de SQLite (web-worker-driver). Para la fase inicial de Wasm, se podría usar una base de datos en memoria o persistida en localstorage para favoritos, limitando el catálogo completo si el navegador no soporta SQLite de forma eficiente.
