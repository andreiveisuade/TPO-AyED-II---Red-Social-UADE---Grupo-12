# CASOS DE USO - Red Social UADE

## Vision General

Este documento especifica todos los **casos de uso** del sistema de Red Social, organizados por:
- **Iteracion** (Iter 1, 2, 3)
- **Actor** (Usuario Autenticado, Sistema)
- **Flujo** (Normal, Alternativo, Excepcional)

---

## Actores Identificados

| Actor | Descripcion |
|-------|-------------|
| **Usuario Autenticado** | Usuario que ha iniciado sesion en el sistema |
| **Sistema** | Procesos automaticos (persistencia, validacion) |
| **Base de Datos** | Almacenamiento persistente (JSON) |

---

## MATRIZ DE CASOS DE USO

```
+----------------------------------------------------------+
|                GESTION DE SESION                          |
|  +-- CU-001: Iniciar Sesion                              |
|  +-- CU-002: Cerrar Sesion                               |
+----------------------------------------------------------+
|              ITERACION 1: CRUD BASICO                     |
|  +-- CU-003: Agregar Cliente                              |
|  +-- CU-004: Buscar Cliente por ID                        |
|  +-- CU-005: Buscar Clientes por Nombre                   |
|  +-- CU-006: Buscar Clientes por Scoring                  |
|  +-- CU-007: Listar Todos los Clientes                    |
|  +-- CU-008: Eliminar Cliente                             |
+----------------------------------------------------------+
|         ITERACION 1: RELACIONES SIMPLES                   |
|  +-- CU-009: Seguir a un Usuario                          |
|  +-- CU-010: Dejar de Seguir a un Usuario                 |
|  +-- CU-011: Ver Lista de Seguidos                        |
|  +-- CU-012: Ver Lista de Seguidores                      |
+----------------------------------------------------------+
|        ITERACION 1: SOLICITUDES DE SEGUIMIENTO            |
|  +-- CU-013: Enviar Solicitud de Seguimiento              |
|  +-- CU-014: Ver Solicitud Pendiente                      |
|  +-- CU-015: Aceptar Solicitud de Seguimiento             |
|  +-- CU-016: Rechazar Solicitud de Seguimiento            |
|  +-- CU-017: Ver Cantidad de Solicitudes Pendientes       |
+----------------------------------------------------------+
|       ITERACION 1: HISTORIAL Y DESHACER/REHACER          |
|  +-- CU-018: Ver Historial de Acciones                    |
|  +-- CU-019: Deshacer Ultima Accion (Undo)                |
|  +-- CU-020: Rehacer Accion (Redo)                        |
|  +-- CU-021: Limpiar Historial                            |
+----------------------------------------------------------+
|         ITERACION 2: CONSULTAS AVANZADAS                  |
|  +-- CU-022: Obtener Vecinos (Seguidos Directos)          |
|  +-- CU-023: Construir Arbol de Relaciones                |
|  +-- CU-024: Obtener Seguidores en Nivel N                |
|  +-- CU-025: Obtener Seguidores Ordenados por Scoring     |
|  +-- CU-026: Obtener Clientes en Cuarto Nivel (ABB)       |
|  +-- CU-027: Consultar Influencia por Scoring             |
+----------------------------------------------------------+
|     ITERACION 3: RELACIONES BIDIRECCIONALES (AMISTADES)  |
|  +-- CU-028: Agregar Amistad Bidireccional               |
|  +-- CU-029: Eliminar Amistad Bidireccional              |
|  +-- CU-030: Ver Lista de Amigos                         |
|  +-- CU-031: Verificar si Son Amigos                     |
|  +-- CU-032: Obtener Cantidad de Amigos                  |
+----------------------------------------------------------+
|         ITERACION 3: ANALISIS DE DISTANCIA               |
|  +-- CU-033: Calcular Distancia (BFS)                    |
|  +-- CU-034: Encontrar Camino Mas Corto                  |
|  +-- CU-035: Verificar Conectividad                      |
+----------------------------------------------------------+
|           SISTEMA: PERSISTENCIA Y DATOS                   |
|  +-- CU-036: Cargar Clientes desde JSON                   |
|  +-- CU-037: Guardar Cambios en JSON                      |
|  +-- CU-038: Validar Integridad de Datos                  |
+----------------------------------------------------------+
```

---

## ESPECIFICACION DETALLADA DE CASOS DE USO

---

# GESTION DE SESION

## **CU-001: Iniciar Sesion**

