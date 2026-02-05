# Análisis Completo de Complejidad Big O - Iteración 1

## TDAs (Tipos de Datos Abstractos)

### 1. Pila (Stack)
| Método | Complejidad | ¿Óptimo? | Notas |
|--------|-------------|----------|-------|
| `apilar()` | O(1) | ✅ | Inserción al inicio de lista enlazada |
| `desapilar()` | O(1) | ✅ | Eliminación del inicio |
| `verTope()` | O(1) | ✅ | Acceso directo al tope |
| `estaVacia()` | O(1) | ✅ | Comparación simple |
| `getCantidad()` | O(1) | ✅ | Retorna atributo |
| `toArray()` | O(n) | ✅ | Recorre todos los nodos una vez |

**Conclusión:** Todas las operaciones tienen complejidad óptima.

---

### 2. Cola (Queue)
| Método | Complejidad | ¿Óptimo? | Notas |
|--------|-------------|----------|-------|
| `encolar()` | O(1) | ✅ | Inserción al final con puntero `fin` |
| `desencolar()` | O(1) | ✅ | Eliminación del frente |
| `verFrente()` | O(1) | ✅ | Acceso directo al frente |
| `estaVacia()` | O(1) | ✅ | Comparación simple |
| `getCantidad()` | O(1) | ✅ | Retorna atributo |

**Conclusión:** Todas las operaciones tienen complejidad óptima.

---

### 3. Diccionario (Hash Table)
| Método | Complejidad | ¿Óptimo? | Notas |
|--------|-------------|----------|-------|
| `insertar()` | O(1) amortizado | ✅ | Hash + inserción al inicio del bucket |
| `obtener()` | O(1) amortizado | ✅ | Hash + búsqueda en bucket |
| `contiene()` | O(1) amortizado | ✅ | Usa `obtener()` |
| `eliminar()` | O(1) amortizado | ✅ | Hash + eliminación en bucket |
| `getCantidad()` | O(1) | ✅ | Retorna atributo |
| `estaVacio()` | O(1) | ✅ | Comparación simple |
| `obtenerClaves()` | O(n + m) | ✅ | n=elementos, m=capacidad tabla |
| `obtenerValores()` | O(n + m) | ✅ | n=elementos, m=capacidad tabla |

**Conclusión:** Todas las operaciones tienen complejidad óptima. La capacidad fija de 64 garantiza O(1) sin rehashing.

---

### 4. Conjunto (Set)
| Método | Complejidad | ¿Óptimo? | Notas |
|--------|-------------|----------|-------|
| `agregar()` | O(1) | ✅ | Delega a Diccionario.insertar() |
| `contiene()` | O(1) | ✅ | Delega a Diccionario.contiene() |
| `eliminar()` | O(1) | ✅ | Delega a Diccionario.eliminar() |
| `getCantidad()` | O(1) | ✅ | Delega a Diccionario.getCantidad() |
| `estaVacio()` | O(1) | ✅ | Delega a Diccionario.estaVacio() |
| `obtenerElementos()` | O(n + m) | ✅ | Delega a Diccionario.obtenerClaves() |

**Conclusión:** Todas las operaciones tienen complejidad óptima por delegación.

---

## Modelo (Entidades)

### 5. Cliente
| Método | Complejidad | ¿Óptimo? | Notas |
|--------|-------------|----------|-------|
| `seguir()` | O(1) | ✅ | Diccionario.insertar() + Diccionario.contiene() |
| `dejarDeSeguir()` | O(1) | ✅ | Diccionario.eliminar() |
| `sigueA()` | O(1) | ✅ | Diccionario.contiene() |
| `getSiguiendo()` | O(n) | ✅ | n = cantidad de seguidos (convierte claves a array) |
| Getters/Setters | O(1) | ✅ | Acceso directo a atributos |

**Conclusión:** Operaciones de seguimiento son O(1) gracias al uso de `Diccionario<Integer, Boolean>` en lugar de array.

