# Especificación de Tipos de Datos Abstractos (TDAs)

Este documento ofrece un análisis académico y técnico sobre la selección, implementación y aplicación de las estructuras de datos fundamentales en el sistema, justificando su idoneidad para resolver los desafíos de rendimiento y modelado del dominio.

---

## 1. Resumen de Complejidad Computacional

Se ha diseñado el sistema priorizando un límite superior asintótico de **O(1)** para las operaciones críticas, asegurando escalabilidad independiente del volumen de datos (N=1,000,000).

| Estructura | Interfaz | Implementación Concreta | Big O (Acceso) | Big O (Inserción) | Big O (Eliminación) | Rol Arquitectónico |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Diccionario** | `IDiccionario<K,V>` | *Hash Table (Separate Chaining)* | O(1)* | O(1)* | O(1)* | Motor de búsqueda e indexación primaria. |
| **Pila** | `IPila<T>` | *Linked List (LIFO)* | O(1) | O(1) | O(1) | Gestión de estados temporales (Historial). |
| **Cola** | `ICola<T>` | *Linked List (FIFO)* | O(1) | O(1) | O(1) | Buffer de procesamiento secuencial. |
| **Conjunto** | `IConjunto` | *Hash Set Adapter* | O(1)* | O(1)* | O(1)* | Validación de unicidad. |
| **ABB** | `IArbolBinarioBusqueda<K,V>` | *BST con duplicados* | O(log N)** | O(log N)** | O(log N)** | Índice secundario por scoring. |

*\* Amortizado promedio, asumiendo Función Hash uniforme y factor de carga controlado.*
*\*\* Promedio, asumiendo distribución razonablemente uniforme de claves. Peor caso O(N) si el árbol degenera.*

---

## 2. Diccionario (Hash Table)

### 2.1. Fundamentación Teórica
El **Diccionario** es la columna vertebral del sistema. Se seleccionó una **Tabla Hash con Encadenamiento Separado (Separate Chaining)** frente a otras alternativas como Árboles Binarios de Búsqueda (BST) o Árboles AVL.
*   **Hash Table vs. BST**: Mientras que un BST ofrece operaciones en O(log n), una Tabla Hash bien dimensionada reduce esto a **O(1)**. Dado el requerimiento de manejar 1 millón de registros, la diferencia entre O(1) y O(log 10^6) ≈ 20 operaciones es significativa en latencia agregada.
*   **Encadenamiento vs. Direccionamiento Abierto**: Se eligió encadenamiento (listas enlazadas en buckets) porque degrada más suavemente ante factores de carga altos sin requerir rehashing costoso ni sufrir de *clustering* primario.

### 2.2. Implementación en Java
La clase `Diccionario<K, V>` implementa una tabla hash genérica manual (sin usar `java.util.HashMap`).

*   **Estructura Interna**: `NodoDiccionario<K, V>[] tabla`.
*   **Función Hash**: `h(k) = Math.abs(k.hashCode()) % M`, donde `M` es la capacidad.
*   **Manejo de Colisiones**: Inserción al inicio de la lista enlazada del bucket correspondiente.

### 2.3. Comportamiento con Claves Enteras (Direct Addressing)
Dado que los IDs de los clientes son enteros secuenciales y únicos (1...1.000.000), y que la implementación de `hashCode()` para la clase `Integer` en Java retorna el propio valor del entero, se produce un fenómeno de **direccionamiento cuasi-directo**:
*   **Cálculo**: `h(id) = id % 1.000.003`.
*   **Resultado**: Para todo $id \leq 1.000.002$, el índice del bucket es exactamente igual al `id`.
*   **Implicación**: En la práctica, el Diccionario actúa como un **Arreglo de Acceso Directo**, eliminando colisiones y garantizando un acceso $O(1)$ determinista, mientras mantiene la flexibilidad genérica para soportar otras claves (ej. Strings en Conjunto).

### 2.4. Aplicación y Optimización Específica (Binding)
El sistema instancia esta estructura de dos formas críticas:

#### A. Repositorio de Clientes (`GestorClientes`)
*   **Instancia**: `Diccionario<Integer, Cliente> clientes`.
*   **Optimización de Memoria/Tiempo**: Se inicializa con una capacidad de **M = 1,000,003** (número primo).
    *   **Justificación**: Al tener M > N (1.000.003 > 1.000.000), el factor de carga α ≈ 1. Esto minimiza drásticamente las colisiones.
    *   **Impacto**: Buscar un usuario por ID es instantáneo.

#### B. Índice por Nombre (`GestorClientes`)
*   **Instancia**: `Diccionario<String, Cola<Cliente>> indiceNombre`.
*   **Uso**: Índice secundario que agrupa clientes por nombre normalizado (lowercase).
*   **Ventaja**: Búsqueda por nombre en **O(1)** en lugar de O(N) secuencial.

