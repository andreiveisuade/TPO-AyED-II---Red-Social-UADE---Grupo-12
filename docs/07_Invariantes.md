# Invariantes de Representación

Este documento define formalmente las condiciones lógicas que garantizan la integridad y consistencia del estado interno de las clases del sistema.

---

## 1. Tipos de Datos Abstractos (TDAs)

### 1.1. `Pila<T>`
*   `cantidad ≥ 0`.
*   `tope != null` ⇔ `cantidad > 0`.
*   La longitud de la cadena de nodos enlazados desde `tope` debe ser exactamente igual a `cantidad`.

### 1.2. `Cola<T>`
*   `cantidad ≥ 0`.
*   `frente != null` ⇔ `fin != null`.
*   `frente == null` ⇔ `cantidad == 0`.
*   Si `cantidad == 1`, entonces `frente == fin`.
*   La longitud de la cadena de nodos desde `frente` hasta `fin` debe ser exactamente igual a `cantidad`.

### 1.3. `Diccionario<K,V>`
*   `cantidad ≥ 0`.
*   `tabla != null` y `tabla.length == CAPACIDAD` (64).
*   Para cada bucket `tabla[i]`, no existen dos nodos con la misma clave `K`.
*   La suma de la longitud de todas las listas enlazadas en los buckets debe ser igual a `cantidad`.

---

## 2. Entidades de Dominio

### 2.1. `Cliente`
*   `id` es un entero positivo único en el sistema.
*   `nombre` no es nulo, no está vacío y no contiene solo espacios.
*   `0 ≤ scoring ≤ 100`.
*   `siguiendo` no es nulo.
*   `solicitudesPendientes` no es nulo.
*   Propiedad reflexiva: Un cliente nunca puede contener su propio ID en `siguiendo`.

### 2.2. `Sesion` (Singleton)
*   Si `estaAutenticado == true`, entonces `usuarioActual != null`.
*   Si `estaAutenticado == false`, entonces `usuarioActual == null`.
*   `historial` y `pilaRehacer` nunca son nulos (se inicializan al crear la sesión).

### 2.3. `Accion` (Value Object)
*   Inmutabilidad: Todos los campos (`tipo`, `datos`, `timestamp`) son finales y no modificables tras la construcción.
*   `timestamp` ≤ `LocalDateTime.now()` (causalidad temporal).

---

## 3. Mecanismos de Validación

El sistema asegura el cumplimiento de estos invariantes mediante:
1.  **Validación en Constructores**: Rechazo inmediato de estados iniciales inválidos (precondiciones).
2.  **Encapsulamiento**: Todos los atributos críticos son privados y solo modificables mediante métodos controlados.
3.  **Excepciones en Tiempo de Ejecución**: Uso de `IllegalArgumentException` e `IllegalStateException` para abortar operaciones que violen la integridad.
