# Documento de Diseño - TPO Algoritmos y Estructuras de Datos II


## 1. Descripción del Sistema

Plataforma de gestión de clientes en una red social con:

- Administración de clientes con scoring de influencia
- Relaciones de seguimiento entre clientes (sin límite)
- Conexiones generales (amistades) entre clientes
- Historial de acciones con undo/redo
- Procesamiento de solicitudes de seguimiento en orden de llegada

---

## 2. Casos de Uso

### 2.1 Iteración 1 - Gestión Básica de Clientes

#### CU-01: Agregar Cliente
**Actor:** Usuario  
**Precondiciones:** Ninguna  
**Flujo Principal:**
1. Usuario ingresa nombre y scoring (0-100)
2. Sistema valida que el nombre no esté vacío
3. Sistema valida que el scoring esté en rango [0, 100]
4. Sistema verifica que el nombre no exista (búsqueda case-insensitive)
5. Sistema crea el cliente y lo almacena en el diccionario
6. Sistema registra la acción en el historial

**Postcondiciones:** Cliente agregado al sistema  
**Flujos Alternativos:**
- 2a. Nombre vacío → rechazar operación
- 3a. Scoring fuera de rango → rechazar operación
- 4a. Nombre duplicado → rechazar operación

---

#### CU-02: Buscar Cliente por Nombre
**Actor:** Usuario  
**Precondiciones:** Ninguna  
**Flujo Principal:**
1. Usuario ingresa nombre a buscar
2. Sistema normaliza el nombre a minúsculas
3. Sistema busca en el diccionario (O(1))
4. Sistema muestra datos del cliente (nombre, scoring, seguidos)

**Postcondiciones:** Cliente mostrado o mensaje de no encontrado  
**Flujos Alternativos:**
- 3a. Cliente no existe → mostrar mensaje "No encontrado"

---

#### CU-03: Buscar Clientes por Scoring
**Actor:** Usuario  
**Precondiciones:** Ninguna  
**Flujo Principal:**
1. Usuario ingresa scoring a buscar (0-100)
2. Sistema valida que el scoring esté en rango
3. Sistema recorre todos los clientes (O(n))
4. Sistema filtra clientes con scoring exacto
5. Sistema muestra lista de clientes encontrados

**Postcondiciones:** Lista de clientes mostrada  
**Flujos Alternativos:**
- 2a. Scoring fuera de rango → rechazar operación
- 4a. Ningún cliente con ese scoring → mostrar lista vacía

---

#### CU-04: Eliminar Cliente
**Actor:** Usuario  
**Precondiciones:** Cliente existe  
**Flujo Principal:**
1. Usuario ingresa nombre del cliente a eliminar
2. Sistema busca el cliente (case-insensitive)
3. Sistema guarda estado completo (scoring, seguidos) para undo
4. Sistema elimina el cliente del diccionario
5. Sistema limpia referencias en otros clientes (cascada)
6. Sistema registra la acción en el historial

**Postcondiciones:** Cliente eliminado y referencias limpiadas  
**Flujos Alternativos:**
- 2a. Cliente no existe → rechazar operación

---

#### CU-05: Deshacer Última Acción
**Actor:** Usuario  
**Precondiciones:** Historial no vacío  
**Flujo Principal:**
1. Usuario solicita deshacer
2. Sistema extrae última acción del historial
3. Sistema ejecuta reversión según tipo de acción:
   - AGREGAR_CLIENTE → eliminar cliente
   - ELIMINAR_CLIENTE → restaurar cliente con estado guardado
   - SEGUIR → dejar de seguir
   - DEJAR_DE_SEGUIR → volver a seguir
4. Sistema muestra acción deshecha

**Postcondiciones:** Acción revertida  
**Flujos Alternativos:**
- 1a. Historial vacío → mostrar mensaje "No hay acciones para deshacer"

---

