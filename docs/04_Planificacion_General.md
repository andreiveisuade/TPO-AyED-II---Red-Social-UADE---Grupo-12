# Planificación Estratégica del Sistema de Gestión de Red Social

Este documento establece la hoja de ruta técnica para el desarrollo del producto, definiendo cronogramas, casos de uso, modelo de datos y arquitectura de estructuras de datos.

---

## 1. Alcance Funcional

El proyecto se despliega en iteraciones incrementales para garantizar la entrega continua de valor.

### 1.1. Iteración 1: Gestión Core (Completada)
*   Administración de ciclo de vida de clientes (CRUD).
*   Gestión de historial transaccional (Undo) mediante patrón Command.
*   Procesamiento asíncrono de solicitudes de seguimiento (Cola FIFO).
*   Carga masiva de datos y persistencia.

### 1.2. Iteración 2: Relaciones y Búsqueda (Completada)
*   Implementación de relaciones dirigidas ("Seguir") con límite de 2 seguidos (`MAX_SEGUIDOS`).
*   Búsqueda eficiente por scoring mediante ABB como índice secundario (O(log N)).
*   Búsqueda eficiente por nombre mediante Diccionario hash como índice secundario (O(1)).
*   Implementación de ABB para consultas por nivel (cuarto nivel del árbol).
*   Rastreo bidireccional de seguidores (`siguiendo` + `seguidores` por cliente).

### 1.3. Iteración 3: Análisis de Red (Futura)
*   Modelado de amistades mediante Grafos no dirigidos.
*   Algoritmos de camino mínimo (BFS) para cálculo de distancias sociales.

---

## 2. Especificación de Casos de Uso (Iteración 1)

### 2.1. Gestión de Entidades
*   **CU-01 Alta de Cliente**: Registro de nuevas entidades con validación estricta de invariantes (unicidad de ID, rango de scoring).
*   **CU-02 Baja de Cliente**: Eliminación lógica y referencial cascada para mantener la integridad de la red.
*   **CU-03 Búsqueda Indexada**: Recuperación O(1) por clave primaria (ID/Usuario).

### 2.2. Operaciones Transaccionales
*   **CU-04 Deshacer Acción**: Reversión de la última operación mutadora del estado.
*   **CU-05 Auditoría**: Visualización secuencial del historial de operaciones.

### 2.3. Gestión de Solicitudes
*   **CU-07 Encolar Solicitud**: Registro de petición de seguimiento en buffer FIFO.
*   **CU-08 Procesar Solicitud**: Aprobación secuencial de peticiones pendientes.

---

## 3. Modelo de Dominio

### 3.1. Entidad `Cliente`
| Atributo | Tipo | Restricción |
| :--- | :--- | :--- |
| `id` | Integer | PK, > 0 |
| `nombre` | String | Not Null, Not Empty |
| `scoring` | Integer | [0, 100] |
| `siguiendo` | Diccionario | Colección de relaciones salientes |
| `seguidores` | Diccionario | Colección de relaciones entrantes |
| `solicitudes` | Cola | Buffer de entrada de peticiones |

### 3.2. Entidad `Accion` (Value Object)
Objeto inmutable que encapsula el estado necesario para la reversibilidad de operaciones.
*   Atributos: `TipoAccion`, `Datos (Snapshot)`, `Timestamp`.

---

## 4. Arquitectura de Datos

### 4.1. Selección de Estructuras (Iteración 1)

**Decisión Clave: Hash Table vs. Lista Enlazada**
Se migró de una implementación lineal O(n) a una tabla hash con encadenamiento para garantizar escalabilidad.

| Estructura | Complejidad | Justificación |
| :--- | :--- | :--- |
| **Diccionario** | O(1) Amortizado | Acceso crítico por ID en dataset masivo (1M+). También usado como índice por nombre. |
| **ABB** | O(log N) Promedio | Índice secundario por scoring. Consultas por nivel (BFS). |
| **Pila** | O(1) | Semántica LIFO nativa para gestión de historial. |
| **Cola** | O(1) | Semántica FIFO requerida para equidad en solicitudes. |

### 4.2. Estrategia de Persistencia
*   **Modelo**: Carga total en RAM ("Memory-First").
*   **Formato**: JSON serializado vía Gson.
*   **Ciclo**: `Read-Once` (Inicio) -> `In-Memory Operations` -> `Write-Once` (Cierre).

---

## 5. Plan de Aseguramiento de Calidad

### 5.1. Cobertura de Pruebas
Se priorizan pruebas de caja blanca para componentes críticos:
*   Unidad: Validación exhaustiva de métodos de TDAs (`insertar`, `eliminar`, `buscar`).
*   Integración: Flujos completos de `GestorClientes` (Alta -> Modificación -> Undo -> Redo).
*   Invariantes: Verificación de condiciones de frontera y restricciones de modelo.

### 5.2. Escenarios de Prueba
| Escenario | Resultado Esperado |
| :--- | :--- |
| Inserción de ID duplicado | Rechazo (Excepción) |
| Búsqueda de ID inexistente | Null / Option.Empty |
| Undo con pila vacía | Operación ignorada / Log |
| Carga de archivo corrupto | Fallo controlado / Notificación |

---