#### C. Grafo de Relaciones (`Cliente`)
*   **Instancia**: `Diccionario<Integer, Boolean> siguiendo`, `Diccionario<Integer, Boolean> seguidores`, y `Diccionario<Integer, Boolean> amistades` (Iteración 3).
*   **Uso**: Representa las aristas del grafo social. "Siguiendo/Seguidores" son dirigidas (Iteraciones 1-2). "Amistades" son bidireccionales (Iteración 3).
*   **Ventaja**: Permite verificar si "Usuario A sigue a Usuario B" o si "Son amigos" en **O(1)**.

---

## 3. Pila (Stack)

### 3.1. Fundamentación Teórica
La **Pila** es una estructura LIFO (Last In, First Out) ideal para problemas que requieren backtracking o reversión de estados.

### 3.2. Implementación en Java
La clase `Pila<T>` utiliza una lista enlazada simple, manteniendo únicamente una referencia al nodo `tope`.
*   **Eficiencia**: No requiere desplazamiento de elementos (como en un ArrayStack), por lo que `push` y `pop` son siempre O(1) reales, sin amortización.

### 3.3. Aplicación en el Sistema: Patrón Command
*   **Contexto**: Funcionalidad de "Deshacer" (Undo).
*   **Instancia**: `Pila<Accion> historial` dentro de `HistorialAcciones` (en `Sesion`).
*   **Mecánica**:
    1.  Cada operación reversible crea un objeto `Accion` (Command).
    2.  Esta acción se apila (`push`) en el historial.
    3.  Al solicitar "Deshacer", se desapila (`pop`) la última acción y se ejecuta su lógica inversa.

---

## 4. Cola (Queue)

### 4.1. Fundamentación Teórica
La **Cola** es una estructura FIFO (First In, First Out) esencial para gestionar flujos de trabajo asincrónicos o bufferizados donde el orden de llegada determina la prioridad de atención (Fairness).

### 4.2. Implementación en Java
La clase `Cola<T>` emplea una lista enlazada con doble puntero:
*   `frente`: Para eliminaciones (desencolar) en O(1).
*   `fin`: Para inserciones (encolar) en O(1).

### 4.3. Aplicación en el Sistema: Gestión de Solicitudes
*   **Contexto**: Recepción de solicitudes de seguimiento.
*   **Instancia**: `Cola<SolicitudSeguimiento> solicitudesPendientes` dentro de cada `Cliente`.
*   **Valor Académico**: Garantiza la **equidad** en el procesamiento.

---

## 5. Conjunto (Set)

### 5.1. Fundamentación Teórica
El **Conjunto** modela la abstracción matemática de una colección de elementos únicos, abstrayendo el orden.

### 5.2. Implementación en Java (Adapter Pattern)
La clase `Conjunto` se implementa como un **Adapter** sobre el `Diccionario`.
*   **Mecanismo**: Almacena el elemento como *clave* y un valor dummy (`true`) como *valor*.

---

## 6. Árbol Binario de Búsqueda (ABB)

### 6.1. Fundamentación Teórica
El **ABB** es una estructura jerárquica donde cada nodo mantiene la propiedad: claves del subárbol izquierdo < clave del nodo <= claves del subárbol derecho. Permite búsquedas O(log N) en promedio, y recorridos ordenados naturales (inorder).

### 6.2. Implementación en Java
La clase `ArbolBinarioBusqueda<K extends Comparable<K>, V>` implementa un ABB genérico con soporte para duplicados.
*   **Estructura Interna**: `NodoABB<K, V> raiz` con punteros `izquierdo` y `derecho`.
*   **Duplicados**: Claves iguales se insertan en el subárbol derecho.
*   **Eliminación**: Maneja los 3 casos clásicos (hoja, un hijo, dos hijos con sucesor inorder).
*   **Recorrido por niveles**: BFS usando `Cola` (TDA propio) para obtener nodos en un nivel específico.

### 6.3. Aplicación en el Sistema: Índice por Scoring
*   **Instancia**: `ArbolBinarioBusqueda<Integer, Cliente> indiceScoring` dentro de `GestorClientes`.
*   **Ventaja**: Búsqueda por scoring pasa de O(N) a **O(log N + k)** donde k = coincidencias.
*   **Caso de uso**: Obtener clientes en el cuarto nivel del árbol para análisis de distribución de scoring.

---

## 7. Índice Secundario por Nombre

### 7.1. Fundamentación
La búsqueda por nombre originalmente requería un recorrido lineal O(N) de todos los clientes. Para cumplir con el requerimiento de búsquedas eficientes, se implementó un **índice hash secundario**.

### 7.2. Implementación
*   **Instancia**: `Diccionario<String, Cola<Cliente>> indiceNombre` dentro de `GestorClientes`.
*   **Clave**: Nombre normalizado (lowercase) para búsqueda case-insensitive.
*   **Valor**: `Cola<Cliente>` que agrupa los clientes con el mismo nombre (puede haber duplicados).
*   **Complejidad**: O(1) para el lookup + O(k) para recorrer las k coincidencias.

### 7.3. Sincronización
El índice se mantiene sincronizado con el diccionario principal en todas las operaciones:
*   `agregarCliente()`: Inserta en el índice.
*   `eliminarCliente()`: Elimina del índice (reconstruye la cola sin el cliente eliminado).
*   `ejecutarUndo()`: Actualiza el índice al deshacer agregar o eliminar.