---

### 6. Accion
| Método | Complejidad | ¿Óptimo? | Notas |
|--------|-------------|----------|-------|
| Constructor | O(k) | ✅ | k = cantidad de datos (varargs) |
| Getters | O(1) | ✅ | Acceso directo |
| `getDetalle()` | O(k) | ✅ | k = longitud del array datos |
| `toString()` | O(k) | ✅ | Construye string con datos |

**Conclusión:** Óptimo para una estructura inmutable.

---

### 7. SolicitudSeguimiento
| Método | Complejidad | ¿Óptimo? | Notas |
|--------|-------------|----------|-------|
| Constructor | O(1) | ✅ | Validaciones simples |
| Getters | O(1) | ✅ | Acceso directo |
| `getClave()` | O(1) | ✅ | Concatenación de strings |

**Conclusión:** Todas las operaciones tienen complejidad óptima.

---

## Servicios (Lógica de Negocio)

### 8. GestorClientes
| Método | Complejidad Actual | ¿Óptimo? | Notas |
|--------|-------------------|----------|-------|
| `agregarCliente()` | O(1) | ✅ | Validaciones + inserción en Diccionario |
| `buscarPorNombre()` | O(1) | ✅ | Búsqueda en Diccionario |
| `existeCliente()` | O(1) | ✅ | Búsqueda en Diccionario |
| `buscarPorScoring()` | O(n) | ✅ | Inevitable: debe recorrer todos los clientes |
| `eliminarCliente()` | O(n) | ✅ | Inevitable: limpieza en cascada |
| `seguir()` | O(1) | ✅ | Búsqueda + operación en Cliente |
| `dejarDeSeguir()` | O(1) | ✅ | Búsqueda + operación en Cliente |
| `deshacer()` | O(1) o O(n) | ✅ | O(1) para AGREGAR/SEGUIR, O(n) para ELIMINAR (cascada) |
| `rehacer()` | O(1) o O(n) | ✅ | Mismo que deshacer |
| `obtenerHistorialCompleto()` | O(n) | ✅ | Usa toArray() optimizado |
| `normalizar()` | O(k) | ✅ | k = longitud del nombre |

**Mejoras implementadas:**
- ✅ Normalización centralizada evita duplicación
- ✅ `buscarPorScoring()` con dos pasadas evita desperdicio de memoria
- ✅ Redo implementado con pila adicional

**Conclusión:** Todas las operaciones tienen complejidad óptima según las restricciones del problema.

---

### 9. HistorialAcciones
| Método | Complejidad Actual | ¿Óptimo? | Notas |
|--------|-------------------|----------|-------|
| `registrar()` | O(1) | ✅ | Apilar en pila |
| `extraerUltima()` | O(1) | ✅ | Desapilar de pila |
| `verUltima()` | O(1) | ✅ | Ver tope de pila |
| `estaVacio()` | O(1) | ✅ | Delega a pila |
| `getCantidad()` | O(1) | ✅ | Delega a pila |
| `obtenerTodas()` | O(n) | ✅ | **MEJORADO:** Usa toArray() sin pila temporal |

**Mejora implementada:**
- ✅ Antes: O(2n) con doble recorrido (vaciar + restaurar)
- ✅ Ahora: O(n) con un solo recorrido usando toArray()

**Conclusión:** Todas las operaciones tienen complejidad óptima.

---

### 10. Gestión de Solicitudes (en Cliente)
| Método | Complejidad | ¿Óptimo? | Notas |
|--------|-------------|----------|-------|
| `recibirSolicitud()` | O(1) | ✅ | Encolar en Cola (LinkedList) |
| `procesarSiguienteSolicitud()` | O(1) | ✅ | Desencolar |
| `verSiguienteSolicitud()` | O(1) | ✅ | Ver frente |
| `tieneSolicitudesPendientes()` | O(1) | ✅ | Verifica si la cola está vacía |
| `getCantidadSolicitudesPendientes()` | O(1) | ✅ | Retorna contador de la cola |

