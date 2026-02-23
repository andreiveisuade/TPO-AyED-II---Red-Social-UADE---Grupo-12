# 📋 CASOS DE USO - Red Social UADE

## Visión General

Este documento especifica todos los **casos de uso** del sistema de Red Social, organizados por:
- **Iteración** (Iter 1, 2, 3)
- **Actor** (Usuario Autenticado, Sistema)
- **Flujo** (Normal, Alternativo, Excepcional)

---

## 🎭 Actores Identificados

| Actor | Descripción |
|-------|-------------|
| **Usuario Autenticado** | Usuario que ha iniciado sesión en el sistema |
| **Sistema** | Procesos automáticos (persistencia, validación) |
| **Base de Datos** | Almacenamiento persistente (JSON) |

---

## 📊 MATRIZ DE CASOS DE USO

```
┌─────────────────────────────────────────────────────────┐
│                GESTIÓN DE SESIÓN                        │
│  ├─ CU-001: Iniciar Sesión                              │
│  └─ CU-002: Cerrar Sesión                               │
├─────────────────────────────────────────────────────────┤
│              ITERACIÓN 1: CRUD BÁSICO                    │
│  ├─ CU-003: Agregar Cliente                             │
│  ├─ CU-004: Buscar Cliente por ID                       │
│  ├─ CU-005: Buscar Clientes por Nombre                  │
│  ├─ CU-006: Buscar Clientes por Scoring                 │
│  ├─ CU-007: Listar Todos los Clientes                   │
│  └─ CU-008: Eliminar Cliente                            │
├─────────────────────────────────────────────────────────┤
│         ITERACIÓN 1: RELACIONES SIMPLES                  │
│  ├─ CU-009: Seguir a un Usuario                         │
│  ├─ CU-010: Dejar de Seguir a un Usuario                │
│  ├─ CU-011: Ver Lista de Seguidos                       │
│  └─ CU-012: Ver Lista de Seguidores                     │
├─────────────────────────────────────────────────────────┤
│        ITERACIÓN 1: SOLICITUDES DE SEGUIMIENTO           │
│  ├─ CU-013: Enviar Solicitud de Seguimiento             │
│  ├─ CU-014: Ver Solicitud Pendiente                     │
│  ├─ CU-015: Aceptar Solicitud de Seguimiento            │
│  ├─ CU-016: Rechazar Solicitud de Seguimiento           │
│  └─ CU-017: Ver Cantidad de Solicitudes Pendientes      │
├─────────────────────────────────────────────────────────┤
│       ITERACIÓN 1: HISTORIAL Y DESHACER/REHACER          │
│  ├─ CU-018: Ver Historial de Acciones                   │
│  ├─ CU-019: Deshacer Última Acción (Undo)               │
│  ├─ CU-020: Rehacer Acción (Redo)                       │
│  └─ CU-021: Limpiar Historial                           │
├─────────────────────────────────────────────────────────┤
│         ITERACIÓN 2: CONSULTAS AVANZADAS                 │
│  ├─ CU-022: Obtener Vecinos (Seguidos Directos)         │
│  ├─ CU-023: Construir Árbol de Relaciones               │
│  ├─ CU-024: Obtener Seguidores en Nivel N               │
│  ├─ CU-025: Obtener Seguidores Ordenados por Scoring    │
│  ├─ CU-026: Obtener Clientes en Cuarto Nivel (ABB)      │
│  └─ CU-027: Consultar Influencia por Scoring            │
├─────────────────────────────────────────────────────────┤
│    ITERACIÓN 3: RELACIONES BIDIRECCIONALES (AMISTADES)   │
│  ├─ CU-028: Agregar Amistad Bidireccional               │
│  ├─ CU-029: Eliminar Amistad Bidireccional              │
│  ├─ CU-030: Ver Lista de Amigos                         │
│  ├─ CU-031: Verificar si Dos Usuarios son Amigos        │
│  └─ CU-032: Obtener Cantidad de Amigos                  │
├─────────────────────────────────────────────────────────┤
│      ITERACIÓN 3: ANÁLISIS DE DISTANCIA (BFS)            │
│  ├─ CU-033: Calcular Distancia entre Dos Usuarios       │
│  ├─ CU-034: Encontrar Camino Más Corto                  │
│  └─ CU-035: Verificar Conectividad entre Usuarios       │
├─────────────────────────────────────────────────────────┤
│           SISTEMA: PERSISTENCIA Y DATOS                  │
│  ├─ CU-036: Cargar Clientes desde JSON                  │
│  ├─ CU-037: Guardar Cambios en JSON                     │
│  └─ CU-038: Validar Integridad de Datos                 │
└─────────────────────────────────────────────────────────┘
```

---

## 📝 ESPECIFICACIÓN DETALLADA DE CASOS DE USO

---

# GESTIÓN DE SESIÓN

## **CU-001: Iniciar Sesión**

**Iteración:** Previa (Sesión)
**Actor:** Usuario
**Precondiciones:** Sistema iniciado, al menos 1 cliente en base de datos

**Flujo Normal:**
```
1. Usuario ingresa su ID
2. Sistema busca cliente por ID (O(1))
3. Si existe:
   - Sistema registra cliente en Sesion (Singleton)
   - Sistema muestra mensaje de bienvenida
   - Sistema verifica si hay solicitudes pendientes
   - Sistema muestra cantidad de solicitudes
4. Si no existe:
   - Sistema muestra error
   - Vuelve al paso 1
```