**Iteracion:** Previa (Sesion)
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
2. Sistema cierra la aplicacion
```

**Flujo Excepcional:**
```
1. Usuario ingresa valor invalido (negativo)
2. Sistema muestra error y pide reintentar
```

**Complejidad:** O(1)
**Clases Involucradas:** `Menu.java`, `Sesion.java`, `GestorClientes.java`

---

## **CU-002: Cerrar Sesion**

**Iteracion:** Previa (Sesion)
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario con sesion activa

**Flujo Normal:**
```
1. Usuario selecciona "Salir" del menu principal
2. Sistema guarda los cambios en JSON (O(N))
3. Sistema limpia la sesion (Singleton)
4. Sistema muestra mensaje "Hasta pronto!"
5. Usuario vuelve a pantalla de login
```

**Complejidad:** O(N)
**Clases Involucradas:** `Menu.java`, `Sesion.java`, `GestorClientes.java`, `PersistenciaClientes.java`

---

# ITERACION 1: CRUD BASICO

## **CU-003: Agregar Cliente**

**Iteracion:** 1
**Actor:** Sistema (automatico al cargar)
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

**Iteracion:** 1
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

**Iteracion:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Cliente autenticado

**Flujo Normal:**
```
1. Usuario ingresa nombre (ej: "Alice")
2. Sistema busca en indice Diccionario<Nombre> (O(1))
3. Sistema obtiene Cola<Cliente> de ese nombre
4. Para cada cliente encontrado:
   - Sistema muestra ID, nombre e influencia
5. Si hay resultados:
   - Usuario puede seleccionar ID para enviar solicitud
6. Si no hay:
   - Sistema muestra "Sin resultados"
```

**Complejidad:** O(1) busqueda + O(k) iteracion (k = cantidad de clientes con ese nombre)
**Clases Involucradas:** `GestorClientes.java`, `MenuSolicitudes.java`

---

## **CU-006: Buscar Clientes por Scoring**

**Iteracion:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Cliente autenticado

**Flujo Normal:**
```
1. Usuario ingresa valor de scoring (0-100)
2. Sistema construye ABB<Scoring, Cola<Cliente>> (lazy loading) - O(N log 101) una sola vez
3. Sistema busca en ABB por scoring (O(log 101))
4. Para cada cliente encontrado:
   - Sistema muestra ID, nombre
5. Si hay resultados:
   - Sistema muestra cantidad total
6. Si no hay:
   - Sistema muestra "Sin resultados"
```

**Complejidad:** O(log 101 + k) donde k = cantidad con ese scoring
**Clases Involucradas:** `GestorClientes.java`, `IndiceClientes.java`, `MenuSolicitudes.java`, `ArbolBinarioBusqueda.java`

---

## **CU-007: Listar Todos los Clientes**

**Iteracion:** 1
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
5. Usuario puede navegar (opcion 0 para volver)
```

**Complejidad:** O(N)
**Clases Involucradas:** `MenuSolicitudes.java`, `GestorClientes.java`

---

## **CU-008: Eliminar Cliente**

**Iteracion:** 1
**Actor:** Sistema (implicito)
**Precondiciones:** Cliente existe en base de datos

**Flujo Normal:**
```
1. Sistema identifica cliente a eliminar
2. Sistema remueve de Diccionario<ID> (O(1))
3. Sistema remueve de indice Diccionario<Nombre> (O(1))
4. Sistema remueve de ABB<Scoring> (O(log 101))
5. Sistema limpia referencias bidireccionales:
   - De los que lo seguian (O(seguidores))
   - A los que el seguia (O(siguiendo))
6. Sistema remueve de persistencia
```

**Complejidad:** O(seguidores + siguiendo) cascada bidireccional
**Clases Involucradas:** `GestorClientes.java`, `IndiceClientes.java`, `GestorRelaciones.java`, `Cliente.java`

---

# ITERACION 1: RELACIONES SIMPLES

## **CU-009: Seguir a un Usuario**

**Iteracion:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Cliente autenticado, usuario objetivo existe

**Flujo Normal:**
```
1. Usuario busca y selecciona otro usuario (CU-004, CU-005, CU-006)
2. Usuario selecciona "Enviar solicitud"
3. Sistema valida:
   - Origen != Destino (no auto-seguimiento)
   - No ya sigue (no duplicado)
   - Limite de 2 seguidos (restriccion de negocio)
4. Si valido:
   - Sistema agrega ID en array de "siguiendo" (O(1))
   - Sistema agrega origen en "seguidores" del objetivo (O(1))
   - Sistema registra accion en Historial
   - Sistema muestra confirmacion
5. Si invalido:
   - Sistema muestra error especifico
```

**Flujo Alternativo (Cola de Solicitudes):**
```
Ver CU-013: Enviar Solicitud de Seguimiento
```

**Complejidad:** O(1)
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`, `HistorialAcciones.java`

---

## **CU-010: Dejar de Seguir a un Usuario**

**Iteracion:** 1
**Actor:** Usuario Autenticado (implicito en undo)
**Precondiciones:** Usuario seguia a otro usuario

**Flujo Normal:**
```
1. Usuario ejecuta Undo de CU-009 (ver CU-019)
2. Sistema remueve ID del array "siguiendo" (O(1))
3. Sistema remueve origen del array "seguidores" (O(1))
4. Sistema registra accion inversa en Historial
5. Sistema muestra confirmacion
```

**Complejidad:** O(1)
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`, `HistorialAcciones.java`

---

## **CU-011: Ver Lista de Seguidos**

**Iteracion:** 1
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
   - Sistema muestra "No sigues a nadie aun"