#### CU-06: Ver Historial Completo
**Actor:** Usuario  
**Precondiciones:** Ninguna  
**Flujo Principal:**
1. Usuario solicita ver historial
2. Sistema obtiene todas las acciones de la pila
3. Sistema muestra cada acción con:
   - Tipo de acción
   - Datos involucrados
   - Timestamp
4. Sistema muestra cantidad total de acciones

**Postcondiciones:** Historial mostrado  
**Flujos Alternativos:**
- 2a. Historial vacío → mostrar mensaje "Sin acciones registradas"

---

#### CU-07: Agregar Solicitud de Seguimiento
**Actor:** Usuario  
**Precondiciones:** Ambos clientes existen  
**Flujo Principal:**
1. Usuario ingresa solicitante y objetivo
2. Sistema valida que ambos clientes existan
3. Sistema valida que solicitante ≠ objetivo
4. Sistema verifica que no exista solicitud duplicada (O(1))
5. Sistema encola la solicitud
6. Sistema registra en conjunto auxiliar

**Postcondiciones:** Solicitud agregada a la cola  
**Flujos Alternativos:**
- 2a. Cliente inexistente → rechazar operación
- 3a. Solicitante = objetivo → rechazar operación
- 4a. Solicitud duplicada → rechazar operación

---

#### CU-08: Procesar Solicitud de Seguimiento
**Actor:** Usuario  
**Precondiciones:** Cola no vacía  
**Flujo Principal:**
1. Usuario solicita procesar siguiente
2. Sistema desencola solicitud (FIFO)
3. Sistema establece relación de seguimiento
4. Sistema registra acción en historial
5. Sistema muestra resultado

**Postcondiciones:** Solicitud procesada y relación establecida  
**Flujos Alternativos:**
- 1a. Cola vacía → mostrar mensaje "No hay solicitudes pendientes"

---

### 2.2 Iteración 2 - Relaciones de Seguimiento (PENDIENTE)

#### CU-09: Seguir Cliente
**Actor:** Usuario  
**Precondiciones:** Ambos clientes existen  
**Flujo Principal:**
1. Usuario ingresa solicitante y objetivo
2. Sistema valida existencia de ambos
3. Sistema valida que solicitante ≠ objetivo
4. Sistema verifica que no lo siga ya
5. Sistema agrega objetivo al diccionario de seguidos
6. Sistema registra acción en historial

**Postcondiciones:** Relación de seguimiento establecida  
**Flujos Alternativos:**
- 2a. Cliente inexistente → rechazar
- 3a. Solicitante = objetivo → rechazar
- 4a. Ya lo sigue → rechazar

---

#### CU-10: Dejar de Seguir
**Actor:** Usuario  
**Precondiciones:** Relación de seguimiento existe  
**Flujo Principal:**
1. Usuario ingresa solicitante y objetivo
2. Sistema verifica que la relación exista
3. Sistema elimina objetivo del array de seguidos
4. Sistema registra acción en historial

**Postcondiciones:** Relación eliminada  
**Flujos Alternativos:**
- 2a. Relación no existe → rechazar

---

#### CU-11: Ver Clientes en Nivel K del ABB
**Actor:** Usuario  
**Precondiciones:** ABB construido  
**Flujo Principal:**
1. Usuario ingresa nivel K (ej: 4)
2. Sistema ejecuta BFS con cola auxiliar
3. Sistema recolecta nodos en nivel K
4. Sistema muestra clientes de esos nodos

**Postcondiciones:** Clientes del nivel K mostrados  
**Flujos Alternativos:**
- 2a. Árbol vacío → mostrar lista vacía
- 2b. Árbol con < K niveles → mostrar lista vacía

---

### 2.3 Iteración 3 - Conexiones Generales (PENDIENTE)

#### CU-12: Agregar Conexión
**Actor:** Usuario  
**Precondiciones:** Ambos clientes existen  
**Flujo Principal:**
1. Usuario ingresa cliente1 y cliente2
2. Sistema valida que ambos existan
3. Sistema valida que cliente1 ≠ cliente2
4. Sistema agrega arista bidireccional en el grafo
5. Sistema registra acción en historial