**Flujo Alternativo (Salir):**
```
1. Usuario ingresa 0
2. Sistema cierra la aplicación
```

**Flujo Excepcional:**
```
1. Usuario ingresa valor inválido (negativo)
2. Sistema muestra error y pide reintentar
```

**Complejidad:** O(1)
**Clases Involucradas:** `Menu.java`, `Sesion.java`, `GestorClientes.java`

---

## **CU-002: Cerrar Sesión**

**Iteración:** Previa (Sesión)
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario con sesión activa

**Flujo Normal:**
```
1. Usuario selecciona "Salir" del menú principal
2. Sistema guarda los cambios en JSON (O(N))
3. Sistema limpia la sesión (Singleton)
4. Sistema muestra mensaje "Hasta pronto!"
5. Usuario vuelve a pantalla de login
```

**Complejidad:** O(N)
**Clases Involucradas:** `Menu.java`, `Sesion.java`, `GestorClientes.java`, `PersistenciaClientes.java`

---

# ITERACIÓN 1: CRUD BÁSICO

## **CU-003: Agregar Cliente**

**Iteración:** 1
**Actor:** Sistema (automático al cargar)
**Precondiciones:** Archivo JSON disponible

**Flujo Normal:**
```
1. Sistema carga archivo JSON (O(N))
2. Para cada cliente en JSON:
   - Sistema crea instancia de Cliente
   - Sistema carga sus seguidos
   - Sistema carga sus solicitudes
   - Sistema inserta en Diccionario<ID> (O(1))
   - Sistema indexa por nombre (O(1))
3. Sistema reporta cantidad de clientes cargados
```

**Complejidad:** O(N)
**Clases Involucradas:** `PersistenciaClientes.java`, `GestorClientes.java`, `Cliente.java`

---

## **CU-004: Buscar Cliente por ID**

**Iteración:** 1
**Actor:** Usuario Autenticado, Sistema
**Precondiciones:** Cliente autenticado

**Flujo Normal:**
```
1. Usuario ingresa ID de usuario a buscar
2. Sistema busca en Diccionario<ID> (O(1))
3. Si existe:
   - Sistema muestra detalles del cliente
   - Sistema muestra cantidad de seguidos
   - Sistema muestra cantidad de seguidores
4. Si no existe:
   - Sistema muestra error "No encontrado"
```

**Casos de Uso que lo Usan:**
- CU-009: Seguir a un Usuario
- CU-013: Enviar Solicitud
- CU-022: Obtener Vecinos

**Complejidad:** O(1)
**Clases Involucradas:** `GestorClientes.java`, `Cliente.java`

---

## **CU-005: Buscar Clientes por Nombre**

**Iteración:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Cliente autenticado

**Flujo Normal:**
```
1. Usuario ingresa nombre (ej: "Alice")
2. Sistema busca en índice Diccionario<Nombre> (O(1))
3. Sistema obtiene Cola<Cliente> de ese nombre
4. Para cada cliente encontrado:
   - Sistema muestra ID, nombre e influencia
5. Si hay resultados:
   - Usuario puede seleccionar ID para enviar solicitud
6. Si no hay:
   - Sistema muestra "Sin resultados"
```

**Complejidad:** O(1) búsqueda + O(k) iteración (k = cantidad de clientes con ese nombre)
**Clases Involucradas:** `GestorClientes.java`, `MenuSolicitudes.java`

---

## **CU-006: Buscar Clientes por Scoring**

**Iteración:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Cliente autenticado

**Flujo Normal:**
```
1. Usuario ingresa valor de scoring (0-100)
2. Sistema construye ABB<Scoring> (lazy loading) - O(N log N) una sola vez
3. Sistema busca en ABB por scoring (O(log N))
4. Para cada cliente encontrado:
   - Sistema muestra ID, nombre
5. Si hay resultados:
   - Sistema muestra cantidad total
6. Si no hay:
   - Sistema muestra "Sin resultados"
```

**Complejidad:** O(log N + k) donde k = cantidad con ese scoring
**Clases Involucradas:** `GestorClientes.java`, `MenuSolicitudes.java`, `ArbolBinarioBusqueda.java`

---

## **CU-007: Listar Todos los Clientes**

**Iteración:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Cliente autenticado, al menos 1 cliente en el sistema

**Flujo Normal:**
```
1. Usuario selecciona "Listar todos"
2. Sistema obtiene todos los clientes del Diccionario (O(N))
3. Sistema muestra tabla con:
   - ID
   - Nombre
   - Influencia (Scoring)
4. Sistema muestra total de usuarios
5. Usuario puede navegar (opción 0 para volver)
```

**Complejidad:** O(N)
**Clases Involucradas:** `MenuSolicitudes.java`, `GestorClientes.java`

---

## **CU-008: Eliminar Cliente**

**Iteración:** 1
**Actor:** Sistema (implícito)
**Precondiciones:** Cliente existe en base de datos

**Flujo Normal:**
```
1. Sistema identifica cliente a eliminar
2. Sistema remueve de Diccionario<ID> (O(1))
3. Sistema remueve de índice Diccionario<Nombre> (O(1))
4. Sistema remueve de ABB<Scoring> (O(log N))
5. Sistema limpia referencias bidireccionales:
   - De los que lo seguían (O(k))
   - A los que él seguía (O(k))
6. Sistema remueve de persistencia
```

**Complejidad:** O(N) en peor caso (cascada de referencias)
**Clases Involucradas:** `GestorClientes.java`, `GestorRelaciones.java`, `Cliente.java`

