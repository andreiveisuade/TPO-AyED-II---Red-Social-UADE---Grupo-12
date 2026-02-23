# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Trabajo Práctico: Red Social** — A high-performance social network system managing 1M+ users using custom abstract data types (TDAs) in Java 17+, optimized for O(1) operations through hash tables and linked structures.

## Common Commands

### Build and Run
```bash
# Clean build and run the application
./run.sh

# Manual compilation (if needed)
cd src
javac -cp ../lib/gson-2.10.1.jar -d ../out Main.java tda/*.java modelo/*.java servicio/*.java vista/*.java interfaces/*.java util/*.java
cd ..
java -cp out:lib/gson-2.10.1.jar Main
```

### Testing
```bash
# Run all tests
./test.sh

# Run a single test (example: ClienteTest)
javac -cp "lib/gson-2.10.1.jar" -d out test/ClienteTest.java src/tda/*.java src/modelo/*.java src/servicio/*.java src/util/*.java src/interfaces/*.java
java -ea -cp out:"lib/gson-2.10.1.jar" ClienteTest

# Available tests: TDATest, ABBTest, ClienteTest, GestorClientesTest, JsonLoaderTest, ColaSolicitudesTest, DistanciaTest
```

## Architecture

### Layered Design
```
┌─────────────────────────────────────────────┐
│  VIEW (vista/): Console UI                  │ User interaction
├─────────────────────────────────────────────┤
│  SERVICES (servicio/): Business Logic       │ Coordinates operations
│  - GestorClientes: CRUD + undo/redo        │
│  - IndiceClientes: Scoring + Name indices   │
│  - GestorRelaciones: Follower relationships │
│  - HistorialAcciones: Undo/Redo stack      │
│  - PersistenciaClientes: JSON I/O           │
├─────────────────────────────────────────────┤
│  DATA (tda/, modelo/): Custom Data Types    │ O(1) operations
│  - Diccionario (Hash Table): O(1) lookup    │
│  - Pila (Stack): O(1) operations            │
│  - Cola (Queue): O(1) operations            │
│  - ABB (Binary Search Tree): O(log N)       │
└─────────────────────────────────────────────┘
```

### Service Responsibilities

**GestorClientes** (`src/servicio/GestorClientes.java`)
- Coordinates CRUD operations and undo/redo actions
- Manages primary index (ID hash) directly
- Delegates secondary indices (name, scoring) to `IndiceClientes`
- Delegates relationships to `GestorRelaciones`, persistence to `PersistenciaClientes`
- Cascade deletion optimized via bidirectional indices: O(seguidores + siguiendo)
- **Key methods:** `agregarCliente()` [O(1)], `buscarPorId()` [O(1)], `eliminarCliente()` [O(seg+sig)], `obtenerClientesMasPopulares()` [O(N)]

**IndiceClientes** (`src/servicio/IndiceClientes.java`)
- Manages secondary indices: Name hash + Scoring ABB<Integer, Cola<Cliente>>
- Scoring ABB stores one node per unique scoring value (max 101 nodes for 0-100), each containing a Cola
- Scoring index uses lazy loading (built on first use)
- **Key methods:** `buscarPorNombre()` [O(1)], `buscarPorScoring()` [O(log 101 + k)], `obtenerClientesEnNivel()` [O(N)]

**GestorRelaciones** (`src/servicio/GestorRelaciones.java`)
- Manages follower relationships and graphs
- Builds relation trees with ABB sorting
- Performs BFS for distance calculations (iteration 3)
- **Key methods:** `seguir()` [O(1)], `dejarDeSeguir()` [O(1)], `obtenerVecinos()` [O(k)], `construirArbolRelaciones()` [O(k log k)], `calcularDistancia()` [O(V+E)]

**PersistenciaClientes** (`src/servicio/PersistenciaClientes.java`)
- Loads JSON at startup (one-time O(N))
- Saves state at shutdown (O(N))
- Encapsulates serialization logic via DTOs
- Dynamic capacity: `cargarDesdeArchivo()` auto-sizes Diccionario via `NumerosPrimos.capacidadOptima(N)` (α = 0.75)

**HistorialAcciones** (`src/servicio/HistorialAcciones.java`)
- Implements Command pattern with stacks
- Provides undo/redo for user actions (SEGUIR, DEJAR_DE_SEGUIR only)
- CRUD operations (agregar/eliminar cliente) are system-level and not tracked

### Data Persistence Strategy

The system uses a **"Load on Startup / Save on Shutdown"** strategy:
1. At launch: Deserialize complete JSON dataset into memory (O(N), ~1-2 sec for 1M users)
2. During execution: All operations in RAM (O(1) for hash operations)
3. At exit: Serialize current state back to JSON (O(N))

**Dynamic Diccionario Capacity:** At load time, `PersistenciaClientes` counts clients in the JSON
and sizes the Diccionario using `NumerosPrimos.capacidadOptima(N)` = `siguientePrimo(⌈N / 0.75⌉)`.
This ensures α ≤ 0.75 regardless of dataset size (10 clients or 1M).

