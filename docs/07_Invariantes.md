# Invariantes de Representación - TPO AyED II

## Definición

Un **invariante de representación** es una condición o restricción que debe mantenerse válida en todo momento para una estructura de datos o clase. Estas condiciones garantizan la consistencia e integridad de los datos.

## Invariantes Implementados

### TDAs (Tipos Abstractos de Datos)

#### **Pila<T>**
- `cantidad >= 0` siempre
- `(tope == null) <==> (cantidad == 0)`
- Si `cantidad > 0`, entonces `tope != null`
- La cantidad de nodos enlazados desde tope es exactamente igual a `cantidad`

#### **Cola<T>**
- `cantidad >= 0` siempre
- `(frente == null) <==> (fin == null) <==> (cantidad == 0)`
- Si `cantidad == 1`, entonces `frente == fin`
- Si `cantidad > 1`, entonces `frente != fin`
- La cantidad de nodos enlazados desde frente hasta fin es exactamente igual a `cantidad`

#### **Diccionario<K, V>**
- `cantidad >= 0` siempre
- `(primero == null) <==> (cantidad == 0)`
- Si `cantidad > 0`, entonces `primero != null`
- La cantidad de nodos enlazados desde `primero` es exactamente igual a `cantidad`
- No existen claves duplicadas en la lista enlazada

#### **Conjunto**
- `elementos != null` siempre
- No existen elementos duplicados en el conjunto
- Todos los valores del diccionario interno son `true`

---

### Modelo

#### **Cliente**
- `nombre != null && !nombre.trim().isEmpty()`
- `0 <= scoring <= 100`
- `siguiendo != null`
- No existen duplicados en `siguiendo` (garantizado por Diccionario)
- Ningún cliente se sigue a sí mismo
- `solicitudesPendientes != null`

#### **Accion**
- `tipo != null`
- `datos != null`
- `timestamp != null`
- `timestamp <= LocalDateTime.now()` (la acción no puede ser futura)

#### **SolicitudSeguimiento**
- `solicitante != null`
- `objetivo != null`
- `solicitante != objetivo` (un cliente no puede solicitarse seguir a sí mismo)

---

### Servicios

#### **GestorClientes**
- `clientes != null`
- `historial != null`
- No existen clientes con nombre `null` o vacío en el diccionario
- Todos los clientes tienen `scoring` entre `0` y `100`
- Cada acción en historial corresponde a una operación válida realizada

#### **HistorialAcciones**
- `historial != null`
- Todas las acciones en historial son `!= null`
- Las acciones están ordenadas cronológicamente (más reciente en tope)

#### **ColaSolicitudes**
- `cola != null`
- `clavesExistentes != null`
- `cola.getCantidad() == clavesExistentes.getCantidad()`
- Para cada solicitud en cola existe su clave en `clavesExistentes`
- No existen solicitudes duplicadas en la cola
- Una clave tiene formato `"solicitante:objetivo"`

---

## Importancia de los Invariantes

Los invariantes de representación son fundamentales porque:

1. **Garantizan la consistencia**: Aseguran que los objetos siempre estén en un estado válido
2. **Facilitan el debugging**: Permiten detectar errores más rápidamente
3. **Documentan restricciones**: Hacen explícitas las condiciones que deben cumplirse
4. **Mejoran la encapsulación**: Definen claramente el contrato de cada clase

## Validación

Todas las clases verifican que se cumplan sus invariantes:
- En los **constructores** se validan las precondiciones iniciales
- En los **métodos modificadores** se mantienen los invariantes después de cada operación
- Se lanzan excepciones (`IllegalArgumentException`) cuando se violan restricciones