5. Usuario puede navegar (opcion 0 para volver)
```

**Complejidad:** O(k) donde k = cantidad de seguidos (max 2)
**Clases Involucradas:** `MenuSolicitudes.java`, `GestorClientes.java`, `Cliente.java`

---

## **CU-012: Ver Lista de Seguidores**

**Iteracion:** 1
**Actor:** Usuario Autenticado (implicito, a traves de CU-022)
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

# ITERACION 1: SOLICITUDES DE SEGUIMIENTO

## **CU-013: Enviar Solicitud de Seguimiento**

**Iteracion:** 1
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
4. Si valido:
   - Sistema crea SolicitudSeguimiento (inmutable)
   - Sistema encola en Cola<SolicitudSeguimiento> del objetivo (O(1))
   - Sistema registra en Historial
   - Sistema muestra: "[OK] Solicitud enviada a @{nombre}"
5. Si invalido:
   - Sistema muestra error
```

**Complejidad:** O(1)
**Clases Involucradas:** `GestorClientes.java`, `Cliente.java`, `SolicitudSeguimiento.java`, `Cola.java`, `HistorialAcciones.java`

---

## **CU-014: Ver Solicitud Pendiente**

**Iteracion:** 1
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

**Iteracion:** 1
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

**Iteracion:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario acepto solicitud (CU-015)

**Flujo Normal:**
```
1. Usuario selecciona Undo (CU-019)
2. Sistema revierte la aceptacion de solicitud
3. Sistema remueve relacion bidireccional
4. Solicitud vuelve a cola (o se descarta)
5. Sistema muestra confirmacion
```

**Complejidad:** O(1)
**Clases Involucradas:** `HistorialAcciones.java`, `GestorRelaciones.java`

---

## **CU-017: Ver Cantidad de Solicitudes Pendientes**

**Iteracion:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario autenticado

**Flujo Normal:**
```
1. Sistema obtiene cantidad de solicitudes de la Cola (O(1))
2. Sistema muestra cantidad en:
   - Pantalla de login (CU-001)
   - Menu principal
3. Sistema actualiza en tiempo real despues de CU-015
```

**Complejidad:** O(1)
**Clases Involucradas:** `Cliente.java`, `Cola.java`, `Menu.java`

---

# ITERACION 1: HISTORIAL Y DESHACER/REHACER

## **CU-018: Ver Historial de Acciones**

**Iteracion:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario ha realizado al menos 1 accion

**Flujo Normal:**
```
1. Usuario selecciona "Historial"
2. Sistema obtiene lista de Acciones por sesion (O(1) acceso)
3. Para cada Accion en orden (mas reciente primero):
   - Sistema muestra:
     - Tipo de accion (AGREGAR, SEGUIR, ACEPTAR_SOLICITUD, etc)
     - Descripcion legible
     - Timestamp
4. Sistema muestra total de acciones en sesion
5. Usuario puede:
   - Ver detalles (Opcion especifica)
   - Deshacer (CU-019)
   - Rehacer (CU-020)
```

**Complejidad:** O(k) donde k = cantidad de acciones
**Clases Involucradas:** `MenuHistorial.java`, `HistorialAcciones.java`, `Accion.java`, `Sesion.java`

---

## **CU-019: Deshacer Ultima Accion (Undo)**

**Iteracion:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Al menos 1 accion en historial

**Flujo Normal:**
```
1. Usuario selecciona "Deshacer"
2. Sistema obtiene ultima Accion de Pila<Accion> (O(1))
3. Sistema ejecuta reversion segun tipo:

   a) Si accion = SEGUIR:
      - Sistema remueve relacion bidireccional
      - Sistema decrementa contadores

   b) Si accion = DEJAR_DE_SEGUIR:
      - Sistema restaura relacion bidireccional
      - Sistema incrementa contadores

4. Sistema mueve Accion a Pila<Redo>
5. Sistema muestra: "[OK] Accion deshecha"

Casos Especiales:
- Si pila Undo vacia: "No hay acciones para deshacer"
```

**Complejidad:** O(1) a O(N) segun tipo de accion
**Clases Involucradas:** `HistorialAcciones.java`, `Pila.java`, `GestorRelaciones.java`, `Sesion.java`

---

## **CU-020: Rehacer Accion (Redo)**

**Iteracion:** 1
**Actor:** Usuario Autenticado
**Precondiciones:** Al menos 1 accion en pila Redo

**Flujo Normal:**
```
1. Usuario selecciona "Rehacer"
2. Sistema obtiene Accion de Pila<Redo> (O(1))
3. Sistema re-ejecuta la Accion original
4. Sistema mueve Accion de vuelta a Pila<Undo>
5. Sistema muestra: "[OK] Accion rehecha"

Casos Especiales:
- Si pila Redo vacia: "No hay acciones para rehacer"
- Si usuario realiza nueva accion: pila Redo se limpia
```

**Complejidad:** O(1) a O(N) segun tipo de accion
**Clases Involucradas:** `HistorialAcciones.java`, `Pila.java`, `Sesion.java`

