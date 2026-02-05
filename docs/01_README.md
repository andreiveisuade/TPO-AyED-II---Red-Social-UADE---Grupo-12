# TPO Algoritmos y Estructuras de Datos II

**Red Social Simplificada** - Sistema de gestión de clientes con seguimiento, historial y lazy loading.

---

## 🚀 Inicio Rápido

### Compilar y Ejecutar

```bash
# Desde el directorio raíz
cd TPO_AyED_II

# Opción 1: Script automático
./run.sh

# Opción 2: Manual
javac -d out src/**/*.java
java -cp out Main
```

### Flujo de Inicio

```
Main.java → Menu.iniciar() → Pantalla Bienvenida → Login → Menú Principal
```

---

## ✨ Características

- ✅ **1M+ usuarios** con carga inicial O(n)
- ✅ **Seguimiento sin límite** entre usuarios
- ✅ **Undo/Redo** de acciones
- ✅ **Solicitudes FIFO** procesadas en orden
- ✅ **Búsqueda O(1)** por ID usando Hash Table
- ✅ **TDAs propios** (sin `java.util`)

---

## 📐 Arquitectura
 
 ### Capas del Sistema
 
 ```
 ┌─────────────────────────────────────┐
 │  VISTA (Menu, MenuSolicitudes)      │  ← Interacción con usuario
 └──────────────┬──────────────────────┘
                │
 ┌──────────────▼──────────────────────┐
 │  SERVICIO (GestorClientes)          │  ← Lógica de negocio
 └──────────────┬──────────────────────┘
                │
 ┌──────────────▼──────────────────────┐
 │  MODELO (Cliente, Sesion, Accion)   │  ← Entidades del dominio
 └──────────────┬──────────────────────┘
                │
 ┌──────────────▼──────────────────────┐
 │  TDA (Diccionario, Pila, Cola)      │  ← Estructuras de datos
 └─────────────────────────────────────┘
 ```
 
 **Principio**: Flujo unidireccional Vista → Servicio → Modelo → TDA
 
 ---
 
 ## 🔑 Conceptos Clave
 
 ### 1. Singleton (Sesión Única)
 
 ```java
 Sesion sesion = Sesion.getInstancia();  // Siempre la misma instancia
 sesion.iniciarSesion(cliente);
 Cliente usuario = sesion.getUsuarioActual();
 ```
 
 ---
 
 ### 2. Persistencia Simplificada (RAM + JSON)
 
 Para maximizar la eficiencia en tiempo de ejecución (O(1)) y cumplir con los requisitos de persistencia, se utiliza una estrategia de **"Carga Inicial / Guardado Final"**:
 
 1.  **Carga (Inicio)**: Se lee todo el JSON `clientes_1M.json` y se carga en el `Diccionario` en memoria.
 2.  **Ejecución (Runtime)**: Todas las operaciones (seguir, solicitudes) ocurren **exclusivamente en RAM**, garantizando velocidad O(1).
 3.  **Persistencia (Cierre)**: Al salir de la aplicación, se sobrescribe el archivo JSON con el estado actual de la memoria.
 
 ---
 
 ### 3. Undo/Redo (Historial)
 
 **Dos pilas** en `Sesion`:
 - `historial`: Acciones realizadas
 - `pilaRehacer`: Acciones deshechas
 
 **Flujo**:
 ```
 Acción → historial.apilar()
 Undo   → historial.desapilar() → ejecutar reversión → pilaRehacer.apilar()
 Redo   → pilaRehacer.desapilar() → re-ejecutar → historial.apilar()
 ```
 
 **Complejidad**: O(1) para todas las operaciones (en memoria).
 
 ---
 
 ## 📚 Estructuras de Datos (TDAs)
 
 | TDA | Política | Complejidad | Uso Principal |
 |-----|----------|-------------|---------------|
 | `Diccionario<K,V>` | Hash Table | O(1) | Clientes por ID |
 | `Pila<T>` | LIFO | O(1) | Historial + Redo |
 | `Cola<T>` | FIFO | O(1) | Solicitudes pendientes |
 
 ### Diccionario (Hash Table)
 
 **Implementación**: Array de 64 buckets con encadenamiento
 
 ```java
 // Función hash
 private int hash(K clave) {
     if (clave == null) {
         throw new IllegalArgumentException("La clave no puede ser null");
     }
     return Math.abs(clave.hashCode() % 64);
 }
 ```
 
 **Operaciones**:
 - `insertar()`, `obtener()`, `eliminar()`: O(1) amortizado
 - `obtenerClaves()`, `obtenerValores()`: O(n)
 
 **Uso**:
 ```java
 // GestorClientes
 Diccionario<Integer, Cliente> clientes;
 clientes.insertar(1001, new Cliente(1001, "Ana", 85));
 Cliente c = clientes.obtener(1001);  // O(1)
 ```
 
 ---
 
 ### Pila (LIFO)
 
 **Estructura**: Lista enlazada con puntero al tope
 
 **Operaciones**:
 - `apilar()`, `desapilar()`, `verTope()`: O(1)
 - `toArray()`: O(n)
 
 **Uso**:
 ```java
 // HistorialAcciones
 Pila<Accion> historial;
 historial.apilar(new Accion(TipoAccion.SEGUIR, "1001", "5000"));
 Accion ultima = historial.desapilar();
 ```
 
 ---
 
 ### Cola (FIFO)
 
 **Estructura**: Lista enlazada con punteros `frente` y `fin`
 
 **Operaciones**:
 - `encolar()`, `desencolar()`, `verFrente()`: O(1)
 
 **Uso**:
 ```java
 // Cliente
 Cola<SolicitudSeguimiento> solicitudesPendientes;
 solicitudesPendientes.encolar(new SolicitudSeguimiento("1001", "5000"));
 SolicitudSeguimiento siguiente = solicitudesPendientes.desencolar();
 ```
 
 ---
 
 ## 🎯 Patrones de Diseño
 
 ### 1. Singleton
 **Clase**: `Sesion`  
 **Justificación**: Garantiza única sesión activa, simplifica API
 
 ### 2. Facade
 **Clase**: `GestorClientes`  
 **Justificación**: Oculta complejidad de múltiples TDAs y Persistencia
 
 ### 3. Value Object
 **Clases**: `SolicitudSeguimiento`, `ResultadoValidacion`  
 **Justificación**: Inmutabilidad, seguridad ante modificaciones
 
 ### 4. Static Utility
 **Clases**: `Validador`, `Terminal`  
 **Justificación**: Lógica reutilizable sin estado
 
 ---
 
 ## 📊 Complejidad Algorítmica
 
 ### Operaciones Críticas
 
 | Operación | Complejidad (Memoria) | Complejidad (Persistencia) | Estado |
 |-----------|-------------|----------------------------|--------|
 | Agregar cliente | O(1) | - | ✅ Óptimo |
 | Buscar por ID | O(1) | - | ✅ Óptimo |
 | Seguir usuario | O(1) | - | ✅ Óptimo |
 | Procesar solicitud | O(1) | - | ✅ Óptimo |
 | Undo/Redo | O(1) | - | ✅ Óptimo |
 | Buscar por scoring | O(n) | - | ✅ Inevitable |