**Postcondiciones:** Conexión establecida  
**Flujos Alternativos:**
- 2a. Cliente inexistente → rechazar
- 3a. Cliente1 = cliente2 → rechazar
- 4a. Conexión ya existe → ignorar (idempotente)

---

#### CU-13: Eliminar Conexión
**Actor:** Usuario  
**Precondiciones:** Conexión existe  
**Flujo Principal:**
1. Usuario ingresa cliente1 y cliente2
2. Sistema verifica que la conexión exista
3. Sistema elimina arista bidireccional del grafo
4. Sistema registra acción en historial

**Postcondiciones:** Conexión eliminada  
**Flujos Alternativos:**
- 2a. Conexión no existe → ignorar silenciosamente

---

#### CU-14: Calcular Distancia entre Clientes
**Actor:** Usuario  
**Precondiciones:** Ambos clientes existen  
**Flujo Principal:**
1. Usuario ingresa origen y destino
2. Sistema valida que ambos existan
3. Sistema ejecuta BFS desde origen
4. Sistema calcula distancia mínima
5. Sistema muestra resultado

**Postcondiciones:** Distancia mostrada  
**Flujos Alternativos:**
- 2a. Cliente inexistente → retornar -1
- 3a. Origen = destino → retornar 0
- 4a. No hay camino → retornar -1

---

#### CU-15: Ver Conexiones de un Cliente
**Actor:** Usuario  
**Precondiciones:** Cliente existe  
**Flujo Principal:**
1. Usuario ingresa nombre del cliente
2. Sistema busca el cliente
3. Sistema obtiene conjunto de vecinos del grafo (O(1))
4. Sistema muestra lista de conexiones

**Postcondiciones:** Conexiones mostradas  
**Flujos Alternativos:**
- 2a. Cliente no existe → rechazar
- 3a. Cliente sin conexiones → mostrar lista vacía

---

### 2.4 Casos de Uso Transversales

#### CU-16: Cargar Datos desde JSON
**Actor:** Sistema  
**Precondiciones:** Archivo JSON válido  
**Flujo Principal:**
1. Sistema lee archivo `data/clientes.json`
2. Sistema parsea JSON
3. Para cada cliente en el JSON:
   - Crear cliente con nombre y scoring
   - Agregar al diccionario
4. Sistema muestra cantidad de clientes cargados

**Postcondiciones:** Datos cargados en memoria  
**Flujos Alternativos:**
- 1a. Archivo no existe → continuar con sistema vacío
- 2a. JSON inválido → mostrar error y terminar

---

#### CU-17: Listar Todos los Clientes
**Actor:** Usuario  
**Precondiciones:** Ninguna  
**Flujo Principal:**
1. Usuario solicita listar
2. Sistema obtiene todos los valores del diccionario (O(n))
3. Sistema muestra cada cliente con:
   - Nombre
   - Scoring
   - Cantidad de seguidos
4. Sistema muestra total de clientes

**Postcondiciones:** Lista mostrada  
**Flujos Alternativos:**
- 2a. Sistema vacío → mostrar mensaje "No hay clientes"

---


## 2. Cronograma de Entregas

| Iteración | Fecha | Alcance |
|-----------|-------|---------|
| 1 | 05/02/2026 | Gestión de clientes (Optimizado O(1)), historial, cola |
| 2 | 24/02/2026 | PENDIENTE: Relaciones de seguimiento, ABB ordenado por scoring |
| 3 | 24/02/2026 | Grafo de conexiones generales, cálculo de distancias |

---

## 3. Modelo de Datos

### 3.1 Cliente

| Atributo | Tipo | Restricciones |
|----------|------|---------------|
| id | int | Mayor a 0, único |
| nombre | String | No nulo, no vacío |
| scoring | int | Rango [0, 100] |
| siguiendo | Diccionario<Integer, Boolean> | Sin duplicados, sin auto-referencia |