---

## **CU-021: Limpiar Historial**

**Iteracion:** 1 (Implicito)
**Actor:** Sistema
**Precondiciones:** Sesion se cierra

**Flujo Normal:**
```
1. Usuario cierra sesion (CU-002)
2. Sistema preserva historial de la sesion en Sesion (Singleton)
3. Usuario inicia nueva sesion (CU-001)
4. Sistema crea nuevo historial para nueva sesion
5. Historial anterior se descarta al cerrar aplicacion
```

**Complejidad:** O(k) donde k = cantidad de acciones
**Clases Involucradas:** `Sesion.java`, `HistorialAcciones.java`

---

# ITERACION 2: CONSULTAS AVANZADAS

## **CU-022: Obtener Vecinos (Seguidos Directos)**

**Iteracion:** 2
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

**Complejidad:** O(k) donde k = cantidad de seguidos (max 2)
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`

---

## **CU-023: Construir Arbol de Relaciones**

**Iteracion:** 2
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
   Altura ~ log(k) donde k = cantidad de seguidores
```

**Complejidad:** O(k log k) donde k = cantidad de seguidores
**Clases Involucradas:** `GestorRelaciones.java`, `ArbolBinarioBusqueda.java`, `Cliente.java`

---

## **CU-024: Obtener Seguidores en Nivel N**

**Iteracion:** 2
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario tiene seguidores, nivel >= 0

**Flujo Normal:**
```
1. Usuario especifica nivel (ej: nivel 3 = cuarto nivel, 0-indexed)
2. Sistema construye ABB de relaciones (CU-023)
3. Sistema recorre ABB para obtener nodos en nivel N (O(N) primera vez)
4. Sistema caching: nivel se calcula una sola vez
5. Para cada cliente en nivel:
   - Sistema muestra: ID, Nombre, Scoring, Cantidad de Seguidores
6. Sistema ordena por Seguidores descendente (Selection Sort, O(k^2))
7. Sistema muestra total de clientes en nivel

Caso especial: Cuarto nivel usado en CU-026
```

**Complejidad:** O(N) primera vez + caching O(1)
**Clases Involucradas:** `GestorRelaciones.java`, `MenuSolicitudes.java`, `ArbolBinarioBusqueda.java`

---

## **CU-025: Obtener Seguidores Ordenados por Scoring**

**Iteracion:** 2
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
5. Usuario puede ver seguidores mas influyentes primero
```

**Complejidad:** O(k log k) donde k = cantidad de seguidores
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`

---

## **CU-026: Obtener Clientes en Cuarto Nivel (ABB)**

**Iteracion:** 2
**Actor:** Usuario Autenticado
**Precondiciones:** Hay clientes en cuarto nivel del ABB global

**Flujo Normal:**
```
1. Usuario selecciona "Analisis ABB - Cuarto nivel"
2. Sistema obtiene ABB global de scoring (lazy loaded)
3. Sistema obtiene clientes en nivel 3 (cuarto nivel, 0-indexed) - O(N)
4. Para cada cliente:
   - Sistema muestra: ID, Nombre, Scoring, Cantidad de Seguidores
5. Sistema ordena por Seguidores descendente (Selection Sort)
6. Sistema destaca cliente con mayor influencia (mas seguidores)
7. Usuario puede navegar (opcion 0 para volver)

Nota: Sistema ABB es global y construido bajo demanda (lazy loading)
```

**Complejidad:** O(N) primera vez, O(k) iteraciones
**Clases Involucradas:** `MenuSolicitudes.java`, `GestorClientes.java`, `IndiceClientes.java`, `ArbolBinarioBusqueda.java`

---

## **CU-027: Consultar Influencia por Scoring**

**Iteracion:** 2
**Actor:** Usuario Autenticado
**Precondiciones:** Al menos 1 cliente con ese scoring

**Flujo Normal:**
```
1. Usuario selecciona "Buscar por Influencia"
2. Usuario ingresa valor de scoring (0-100)
3. Sistema construye ABB<Scoring, Cola<Cliente>> si no existe (lazy loading)
4. Sistema busca en ABB (O(log 101))
5. Para cada cliente encontrado:
   - Sistema muestra: ID, Nombre, Scoring
6. Sistema muestra cantidad total de resultados
7. Si no hay:
   - Sistema muestra "[AVISO] Sin resultados"
8. Usuario puede navegar
```

**Complejidad:** O(log 101 + k) donde k = cantidad con ese scoring
**Clases Involucradas:** `MenuSolicitudes.java`, `GestorClientes.java`, `IndiceClientes.java`, `ArbolBinarioBusqueda.java`

---

# ITERACION 3: RELACIONES BIDIRECCIONALES (AMISTADES)

## **CU-028: Agregar Amistad Bidireccional**

**Iteracion:** 3
**Actor:** Usuario Autenticado
**Precondiciones:** Ambos clientes existen, no son amigos, ninguno alcanzo MAX_AMIGOS