---

## 8. Abstracción y Diseño Orientado a Objetos

El sistema demuestra madurez en ingeniería de software mediante el uso estricto de **Interfaces y Genéricos**.

### 6.1. Principio de Inversión de Dependencias (DIP)
Las clases de alto nivel (`GestorClientes`, `Sesion`) no dependen de implementaciones concretas, sino de interfaces:
*   `IDiccionario<K,V>`
*   `IPila<T>`
*   `ICola<T>`

### 6.2. Genéricos (Generics)
El uso de `<T>`, `<K, V>` permite un **Polimorfismo Paramétrico**. Una única clase `Diccionario` sirve tanto para indexar Clientes por ID (`<Integer, Cliente>`) como para verificar seguidos (`<Integer, Boolean>`), garantizando **Type Safety**.

---

## 7. Diagrama Conceptual de Estructuras

```mermaid
classDiagram
    %% Relaciones de Uso
    GestorClientes ..> IDiccionario : usa <Integer, Cliente> (índice por ID)
    GestorClientes ..> IDiccionario : usa <String, Cola> (índice por nombre)
    GestorClientes ..> IArbolBinarioBusqueda : usa <Integer, Cliente> (índice por scoring)
    Cliente ..> IDiccionario : usa <Integer, Boolean> (siguiendo + seguidores)
    Cliente ..> ICola : usa <Solicitud>
    HistorialAcciones ..> IPila : usa <Accion>

    %% Interfaces
    class IDiccionario~K,V~ {
        +insertar(K, V)
        +obtener(K): V
    }
    class IArbolBinarioBusqueda~K,V~ {
        +insertar(K, V)
        +buscar(K): Object[]
        +obtenerEnNivel(int): Object[]
    }
    class IPila~T~ {
        +apilar(T)
        +desapilar(): T
    }
    class ICola~T~ {
        +encolar(T)
        +desencolar(): T
    }

    %% Implementaciones
    class Diccionario~K,V~ {
        -tabla: Nodo[]
        -h(k): int
    }
    class ArbolBinarioBusqueda~K,V~ {
        -raiz: NodoABB
        -cantidad: int
    }
    class Pila~T~ {
        -tope: Nodo
    }
    class Cola~T~ {
        -frente: Nodo
        -fin: Nodo
    }

    IDiccionario <|.. Diccionario
    IArbolBinarioBusqueda <|.. ArbolBinarioBusqueda
    IPila <|.. Pila
    ICola <|.. Cola
```

## 8. Conclusión

La selección de estructuras no es accidental, sino el resultado de un análisis de los requisitos no funcionales del sistema:
1.  **Diccionario O(1)**: Necesario por el volumen de 1M de usuarios (índice por ID y por nombre).
2.  **ABB O(log N)**: Necesario para búsquedas por scoring sin recorrer todos los clientes.
3.  **Pila LIFO**: Necesaria por la lógica de reversión temporal (Undo).
4.  **Cola FIFO**: Necesaria por la lógica de equidad temporal (Solicitudes).

## 9. Análisis de Escalabilidad (Escenario 10M+ Usuarios)

Ante la pregunta de cómo se comportaría el sistema si la base de usuarios creciera de 1.000.000 (1M) a 10.000.000 (10M), el análisis técnico es el siguiente:

### 9.1. Comportamiento en la Implementación Actual
Actualmente, el Diccionario tiene una capacidad fija de **M = 1,000,003** buckets.
*   **Factor de Carga ($\alpha = N/M$)**: Con 10M de usuarios, $\alpha \approx 10$.
*   **Impacto en Rendimiento/Colisiones**: En promedio, cada bucket contendría una lista enlazada de **10 nodos**.
*   **Degradación del Tiempo de Acceso**: La búsqueda sigue siendo técnicamente $O(1)$ amortizado porque el factor de carga es constante respecto a N (si consideramos N=10M fijo), pero esa "constante" es 10 veces mayor que con 1M.

### 9.2. ¿Es la estructura (Hash Table) la correcta para 10M o más?
**SÍ**. La Tabla Hash sigue siendo la estructura de datos superior para este problema.
*   **Comparación**: Cambiar a un Árbol (AVL, Rojo-Negro) o B-Tree aumentaría la complejidad de acceso de $O(1)$ a $O(\log N)$ de forma permanente.

### 9.3. Evolución Necesaria: Rehashing Dinámico
Para escalar indefinidamente manteniendo $\alpha \approx 1$, se requeriría implementar **Rehashing Dinámico**:
1.  **Monitoreo**: Verificar $\alpha$ tras cada inserción.
2.  **Expansión**: Duplicar la capacidad de la tabla cuando $\alpha > 0.75$.
3.  **Reubicación**: Recalcular hashes y mover elementos ($O(N)$ amortizado).

**Veredicto Final**: Para escalar a 10M, la estructura correcta sigue siendo el Diccionario (Hash Table). La única modificación requerida sería implementar una estrategia dinámica de redimensionamiento.