**Validaciones:**
- ID <= 0 → rechazar operación
- Nombre nulo/vacío → rechazar operación
- Scoring fuera de rango → rechazar operación
- Intentar seguirse a sí mismo → rechazar operación

### 3.2 Accion

| Atributo | Tipo | Descripción |
|----------|------|-------------|
| tipo | TipoAccion | Tipo de operación realizada |
| datos | String[] | Estado completo para ejecutar/revertir |
| timestamp | LocalDateTime | Fecha y hora de ejecución |

### 3.3 TipoAccion (Enum)

| Valor | Datos almacenados | Reversión |
|-------|-------------------|-----------|
| AGREGAR_CLIENTE | [nombre, scoring] | Eliminar cliente |
| ELIMINAR_CLIENTE | [nombre, scoring, JSON_seguidos, JSON_conexiones] | Reinsertar con relaciones |
| SEGUIR | [solicitante, objetivo] | Dejar de seguir |
| DEJAR_DE_SEGUIR | [solicitante, objetivo] | Volver a seguir |
| AGREGAR_CONEXION | [cliente1, cliente2] | Eliminar conexión |
| ELIMINAR_CONEXION | [cliente1, cliente2] | Agregar conexión |

### 3.4 SolicitudSeguimiento

| Atributo | Tipo | Restricciones |
|----------|------|---------------|
| solicitante | String | Cliente existente, ≠ objetivo |
| objetivo | String | Cliente existente |

---

## 4. Estructuras de Datos

### 4.1 Iteración 1

#### Diccionario de Clientes (Lista Asociativa)

**Estructura:** Diccionario<String, Cliente> implementado como lista enlazada de pares (clave, valor).  
**Clave:** nombre

| Operación | Tiempo |
|-----------|--------|
| Insertar | O(1) |
| Buscar por nombre | O(1) |
| Eliminar | O(1) |

##### Decisión de Diseño: Lista Asociativa vs Hash Table

**Alternativas evaluadas:**

1. **Hash Table (capacidad fija)**: O(1) - requiere tamaño predefinido
2. **Hash Table (con rehashing)**: O(1) - dinámico pero implementación compleja
3. **Lista Asociativa**: O(n) - totalmente dinámica, implementación simple

**Elección ACTUAL: Hash Table (O(1))**

**Justificación del cambio (Optimización):**
Originalmente se planeó Lista Asociativa por simplicidad, pero se detectaron cuellos de botella O(n²) en operaciones críticas. Se migró a Hash Table para garantizar O(1) en operaciones base y reducir la complejidad sistémica.

1. **Eficiencia**: Búsqueda, inserción y eliminación en O(1).
2. **Escalabilidad**: Soporta mayor volumen de datos sin degradación lineal.

**Trade-off aceptado:**

| Métrica | Hash Table | Lista Asociativa |
|---------|------------|------------------|
| Insertar | **O(1)** | O(n) |
| Buscar | **O(1)** | O(n) |
| Complejidad código | Media | Baja |
| Escalabilidad | Alta | Baja |

Se priorizó el rendimiento (O(1)) sobre la simplicidad inicial.

**Casos borde:**
- Insertar duplicado → verificar recorriendo lista, rechazar si existe
- Eliminar inexistente → recorrer lista, retornar null si no existe
- Modificar scoring → eliminar del ABB en posición vieja, reinsertar en nueva

#### Sistema Undo/Redo (Patrón Command)

**Estructura:** Dos pilas

```
Pila<Accion> historial    // acciones ejecutadas
Pila<Accion> rehacer      // acciones deshechas
```

| Operación | Algoritmo | Tiempo |
|-----------|-----------|--------|
| Ejecutar | push(historial), vaciar(rehacer) | O(1) |
| Undo | pop(historial) → revertir → push(rehacer) | O(1) |
| Redo | pop(rehacer) → ejecutar → push(historial) | O(1) |