**Flujo Normal:**
```
1. Usuario selecciona "Agregar amistad"
2. Usuario ingresa ID del otro cliente
3. Sistema valida:
   - Origen != Destino (no amistad consigo mismo)
   - No ya son amigos (no duplicado)
   - Origen no alcanzo MAX_AMIGOS=2
   - Destino no alcanzo MAX_AMIGOS=2
4. Si valido:
   - Sistema agrega B en amistades de A (O(1))
   - Sistema agrega A en amistades de B (O(1))
   - Sistema muestra: "[OK] Ahora eres amigo de @{nombre} (bidireccional)"
5. Si invalido:
   - Si limite propio: "[ERROR] Alcanzaste el limite de 2 amigos"
   - Si limite del otro: "[ERROR] @{nombre} ya tiene 2 amigos"
```

**Regla de negocio:** MAX_AMIGOS=2. Ambos lados deben tener espacio. Si falla, ninguno se modifica.

**Complejidad:** O(1)
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`, `MenuSolicitudes.java`

---

## **CU-029: Eliminar Amistad Bidireccional**

**Iteracion:** 3
**Actor:** Usuario Autenticado
**Precondiciones:** Ambos clientes son amigos

**Flujo Normal:**
```
1. Usuario selecciona "Eliminar amistad"
2. Usuario ingresa ID del amigo a eliminar
3. Sistema valida que son amigos
4. Si valido:
   - Sistema elimina B de amistades de A (O(1))
   - Sistema elimina A de amistades de B (O(1))
   - Sistema muestra: "[OK] Amistad eliminada con @{nombre} (ambos lados)"
5. Si no son amigos:
   - Sistema muestra: "[AVISO] No son amigos"
```

**Complejidad:** O(1)
**Clases Involucradas:** `GestorRelaciones.java`, `Cliente.java`, `MenuSolicitudes.java`

---

## **CU-030: Ver Lista de Amigos**

**Iteracion:** 3
**Actor:** Usuario Autenticado
**Precondiciones:** Usuario autenticado

**Flujo Normal:**
```
1. Usuario selecciona "Amistades (bidireccionales)"
2. Sistema obtiene IDs de amigos del Diccionario de amistades (O(k))
3. Para cada ID:
   - Sistema busca Cliente por ID (O(1))
   - Sistema muestra: ID, Nombre, Scoring
4. Sistema muestra total de amigos
5. Si no tiene amigos:
   - Sistema muestra "[AVISO] Sin amistades"
```

**Complejidad:** O(k) donde k = cantidad de amigos (max 2)
**Clases Involucradas:** `MenuSolicitudes.java`, `GestorClientes.java`, `Cliente.java`

---

## **CU-031: Verificar si Son Amigos**

**Iteracion:** 3
**Actor:** Sistema
**Precondiciones:** Ambos clientes existen

**Flujo Normal:**
```
1. Sistema recibe IDs de dos clientes
2. Sistema busca en Diccionario de amistades de A si contiene B (O(1))
3. Retorna true/false
4. Propiedad: sonAmigos(A, B) == sonAmigos(B, A) (simetrica)
```

**Complejidad:** O(1)
**Clases Involucradas:** `Cliente.java`, `GestorRelaciones.java`

---

## **CU-032: Obtener Cantidad de Amigos**

**Iteracion:** 3
**Actor:** Sistema
**Precondiciones:** Cliente existe

**Flujo Normal:**
```
1. Sistema consulta cantidad de entradas en Diccionario de amistades (O(1))
2. Retorna entero (0 a MAX_AMIGOS)
3. Usado internamente para validar limite antes de agregar amistad
```

**Complejidad:** O(1)
**Clases Involucradas:** `Cliente.java`

---

# ITERACION 3: ANALISIS DE DISTANCIA

## **CU-033: Calcular Distancia (BFS)**

**Iteracion:** 3
**Actor:** Usuario Autenticado
**Precondiciones:** Ambos clientes existen

**Flujo Normal:**
```
1. Usuario selecciona "Calcular Distancia (BFS)"
2. Usuario ingresa ID origen (0 = su propio ID)
3. Usuario ingresa ID destino
4. Sistema valida que ambos existen
5. Sistema ejecuta BFS desde origen en grafo dirigido de seguimientos:
   - Usa Cola para recorrer por niveles
   - Usa Diccionario<ID, Boolean> como conjunto de visitados
   - Cuenta saltos hasta encontrar destino
6. Sistema muestra resultado:
   - Si mismo cliente: "0 saltos (mismo cliente)"
   - Si hay camino: "{N} salto(s)"
   - Si no hay camino: "No existe camino entre estos clientes"
7. Sistema muestra tiempo de BFS en ms
```

**Complejidad:** O(V+E) donde V = vertices, E = aristas
**Clases Involucradas:** `GestorRelaciones.java`, `MenuSolicitudes.java`, `Cola.java`, `Diccionario.java`

---

## **CU-034: Encontrar Camino Mas Corto**

**Iteracion:** 3
**Actor:** Sistema
**Precondiciones:** Origen y destino existen en el grafo

**Flujo Normal:**
```
1. Sistema recibe IDs de origen y destino
2. BFS garantiza camino mas corto en grafo no ponderado:
   - Primer nivel: vecinos directos (1 salto)
   - Segundo nivel: vecinos de vecinos (2 saltos)
   - Se detiene al encontrar destino