---

# ITERACIÓN 1: RELACIONES SIMPLES

## **CU-009: Seguir a un Usuario**

**Iteración:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Cliente autenticado, usuario objetivo existe

**Flujo Normal:**
```
1. Usuario busca y selecciona otro usuario (CU-004, CU-005, CU-006)
2. Usuario selecciona "Enviar solicitud"
3. Sistema valida:
   - Origen ≠ Destino (no auto-seguimiento)
   - No ya sigue (no duplicado)
   - Límite de 2 seguidos (restricción de negocio)
4. Si válido:
   - Sistema agrega ID en array de "siguiendo" (O(1))
   - Sistema agrega origen en "seguidores" del objetivo (O(1))
   - Sistema registra acción en Historial
   - Sistema muestra confirmación
5. Si inválido:
   - Sistema muestra error específico
```

**Flujo Alternativo (Cola de Solicitudes):**
```
Ver CU-013: Enviar Solicitud de Seguimiento
```

**Complejidad:** O(1)
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`, `HistorialAcciones.java`

---

## **CU-010: Dejar de Seguir a un Usuario**

**Iteración:** 1
**Actor:** Usuario Autenticado (implícito en undo)
**Precondiciones:** Usuario seguía a otro usuario

**Flujo Normal:**
```
1. Usuario ejecuta Undo de CU-009 (ver CU-019)
2. Sistema remueve ID del array "siguiendo" (O(1))
3. Sistema remueve origen del array "seguidores" (O(1))
4. Sistema registra acción inversa en Historial
5. Sistema muestra confirmación
```

**Complejidad:** O(1)
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`, `HistorialAcciones.java`

---

## **CU-011: Ver Lista de Seguidos**

**Iteración:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Cliente autenticado

**Flujo Normal:**
```
1. Usuario selecciona "Mis Amigos (Siguiendo)"
2. Sistema obtiene array de IDs seguidos del usuario (O(1))
3. Para cada ID seguido:
   - Sistema busca Cliente por ID (O(1))
   - Sistema muestra: ID, Nombre
4. Si no sigue a nadie:
   - Sistema muestra "No sigues a nadie aún"
5. Usuario puede navegar (opción 0 para volver)
```

**Complejidad:** O(k) donde k = cantidad de seguidos (máx 2)
**Clases Involucradas:** `MenuSolicitudes.java`, `GestorClientes.java`, `Cliente.java`

---

## **CU-012: Ver Lista de Seguidores**

**Iteración:** 1
**Actor:** Usuario Autenticado (implícito, a través de CU-022)
**Precondiciones:** Usuario tiene seguidores

**Flujo Normal:**
```
1. Sistema obtiene array de IDs de seguidores (O(1))
2. Para cada ID:
   - Sistema busca Cliente por ID (O(1))
   - Sistema muestra: ID, Nombre, Influencia
3. Sistema ordena por Scoring descendente (O(k log k))
4. Sistema muestra total de seguidores
```

**Complejidad:** O(k log k) donde k = cantidad de seguidores
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`

---

# ITERACIÓN 1: SOLICITUDES DE SEGUIMIENTO

## **CU-013: Enviar Solicitud de Seguimiento**

**Iteración:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario autenticado, usuario objetivo existe

**Flujo Normal:**
```
1. Usuario busca otro usuario (CU-004, CU-005, CU-006)
2. Usuario selecciona "Enviar solicitud"
3. Sistema valida:
   - No auto-solicitud
   - No solicitud duplicada
   - Usuario objetivo existe
4. Si válido:
   - Sistema crea SolicitudSeguimiento (inmutable)
   - Sistema encola en Cola<SolicitudSeguimiento> del objetivo (O(1))
   - Sistema registra en Historial
   - Sistema muestra: "[OK] Solicitud enviada a @{nombre}"
5. Si inválido:
   - Sistema muestra error
```

**Complejidad:** O(1)
**Clases Involucradas:** `GestorClientes.java`, `Cliente.java`, `SolicitudSeguimiento.java`, `Cola.java`, `HistorialAcciones.java`

---

## **CU-014: Ver Solicitud Pendiente**

**Iteración:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario tiene solicitudes pendientes

**Flujo Normal:**
```
1. Usuario selecciona "Solicitudes (Pendientes)"
2. Sistema obtiene siguiente solicitud sin desencolarla (O(1))
3. Si hay solicitud:
   - Sistema obtiene ID del solicitante
   - Sistema busca solicitante por ID (O(1))
   - Sistema muestra: "{nombre} quiere seguirte"
   - Sistema muestra cantidad total de solicitudes pendientes
4. Si no hay:
   - Sistema muestra "No tienes solicitudes pendientes"
```

**Complejidad:** O(1)
**Clases Involucradas:** `MenuSolicitudes.java`, `Cliente.java`, `Cola.java`

---

## **CU-015: Aceptar Solicitud de Seguimiento**

**Iteración:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario tiene solicitudes pendientes (CU-014)

**Flujo Normal:**
```
1. Usuario selecciona "Procesar siguiente"
2. Sistema desencola siguiente solicitud (O(1))
3. Sistema extrae IDs de solicitante y objetivo
4. Sistema ejecuta Seguir (CU-009):
   - Agrega origen en array "siguiendo" del objetivo
   - Agrega objetivo en array "seguidores" del origen
5. Sistema registra en Historial
6. Sistema muestra: "[OK] Solicitud procesada"
7. Sistema muestra: "Ahora sigues a {nombre}"