**JSON Format:** `data/clientes.json` contains array of clients with `id`, `nombre`, `scoring`, `siguiendo[]`, `seguidores[]`, `solicitudes[]`

### Key Design Patterns

| Pattern | Implementation | Benefit |
|---------|----------------|---------|
| **Singleton** | `Sesion` manages auth state | Single active session |
| **Facade** | `GestorClientes` hides TDA complexity | Simplified client API |
| **Value Object** | `SolicitudSeguimiento` immutable | Referential integrity |
| **Command** | Action history with stacks (SEGUIR/DEJAR_DE_SEGUIR) | Undo/Redo support |

## Important Implementation Details

### Lazy Loading of Scoring Index
- The ABB for scoring-based searches is built on-first-use, not during initialization
- This avoids O(N²) insertion overhead during load
- First search triggers one-time O(N log N) build; subsequent searches are O(log N)

### Bidirectional Relationships
- When following: updates both client's `siguiendo[]` and target's `seguidores[]`
- Undo operations must maintain graph consistency
- Limit: max 2 followers per client

### Cascade Deletion (Optimized)
- Deleting a client removes all references in followers' `seguidores[]` and followed clients' `siguiendo[]`
- Uses bidirectional indices (`seguidores` + `siguiendo` on each Cliente) to avoid full scan
- Complexity: O(seguidores + siguiendo) instead of O(N) — only visits affected clients

### Client Cache (getSiguiendo/getSeguidores)
- `Cliente` caches the `int[]` arrays returned by `getSiguiendo()` and `getSeguidores()`
- Cache invalidated (set to null) on any mutation (seguir, dejarDeSeguir, agregarSeguidor, eliminarSeguidor)
- Avoids repeated O(k) parsing of Diccionario keys on each call

## Code Organization

```
src/
├── Main.java                  # Entry point
├── tda/                       # Custom abstract data types
│   ├── Diccionario.java      # Hash table implementation
│   ├── Pila.java             # Stack (LIFO)
│   ├── Cola.java             # Queue (FIFO)
│   └── ABB.java              # Binary search tree
├── modelo/                    # Domain objects
│   ├── Cliente.java          # User entity
│   └── SolicitudSeguimiento.java
├── servicio/                 # Business logic coordinators
│   ├── GestorClientes.java   # CRUD + undo/redo coordinator
│   ├── IndiceClientes.java   # Secondary indices (name + scoring)
│   ├── GestorRelaciones.java
│   ├── PersistenciaClientes.java
│   └── HistorialAcciones.java
├── vista/                    # Console UI
├── util/                     # Utilities (NumerosPrimos, Validador)
└── interfaces/               # Interface definitions
test/
├── TDATest.java             # TDA structure tests
├── ABBTest.java             # ABB-specific tests
├── ClienteTest.java
├── GestorClientesTest.java
├── DistanciaTest.java       # Iteration 3: BFS distance
└── ...
```

## Complexity Analysis Reference

| Operation | Complexity | Notes |
|-----------|-----------|-------|
| Add client | O(1) amortized | Hash insertion |
| Delete client | O(seg+sig) | Bidirectional cascade cleanup |
| Get top popular | O(N) amortized | Single-pass buffer algorithm |
| Search by ID | O(1) | Hash lookup |
| Search by name | O(1) + O(k) | Hash lookup + queue traversal |
| Search by scoring | O(log 101 + k) | ABB<Int, Cola> (lazy-loaded, max 101 nodes) |
| Create relation | O(1) | Append to arrays |
| Find neighbors | O(k) | k = followers count |
| Build relation tree | O(k log k) | ABB insertion |
| Get followers at level | O(N) | One-time BFS; cached after |
| Add friendship (bidirectional) | O(1) | Hash insertion (both sides) |
| Remove friendship (bidirectional) | O(1) | Hash deletion (both sides) |
| Get friends | O(k) | k = friend count |
| Check if friends | O(1) | Hash lookup |
| Calculate distance | O(V+E) | BFS traversal |
| Save to JSON | O(N*M) | N clients, M avg relations |

## Testing Strategy

Tests use assertions (`-ea` flag) to validate:
- **TDATest**: Custom data structure correctness
- **ABBTest**: Binary search tree operations and balancing
- **ClienteTest**: Client entity logic
- **GestorClientesTest**: CRUD and search operations
- **JsonLoaderTest**: JSON serialization/deserialization
- **ColaSolicitudesTest**: Follow request queue management
- **DistanciaTest**: BFS-based distance calculation

Run all: `./test.sh`
Run single: `javac [...] test/{TestName}.java [...] && java -ea -cp out:"lib/gson-2.10.1.jar" {TestName}`

## Development Notes

- **No java.util collections**: All structures implemented as custom TDAs from scratch
- **SOLID principles**: Each service has single responsibility; easy to extend via OCP
- **Assertions enabled**: Run tests with `-ea` flag for invariant checking
- **Gson dependency**: Required for JSON I/O (`lib/gson-2.10.1.jar`)
- **Memory-first approach**: Trades memory for speed; suitable for datasets up to 1M+ records on modern hardware



Do not add Co-Authored-by trailers to commit messages!!
Claude is not the co-author
