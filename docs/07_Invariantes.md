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

---

## 2. Entidades de Dominio

### 2.1. `Cliente`
*   `id` es un entero positivo (`id > 0`) único en el sistema.
*   `nombre` no es nulo, no está vacío y no contiene solo espacios.
*   `0 ≤ scoring ≤ 100`.
*   `siguiendo` no es nulo (Diccionario inicializado).
*   `solicitudesPendientes` no es nulo (Cola inicializada).
*   **Reflexividad**: `siguiendo` NO contiene la clave `id` (un cliente no se sigue a sí mismo).
*   **Consistencia**: `siguiendo.cantidad` debe ser igual a `getCantidadSiguiendo()`.

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
*   `solicitante` (ID) no es nulo ni vacío.
*   `objetivo` (ID) no es nulo ni vacío.
*   `fecha` no es nulo.

---

## 3. Gestores del Sistema

### 3.1. `GestorClientes`
*   `clientes` no es nulo.
*   `proximoId` siempre es mayor que el mayor ID existente en el sistema.
*   Para todo `Cliente c` en `clientes`, `c.id` corresponde a su clave en el diccionario.

---

## 4. Mecanismos de Validación

El sistema asegura el cumplimiento de estos invariantes mediante:
1.  **Validación en Constructores**: Rechazo inmediato de estados iniciales inválidos (precondiciones).
2.  **Encapsulamiento**: Todos los atributos críticos son privados (`private`) y solo modificables mediante métodos controlados (`setters` con validación o métodos de negocio).
3.  **Excepciones Runtime**: Uso de `IllegalArgumentException` para argumentos inválidos y `IllegalStateException` para llamadas a métodos en estados incorrectos (ej. pedir historial sin sesión).