Flujo de Rechazo:
- Usuario ejecuta Undo (CU-019) para rechazar
```

**Complejidad:** O(1)
**Clases Involucradas:** `MenuSolicitudes.java`, `GestorRelaciones.java`, `Cliente.java`, `HistorialAcciones.java`

---

## **CU-016: Rechazar Solicitud de Seguimiento**

**Iteración:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario aceptó solicitud (CU-015)

**Flujo Normal:**
```
1. Usuario selecciona Undo (CU-019)
2. Sistema revierte la aceptación de solicitud
3. Sistema remueve relación bidireccional
4. Solicitud vuelve a cola (o se descarta)
5. Sistema muestra confirmación
```

**Complejidad:** O(1)
**Clases Involucradas:** `HistorialAcciones.java`, `GestorRelaciones.java`

---

## **CU-017: Ver Cantidad de Solicitudes Pendientes**

**Iteración:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario autenticado

**Flujo Normal:**
```
1. Sistema obtiene cantidad de solicitudes de la Cola (O(1))
2. Sistema muestra cantidad en:
   - Pantalla de login (CU-001)
   - Menú principal
3. Sistema actualiza en tiempo real después de CU-015
```

**Complejidad:** O(1)
**Clases Involucradas:** `Cliente.java`, `Cola.java`, `Menu.java`

---

# ITERACIÓN 1: HISTORIAL Y DESHACER/REHACER

## **CU-018: Ver Historial de Acciones**

**Iteración:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario ha realizado al menos 1 acción

**Flujo Normal:**
```
1. Usuario selecciona "Historial"
2. Sistema obtiene lista de Acciones por sesión (O(1) acceso)
3. Para cada Acción en orden (más reciente primero):
   - Sistema muestra:
     - Tipo de acción (AGREGAR, SEGUIR, ACEPTAR_SOLICITUD, etc)
     - Descripción legible
     - Timestamp
4. Sistema muestra total de acciones en sesión
5. Usuario puede:
   - Ver detalles (Opción específica)
   - Deshacer (CU-019)
   - Rehacer (CU-020)
```

**Complejidad:** O(k) donde k = cantidad de acciones
**Clases Involucradas:** `MenuHistorial.java`, `HistorialAcciones.java`, `Accion.java`, `Sesion.java`

---

## **CU-019: Deshacer Última Acción (Undo)**

**Iteración:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Al menos 1 acción en historial

**Flujo Normal:**
```
1. Usuario selecciona "Deshacer"
2. Sistema obtiene última Acción de Pila<Accion> (O(1))
3. Sistema ejecuta reversión según tipo:

   a) Si acción = SEGUIR:
      - Sistema remueve relación bidireccional
      - Sistema decrementa contadores

   b) Si acción = ACEPTAR_SOLICITUD:
      - Sistema revierte el seguimiento
      - Sistema reencola solicitud

   c) Si acción = AGREGAR_CLIENTE:
      - Sistema remueve cliente de todos los índices
      - Sistema limpia referencias (cascada)

4. Sistema mueve Acción a Pila<Redo>
5. Sistema muestra: "[OK] Acción deshecha"

Casos Especiales:
- Si pila Undo vacía: "No hay acciones para deshacer"
```

**Complejidad:** O(1) a O(N) según tipo de acción
**Clases Involucradas:** `HistorialAcciones.java`, `Pila.java`, `GestorRelaciones.java`, `Sesion.java`

---

## **CU-020: Rehacer Acción (Redo)**

**Iteración:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Al menos 1 acción en pila Redo

**Flujo Normal:**
```
1. Usuario selecciona "Rehacer"
2. Sistema obtiene Acción de Pila<Redo> (O(1))
3. Sistema re-ejecuta la Acción original
4. Sistema mueve Acción de vuelta a Pila<Undo>
5. Sistema muestra: "[OK] Acción rehecha"

Casos Especiales:
- Si pila Redo vacía: "No hay acciones para rehacer"
- Si usuario realiza nueva acción: pila Redo se limpia
```

**Complejidad:** O(1) a O(N) según tipo de acción
**Clases Involucradas:** `HistorialAcciones.java`, `Pila.java`, `Sesion.java`

---

## **CU-021: Limpiar Historial**

**Iteración:** 1 (Implícito)
**Actor:** Sistema
**Precondiciones:** Sesión se cierra

**Flujo Normal:**
```
1. Usuario cierra sesión (CU-002)
2. Sistema preserva historial de la sesión en Sesion (Singleton)
3. Usuario inicia nueva sesión (CU-001)
4. Sistema crea nuevo historial para nueva sesión
5. Historial anterior se descarta al cerrar aplicación
```

**Complejidad:** O(k) donde k = cantidad de acciones
**Clases Involucradas:** `Sesion.java`, `HistorialAcciones.java`

---

# ITERACIÓN 2: CONSULTAS AVANZADAS

## **CU-022: Obtener Vecinos (Seguidos Directos)**

**Iteración:** 2
**Actor:** Sistema, Usuario Autenticado
**Precondiciones:** Usuario existe, tiene seguidos

**Flujo Normal:**
```
1. Sistema obtiene array de IDs seguidos (O(1))
2. Para cada ID:
   - Sistema busca Cliente por ID (O(1))
   - Valida que exista
3. Sistema retorna Cliente[]
4. Usuario puede ver la lista de vecinos con contexto

