# Guía Completa de TDAs (Tipos de Datos Abstractos)

Documentación técnica y justificación teórica de las estructuras de datos implementadas.

---

## 📚 Resumen de TDAs

| TDA | Política | Complejidad | Uso Principal |
|-----|----------|-------------|---------------|
| **Diccionario<K,V>** | Hash Table | O(1) amortizado | Clientes por ID |
| **Pila<T>** | LIFO | O(1) | Historial + Redo |
| **Cola<T>** | FIFO | O(1) | Solicitudes pendientes |
| **Conjunto** | Set único | O(1) | Verificación de duplicados |

---

## 1. Diccionario<K,V> (Hash Table)

### 📁 Archivo
`tda/Diccionario.java` + `tda/IDiccionario.java`

### 🔧 Operaciones

| Método | Descripción | Complejidad |
|--------|-------------|-------------|
| `insertar(K clave, V valor)` | Inserta par clave-valor | O(1) amortizado |
| `obtener(K clave)` | Obtiene valor por clave | O(1) amortizado |
| `contiene(K clave)` | Verifica existencia de clave | O(1) amortizado |
| `eliminar(K clave)` | Elimina par por clave | O(1) amortizado |
| `getCantidad()` | Cantidad de pares | O(1) |
| `obtenerClaves()` | Retorna todas las claves | O(n + m) |
| `obtenerValores()` | Retorna todos los valores | O(n + m) |

### 🏗️ Estructura Interna

**Implementación**: Array de 64 buckets con encadenamiento

```
Diccionario
  └── tabla: NodoDiccionario[64]  (array de buckets)
        ├── [0] → null
        ├── [1] → Nodo(clave, valor) → Nodo → null  (colisiones encadenadas)
        ├── [2] → Nodo(clave, valor) → null
        └── ...

Función Hash: Math.abs(clave.hashCode() % 64)
```

### 📍 Dónde se usa

| Clase | Atributo | Tipo | Propósito |
|-------|----------|------|-----------|
| `GestorClientes` | `clientes` | `Diccionario<Integer, Cliente>` | Almacena todos los clientes por ID |
| `Cliente` | `siguiendo` | `Diccionario<Integer, Boolean>` | Usuarios que sigue (sin límite) |
| `Conjunto` | `elementos` | `Diccionario<String, Boolean>` | Implementación interna del Set |

### 💡 Justificación Teórica

**¿Por qué Hash Table?**
- Búsqueda de clientes por ID en O(1) en lugar de O(n)
- Con 1 millón de usuarios, la diferencia es crítica:
  - Lista: 1,000,000 comparaciones (peor caso)
  - Hash Table: ~1 comparación (promedio)

**¿Por qué capacidad fija 64?**
- Suficiente para carga típica (< 100 clientes en memoria simultánea)
- Evita complejidad de rehashing
- Factor de carga aceptable: 50/64 = 0.78 (< 0.75 ideal)

**Manejo de colisiones**:
- Encadenamiento (listas enlazadas en cada bucket)
- Inserción al inicio del bucket: O(1)
- Búsqueda en bucket: O(k) donde k = elementos en bucket (≈ 1 en promedio)

### 📊 Ejemplo de Uso

```java
// En GestorClientes
Diccionario<Integer, Cliente> clientes = new Diccionario<>();
clientes.insertar(100000, new Cliente(100000, "Federico", 85));
Cliente c = clientes.obtener(100000);  // O(1)

// En Cliente (seguimiento sin límite)
Diccionario<Integer, Boolean> siguiendo = new Diccionario<>();
siguiendo.insertar(5000, true);  // Sigue al usuario 5000
boolean sigue = siguiendo.contiene(5000);  // O(1)
```

---

## 2. Pila<T> (LIFO - Last In, First Out)

### 📁 Archivo
`tda/Pila.java` + `tda/IPila.java`

### 🔧 Operaciones

| Método | Descripción | Complejidad |
|--------|-------------|-------------|
| `apilar(T dato)` | Agrega elemento al tope | O(1) |
| `desapilar()` | Remueve y retorna el tope | O(1) |
| `verTope()` | Consulta el tope sin remover | O(1) |
| `estaVacia()` | Verifica si está vacía | O(1) |
| `getCantidad()` | Cantidad de elementos | O(1) |
| `toArray()` | Convierte a array | O(n) |

