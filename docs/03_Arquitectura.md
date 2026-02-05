# Arquitectura del Sistema

Descripción técnica detallada de patrones, principios y decisiones de diseño.

---

## 📐 Arquitectura en Capas

El proyecto implementa **separación de responsabilidades** con flujo unidireccional:

```
┌─────────────────────────────────────────────────┐
│              CAPA VISTA                         │
│  (Menu, MenuSolicitudes, MenuHistorial)         │
└────────────────┬────────────────────────────────┘
                 │ usa
                 ▼
┌─────────────────────────────────────────────────┐
│              CAPA LOGICA                        │
│           (Servicio + Modelo)                   │
│ (GestorClientes, Cliente, Sesion)               │
└────────────────┬────────────────────────────────┘
                 │ gestiona
                 ▼
┌─────────────────────────────────────────────────┐
│              CAPA TDA                           │
│    (Diccionario, Pila, Cola)                    │
└─────────────────────────────────────────────────┘
```

**Regla de oro**: Las capas superiores usan las inferiores, **nunca al revés**.

---

## 🎯 Patrones de Diseño

### 1. Singleton (Creacional)

**Clase**: `Sesion`

**Implementación**:
```java
public class Sesion {
    private static Sesion instancia;
    
    private Sesion() { /* constructor privado */ }
    
    public static Sesion getInstancia() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }
}
```

**Justificación**: 
- Garantiza única sesión activa en el sistema
- Simplifica API eliminando paso de `Sesion` como parámetro

**Beneficio**:
```java
// Antes (sin Singleton)
gestor.seguir(id1, id2, sesion);

// Ahora (con Singleton)
gestor.seguir(id1, id2);  // Sesion.getInstancia() internamente
```

---

### 2. Facade (Estructural)

**Clase**: `GestorClientes`

**Responsabilidad**: Interfaz simplificada para operaciones complejas con múltiples TDAs.

**Ejemplo**:
```java
public boolean seguir(int idSolicitante, int idObjetivo) {
    // 1. Busca en Diccionario
    Cliente solicitante = clientes.obtener(idSolicitante);
    Cliente objetivo = clientes.obtener(idObjetivo);
    
    // 2. Modifica Cliente
    if (solicitante.seguir(idObjetivo)) {
        // 3. Registra en Historial (Pila)
        Accion accion = new Accion(TipoAccion.SEGUIR, ...);
        Sesion.getInstancia().getHistorial().registrar(accion);
        return true;
    }
    return false;
}
```

**Beneficio**: La vista no necesita conocer la complejidad interna.

---

### 3. Value Object (Estructural)

**Clases**: `SolicitudSeguimiento`, `ResultadoValidacion`

**Características**:
- Inmutables (todos los atributos `final`)
- Sin identidad propia (igualdad por valor)
- No tienen lógica de negocio compleja

**Ejemplo**:
```java
public class SolicitudSeguimiento {
    private final String solicitante;
    private final String objetivo;
    
    public SolicitudSeguimiento(String solicitante, String objetivo) {
        this.solicitante = solicitante;
        this.objetivo = objetivo;
    }
    
    // Solo getters, sin setters
}
```

**Beneficio**: Seguridad ante modificaciones accidentales.

---

### 4. Static Utility (Estructural)

**Clases**: `Validador`, `Terminal`

**Características**:
- Constructor privado
- Todos los métodos `static`
- Sin estado interno

**Ejemplo**:
```java
public class Validador {
    private Validador() {}  // No instanciable
    
    public static ResultadoValidacion validarNombre(String nombre) {
        if (nombre == null) {
            return ResultadoValidacion.error("El nombre no puede ser nulo");
        }
        if (nombre.trim().isEmpty()) {
            return ResultadoValidacion.error("El nombre no puede estar vacío");
        }
        return ResultadoValidacion.ok();
    }
}
```

**Beneficio**: Centraliza lógica reutilizable sin necesidad de instancias.

---

## 🔐 Principios SOLID

### Single Responsibility Principle (SRP)
- `GestorClientes`: Solo gestiona clientes
- `HistorialAcciones`: Solo gestiona historial
- `Validador`: Solo valida datos
- Cada menú tiene una responsabilidad específica

### Open/Closed Principle (OCP)
- `TipoAccion` (enum): Cerrado para modificación, abierto para extensión
- TDAs genéricos: Reutilizables sin modificar código

### Dependency Inversion Principle (DIP)
- Vista depende de abstracciones (interfaces conceptuales)
- `GestorClientes` no conoce detalles de UI
- TDAs implementan interfaces (`IPila`, `ICola`, `IDiccionario`)

---

## 🎓 Principios GRASP

### Information Expert
- `Cliente` conoce sus propios seguidos → método `seguir()`
- `Diccionario` conoce sus claves → método `contiene()`
- `Sesion` conoce su estado de autenticación

### Creator
- `GestorClientes` crea instancias de `Cliente` y `Accion`
- `Cliente` crea instancias de `Cola<SolicitudSeguimiento>`

### Low Coupling
- Vista no conoce TDAs directamente
- Modelo no conoce Vista
- Capas desacopladas mediante interfaces claras

### High Cohesion
- Cada clase tiene responsabilidades relacionadas
- Métodos de una clase trabajan sobre los mismos datos

---

## ⚙️ Características Técnicas

### 1. Carga Inicial y Persistencia Simple

**Estrategia**: Carga total en RAM al inicio, guardado total al salir.