Caso de Uso: Mostrar "Mis Amigos" (CU-011) usa esto internamente
```

**Complejidad:** O(k) donde k = cantidad de seguidos (máx 2)
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`

---

## **CU-023: Construir Árbol de Relaciones**

**Iteración:** 2
**Actor:** Sistema
**Precondiciones:** Usuario tiene seguidores

**Flujo Normal:**
```
1. Sistema obtiene IDs de seguidores de usuario (O(1))
2. Para cada seguidor:
   - Sistema busca Cliente (O(1))
   - Sistema obtiene Scoring del cliente
3. Sistema inserta en ABB<Scoring> (O(log N))
4. Sistema retorna ABB construido
5. ABB permite consultas eficientes por nivel (CU-024)

Estructura:
   ABB ordenado por Scoring (influencia)
   Cada nodo = Cliente
   Altura ≈ log(k) donde k = cantidad de seguidores
```

**Complejidad:** O(k log k) donde k = cantidad de seguidores
**Clases Involucradas:** `GestorRelaciones.java`, `ArbolBinarioBusqueda.java`, `Cliente.java`

---

## **CU-024: Obtener Seguidores en Nivel N**

**Iteración:** 2
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario tiene seguidores, nivel ≥ 0

**Flujo Normal:**
```
1. Usuario especifica nivel (ej: nivel 3 = cuarto nivel, 0-indexed)
2. Sistema construye ABB de relaciones (CU-023)
3. Sistema recorre ABB para obtener nodos en nivel N (O(N) primera vez)
4. Sistema caching: nivel se calcula una sola vez
5. Para cada cliente en nivel:
   - Sistema muestra: ID, Nombre, Scoring, Cantidad de Seguidores
6. Sistema ordena por Seguidores descendente (Selection Sort, O(k²))
7. Sistema muestra total de clientes en nivel

Caso especial: Cuarto nivel usado en CU-026
```

**Complejidad:** O(N) primera vez + caching O(1)
**Clases Involucradas:** `GestorRelaciones.java`, `MenuSolicitudes.java`, `ArbolBinarioBusqueda.java`

---

## **CU-025: Obtener Seguidores Ordenados por Scoring**

**Iteración:** 2
**Actor:** Sistema
**Precondiciones:** Usuario tiene seguidores

**Flujo Normal:**
```
1. Sistema obtiene array de IDs de seguidores (O(1))
2. Para cada ID:
   - Sistema busca Cliente (O(1))
   - Obtiene Scoring del cliente
3. Sistema ordena por Scoring descendente:
   - Algoritmo: Selection Sort
   - Complejidad: O(k log k)
4. Sistema retorna Cliente[] ordenado
5. Usuario puede ver seguidores más influyentes primero
```

**Complejidad:** O(k log k) donde k = cantidad de seguidores
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`

---

## **CU-026: Obtener Clientes en Cuarto Nivel (ABB)**

**Iteración:** 2
**Actor:** Usuario Autenticado
**Precondiciones:** Hay clientes en cuarto nivel del ABB global

**Flujo Normal:**
```
1. Usuario selecciona "Análisis ABB - Cuarto nivel"
2. Sistema obtiene ABB global de scoring (lazy loaded)
3. Sistema obtiene clientes en nivel 3 (cuarto nivel, 0-indexed) - O(N)
4. Para cada cliente:
   - Sistema muestra: ID, Nombre, Scoring, Cantidad de Seguidores
5. Sistema ordena por Seguidores descendente (Selection Sort)
6. Sistema destaca cliente con mayor influencia (más seguidores)
7. Usuario puede navegar (opción 0 para volver)

Nota: Sistema ABB es global y construido bajo demanda (lazy loading)
```

**Complejidad:** O(N) primera vez, O(k) iteraciones
**Clases Involucradas:** `MenuSolicitudes.java`, `GestorClientes.java`, `ArbolBinarioBusqueda.java`

---

## **CU-027: Consultar Influencia por Scoring**

**Iteración:** 2
**Actor:** Usuario Autenticado
**Precondiciones:** Al menos 1 cliente con ese scoring

**Flujo Normal:**
```
1. Usuario selecciona "Buscar por Influencia"
2. Usuario ingresa valor de scoring (0-100)
3. Sistema construye ABB<Scoring> si no existe (lazy loading)
4. Sistema busca en ABB (O(log N))
5. Para cada cliente encontrado:
   - Sistema muestra: ID, Nombre, Scoring
6. Sistema muestra cantidad total de resultados
7. Si no hay:
   - Sistema muestra "[AVISO] Sin resultados"
8. Usuario puede navegar
```

**Complejidad:** O(log N + k) donde k = cantidad con ese scoring
**Clases Involucradas:** `MenuSolicitudes.java`, `GestorClientes.java`, `ArbolBinarioBusqueda.java`

---

# ITERACIÓN 3: RELACIONES BIDIRECCIONALES (AMISTADES)

## **CU-028: Agregar Amistad Bidireccional**

**Iteración:** 3
**Actor:** Usuario Autenticado (implícito) / Sistema
**Precondiciones:** Dos usuarios existen, no son amigos

**Flujo Normal:**
```
1. Sistema inicia relación de amistad entre Usuario A y Usuario B
2. Sistema crea entrada en Diccionario<ID, Boolean> de A (O(1))
3. Sistema crea entrada en Diccionario<ID, Boolean> de B (O(1))
   → Garantía: Si A→B es amigo, B→A también es amigo
4. Sistema registra en Historial
5. Sistema persiste en JSON (campo "amistades")

