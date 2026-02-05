# Análisis de Complejidad Algorítmica (Big O)

Este documento detalla el análisis asintótico temporal de las operaciones implementadas en el sistema. Se utiliza la notación Big O para describir el comportamiento en el peor caso.

---

## 1. Tipos de Datos Abstractos (TDAs)

### 1.1. Pila (Stack)
Implementación basada en lista enlazada simple.

| Operación | Complejidad | Justificación |
| :--- | :--- | :--- |
| `apilar(T)` | **O(1)** | Inserción en cabeza de la lista. |
| `desapilar()` | **O(1)** | Eliminación de cabeza de la lista. |
| `verTope()` | **O(1)** | Acceso a referencia `tope`. |
| `estaVacia()` | **O(1)** | Verificación de nulidad de referencia. |

### 1.2. Cola (Queue)
Implementación basada en lista enlazada con punteros a extremos.

| Operación | Complejidad | Justificación |
| :--- | :--- | :--- |
| `encolar(T)` | **O(1)** | Inserción usando puntero `fin`. |
| `desencolar()` | **O(1)** | Eliminación usando puntero `frente`. |
| `verFrente()` | **O(1)** | Acceso a referencia `frente`. |

### 1.3. Diccionario (Hash Table)
Implementación basada en hashing directo (capacidad 64).

| Operación | Complejidad | Justificación |
| :--- | :--- | :--- |
| `insertar(K,V)` | **O(1)*** | Hash + inserción en bucket. |
| `obtener(K)` | **O(1)*** | Hash + búsqueda lineal en bucket. |
| `eliminar(K)` | **O(1)*** | Hash + eliminación en bucket. |

*\* Amortizado promedio, O(n) en peor caso de colisión total.*

---

## 2. Servicios de Negocio (`GestorClientes`)

La lógica de negocio se ha diseñado para minimizar operaciones lineales.

### 2.1. Gestión de Entidades

| Operación | Complejidad | Análisis |
| :--- | :--- | :--- |
| Crear Cliente | **O(1)** | Inserción directa en diccionario por ID. |
| Buscar por ID | **O(1)** | Acceso indexado en diccionario. |
| Buscar por Scoring | **O(n)** | Requiere recorrido exhaustivo de todos los registros (inevitable sin índice secundario). |

### 2.2. Interacciones

| Operación | Complejidad | Análisis |
| :--- | :--- | :--- |
| Seguir Usuario | **O(1)** | Inserción en diccionario de seguidos del cliente. |
| Dejar de Seguir | **O(1)** | Eliminación en diccionario de seguidos. |
| Enviar Solicitud | **O(1)** | Encolado en estructura FIFO del objetivo. |
| Procesar Solicitud | **O(1)** | Desencolado y actualización de estado. |

### 2.3. Funcionalidad Undo/Redo

| Operación | Complejidad | Análisis |
| :--- | :--- | :--- |
| Deshacer Acción | **O(1)** | Movimiento de pila `historial` a `pilaRehacer`. |
| Rehacer Acción | **O(1)** | Movimiento de pila `pilaRehacer` a `historial`. |

---

## 3. Persistencia

El sistema utiliza una estrategia de persistencia monolítica.

| Operación | Complejidad | Descripción |
| :--- | :--- | :--- |
| Carga Inicial | **O(n)** | Lectura secuencial y deserialización del dataset JSON completo al iniciar. |
| Guardado Final | **O(n)** | Serialización y escritura secuencial de todo el estado en memoria al cerrar. |

---

## 4. Conclusión

El análisis demuestra que el sistema cumple con los requisitos de eficiencia:
*   Todas las operaciones interactivas críticas son **O(1)**.
*   Las operaciones de mantenimiento (búsqueda por atributo no indexado, carga/guardado) son **O(n)**, comportamiento esperado y aceptable.
*   No existen operaciones cuadráticas **O(n²)** que comprometan la escalabilidad.