**Nota:** 
- La implementación actual utiliza una `Cola<SolicitudSeguimiento>` dentro de la clase `Cliente`.
- No se realiza verificación de duplicados al encolar (O(1)), se confía en la validación al procesar o en la UI.
- Esto difiere de planificaciones anteriores que sugerían un `Conjunto` auxiliar.

**Conclusión:** Todas las operaciones son O(1) puro.

---

## Persistencia

### 11. Carga de Datos (Gson)
| Método | Complejidad | ¿Óptimo? | Notas |
|--------|-------------|----------|-------|
| `cargarDesdeArchivo()` | O(n + r) | ✅ | n=clientes, r=relaciones. Lectura secuencial con Gson. |

**Análisis:**
- Se utiliza la librería **Gson** para parsear el archivo JSON de una sola pasada.
- La complejidad es lineal respecto al tamaño del archivo.
- Se ejecuta una única vez al iniciar la aplicación.
- Se eliminó el "Lazy Loading" (carga bajo demanda) para simplificar la arquitectura y aprovechar la memoria disponible.

**Conclusión:** Complejidad óptima O(n) para la carga inicial.

---

## Vista (Interfaz de Usuario)

### 12. MenuUtils
| Método | Complejidad | ¿Óptimo? | Notas |
|--------|-------------|----------|-------|
| `mostrarCabecera()` | O(k) | ✅ | k = longitud de la ruta (breadcrumb) |
| `centrar()` | O(n) | ✅ | n = ancho (constante pequeña) |
| `leerEntero()` | O(k) | ✅ | k = longitud del input |
| `capitalizarNombre()` | O(k) | ✅ | k = longitud del nombre |

**Conclusión:** Todas las operaciones tienen complejidad óptima para UI.

---

## Resumen General

### Operaciones Críticas (Iteración 1)

| Operación | Complejidad | Estado |
|-----------|-------------|--------|
| Agregar cliente | O(1) | ✅ Óptimo |
| Buscar por nombre | O(1) | ✅ Óptimo |
| Buscar por scoring | O(n) | ✅ Inevitable |
| Eliminar cliente | O(n) | ✅ Inevitable (cascada) |
| Agregar solicitud | O(1) | ✅ Óptimo |
| Procesar solicitud | O(1) | ✅ Óptimo |
| Undo/Redo | O(1) o O(n) | ✅ Óptimo según tipo |
| Ver historial | O(n) | ✅ Mejorado de O(2n) |
| Cargar JSON | O(n) | ✅ Óptimo (Gson) |

### Mejoras Implementadas

1. ✅ **Pila.toArray():** Reduce obtenerHistorialCompleto() de O(2n) a O(n)
2. ✅ **Normalización centralizada:** Evita duplicación de código
3. ✅ **buscarPorScoring() con dos pasadas:** Evita desperdicio de memoria
4. ✅ **Conjunto.obtenerElementos():** Permite iteración futura
5. ✅ **Redo completo:** Implementado con pila adicional
6. ✅ **equalsIgnoreCase() en Cliente:** Consistencia case-insensitive
7. ✅ **Eliminación de Lazy Loading:** Simplificación arquitectónica usando Gson

### Única Oportunidad de Optimización Restante

No quedan optimizaciones críticas pendientes para la Iteración 1. El sistema es eficiente y simple.

---

## Conclusión Final

✅ **Todas las operaciones críticas tienen complejidad Big O óptima** según las restricciones del problema.

✅ **Las 7 mejoras propuestas fueron implementadas exitosamente.**

✅ **No hay cuellos de botella en operaciones frecuentes del usuario.**

⚠️ **JsonLoader podría optimizarse, pero no es prioritario para Iteración 1.**