Validaciones:
- A ≠ B (no auto-amistad)
- No relación duplicada
```

**Complejidad:** O(1)
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`, `Diccionario.java`

---

## **CU-029: Eliminar Amistad Bidireccional**

**Iteración:** 3
**Actor:** Usuario Autenticado (implícito) / Sistema
**Precondiciones:** Dos usuarios son amigos

**Flujo Normal:**
```
1. Sistema elimina entrada de Usuario A (O(1))
2. Sistema elimina entrada de Usuario B (O(1))
   → Garantía: Si se elimina A→B, se elimina B→A
3. Sistema registra en Historial
4. Sistema persiste cambio en JSON

Validaciones:
- Verificar que ambos eran amigos
- Proteger contra errores de índice
```

**Complejidad:** O(1)
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`, `Diccionario.java`

---

## **CU-030: Ver Lista de Amigos**

**Iteración:** 3
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario tiene amigos

**Flujo Normal:**
```
1. Usuario solicita ver amigos (implícito, a través de API)
2. Sistema obtiene array de IDs amigos del Diccionario (O(1) acceso)
3. Para cada ID amigo:
   - Sistema busca Cliente por ID (O(1))
   - Sistema muestra: ID, Nombre, Scoring
4. Sistema muestra cantidad total de amigos
5. Sistema permite filtrado/ordenamiento opcional

Diferencia con CU-011:
- CU-011: "Siguiendo" (dirigido A→B)
- CU-030: "Amigos" (bidireccional A↔B)
```

**Complejidad:** O(k) donde k = cantidad de amigos
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`

---

## **CU-031: Verificar si Dos Usuarios son Amigos**

**Iteración:** 3
**Actor:** Sistema, Usuario Autenticado
**Precondiciones:** Dos usuarios existen

**Flujo Normal:**
```
1. Sistema consulta si A es amigo de B (O(1))
2. Sistema busca B en Diccionario<ID> de A
3. Retorna:
   - true: Son amigos bidireccionales
   - false: No son amigos
4. Garantía: Si A→B = true, B→A = true

Casos de Uso que lo usan:
- Validación antes de CU-029
- Recomendaciones de amigos
- Análisis de red social
```

**Complejidad:** O(1)
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`

---

## **CU-032: Obtener Cantidad de Amigos**

**Iteración:** 3
**Actor:** Sistema, Usuario Autenticado
**Precondiciones:** Usuario autenticado

**Flujo Normal:**
```
1. Sistema obtiene tamaño del Diccionario<ID> de amigos (O(1))
2. Sistema retorna cantidad
3. Sistema muestra en:
   - Menú principal
   - Perfil del usuario
   - Estadísticas
```

**Complejidad:** O(1)
**Clases Involucradas:** `Cliente.java`, `Diccionario.java`

---

# ITERACIÓN 3: ANÁLISIS DE DISTANCIA (BFS)

## **CU-033: Calcular Distancia entre Dos Usuarios**

**Iteración:** 3
**Actor:** Usuario Autenticado, Sistema
**Precondiciones:** Dos usuarios existen

**Flujo Normal:**
```
1. Usuario especifica Usuario Origen (A) y Usuario Destino (B)
2. Sistema implementa BFS (Breadth-First Search):
   a) Caso base: A = B → distancia = 0
   b) Inicializar:
      - Cola<Integer> para IDs a visitar
      - Diccionario<Integer, Boolean> para visitados
   c) Encolar A, marcar visitado
   d) Mientras cola no vacía:
      - Desencolar ID actual
      - Para cada vecino (usuario que sigue):
        * Si vecino = B → retornar distancia
        * Si no visitado:
          - Marcar visitado
          - Encolar vecino
   e) Si no encontrado → retornar -1

3. Sistema retorna:
   - Número de saltos si hay camino
   - -1 si no hay camino
   - 0 si mismo usuario

Ejemplo:
   A → B → C
   distancia(A, C) = 2

   A → B (reverso, no alcanzable)
   distancia(B, A) = -1
```

**Complejidad:** O(V + E) donde:
- V = cantidad de usuarios alcanzables
- E = cantidad de relaciones

**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`, `Cola.java`, `Diccionario.java`

---

## **CU-034: Encontrar Camino Más Corto**

**Iteración:** 3
**Actor:** Sistema
**Precondiciones:** Dos usuarios existe, hay camino entre ellos

**Flujo Normal:**
```
1. Sistema ejecuta CU-033 (calcula distancia)
2. Sistema registra camino durante BFS:
   - Mantener Diccionario<ID, ID> de padre
   - Cuando se encuentra destino, reconstruir camino

3. Sistema retorna:
   - Array de IDs: [A, ..., B]
   - Opcional: nombres de usuarios en orden

Ejemplo:
   Camino: Alice → Bob → Charlie → Diana
   distancia = 3
   camino = [1, 2, 3, 4]
```

**Complejidad:** O(V + E) BFS + O(distancia) reconstrucción
**Clases Involucradas:** `GestorRelaciones.java`, `Diccionario.java`

---

## **CU-035: Verificar Conectividad entre Usuarios**

**Iteración:** 3
**Actor:** Sistema
**Precondiciones:** Dos usuarios existen

**Flujo Normal:**
```
1. Sistema ejecuta CU-033 (calcula distancia)
2. Sistema retorna:
   - true: Si distancia ≥ 0 (hay camino)
   - false: Si distancia = -1 (no hay camino)

Caso de Uso: Recomendaciones de conexión
- Si A y C no conectados, pero A→B y B→C
- Sistema recomienda seguir a B
```