**Conclusión**: Todas las operaciones frecuentes son O(1).

---

## 🗂️ Estructura del Proyecto

```
TPO_AyED_II/
├── src/
│   ├── Main.java                    ← Punto de entrada
│   ├── modelo/                      ← Entidades
│   │   ├── Sesion.java             ← Singleton
│   │   ├── Cliente.java
│   │   ├── Accion.java
│   │   └── ...
│   ├── servicio/                    ← Lógica de negocio
│   │   ├── GestorClientes.java     ← Facade
│   │   └── HistorialAcciones.java
│   ├── vista/                       ← UI
│   │   ├── Menu.java
│   │   └── MenuSolicitudes.java
│   ├── tda/                         ← Estructuras de datos
│   │   ├── Diccionario.java
│   │   ├── Pila.java
│   │   └── Cola.java
│   └── util/
│       └── Validador.java
├── test/                            ← Tests unitarios
└── docs/                            ← Documentación
```

---

## 🧪 Ejecutar Tests

```bash
./test.sh
```

---

## 📖 Documentación Completa

- **[Arquitectura Detallada](docs/ARQUITECTURA.md)** - Patrones, SOLID, GRASP
- **[Análisis Algorítmico](docs/04_Analisis_Algoritmico.md)** - Big O de cada método
- **[TDAs](docs/TDAs.md)** - Guía completa de estructuras de datos
- **[Invariantes](docs/07_Invariantes.md)** - Reglas de representación
- **[Iteración 1](docs/ITERACION_1.md)** - Casos de uso implementados
- **[Code Review](docs/code_review_report.md)** - Análisis de calidad