Sin límite de historial. Al ejecutar nueva acción, se vacía la pila de redo.

**Casos borde:**
- Undo con historial vacío → verificar `!historial.estaVacia()` antes de pop
- Redo con pila vacía → verificar `!rehacer.estaVacia()` antes de pop
- Undo de eliminación cuando cliente fue recreado → verificar inexistencia antes de restaurar

#### Cola de Solicitudes

**Estructuras:**
- Cola<SolicitudSeguimiento> para procesamiento FIFO
- Conjunto<String> auxiliar con claves "solicitante:objetivo" para verificación O(n)

| Operación | Tiempo |
|-----------|--------|
| Agregar (enqueue) | O(1) |
| Procesar (dequeue) | O(1) |
| Verificar duplicado | O(1) |

**Nota:** El Conjunto usa internamente Hash Table, por lo que es O(1).

**Casos borde:**
- Solicitud duplicada → verificar en Conjunto antes de encolar, rechazar si existe
- Solicitud a sí mismo → verificar `solicitante ≠ objetivo`, rechazar
- Solicitud a cliente inexistente → verificar existencia en Diccionario, rechazar
- Procesar cola vacía → verificar `!cola.estaVacia()`, retornar null/error
- Procesar cuando solicitante ya sigue a 2 → descartar solicitud, notificar
- Cliente eliminado con solicitudes pendientes → cascada elimina sus solicitudes


---

### 4.2 Iteración 2

#### ABB de Clientes por Scoring

**Estructura:** ABB donde cada nodo contiene Lista<Cliente>  
**Clave del nodo:** scoring (int)  
**Valor del nodo:** Lista de clientes con ese scoring

```
NodoABB {
    int scoring
    Lista<Cliente> clientes
    NodoABB izq, der
}
```

| Operación | Tiempo |
|-----------|--------|
| Insertar | O(log n) + O(1) agregar a lista |
| Buscar por scoring exacto | O(log n) + O(k) donde k = clientes con ese score |
| Buscar por rango | O(log n + m) donde m = nodos en rango |
| Recorrido inorden | O(n) |
| Eliminar cliente | O(log n) + O(k) buscar en lista |

**Justificación de Lista por nodo:**
1. Maneja naturalmente clientes con mismo scoring
2. Permite obtener "todos con scoring X" directamente
3. Simplifica inserción/eliminación
4. Costo O(k) despreciable si pocos comparten scoring

**Justificación de ABB sobre AVL:**
1. Enunciado especifica "ABB visto en clase"
2. Datos del JSON desordenados → no degrada a O(n)
3. Menor complejidad de implementación
4. Suficiente para volumen del TP

**Casos borde:**
- Árbol vacío → raíz null, todas las operaciones verifican antes
- Buscar nivel K en árbol con < K niveles → retornar lista vacía
- Todos los clientes con mismo scoring → un nodo con lista grande, O(n) en ese nodo
- Eliminar único cliente de un nodo → si lista queda vacía, eliminar nodo del árbol

#### Seguidos por Cliente

**Estructura:** Array estático [2] dentro de Cliente

| Operación | Tiempo |
|-----------|--------|
| Agregar seguido | O(1) |
| Consultar seguidos | O(1) |
| Eliminar seguido | O(1) |

**Casos borde:**
- Seguir al mismo cliente dos veces → verificar no existencia en array antes
- Seguir cuando array lleno → verificar `count < 2`, rechazar si lleno
- Dejar de seguir a quien no sigue → verificar existencia, retornar false

#### Obtención de Nivel K

**Algoritmo:** BFS con cola auxiliar

```
obtenerNivel(arbol, k):
    si raiz == null: retornar []
    
    cola ← nueva Cola()
    cola.encolar((raiz, 0))
    resultado ← []
    
    mientras no cola.vacia():
        (nodo, nivel) ← cola.desencolar()
        
        si nivel == k:
            resultado.agregar(nodo)
        sino si nivel < k:
            si nodo.izq ≠ null: cola.encolar((nodo.izq, nivel+1))
            si nodo.der ≠ null: cola.encolar((nodo.der, nivel+1))
    
    retornar resultado
```