**Implementación**:
```java
// GestorClientes
public void cargarDesdeArchivo() {
    // Lee JSON completo -> Diccionario (RAM)
}

public void guardarCambios() {
    // Diccionario (RAM) -> Sobrescribe JSON
}
```

**Beneficio**: 
- Elimina complejidad de DAOs y DTOs.
- Todas las operaciones en tiempo de ejecución son 100% en memoria (O(1)).
- Persistencia garantizada al cerrar la sesión.

**Complejidad**:
- Carga/Guardado: O(N) (solo al inicio y fin)
- Operaciones (Seguir, Buscar): O(1) (siempre)

---

### 2. Undo/Redo (Command Pattern)

**Implementación**: Dos pilas en `Sesion`

```java
public class Sesion {
    private HistorialAcciones historial;  // Pila de acciones realizadas
    private Pila<Accion> pilaRehacer;     // Pila de acciones deshechas
}
```

**Flujo**:
1. **Acción ejecutada** → se apila en `historial`
2. **Undo** → se extrae de `historial`, se revierte, se apila en `pilaRehacer`
3. **Redo** → se extrae de `pilaRehacer`, se re-ejecuta, se apila en `historial`

**Complejidad**: O(1) para todas las operaciones.

**Ejemplo**:
```java
// Ejecutar acción
gestor.seguir(1001, 5000);  // Se registra automáticamente

// Deshacer
Accion accion = gestor.deshacer();  // Revierte y mueve a pilaRehacer

// Rehacer
gestor.rehacer();  // Re-ejecuta y mueve a historial
```

---

### 3. Validación Centralizada (DRY)

**Clase**: `Validador`

**Beneficio**: Evita duplicación de lógica de validación.

**Ejemplo**:
```java
// En Cliente.java
ResultadoValidacion validacion = Validador.validarNombre(nombre);
if (!validacion.esValido()) {
    throw new IllegalArgumentException(validacion.getMensajeError());
}
```

**Validaciones disponibles**:
- `validarNombre(String)`: No nulo, no vacío
- `validarScoring(int)`: Rango [0, 100]
- `validarNombresDistintos(String, String)`: Case-insensitive

---

## 🔧 Decisiones de Diseño

### ¿Por qué Singleton para Sesion?

**Alternativa considerada**: Inyección de dependencias (pasar `Sesion` como parámetro)

**Decisión**: Singleton

**Razones**:
- Garantiza única sesión activa (requisito del dominio)
- Simplifica API (menos parámetros)
- Apropiado para un TP académico que demuestra patrones

---

### ¿Por qué TDAs propios en lugar de java.util?

**Decisión**: Implementación propia de `Diccionario`, `Pila`, `Cola`

**Razones**:
- Requisito académico (demostrar conocimiento de estructuras)
- Control total sobre complejidad algorítmica
- Aprendizaje profundo de implementación

---

### ¿Por qué no usar base de datos?

**Decisión**: JSON + In-Memory

**Razones**:
- Simplicidad para entorno académico.
- Velocidad extrema (operaciones en RAM).
- Facilidad de debug (archivo legible).

---

### ¿Por qué Hash Table con capacidad fija 64?

**Decisión**: No implementar rehashing

**Razones**:
- Suficiente para carga típica (< 100 clientes en memoria simultánea)
- Evita complejidad de rehashing
- Mantiene O(1) amortizado con buen factor de carga

**Cálculo**:
```
Factor de carga = n / m
Donde: n = elementos, m = capacidad

Con 50 clientes en memoria:
Factor = 50 / 64 = 0.78  ✅ Aceptable (< 0.75 ideal)
```

---

## 📊 Invariantes de Representación

### Cliente
- `id > 0`
- `nombre != null && !nombre.trim().isEmpty()`
- `0 <= scoring <= 100`
- `siguiendo != null`
- Ningún cliente se sigue a sí mismo

### Sesion
- Si autenticado → `usuarioActual != null`
- `historial != null`
- `pilaRehacer != null`

### Diccionario
- `cantidad >= 0`
- `(primero == null) <==> (cantidad == 0)`
- No existen claves duplicadas

### Pila
- `cantidad >= 0`
- `(tope == null) <==> (cantidad == 0)`

### Cola
- `cantidad >= 0`
- `(frente == null) <==> (fin == null) <==> (cantidad == 0)`

---

## 🎯 Flujo de Datos

```
Usuario → Menu → GestorClientes → Diccionario<ID, Cliente>
                      ↓
                  Sesion.getInstancia()
                      ↓
              HistorialAcciones (Pila)
```

---

## 📈 Métricas del Proyecto

| Métrica | Valor |
|---------|-------|
| Capas arquitectónicas | 4 (Vista, Servicio, Modelo, TDA) |
| Clases totales | ~20 |
| TDAs implementados | 3 (Diccionario, Pila, Cola) |
| Patrones de diseño | 4 (Singleton, Value Object, Facade, Static Utility) |
| Complejidad promedio | O(1) para operaciones críticas |
| Dataset soportado | 1M+ clientes |

---

## ✅ Conclusión

El sistema implementa una arquitectura limpia y mantenible mediante:

1. **Separación en capas** con responsabilidades claras
2. **Patrones de diseño** aplicados con criterio (sin sobre-ingeniería)
3. **Principios SOLID/GRASP** para código extensible
4. **TDAs eficientes** con complejidad O(1) en operaciones críticas
5. **Optimizaciones** (Lazy Loading) para manejar grandes volúmenes de datos

La arquitectura permite agregar nuevas funcionalidades sin modificar código existente, cumpliendo con los principios de diseño orientado a objetos.