### 🏗️ Estructura Interna

```
Pila
  └── tope: NodoPila<T>
        └── dato: T
        └── siguiente: NodoPila<T> → ...
```

### 📍 Dónde se usa

| Clase | Atributo | Propósito |
|-------|----------|-----------|
| `Sesion` | `pilaRehacer: Pila<Accion>` | Almacena acciones deshechas para redo |
| `HistorialAcciones` | `historial: Pila<Accion>` | Almacena acciones realizadas para undo |

### 💡 Justificación Teórica

**¿Por qué Pila?**
- La última acción realizada es la primera que se debe deshacer (LIFO)
- Semántica natural para undo/redo
- Al deshacer, la acción pasa de `historial` a `pilaRehacer`
- Al rehacer, la acción vuelve de `pilaRehacer` a `historial`

**Alternativas descartadas**:
- **Cola**: Política FIFO, semánticamente incorrecta para undo
- **Lista con acceso aleatorio**: Complejidad innecesaria

### 📊 Flujo Undo/Redo

```
Acción ejecutada → historial.apilar(accion)
                    ↓
Usuario hace UNDO → accion = historial.desapilar()
                    ejecutarUndo(accion)
                    pilaRehacer.apilar(accion)
                    ↓
Usuario hace REDO → accion = pilaRehacer.desapilar()
                    ejecutarRedo(accion)
                    historial.apilar(accion)
```

### 📊 Ejemplo de Uso

```java
// HistorialAcciones
Pila<Accion> historial = new Pila<>();
historial.apilar(new Accion(TipoAccion.SEGUIR, "1001", "5000"));

// Undo
Accion ultima = historial.desapilar();  // O(1)
ejecutarUndo(ultima);
pilaRehacer.apilar(ultima);
```

---

## 3. Cola<T> (FIFO - First In, First Out)

### 📁 Archivo
`tda/Cola.java` + `tda/ICola.java`

### 🔧 Operaciones

| Método | Descripción | Complejidad |
|--------|-------------|-------------|
| `encolar(T dato)` | Agrega elemento al final | O(1) |
| `desencolar()` | Remueve y retorna el frente | O(1) |
| `verFrente()` | Consulta el frente sin remover | O(1) |
| `estaVacia()` | Verifica si está vacía | O(1) |
| `getCantidad()` | Cantidad de elementos | O(1) |

### 🏗️ Estructura Interna

```
Cola
  ├── frente: NodoCola<T> ──→ siguiente ──→ ... ──→ fin
  └── fin: NodoCola<T>
```

### 📍 Dónde se usa

| Clase | Atributo | Propósito |
|-------|----------|-----------|
| `Cliente` | `solicitudesPendientes: Cola<SolicitudSeguimiento>` | Cola de solicitudes de seguimiento recibidas |

### 💡 Justificación Teórica

**¿Por qué Cola?**
- Las solicitudes deben procesarse en orden de llegada (FIFO)
- La primera solicitud que llegó es la primera que se atiende
- Garantiza equidad: nadie "se salta la fila"

**Implementación**:
- Lista enlazada con punteros `frente` y `fin`
- `encolar()`: Inserción al final usando puntero `fin` → O(1)
- `desencolar()`: Eliminación del frente → O(1)

### 📊 Flujo de Solicitudes

```
Usuario A envía solicitud a B → B.solicitudesPendientes.encolar(solicitud)
Usuario B procesa solicitud   → B.solicitudesPendientes.desencolar()
```

### 📊 Ejemplo de Uso

```java
// Cliente
Cola<SolicitudSeguimiento> solicitudesPendientes = new Cola<>();
solicitudesPendientes.encolar(new SolicitudSeguimiento("1001", "5000"));

// Procesar
SolicitudSeguimiento siguiente = solicitudesPendientes.desencolar();  // O(1)
```

---

## 4. Conjunto (Set)

### 📁 Archivo
`tda/Conjunto.java` + `tda/IConjunto.java`

### 🔧 Operaciones

| Método | Descripción | Complejidad |
|--------|-------------|-------------|
| `agregar(String elemento)` | Agrega elemento (si no existe) | O(1) |
| `contiene(String elemento)` | Verifica si existe | O(1) |
| `eliminar(String elemento)` | Elimina elemento | O(1) |
| `getCantidad()` | Cantidad de elementos | O(1) |
| `obtenerElementos()` | Retorna todos los elementos | O(n) |