Ver índice completo: **[docs/INDEX.md](docs/INDEX.md)**

---

## 🔍 Ejemplo de Uso

### Caso: Usuario 1001 sigue a usuario 5000

```java
// 1. Login
Cliente usuario = gestor.buscarPorId(1001);
Sesion.getInstancia().iniciarSesion(usuario);

// 2. Buscar objetivo
Cliente objetivo = gestor.buscarPorId(5000);

// 3. Enviar solicitud
SolicitudSeguimiento sol = new SolicitudSeguimiento("1001", "5000");
objetivo.recibirSolicitud(sol);  // Encola en Cola<>

// 4. Procesar solicitud (usuario 5000)
SolicitudSeguimiento siguiente = objetivo.procesarSiguienteSolicitud();
gestor.seguir(1001, 5000);  // Registra en historial

// 5. Undo (si se arrepiente)
gestor.deshacer();  // Revierte la acción
```

---

## 📋 Principios Aplicados

### SOLID
- **SRP**: Cada clase tiene una responsabilidad
- **OCP**: TDAs genéricos reutilizables
- **DIP**: Interfaces para TDAs

### GRASP
- **Information Expert**: `Cliente` conoce sus seguidos
- **Creator**: `GestorClientes` crea `Cliente` y `Accion`
- **Low Coupling**: Capas desacopladas
- **High Cohesion**: Métodos relacionados en misma clase

---

## 🎓 Decisiones de Diseño

### ¿Por qué Singleton para Sesion?
- Garantiza única sesión activa (requisito del dominio)
- Simplifica API (menos parámetros)
- Apropiado para TP académico

### ¿Por qué TDAs propios?
- Requisito académico (demostrar conocimiento)
- Control total sobre complejidad
- Aprendizaje profundo de implementación

### ¿Por qué Hash Table con capacidad fija 64?
- Suficiente para carga típica (< 100 clientes en memoria)
- Evita complejidad de rehashing
- Mantiene O(1) amortizado

---

## 📈 Métricas

| Métrica | Valor |
|---------|-------|
| Capas arquitectónicas | 4 |
| Clases totales | ~20 |
| TDAs implementados | 3 |
| Patrones de diseño | 4 |
| Complejidad promedio | O(1) |
| Dataset soportado | 1M+ clientes |

---

## 🚦 Estado del Proyecto

**Iteración 1**: ✅ Completada  
**Iteración 2**: ⏳ Pendiente (ABB, relaciones avanzadas)  
**Iteración 3**: ⏳ Pendiente (Grafo, distancias)

---

## 👥 Autor

Proyecto académico - UADE - Algoritmos y Estructuras de Datos II

---

## 📝 Licencia

Proyecto educativo - Uso académico únicamente
