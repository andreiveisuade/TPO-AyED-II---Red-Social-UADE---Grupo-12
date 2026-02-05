# Análisis SOLID y GRASP - Estado del Proyecto

## Estado Actual del Proyecto

### ✅ Principios Bien Aplicados

#### SOLID

1. **SRP (Single Responsibility Principle)** ✅
   - `GestorClientes`: Solo gestiona clientes
   - `HistorialAcciones`: Solo gestiona historial
   - `ColaSolicitudes`: Solo gestiona solicitudes (en Modelo)
   - `MenuClientes`, `MenuHistorial`, `MenuSolicitudes`: Cada uno su dominio

2. **DIP (Dependency Inversion Principle)** ✅
   - Interfaces: `IPila`, `ICola`, `IDiccionario`, `IConjunto`
   - Interfaces de servicio: `IHistorial`, `IGestorSolicitudes`, `ICargadorDatos`
   - Las clases dependen de abstracciones, no de implementaciones concretas

3. **ISP (Interface Segregation Principle)** ✅
   - Interfaces específicas y cohesivas
   - No hay métodos no utilizados

#### GRASP

1. **Information Expert** ✅
   - `GestorClientes` conoce el Diccionario de clientes
   - `Cliente` conoce sus seguidos
   - `HistorialAcciones` conoce la Pila de acciones

2. **Creator** ✅
   - `GestorClientes` crea instancias de `Cliente` y `Accion`
   - `Cliente` crea instancias de `Cola` para solicitudes

3. **Low Coupling** ✅
   - Los menús dependen de servicios, no de TDAs directamente
   - Los servicios dependen de interfaces de TDAs

---

## ⚠️ Oportunidades de Mejora

### 1. **Violación de SRP en `Menu.java`**

**Problema:** La clase `Menu` tiene múltiples responsabilidades:
- Coordinar navegación
- Cargar datos desde JSON
- Validar usuario
- Mostrar UI

**Líneas problemáticas:**
```java
// Línea 94-102: Responsabilidad de carga de datos
private void cargarDatosIniciales() {
    try {
        JsonLoader loader = new JsonLoader();
        loader.cargarDesdeArchivo(RUTA_JSON, gestor, colaSolicitudes);
        mensajeEstado = "Datos cargados: " + gestor.getCantidadClientes() + " usuarios";
    } catch (IOException e) {
        mensajeEstado = "Sin datos precargados (" + e.getMessage() + ")";
    }
}

// Línea 55-89: Responsabilidad de autenticación
private void mostrarPantallaBienvenida() {
    // Validación de usuario
    // Lógica de autenticación
}
```

**Mejora propuesta:** Crear `ServicioAutenticacion` y `ServicioCargaDatos`

---

### 2. **Violación de OCP (Open/Closed Principle) en Menús**

**Problema:** Agregar una nueva opción al menú requiere modificar el switch:

```java
// MenuHistorial.java línea 53-64
switch (opcion) {
    case 1:
        mensaje = verUltimaAccion();
        break;
    case 2:
        mensaje = deshacerAccion();
        break;
    case 3:
        verHistorialCompleto();
        pausar(scanner);
        break;
}
```

**Mejora propuesta:** Patrón Command con mapa de acciones:
```java
Map<Integer, Comando> comandos = new HashMap<>();
comandos.put(1, new VerUltimaAccionComando(gestor));
comandos.put(2, new DeshacerComando(gestor));
comandos.put(3, new VerHistorialCompletoComando(gestor));

// Ejecutar
Comando cmd = comandos.get(opcion);
if (cmd != null) cmd.ejecutar();
```

---

### 3. **Acoplamiento a Scanner en Menús**

**Problema:** Todos los menús dependen directamente de `Scanner`:

```java
public MenuClientes(GestorClientes gestor, Scanner scanner) {
    this.gestor = gestor;
    this.scanner = scanner;
    this.utils = new MenuUtils(scanner);
}
```