3. Si hay multiples caminos, BFS retorna el de menor saltos
4. Retorna distancia (int) o -1 si no hay camino
```

**Complejidad:** O(V+E)
**Clases Involucradas:** `GestorRelaciones.java`, `Cola.java`

---

## **CU-035: Verificar Conectividad**

**Iteracion:** 3
**Actor:** Sistema
**Precondiciones:** Grafo dirigido construido

**Flujo Normal:**
```
1. Sistema ejecuta BFS entre dos clientes (CU-033)
2. Si distancia >= 0: estan conectados
3. Si distancia == -1: no hay camino (componentes desconectadas)
4. Nota: el grafo es DIRIGIDO, por lo que A conectado a B
   no implica B conectado a A
```

**Complejidad:** O(V+E)
**Clases Involucradas:** `GestorRelaciones.java`

---

# SISTEMA: PERSISTENCIA Y DATOS

## **CU-036: Cargar Clientes desde JSON**

**Iteracion:** Todas
**Actor:** Sistema
**Precondiciones:** Aplicacion inicia, archivo JSON existe

**Flujo Normal:**
```
1. Sistema al iniciar llama a GestorClientes()
2. GestorClientes llama a PersistenciaClientes.cargarDesdeArchivo()
3. PersistenciaClientes abre archivo JSON
4. Para cada cliente en JSON:
   - Crea instancia de Cliente
   - Carga IDs de "siguiendo" -> array
   - Carga solicitudes -> Cola<SolicitudSeguimiento>
   - Carga IDs de "seguidores" -> array
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
      "seguidores": [4, 5]
    }
  ]
}

Validaciones:
- Si archivo no existe -> diccionario vacio
- Si JSON malformado -> manejo de excepciones
```

**Complejidad:** O(N)
**Clases Involucradas:** `PersistenciaClientes.java`, `GestorClientes.java`, `Cliente.java`, `Gson.java`

---

## **CU-037: Guardar Cambios en JSON**

**Iteracion:** Todas
**Actor:** Sistema
**Precondiciones:** Usuario cierra sesion (CU-002) o aplicacion termina

**Flujo Normal:**
```
1. Usuario selecciona Salir o aplicacion termina
2. Sistema llama a GestorClientes.guardarCambios()
3. GestorClientes llama a PersistenciaClientes.guardarCambios()
4. Para cada cliente en Diccionario:
   - Crea ClienteDTO con estado actual:
     * id, nombre, scoring
     * array "siguiendo"
     * array "solicitudes"
     * array "seguidores"
   - Serializa a JSON
5. PersistenciaClientes sobrescribe archivo JSON
6. Sistema muestra: "Datos guardados exitosamente"

Estrategia: "Carga Inicial / Guardado Final"
- Carga: O(N) al iniciar
- Ejecucion: O(1) en memoria
- Guardado: O(N) al terminar
-> Elimina latencia de I/O durante ejecucion
```

**Complejidad:** O(N)
**Clases Involucradas:** `PersistenciaClientes.java`, `GestorClientes.java`, `Cliente.java`, `Gson.java`

---

## **CU-038: Validar Integridad de Datos**

**Iteracion:** Todas
**Actor:** Sistema
**Precondiciones:** Datos cargados o despues de operacion

**Flujo Normal:**
```
1. Sistema valida despues de CU-036 (carga):
   - Todos los IDs en "siguiendo" existen en base
   - Todos los IDs en "seguidores" existen en base
   - Relaciones son bidireccionales (A sigue B <=> B tiene A en seguidores)
   - Cola de solicitudes valida

2. Sistema valida en tiempo de ejecucion:
   - Limite de 2 seguidos respetado
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

## RESUMEN: MATRIZ DE COBERTURA

