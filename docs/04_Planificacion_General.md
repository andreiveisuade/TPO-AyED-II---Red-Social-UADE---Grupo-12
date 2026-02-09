# Planificación Estratégica del Sistema de Gestión de Red Social

Este documento establece la hoja de ruta técnica para el desarrollo del producto, definiendo cronogramas, casos de uso, modelo de datos y arquitectura de estructuras de datos.

---

## 1. Alcance Funcional

El proyecto se despliega en iteraciones incrementales para garantizar la entrega continua de valor.

### 1.1. Iteración 1: Gestión Core (Completada)
*   Administración de ciclo de vida de clientes (CRUD).
*   Gestión de historial transaccional (Undo/Redo) mediante patrón Command.
*   Procesamiento asíncrono de solicitudes de seguimiento (Cola FIFO).
*   Carga masiva de datos y persistencia.

### 1.2. Iteración 2: Relaciones y Búsqueda (Futura)
*   Implementación de relaciones dirigidas ("Seguir").
*   Búsqueda por scoring mediante recorrido del diccionario.
*   Implementación de ABB para modelar relaciones de seguimiento y consultas por nivel.
*   Consultas estructurales (e.g., usuarios en nivel K del árbol).

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
*   **CU-05 Rehacer Acción**: Restauración de una operación previamente deshecha.
*   **CU-06 Auditoría**: Visualización secuencial del historial de operaciones.

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
| **Diccionario** | O(1) Amortizado | Acceso crítico por ID en dataset masivo (1M+). |
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
| Buscar por nombre | O(N) | O(N) |
| Buscar por scoring | O(N) | O(N) |
| Eliminar cliente (cascada) | O(n + E) | O(k) |
| Undo/Redo | O(costo operación) | O(1) |
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
| **Information Expert** | `Cliente` conoce sus seguidos, `Grafo` conoce sus adyacencias |
| **Creator** | `GestorClientes` crea instancias de `Cliente`, `JsonLoader` crea objetos desde JSON |
| **Controller** | `Menu` coordina la interacción usuario-sistema sin lógica de negocio |
| **Low Coupling** | TDAs no dependen entre sí; servicios solo conocen las interfaces de TDAs |
| **High Cohesion** | Cada módulo tiene responsabilidades relacionadas (`tda/` solo estructuras, `modelo/` solo entidades) |
| **Pure Fabrication** | `HistorialAcciones` es una fabricación pura para manejar undo/redo sin contaminar entidades |
| **Indirection** | Los servicios actúan como intermediarios entre la vista y los TDAs |
| **Protected Variations** | Cambiar implementación de ABB a AVL no afecta a los servicios que lo usan |

---

## 12. Preparación Técnica para Iteración 2

### 12.1. Estructura de ABB (ya implementada)

| Archivo | Descripción |
|---------|-------------|
| `src/tda/ABB.java` | Implementación del Árbol Binario de Búsqueda |
| `src/tda/NodoABB.java` | Nodo del árbol con clave, valor, izquierdo, derecho |

### 12.2. Uso del ABB según consigna

Según la consigna de Iteración 2, el ABB se utilizará para **modelar las relaciones de seguimiento** entre clientes, permitiendo imprimir los clientes en el cuarto nivel del árbol para determinar quién tiene más seguidores.

> **Nota sobre scoring**: Inicialmente se implementó el ABB como índice secundario por scoring, pero fue descartado porque con 1M de clientes y solo 101 valores posibles de scoring (0-100), el árbol degeneraba a una lista enlazada (~10.000 nodos por valor), causando stack overflow en la inserción recursiva. La búsqueda por scoring se resuelve con un recorrido lineal O(N) del diccionario, que es eficiente en la práctica (~1s para 1M registros).

### 12.3. Operaciones del ABB

| Operación | Complejidad Esperada |
|-----------|---------------------|
| `insertar(clave, valor)` | O(log n) promedio |
| `buscar(clave)` | O(log n) promedio |
| `eliminar(clave, valor)` | O(log n) promedio |
| `buscarRango(min, max)` | O(log n + k) |
| `inOrder()` | O(n) |

### 12.4. Invariantes del ABB

- Nodo izquierdo < Nodo padre
- Nodo derecho >= Nodo padre (permite duplicados)
- Altura balanceada no garantizada (ABB simple, no AVL)