**Mejora propuesta:** Crear interfaz `IEntradaSalida`:
```java
public interface IEntradaSalida {
    String leerLinea();
    int leerEntero();
    void mostrar(String mensaje);
}

public class EntradaSalidaConsola implements IEntradaSalida {
    private Scanner scanner;
    // Implementación
}

// En los menús
public MenuClientes(GestorClientes gestor, IEntradaSalida io) {
    this.gestor = gestor;
    this.io = io;
}
```

**Beneficios:**
- Facilita testing (mock de entrada/salida)
- Permite cambiar a GUI sin modificar lógica
- Cumple DIP

---

### 4. **Falta de Patrón Repository**

**Problema:** `GestorClientes` mezcla lógica de negocio con acceso a datos:

```java
public boolean agregarCliente(String nombre, int scoring) {
    // Validaciones (lógica de negocio)
    if (nombre == null || nombre.trim().isEmpty()) return false;
    if (scoring < 0 || scoring > 100) return false;
    
    // Acceso a datos
    clientes.insertar(nombreNormalizado, cliente);
    
    // Lógica de historial
    if (registrarEnHistorial) {
        historial.registrar(accion);
    }
}
```

**Mejora propuesta:** Separar en capas:

```java
// Capa de Repositorio
public interface IRepositorioClientes {
    void guardar(Cliente cliente);
    Cliente buscar(String nombre);
    void eliminar(String nombre);
    Cliente[] buscarPorScoring(int scoring);
}

public class RepositorioClientesMemoria implements IRepositorioClientes {
    private Diccionario<String, Cliente> clientes;
    // Implementación pura de acceso a datos
}

// Capa de Servicio (lógica de negocio)
public class ServicioClientes {
    private IRepositorioClientes repo;
    private IHistorial historial;
    
    public boolean agregarCliente(String nombre, int scoring) {
        // Solo validaciones y lógica de negocio
        if (!validarNombre(nombre)) return false;
        if (!validarScoring(scoring)) return false;
        
        Cliente cliente = new Cliente(nombre, scoring);
        repo.guardar(cliente);
        historial.registrar(new Accion(...));
        return true;
    }
}
```

**Beneficios:**
- Cumple SRP
- Facilita cambiar persistencia (memoria → BD)
- Facilita testing (mock del repositorio)

---

### 5. **Validación Centralizada (✅ IMPLEMENTADO)**

**Estado:** Implementado en clase `modelo.Validador`.

**Solución aplicada:**

```java
public class Validador {
    private Validador() {}
    
    public static ResultadoValidacion validarNombre(String nombre) { ... }
    public static ResultadoValidacion validarScoring(int scoring) { ... }
}
```

**Beneficio obtenido:** Eliminación de lógica duplicada en `Cliente` y `GestorClientes`.

---

### 6. **Falta de Patrón Strategy para Búsquedas**

**Problema:** `GestorClientes` tiene métodos específicos para cada tipo de búsqueda:

```java
public Cliente buscarPorNombre(String nombre) { ... }
public Cliente[] buscarPorScoring(int scoring) { ... }
// Futuro: buscarPorRango(), buscarPorPrefijo(), etc.
```

**Mejora propuesta:** Patrón Strategy:

```java
public interface CriterioBusqueda {
    boolean cumple(Cliente cliente);
}

public class BusquedaPorScoring implements CriterioBusqueda {
    private int scoring;
    
    public BusquedaPorScoring(int scoring) {
        this.scoring = scoring;
    }
    
    @Override
    public boolean cumple(Cliente cliente) {
        return cliente.getScoring() == scoring;
    }
}

// En GestorClientes
public Cliente[] buscar(CriterioBusqueda criterio) {
    // Primera pasada: contar
    int count = 0;
    for (Cliente c : todosLosClientes) {
        if (criterio.cumple(c)) count++;
    }
    
    // Segunda pasada: llenar
    Cliente[] resultado = new Cliente[count];
    int i = 0;
    for (Cliente c : todosLosClientes) {
        if (criterio.cumple(c)) resultado[i++] = c;
    }
    return resultado;
}

// Uso
Cliente[] resultado = gestor.buscar(new BusquedaPorScoring(50));
```