| Caso de Uso | Iteracion | Actor | Complejidad | Testing |
|------------|-----------|-------|------------|---------|
| CU-001 | - | Usuario | O(1) | CU_001_IniciarSesion |
| CU-002 | - | Usuario | O(N) | CU_002_CerrarSesion |
| CU-003 | 1 | Sistema | O(N) | CU_003_AgregarCliente |
| CU-004 | 1 | Usuario | O(1) | CU_004_BuscarClientePorId |
| CU-005 | 1 | Usuario | O(1)+O(k) | CU_005_BuscarClientesPorNombre |
| CU-006 | 1 | Usuario | O(log 101+k) | CU_006_BuscarClientesPorScoring |
| CU-007 | 1 | Usuario | O(N) | CU_007_ListarTodosClientes |
| CU-008 | 1 | Sistema | O(seg+sig) | CU_008_EliminarCliente |
| CU-009 | 1 | Usuario | O(1) | CU_009_SeguidorUsuario |
| CU-010 | 1 | Usuario | O(1) | CU_010_DejarDeSeguir |
| CU-011 | 1 | Usuario | O(k) | CU_011_VerListaSeguidos |
| CU-012 | 1 | Usuario | O(k log k) | CU_012_VerListaSeguidores |
| CU-013 | 1 | Usuario | O(1) | CU_013_EnviarSolicitud |
| CU-014 | 1 | Usuario | O(1) | CU_014_VerSolicitudPendiente |
| CU-015 | 1 | Usuario | O(1) | CU_015_AceptarSolicitud |
| CU-016 | 1 | Usuario | O(1) | CU_016_RechazarSolicitud |
| CU-017 | 1 | Usuario | O(1) | CU_017_VerCantidadSolicitudes |
| CU-018 | 1 | Usuario | O(k) | CU_018_VerHistorial |
| CU-019 | 1 | Usuario | O(1)-O(N) | CU_019_DeshacerAccion |
| CU-020 | 1 | Usuario | O(1)-O(N) | CU_020_ReHacerAccion |
| CU-021 | 1 | Sistema | O(k) | CU_021_LimpiarHistorial |
| CU-022 | 2 | Sistema | O(k) | CU_022_ObtenerVecinos |
| CU-023 | 2 | Sistema | O(k log k) | CU_023_ConstruirArbolRelaciones |
| CU-024 | 2 | Usuario | O(N) | CU_024_ObtenerSeguidoresEnNivel |
| CU-025 | 2 | Sistema | O(k log k) | CU_025_ObtenerSeguidoresOrdenados |
| CU-026 | 2 | Usuario | O(N) | CU_026_ObtenerClientesCuartoNivel |
| CU-027 | 2 | Usuario | O(log 101+k) | CU_027_ConsultarInfluenciaPorScoring |
| CU-028 | 3 | Usuario | O(1) | CU_028_AgregarAmistadBidireccional |
| CU-029 | 3 | Usuario | O(1) | CU_029_EliminarAmistadBidireccional |
| CU-030 | 3 | Usuario | O(k) | CU_030_VerListaAmigos |
| CU-031 | 3 | Sistema | O(1) | CU_031_VerificarSiSonAmigos |
| CU-032 | 3 | Sistema | O(1) | CU_032_ObtenerCantidadAmigos |
| CU-033 | 3 | Usuario | O(V+E) | CU_033_CalcularDistancia |
| CU-034 | 3 | Sistema | O(V+E) | CU_034_EncontrarCaminoCorto |
| CU-035 | 3 | Sistema | O(V+E) | CU_035_VerificarConectividad |
| CU-036 | Todas | Sistema | O(N) | CU_036_CargarClientesDesdeJson |
| CU-037 | Todas | Sistema | O(N) | CU_037_GuardarCambiosEnJson |
| CU-038 | Todas | Sistema | O(N+E) | CU_038_ValidarIntegridadDatos |

**TOTAL: 38 Casos de Uso**

---

# TESTING: GUIA DE VALIDACION DE CASOS DE USO

## Suite de Tests

La carpeta `test/casos_de_uso/` contiene **tests de integracion** para validar los **38 casos de uso** del sistema:

```
test/casos_de_uso/
+-- CasosDeUsoTests.java           # Coordinador maestro (38 tests)
+-- CU_001_IniciarSesion.java      # 4 tests
+-- CU_002_CerrarSesion.java       # 3 tests
+-- CU_003_AgregarCliente.java     # 5 tests
+-- CU_004_BuscarClientePorId.java # 4 tests
+-- CU_005_BuscarClientesPorNombre.java   # 4 tests
+-- CU_006_BuscarClientesPorScoring.java  # 4 tests
+-- CU_007_ListarTodosClientes.java       # 3 tests
+-- CU_008_EliminarCliente.java           # 3 tests
+-- CU_009_SeguidorUsuario.java           # 6 tests (incluye seguidores ilimitados)
+-- CU_010_DejarDeSeguir.java             # 3 tests
+-- CU_011_VerListaSeguidos.java          # 2 tests
+-- CU_012_VerListaSeguidores.java        # 1 test
+-- CU_013_EnviarSolicitud.java           # 4 tests
+-- CU_014_VerSolicitudPendiente.java     # 1 test
+-- CU_015_AceptarSolicitud.java          # 1 test
+-- CU_016_RechazarSolicitud.java         # 1 test
+-- CU_017_VerCantidadSolicitudes.java    # 1 test
+-- CU_018_VerHistorial.java              # 1 test
+-- CU_019_DeshacerAccion.java            # 1 test
+-- CU_020_ReHacerAccion.java             # 1 test
+-- CU_021_LimpiarHistorial.java          # 1 test
+-- CU_022_ObtenerVecinos.java            # 1 test
+-- CU_023_ConstruirArbolRelaciones.java  # 1 test
+-- CU_024_ObtenerSeguidoresEnNivel.java  # 1 test
+-- CU_025_ObtenerSeguidoresOrdenados.java # 1 test
+-- CU_026_ObtenerClientesCuartoNivel.java # 1 test
+-- CU_027_ConsultarInfluenciaPorScoring.java # 1 test
+-- CU_028_AgregarAmistadBidireccional.java   # 8 tests (incluye MAX_AMIGOS)
+-- CU_029_EliminarAmistadBidireccional.java  # 3 tests
+-- CU_030_VerListaAmigos.java                # 3 tests
+-- CU_031_VerificarSiSonAmigos.java          # 3 tests
+-- CU_032_ObtenerCantidadAmigos.java         # 3 tests (incluye limite MAX_AMIGOS)
+-- CU_033_CalcularDistancia.java             # 5 tests
+-- CU_034_EncontrarCaminoCorto.java          # 3 tests
+-- CU_035_VerificarConectividad.java         # 3 tests
+-- CU_036_CargarClientesDesdeJson.java       # 1 test
+-- CU_037_GuardarCambiosEnJson.java          # 1 test
+-- CU_038_ValidarIntegridadDatos.java        # 1 test
|
+-- edge_cases/
|   +-- EdgeCaseTests.java         # Coordinador (5 edge case suites)
|   +-- EC_001_ValoresNull.java    # 3 tests
|   +-- EC_002_ValoresVacios.java  # 3 tests
|   +-- EC_003_ValoresNegativos.java # 2 tests
|   +-- EC_004_LimitesScoring.java # 4 tests
|   +-- EC_005_LimitesRelaciones.java # 9 tests (seguidos, seguidores, amigos)
|
+-- performance/
    +-- PerformanceTests.java      # Coordinador (5 performance suites)
    +-- PERF_001_BusquedaPorIdO1.java
    +-- PERF_002_ListarTodosON.java
    +-- PERF_003_BusquedaABBOlogN.java
    +-- PERF_004_DistanciaBFS.java
    +-- PERF_005_EscalabilidadMuchosClientes.java
```