Complejidad: O(n) tiempo, O(n) espacio

---

### 4.3 Iteración 3

#### Grafo de Conexiones

**Estructura:** Grafo no dirigido con lista de adyacencia  
**Representación:** Diccionario<String, Conjunto<String>>

| Operación | Tiempo |
|-----------|--------|
| Agregar vértice | O(1) |
| Agregar arista | O(1) |
| Obtener vecinos | O(1) |
| Verificar adyacencia | O(1) |

Invariante: ∀ arista (A,B): B ∈ adyacencias[A] ∧ A ∈ adyacencias[B]

**Casos borde:**
- Conexión a sí mismo → verificar `cliente1 ≠ cliente2`, rechazar
- Conexión duplicada → Conjunto ignora automáticamente (no error)
- Conexión a cliente inexistente → verificar existencia, rechazar
- Cliente sin conexiones → `vecinos()` retorna Conjunto vacío (válido)
- Eliminar conexión inexistente → verificar existencia, ignorar silenciosamente
- Eliminar vértice → eliminar de todas las listas de adyacencia (cascada)

#### Distancia entre Clientes

**Algoritmo:** BFS

```
distancia(grafo, origen, destino):
    si origen == destino: retornar 0
    si !grafo.contiene(origen) OR !grafo.contiene(destino): retornar -1
    
    visitados ← nuevo Conjunto()
    cola ← nueva Cola()
    cola.encolar((origen, 0))
    visitados.agregar(origen)
    
    mientras no cola.vacia():
        (actual, d) ← cola.desencolar()
        
        para cada vecino en grafo.vecinos(actual):
            si vecino == destino: retornar d + 1
            si vecino ∉ visitados:
                visitados.agregar(vecino)
                cola.encolar((vecino, d + 1))
    
    retornar -1
```

Complejidad: O(V + E) tiempo, O(V) espacio

**Casos borde:**
- Distancia a sí mismo → retornar 0 inmediatamente
- Cliente inexistente → retornar -1
- Clientes desconectados → BFS termina sin encontrar, retornar -1

---

## 5. Reglas de Negocio

### 5.1 Eliminación de Cliente (Cascada)

Al eliminar un cliente:
1. Eliminar del Diccionario
2. Eliminar del ABB
3. Eliminar todas sus conexiones del Grafo
4. Eliminar de arrays `siguiendo` de otros clientes
5. Eliminar solicitudes donde participa

**Justificación:**
- Comportamiento esperado en redes sociales
- Evita referencias huérfanas
- Undo revierte completamente (datos[] almacena estado completo)

**Flujo:**
```
eliminarCliente(nombre):
    si !diccionario.contiene(nombre): retornar ERROR
    
    cliente ← diccionario.obtener(nombre)
    
    // Guardar estado para undo
    seguidos ← serializar(cliente.siguiendo)
    conexiones ← serializar(grafo.vecinos(nombre))
    seguidores ← obtenerSeguidores(nombre)  // quiénes lo siguen
    
    // Eliminar de estructuras
    diccionario.eliminar(nombre)
    abb.eliminar(cliente)
    grafo.eliminarVertice(nombre)
    
    // Limpiar referencias entrantes
    para cada c en diccionario.valores():
        c.siguiendo.remover(cliente)
    
    // Limpiar solicitudes
    conjunto.removerSi(s → s.contiene(nombre))
    cola.removerSi(s → s.solicitante == nombre OR s.objetivo == nombre)
    
    // Registrar acción
    historial.push(Accion(ELIMINAR_CLIENTE, [nombre, scoring, seguidos, conexiones, seguidores]))
    rehacer.vaciar()
```

### 5.2 Solicitudes de Seguimiento

- No duplicadas (Conjunto auxiliar)
- Al procesar: validar límite de 2 seguidos
- Si falla validación: descartar solicitud

