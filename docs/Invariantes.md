# Invariantes de Representación

Este documento define formalmente las condiciones lógicas que garantizan la integridad y consistencia del estado interno de las clases del sistema.

---

## 1. Tipos de Datos Abstractos (TDAs)

### 1.1. `Pila<T>`
*   `cantidad ≥ 0`.
*   `tope != null` ⇔ `cantidad > 0`.
*   `tope == null` ⇔ `cantidad == 0`.
*   La longitud de la cadena de nodos enlazados desde `tope` debe ser exactamente igual a `cantidad`.

### 1.2. `Cola<T>`
*   `cantidad ≥ 0`.
*   `frente != null` ⇔ `fin != null`.
*   `frente == null` ⇔ `cantidad == 0`.
*   Si `cantidad == 1`, entonces `frente == fin`.
*   Si `cantidad > 1`, entonces `frente != fin`.
*   La longitud de la cadena de nodos desde `frente` hasta `fin` debe ser exactamente igual a `cantidad`.

### 1.3. `Diccionario<K,V>`
*   `cantidad ≥ 0`.
*   `tabla != null` y `tabla.length > 0`.
*   `tabla.length` es constante tras la inicialización (64 por defecto, 1.000.003 en producción).
*   Para cada bucket `tabla[i]`, no existen dos nodos con la misma clave `K`.
*   La suma de la longitud de todas las listas enlazadas en los buckets debe ser igual a `cantidad`.

### 1.4. `Conjunto`
*   `elementos != null` (es un `Diccionario<String, Boolean>`).
*   Todos los valores almacenados en el diccionario interno son `true`.
*   No existen claves duplicadas (garantizado por el TDA Diccionario).

### 1.5. `ArbolBinarioBusqueda<K,V>`
*   `cantidad ≥ 0`.
*   `raiz != null` ⇔ `cantidad > 0`.
*   `raiz == null` ⇔ `cantidad == 0`.
*   Para todo nodo N: todas las claves en el subárbol izquierdo son estrictamente menores que `N.clave`.
*   Para todo nodo N: todas las claves en el subárbol derecho son mayores o iguales que `N.clave` (duplicados van a la derecha).
*   La cantidad total de nodos accesibles desde `raiz` es exactamente igual a `cantidad`.

---

## 2. Entidades de Dominio

### 2.1. `Cliente`
*   `id` es un entero positivo (`id > 0`) único en el sistema.
*   `nombre` no es nulo, no está vacío y no contiene solo espacios.
*   `0 ≤ scoring ≤ 100`.
*   `siguiendo` no es nulo (Diccionario inicializado).
*   `seguidores` no es nulo (Diccionario inicializado).
*   `solicitudesPendientes` no es nulo (Cola inicializada).
*   **Reflexividad**: `siguiendo` NO contiene la clave `id` (un cliente no se sigue a sí mismo).
*   **Límite**: `siguiendo.cantidad ≤ MAX_SEGUIDOS` (actualmente 2).
*   **Consistencia**: `siguiendo.cantidad` debe ser igual a `getCantidadSiguiendo()`.
*   **Consistencia bidireccional**: Si cliente A tiene B en `siguiendo`, entonces cliente B tiene A en `seguidores` (mantenido por `GestorClientes`).

### 2.2. `Sesion` (Singleton)
*   **Unicidad**: Solo existe una instancia de `Sesion` en el runtime.
*   Estado de Autenticación:
    *   Si `estaAutenticado == true` ⇒ `usuarioActual != null` y `usuarioActual.id > 0`.
    *   Si `estaAutenticado == false` ⇒ `usuarioActual == null`.
*   `historial` nunca es nulo (se reinicia al cerrar sesión, pero el objeto existe).

### 2.3. `HistorialAcciones`
*   `historial` (Pila interna) nunca es nula.
*   Las acciones se almacenan en orden LIFO cronológico inverso (la más reciente en el tope).

### 2.4. `Accion` (Value Object)
*   **Inmutabilidad**: Todos los campos (`tipo`, `datos`, `timestamp`) son finales.
*   `tipo` no es nulo.
*   `datos` no es nulo (puede ser array vacío, pero no null).
*   `timestamp` no es nulo y representa el momento de creación.

### 2.5. `SolicitudSeguimiento` (Value Object)
*   `solicitante` (ID como String) no es nulo ni vacío.
*   `objetivo` (ID como String) no es nulo ni vacío.
*   `solicitante` != `objetivo` (un cliente no puede solicitarse seguir a sí mismo).

---

## 3. Gestores del Sistema

### 3.1. `GestorClientes`
*   `clientes` no es nulo (Diccionario principal por ID).
*   `indiceScoring` no es nulo (ABB<Integer, Cola<Cliente>> secundario por scoring, máx 101 nodos).
*   `indiceNombre` no es nulo (Diccionario secundario por nombre).
*   `scoringIndexConstructed` indica si el índice de scoring fue construido (lazy loading).
*   `proximoId` siempre es mayor que el mayor ID existente en el sistema.
*   Para todo `Cliente c` en `clientes`, `c.id` corresponde a su clave en el diccionario.
*   **Sincronización de índices**: Si `scoringIndexConstructed == true`, todo cliente en `clientes` debe existir también en la `Cola` correspondiente del nodo ABB con su scoring. Todo cliente debe existir en `indiceNombre` (bajo su nombre en lowercase).
*   **Consistencia inversa**: Todo cliente referenciado en `indiceScoring` o `indiceNombre` debe existir en `clientes`.
*   **Unicidad de nodos ABB**: Cada scoring (0-100) tiene a lo sumo un nodo en el ABB; la Cola del nodo contiene todos los clientes con ese scoring.

---

## 4. Mecanismos de Validación

El sistema asegura el cumplimiento de estos invariantes mediante:
1.  **Validación en Constructores**: Rechazo inmediato de estados iniciales inválidos (precondiciones).
2.  **Encapsulamiento**: Todos los atributos críticos son privados (`private`) y solo modificables mediante métodos controlados (`setters` con validación o métodos de negocio).
3.  **Excepciones Runtime**: Uso de `IllegalArgumentException` para argumentos inválidos y `IllegalStateException` para llamadas a métodos en estados incorrectos (ej. pedir historial sin sesión).