## Categoria de CU

### CRUD (CU-003 a CU-008):
- Operacion exitosa
- Operacion fallida (invalido)
- Validaciones aplicadas
- Estado posterior a operacion
- Complejidad esperada

### Relaciones dirigidas (CU-009 a CU-012, CU-022 a CU-027):
- Crear relacion valida
- Validaciones (no auto, no duplicados)
- Bidireccionalidad (siguiendo/seguidores)
- Limite MAX_SEGUIDOS=2
- Seguidores ilimitados (sin tope)
- Operaciones inversas

### Amistades bidireccionales (CU-028 a CU-032):
- Agregar/eliminar amistad
- Bidireccionalidad (A amigo de B <=> B amigo de A)
- Limite MAX_AMIGOS=2 (ambos lados)
- Fallo atomico (si uno esta lleno, ninguno se modifica)
- Liberar cupo al eliminar

### Distancia BFS (CU-033 a CU-035):
- Camino directo e indirecto
- Sin camino (componentes desconectadas)
- Grafo dirigido (A->B no implica B->A)
- Cliente inexistente

### Solicitudes (CU-013 a CU-017):
- Enviar solicitud valida
- Encolamiento correcto (FIFO)
- Procesamiento de solicitudes
- Multiples solicitudes
- Rechazo/Aceptacion

### Historial (CU-018 a CU-021):
- Ver historial completo
- Undo funciona correctamente
- Redo funciona correctamente
- Cascada de cambios se revierte

### Persistencia (CU-036 a CU-038):
- Carga desde JSON correcta
- Guardado en JSON correcto
- Validaciones de integridad
- Round-trip amistades + seguimientos

## Reglas de Negocio Validadas por Tests

| Regla | Constante | Test |
|-------|-----------|------|
| Maximo seguidos por cliente | `MAX_SEGUIDOS=2` | CU_009, EC_005 |
| Seguidores sin limite | (sin tope) | CU_009, EC_005 |
| Maximo amigos por cliente | `MAX_AMIGOS=2` | CU_028, CU_032, EC_005 |
| Limite bidireccional (ambos lados) | `MAX_AMIGOS=2` | CU_028, EC_005 |
| Liberar cupo al eliminar | - | EC_005 |
| Fallo no corrompe estado | - | CU_028, EC_005 |

## Principios de Testing

1. **Cada test es independiente**: Usa su propio archivo TEST_DB
2. **Assertions con mensaje**: Siempre incluir mensaje descriptivo de error
3. **Cleanup automatico**: Los archivos TEST_DB se crean/sobreescriben en cada test
4. **O(1) amortizado**: Algunos tests validan complejidad midiendo tiempos
5. **No dependen de UI**: Son tests puro de logica, sin Scanner ni consola

## Cobertura de Tests

| Categoria | Suites | Tests individuales | Descripcion |
|-----------|--------|-------------------|-------------|
| **Casos de Uso** | 38 | 87 | Funcionalidad de cada CU |
| **Edge Cases** | 5 | 21 | Null, vacio, negativos, scoring, relaciones |
| **Performance** | 5 | 6 | Validacion de complejidades Big O |
| **TOTAL** | **48** | **114** | Tests completamente funcionales |

---