**Complejidad:** O(V + E)
**Clases Involucradas:** `GestorRelaciones.java`

---

# SISTEMA: PERSISTENCIA Y DATOS

## **CU-036: Cargar Clientes desde JSON**

**Iteración:** Todas
**Actor:** Sistema
**Precondiciones:** Aplicación inicia, archivo JSON existe

**Flujo Normal:**
```
1. Sistema al iniciar llama a GestorClientes()
2. GestorClientes llama a PersistenciaClientes.cargarDesdeArchivo()
3. PersistenciaClientes abre archivo JSON
4. Para cada cliente en JSON:
   - Crea instancia de Cliente
   - Carga IDs de "siguiendo" → array
   - Carga solicitudes → Cola<SolicitudSeguimiento>
   - Carga IDs de "seguidores" → array
   - Carga IDs de "amistades" (Iter 3) → Diccionario<ID>
   - Inserta en Diccionario<ID> (O(1))
   - Indexa por nombre en Diccionario<Nombre> (O(1))

5. Sistema reporta:
   - Cantidad de clientes cargados
   - Tiempo de carga

Formato JSON esperado:
{
  "clientes": [
    {
      "id": 1,
      "nombre": "Alice",
      "scoring": 95,
      "siguiendo": [2, 3],
      "solicitudes": [],
      "seguidores": [4, 5],
      "amistades": [2, 6]  // Iter 3
    }
  ]
}

Validaciones:
- Si archivo no existe → diccionario vacío
- Si JSON malformado → manejo de excepciones
```

**Complejidad:** O(N)
**Clases Involucradas:** `PersistenciaClientes.java`, `GestorClientes.java`, `Cliente.java`, `Gson.java`

---

## **CU-037: Guardar Cambios en JSON**

**Iteración:** Todas
**Actor:** Sistema
**Precondiciones:** Usuario cierra sesión (CU-002) o aplicación termina

**Flujo Normal:**
```
1. Usuario selecciona Salir o aplicación termina
2. Sistema llama a GestorClientes.guardarCambios()
3. GestorClientes llama a PersistenciaClientes.guardarCambios()
4. Para cada cliente en Diccionario:
   - Crea ClienteDTO con estado actual:
     * id, nombre, scoring
     * array "siguiendo"
     * array "solicitudes"
     * array "seguidores"
     * array "amistades" (Iter 3)
   - Serializa a JSON
5. PersistenciaClientes sobrescribe archivo JSON
6. Sistema muestra: "Datos guardados exitosamente"

Estrategia: "Carga Inicial / Guardado Final"
- Carga: O(N) al iniciar
- Ejecución: O(1) en memoria
- Guardado: O(N) al terminar
→ Elimina latencia de I/O durante ejecución
```

**Complejidad:** O(N)
**Clases Involucradas:** `PersistenciaClientes.java`, `GestorClientes.java`, `Cliente.java`, `Gson.java`

---

## **CU-038: Validar Integridad de Datos**

**Iteración:** Todas
**Actor:** Sistema
**Precondiciones:** Datos cargados o después de operación

**Flujo Normal:**
```
1. Sistema valida después de CU-036 (carga):
   - Todos los IDs en "siguiendo" existen en base
   - Todos los IDs en "seguidores" existen en base
   - Relaciones son bidireccionales (A sigue B ⇔ B tiene A en seguidores)
   - Cola de solicitudes válida
   - Amistades son bidireccionales (Iter 3)

2. Sistema valida en tiempo de ejecución:
   - Límite de 2 seguidos respetado
   - No auto-ciclos
   - IDs no negativos

3. Si hay inconsistencias:
   - Registra en log
   - Intenta reparar
   - Si no posible: advierte al usuario

Complejidad: O(N + E) donde E = relaciones
```

**Complejidad:** O(N + E)
**Clases Involucradas:** `GestorClientes.java`, `PersistenciaClientes.java`

---

## 📊 RESUMEN: MATRIZ DE COBERTURA