## 6. Métricas de Éxito
*   Tiempo de respuesta en operaciones CRUD < 10ms.
*   Tiempo de carga inicial (1M registros) < 5s.
*   Cero inconsistencias de datos tras ciclos de Undo/Redo.
|------|---------|----------|
| Seguir (< 2) | Alice → Bob | OK |
| Seguir (= 2) | tercer seguido | Error |
| Seguir duplicado | Alice → Bob (ya sigue) | Error |
| Seguir a sí mismo | Alice → Alice | Error |
| Nivel 4 vacío | árbol pequeño | [] |
| Nivel 4 | árbol completo | nodos nivel 4 |
| ABB vacío | buscar en árbol | null/[] |

### Iteración 3

| Caso | Entrada | Esperado |
|------|---------|----------|
| Conexión nueva | Alice ↔ Eve | OK |
| Conexión duplicada | existente | Ignorar |
| Conexión a sí mismo | Alice ↔ Alice | Error |
| Conexión a inexistente | Alice ↔ ZZZ | Error |
| Distancia directa | vecinos | 1 |
| Distancia indirecta | 2 saltos | 2 |
| Distancia a sí mismo | Alice → Alice | 0 |
| Sin camino | desconectados | -1 |
| Distancia a inexistente | Alice → ZZZ | -1 |
| Eliminar con cascada | cliente con relaciones | Todo limpio |
| Undo eliminar | - | Cliente + relaciones restauradas |

---

## 10. Complejidad Consolidada

| Operación | Tiempo | Espacio |
|-----------|--------|---------|
| Insertar cliente | O(1) | O(1) |
| Buscar por nombre | O(1 + k) | O(k) |
| Buscar por scoring | O(log N + k) | O(k) |
| Eliminar cliente (cascada) | O(n + E) | O(k) |
| Undo | O(costo operación) | O(1) |
| Agregar solicitud | O(1) | O(1) |
| Procesar solicitud | O(1) | O(1) |
| Agregar conexión | O(1) | O(1) |
| Obtener vecinos | O(1) | O(1) |
| Calcular distancia | O(V + E) | O(V) |
| Obtener nivel K | O(n) | O(n) |


## 11. Cumplimiento SOLID/GRASP

### SOLID

| Principio | Cumplimiento |
|-----------|--------------|
| **S**ingle Responsibility | Cada clase tiene una única responsabilidad: `GestorClientes` solo gestiona clientes, `HistorialAcciones` solo el historial, etc. |
| **O**pen/Closed | Los TDAs son cerrados a modificación pero abiertos a extensión (ej: ABB puede extenderse a AVL sin modificar código cliente) |
| **L**iskov Substitution | Los TDAs implementan interfaces consistentes; cualquier implementación de Pila puede sustituir a otra |
| **I**nterface Segregation | Interfaces pequeñas y específicas por TDA, no una interfaz monolítica |
| **D**ependency Inversion | Los servicios dependen de abstracciones (TDAs), no de implementaciones concretas |

### GRASP

| Patrón | Aplicación |
|--------|------------|
| **Information Expert** | `Cliente` conoce sus seguidos y seguidores, `GestorClientes` conoce los índices |
| **Creator** | `GestorClientes` crea instancias de `Cliente` y gestiona la carga desde JSON |
| **Controller** | `Menu` coordina la interacción usuario-sistema sin lógica de negocio |
| **Low Coupling** | TDAs no dependen entre sí; servicios solo conocen las interfaces de TDAs |
| **High Cohesion** | Cada módulo tiene responsabilidades relacionadas (`tda/` solo estructuras, `modelo/` solo entidades) |
| **Pure Fabrication** | `HistorialAcciones` es una fabricación pura para manejar undo/redo sin contaminar entidades |
| **Indirection** | Los servicios actúan como intermediarios entre la vista y los TDAs |
| **Protected Variations** | Cambiar implementación de ABB a AVL no afecta a los servicios que lo usan |

---

## 12. Índices Secundarios Implementados (Iteración 2)

### 12.1. Estructura Triple de Indexación en `GestorClientes`

| Índice | Estructura | Archivo | Clave | Complejidad |
|--------|-----------|---------|-------|-------------|
| Primario (ID) | `Diccionario<Integer, Cliente>` | `src/tda/Diccionario.java` | ID del cliente | O(1) |
| Scoring | `ArbolBinarioBusqueda<Integer, Cliente>` | `src/tda/ArbolBinarioBusqueda.java` | Scoring (0-100) | O(log N) |
| Nombre | `Diccionario<String, Cola<Cliente>>` | `src/tda/Diccionario.java` + `src/tda/Cola.java` | Nombre (lowercase) | O(1) |

### 12.2. ABB como Índice por Scoring

El ABB se usa como **índice secundario por scoring**, permitiendo:
*   Búsqueda eficiente por scoring: O(log N + k).
*   Consultas por nivel (BFS): Obtener clientes en el cuarto nivel del árbol para ver quién tiene más seguidores.
*   Manejo de duplicados: Clientes con mismo scoring van al subárbol derecho.

### 12.3. Diccionario como Índice por Nombre

El índice por nombre usa un `Diccionario<String, Cola<Cliente>>` donde:
*   La clave es el nombre normalizado (lowercase).
*   El valor es una `Cola<Cliente>` con todos los clientes que comparten ese nombre.
*   Permite búsqueda O(1) en lugar del recorrido lineal O(N) original.

### 12.4. Sincronización de Índices

Toda operación que modifique clientes (agregar, eliminar, undo) debe actualizar los **3 índices** simultáneamente para mantener consistencia.

### 12.5. Invariantes del ABB

- Nodo izquierdo < Nodo padre
- Nodo derecho >= Nodo padre (permite duplicados)
- Altura balanceada no garantizada (ABB simple, no AVL)