**Beneficios:**
- OCP: Agregar nuevos criterios sin modificar GestorClientes
- Reutilización: Combinar criterios con AND/OR

---

### 7. **Violación de Tell, Don't Ask en MenuClientes**

**Problema:** Los menús preguntan por datos y luego los formatean:

```java
// MenuClientes.java línea 179-195
for (String nombre : nombres) {
    Cliente c = gestor.buscarPorNombre(nombre);
    if (c != null) {
        String nombreCol = String.format("%-18s", c.getNombre());
        String scoreCol = String.format("%-7d", c.getScoring());
        // ... formateo manual
    }
}
```

**Mejora propuesta:** Delegar el formateo:

```java
// En Cliente.java
public String formatearParaTabla() {
    String nombreCol = String.format("%-18s", nombre);
    String scoreCol = String.format("%-7d", scoring);
    StringBuilder sigueCol = new StringBuilder();
    // ... lógica de formateo
    return nombreCol + " | " + scoreCol + " | " + sigueCol;
}

// En MenuClientes
for (String nombre : nombres) {
    Cliente c = gestor.buscarPorNombre(nombre);
    if (c != null) {
        System.out.println(c.formatearParaTabla());
    }
}
```

**Beneficio:** El objeto conoce mejor cómo representarse

---

### 8. **Falta de Patrón Observer para Notificaciones**

**Problema:** Cambios en el sistema no notifican a interesados:

```java
// Si se elimina un cliente, ¿cómo notificar a la UI?
// Si se procesa una solicitud, ¿cómo actualizar estadísticas?
```

**Mejora propuesta:** Patrón Observer:

```java
public interface ObservadorSistema {
    void onClienteAgregado(Cliente cliente);
    void onClienteEliminado(String nombre);
    void onSolicitudProcesada(SolicitudSeguimiento solicitud);
}

public class GestorClientes {
    private List<ObservadorSistema> observadores = new ArrayList<>();
    
    public void agregarObservador(ObservadorSistema obs) {
        observadores.add(obs);
    }
    
    public boolean agregarCliente(String nombre, int scoring) {
        // ... lógica existente
        notificarClienteAgregado(cliente);
    }
    
    private void notificarClienteAgregado(Cliente cliente) {
        for (ObservadorSistema obs : observadores) {
            obs.onClienteAgregado(cliente);
        }
    }
}

// Uso en Menu
public class Menu implements ObservadorSistema {
    @Override
    public void onClienteAgregado(Cliente cliente) {
        mensajeEstado = "Usuario " + cliente.getNombre() + " agregado";
    }
}
```

---

## Resumen de Mejoras Propuestas

| # | Principio Violado | Mejora | Prioridad | Estado |
|---|-------------------|--------|-----------|--------|
| 1 | SRP | Extraer `ServicioAutenticacion` y `ServicioCargaDatos` | Alta | Pendiente |
| 2 | OCP | Patrón Command en menús | Media | Pendiente |
| 3 | DIP | Interfaz `IEntradaSalida` | Alta | Pendiente |
| 4 | SRP | Patrón Repository | Alta | Pendiente |
| 5 | DRY | Validador centralizado | Media | ✅ Implementado |
| 6 | OCP | Patrón Strategy para búsquedas | Baja | Pendiente |
| 7 | Tell Don't Ask | Delegar formateo a Cliente | Baja | Pendiente |
| 8 | - | Patrón Observer | Baja | Pendiente |

---

## Conclusión

El proyecto tiene una **buena base de SOLID/GRASP**, especialmente:
- ✅ DIP con interfaces
- ✅ SRP en servicios
- ✅ Information Expert bien aplicado

Las mejoras propuestas son **refinamientos**, no correcciones críticas. Para Iteración 1, el código es **aceptable** tal como está.

**Prioridad:** Implementar mejoras #3 (IEntradaSalida) para facilitar testing.