| Caso de Uso | Iteración | Actor | Complejidad | Testing |
|------------|-----------|-------|------------|---------|
| CU-001 | - | Usuario | O(1) | ✓ Implícito |
| CU-002 | - | Usuario | O(N) | ✓ Implícito |
| CU-003 | 1 | Sistema | O(N) | ✓ JsonLoaderTest |
| CU-004 | 1 | Usuario | O(1) | ✓ GestorClientesTest |
| CU-005 | 1 | Usuario | O(1)+O(k) | ✓ GestorClientesTest |
| CU-006 | 1 | Usuario | O(log N) | ✓ GestorClientesTest |
| CU-007 | 1 | Usuario | O(N) | ✓ MenuSolicitudes |
| CU-008 | 1 | Sistema | O(N) | ✓ GestorClientesTest |
| CU-009 | 1 | Usuario | O(1) | ✓ GestorClientesTest |
| CU-010 | 1 | Usuario | O(1) | ✓ (Undo) |
| CU-011 | 1 | Usuario | O(k) | ✓ MenuSolicitudes |
| CU-012 | 1 | Usuario | O(k log k) | ✓ Implícito |
| CU-013 | 1 | Usuario | O(1) | ✓ ColaSolicitudesTest |
| CU-014 | 1 | Usuario | O(1) | ✓ MenuSolicitudes |
| CU-015 | 1 | Usuario | O(1) | ✓ MenuSolicitudes |
| CU-016 | 1 | Usuario | O(1) | ✓ (Undo) |
| CU-017 | 1 | Usuario | O(1) | ✓ MenuSolicitudes |
| CU-018 | 1 | Usuario | O(k) | ✓ MenuHistorial |
| CU-019 | 1 | Usuario | O(1)-O(N) | ✓ GestorClientesTest |
| CU-020 | 1 | Usuario | O(1)-O(N) | ✓ GestorClientesTest |
| CU-021 | 1 | Sistema | O(k) | ✓ Implícito |
| CU-022 | 2 | Sistema | O(k) | ✓ GestorClientesTest |
| CU-023 | 2 | Sistema | O(k log k) | ✓ ABBTest |
| CU-024 | 2 | Usuario | O(N) | ✓ GestorClientesTest |
| CU-025 | 2 | Sistema | O(k log k) | ✓ Implícito |
| CU-026 | 2 | Usuario | O(N) | ✓ MenuSolicitudes |
| CU-027 | 2 | Usuario | O(log N) | ✓ GestorClientesTest |
| CU-028 | 3 | Sistema | O(1) | ✓ AmistadTest |
| CU-029 | 3 | Sistema | O(1) | ✓ AmistadTest |
| CU-030 | 3 | Usuario | O(k) | ✓ AmistadTest |
| CU-031 | 3 | Sistema | O(1) | ✓ AmistadTest |
| CU-032 | 3 | Usuario | O(1) | ✓ AmistadTest |
| CU-033 | 3 | Usuario | O(V+E) | ✓ DistanciaTest |
| CU-034 | 3 | Sistema | O(V+E) | ✓ DistanciaTest |
| CU-035 | 3 | Sistema | O(V+E) | ✓ DistanciaTest |
| CU-036 | Todas | Sistema | O(N) | ✓ JsonLoaderTest |
| CU-037 | Todas | Sistema | O(N) | ✓ Implícito |
| CU-038 | Todas | Sistema | O(N+E) | ✓ Implícito |

**TOTAL: 38 Casos de Uso**

---

# 🧪 TESTING: GUÍA DE VALIDACIÓN DE CASOS DE USO

Este documento ahora incluye tanto la **especificación de casos de uso** como la **metodología para testearlos**.

## 📋 Suite de Tests

La carpeta `test/casos_de_uso/` contiene **tests de integración** para validar los **38 casos de uso** del sistema:

```
test/casos_de_uso/
├── CasosDeUsoTests.java           # Coordinador maestro (38 tests)
├── CU_001_IniciarSesion.java
├── CU_002_CerrarSesion.java
├── ... (todos los 38 tests)
│
├── edge_cases/
│   ├── EdgeCaseTests.java         # Coordinador (5 edge case suites)
│   ├── EC_001_ValoresNull.java
│   ├── EC_002_ValoresVacios.java
│   ├── EC_003_ValoresNegativos.java
│   ├── EC_004_LimitesScoring.java
│   └── ... (más edge cases)
│
└── performance/
    ├── PerformanceTests.java      # Coordinador (6 performance suites)
    ├── PERF_001_BusquedaPorIdO1.java
    ├── PERF_002_ListarTodosON.java
    ├── PERF_003_BusquedaABBOlogN.java
    ├── PERF_004_DistanciaOVE.java
    └── PERF_005_EscalabilidadMuchosClientes.java
```


## 🎯 Categoría de CU

### CRUD (CU-003 a CU-008):
- ✅ Operación exitosa
- ✅ Operación fallida (inválido)
- ✅ Validaciones aplicadas
- ✅ Estado posterior a operación
- ✅ Complejidad esperada

### Relaciones (CU-009 a CU-012, CU-022 a CU-035):
- ✅ Crear relación válida
- ✅ Validaciones (no auto, no duplicados)
- ✅ Bidireccionalidad
- ✅ Límites (máx 2 seguidos)
- ✅ Operaciones inversas

### Solicitudes (CU-013 a CU-017):
- ✅ Enviar solicitud válida
- ✅ Encolamiento correcto (FIFO)
- ✅ Procesamiento de solicitudes
- ✅ Múltiples solicitudes
- ✅ Rechazo/Aceptación

### Para CU de Historial (CU-018 a CU-021):
- ✅ Ver historial completo
- ✅ Undo funciona correctamente
- ✅ Redo funciona correctamente
- ✅ Cascada de cambios se revierte

### Para CU de Persistencia (CU-036 a CU-038):
- ✅ Carga desde JSON correcta
- ✅ Guardado en JSON correcto
- ✅ Validaciones de integridad
- ✅ Recuperación de errores

## 📝 Principios de Testing

1. **Cada test es independiente**: Usa su propio archivo TEST_DB
2. **Assertions con mensaje**: Siempre incluir mensaje descriptivo de error
3. **Cleanup automático**: Los archivos TEST_DB se crean/sobreescriben en cada test
4. **O(1) amortizado**: Algunos tests validan complejidad midiendo tiempos
5. **No dependen de UI**: Son tests puro de lógica, sin Scanner ni consola

## 📊 Cobertura de Tests

| Categoría | Cantidad | Descripción |
|-----------|----------|-------------|
| **Casos de Uso** | 38 | Funcionalidad de cada CU |
| **Edge Cases** | 5+ | Valores null, vacío, negativos, límites |
| **Performance** | 6 | Validación de complejidades Big O |
| **TOTAL** | 49+ | Tests completamente funcionales |

---