### 5.3 Sincronización de Estructuras

Al modificar cliente:
1. Diccionario (siempre)
2. ABB si cambió scoring
3. Grafo si cambió conexiones

---

## 6. Interfaz de Usuario

### Menú Principal

```
══════════════════════════════════════════
       SISTEMA DE GESTIÓN DE CLIENTES
══════════════════════════════════════════
 1. Clientes
 2. Historial
 3. Solicitudes
 4. Seguimientos
 5. Conexiones
 6. Cargar JSON
 0. Salir
──────────────────────────────────────────
Opción: _
```

### Submenús

**Clientes:** Agregar, Buscar por nombre, Buscar por scoring, Listar, Eliminar

**Historial:** Ver última, Deshacer, Rehacer, Ver completo

**Solicitudes:** Crear, Procesar siguiente, Ver pendientes

**Seguimientos:** Ver seguidos, Ver árbol, Ver nivel 4

**Conexiones:** Agregar, Ver de cliente, Distancia, Eliminar

---

## 7. Estructura del Proyecto

```
TPO_AyED_II/
├── src/
│   ├── tda/
│   │   ├── Pila.java
│   │   ├── Cola.java
│   │   ├── Diccionario.java
│   │   ├── Conjunto.java
│   │   ├── Lista.java
│   │   ├── ABB.java
│   │   └── Grafo.java
│   │
│   ├── modelo/
│   │   ├── Cliente.java
│   │   ├── Accion.java
│   │   ├── TipoAccion.java
│   │   └── SolicitudSeguimiento.java
│   │
│   ├── servicio/
│   │   ├── GestorClientes.java
│   │   ├── HistorialAcciones.java
│   │   ├── ColaSolicitudes.java
│   │   ├── ArbolClientes.java
│   │   └── GrafoConexiones.java
│   │
│   ├── persistencia/
│   │   └── JsonLoader.java
│   │
│   ├── vista/
│   │   └── Menu.java
│   │
│   ├── util/
│   │   └── PerformanceTimer.java
│   │
│   └── Main.java
│
├── test/
│   ├── GestorClientesTest.java
│   ├── HistorialAccionesTest.java
│   ├── ColaSolicitudesTest.java
│   ├── ArbolClientesTest.java
│   └── GrafoConexionesTest.java
│
└── data/
    └── clientes.json
```

---

## 8. Invariantes de Representación

| Componente | Invariante |
|------------|------------|
| Sistema | Nombre de cliente único, no nulo, no vacío |
| Cliente | 0 ≤ scoring ≤ 100 |
| Cliente | No puede seguirse a sí mismo |
| Cliente | Máximo 2 seguidos, sin duplicados |
| Historial | Cada acción tiene datos suficientes para revertirse |
| Rehacer | Se vacía al ejecutar nueva acción |
| Cola | No hay solicitudes duplicadas (Conjunto auxiliar) |
| ABB | izq.scoring < nodo.scoring < der.scoring |
| ABB | Cada nodo tiene Lista<Cliente> no vacía |
| Grafo | Simetría bidireccional en aristas |
| Grafo | No hay self-loops |
| Referencias | Todo nombre referenciado existe en el Diccionario |

---

## 9. Plan de Pruebas

### Iteración 1

| Caso | Entrada | Esperado |
|------|---------|----------|
| Agregar válido | "Test", 50 | OK |
| Agregar duplicado | nombre existente | Error |
| Agregar nombre vacío | "", 50 | Error |
| Agregar scoring inválido | "X", 150 | Error |
| Buscar existente | "Alice" | Cliente |
| Buscar inexistente | "ZZZ" | null |
| Eliminar inexistente | "ZZZ" | Error |
| Undo con historial vacío | - | Error/null |
| Redo con pila vacía | - | Error/null |
| Undo agregar | - | Cliente eliminado |
| Redo agregar | - | Cliente restaurado |
| Solicitud duplicada | par existente | Rechazada |
| Solicitud a sí mismo | Alice → Alice | Rechazada |
| Solicitud a inexistente | Alice → ZZZ | Rechazada |