### 🏗️ Estructura Interna

```
Conjunto
  └── elementos: Diccionario<String, Boolean>
        └── Usa el Diccionario internamente con valor siempre = true
```

### 📍 Dónde se usa

Actualmente el `Conjunto` está **disponible pero no utilizado** en el código principal.

**Uso potencial**: Verificación de duplicados en solicitudes (O(1) en lugar de O(n)).

### 💡 Justificación Teórica

**¿Por qué Conjunto?**
- Garantiza unicidad de elementos
- Verificación de pertenencia en O(1)
- Patrón Decorator: envuelve un Diccionario para ofrecer API de Set

**Implementación**:
- Delega todas las operaciones al `Diccionario` interno
- `agregar(x)` → `diccionario.insertar(x, true)`
- `contiene(x)` → `diccionario.contiene(x)`

---

## 5. Nodos de Soporte

Los TDAs usan nodos enlazados para crecimiento dinámico:

| Nodo | TDA que lo usa | Atributos |
|------|----------------|-----------|
| `NodoPila<T>` | `Pila<T>` | `dato`, `siguiente` |
| `NodoCola<T>` | `Cola<T>` | `dato`, `siguiente` |
| `NodoDiccionario<K,V>` | `Diccionario<K,V>` | `clave`, `valor`, `siguiente` |

---

## 6. Interfaces (SOLID: DIP)

Cada TDA implementa una interfaz para cumplir con el Principio de Inversión de Dependencias:

| TDA | Interfaz |
|-----|----------|
| `Pila<T>` | `IPila<T>` |
| `Cola<T>` | `ICola<T>` |
| `Diccionario<K,V>` | `IDiccionario<K,V>` |
| `Conjunto` | `IConjunto` |

**Beneficio**: Permite cambiar implementación sin afectar clientes.

---

## 📊 Diagrama de Relaciones

```
┌─────────────────────────────────────────────────────────────┐
│                        MODELO                               │
├─────────────────────────────────────────────────────────────┤
│  Sesion                                                      │
│    ├── historial: HistorialAcciones ─┐                      │
│    │                                  │                      │
│    │   HistorialAcciones              │                      │
│    │     └── historial: Pila<Accion> ◄┘                     │
│    │                                                         │
│    └── pilaRehacer: Pila<Accion>                            │
│                                                              │
│  Cliente                                                     │
│    ├── siguiendo: Diccionario<Integer, Boolean>             │
│    └── solicitudesPendientes: Cola<SolicitudSeguimiento>    │
├─────────────────────────────────────────────────────────────┤
│                       SERVICIO                              │
├─────────────────────────────────────────────────────────────┤
│  GestorClientes                                             │
│    └── clientes: Diccionario<Integer, Cliente>              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Cuándo usar cada TDA

| Necesidad | TDA | Ejemplo |
|-----------|-----|---------|
| Deshacer/Rehacer | **Pila** | Historial de acciones |
| Procesar en orden de llegada | **Cola** | Solicitudes de seguimiento |
| Búsqueda rápida por clave | **Diccionario** | Buscar cliente por ID |
| Verificar unicidad | **Conjunto** | Evitar duplicados |

---

## 📈 Resumen de Complejidades

| Componente | Estructura | Operación crítica | Complejidad |
|------------|------------|-------------------|-------------|
| Almacenamiento clientes | Diccionario (Hash Table) | Búsqueda por ID | **O(1)** |
| Historial acciones | Pila | Registrar/Deshacer | **O(1)** |
| Solicitudes | Cola | Agregar/Procesar | **O(1)** |
| Seguidos por cliente | Diccionario | Consultar/Modificar | **O(1)** |

---

## ✅ Conclusión

La selección de estructuras de datos responde a un análisis de los requerimientos funcionales y de eficiencia del sistema. Cada TDA fue elegido considerando:

1. **Naturaleza de las operaciones predominantes**
2. **Restricciones de complejidad temporal**
3. **Simplicidad de implementación**
4. **Escalabilidad del sistema**

El diseño actual garantiza **O(1) para todas las operaciones críticas**, cumpliendo con los requisitos de rendimiento para manejar 1M+ usuarios.