### Iteración 2

| Caso | Entrada | Esperado |
|------|---------|----------|
| Seguir (< 2) | Alice → Bob | OK |
| Seguir (= 2) | tercer seguido | Error |
| Seguir duplicado | Alice → Bob (ya sigue) | Error |
| Seguir a sí mismo | Alice → Alice | Error |
| Nivel 4 vacío | árbol pequeño | [] |
| Nivel 4 | árbol completo | nodos nivel 4 |
| ABB vacío | buscar scoring | null/[] |

### Iteración 3

| Caso | Entrada | Esperado |
|------|---------|----------|
| Conexión nueva | Alice ↔ Eve | OK |
| Conexión duplicada | existente | Ignorar |
| Conexión a sí mismo | Alice ↔ Alice | Error |
| Conexión a inexistente | Alice ↔ ZZZ | Error |
| Distancia directa | vecinos | 1 |
| Distancia indirecta | 2 saltos | 2 |
| Distancia a sí mismo | Alice → Alice | 0 |
| Sin camino | desconectados | -1 |
| Distancia a inexistente | Alice → ZZZ | -1 |
| Eliminar con cascada | cliente con relaciones | Todo limpio |
| Undo eliminar | - | Cliente + relaciones restauradas |

---

## 10. Complejidad Consolidada

| Operación | Tiempo | Espacio |
|-----------|--------|---------|
| Insertar cliente | O(1) + O(log n) | O(1) |
| Buscar por nombre | O(1) | O(1) |
| Buscar por scoring | O(log n) + O(k) | O(1) |
| Eliminar cliente (cascada) | O(n + E) | O(k) |
| Undo/Redo | O(costo operación) | O(1) |
| Agregar solicitud | O(1) | O(1) |
| Procesar solicitud | O(1) | O(1) |
| Agregar conexión | O(1) | O(1) |
| Obtener vecinos | O(1) | O(1) |
| Calcular distancia | O(V + E) | O(V) |
| Obtener nivel K | O(n) | O(n) |


## 11. Cumplimiento SOLID/GRASP

### SOLID

| Principio | Cumplimiento |
|-----------|--------------|
| **S**ingle Responsibility | Cada clase tiene una única responsabilidad: `GestorClientes` solo gestiona clientes, `HistorialAcciones` solo el historial, etc. |
| **O**pen/Closed | Los TDAs son cerrados a modificación pero abiertos a extensión (ej: ABB puede extenderse a AVL sin modificar código cliente) |
| **L**iskov Substitution | Los TDAs implementan interfaces consistentes; cualquier implementación de Pila puede sustituir a otra |
| **I**nterface Segregation | Interfaces pequeñas y específicas por TDA, no una interfaz monolítica |
| **D**ependency Inversion | Los servicios dependen de abstracciones (TDAs), no de implementaciones concretas |

### GRASP

| Patrón | Aplicación |
|--------|------------|
| **Information Expert** | `Cliente` conoce sus seguidos, `Grafo` conoce sus adyacencias |
| **Creator** | `GestorClientes` crea instancias de `Cliente`, `JsonLoader` crea objetos desde JSON |
| **Controller** | `Menu` coordina la interacción usuario-sistema sin lógica de negocio |
| **Low Coupling** | TDAs no dependen entre sí; servicios solo conocen las interfaces de TDAs |
| **High Cohesion** | Cada módulo tiene responsabilidades relacionadas (`tda/` solo estructuras, `modelo/` solo entidades) |
| **Pure Fabrication** | `HistorialAcciones` es una fabricación pura para manejar undo/redo sin contaminar entidades |
| **Indirection** | Los servicios actúan como intermediarios entre la vista y los TDAs |
| **Protected Variations** | Cambiar implementación de ABB a AVL no afecta a los servicios que lo usan |
