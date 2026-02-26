# Iteracion 3: Relaciones Generales y Distancia entre Clientes

## Indice

**Contexto**
1. [Resumen de requerimientos](#1-resumen-de-requerimientos)
2. [El grafo como modelo de una red social](#2-el-grafo-como-modelo-de-una-red-social)

**R1 — Relaciones Generales (Amistades Bidireccionales)**
3. [R1: Planteo del problema](#3-r1-planteo-del-problema)
4. [R1: Analisis del problema — dirigido vs no dirigido](#4-r1-analisis-del-problema)
5. [R1: Solucion — Diccionario como lista de adyacencia](#5-r1-solucion)
6. [R1: Implementacion en el codigo](#6-r1-implementacion-en-el-codigo)
7. [R1: Complejidad y justificacion de la estructura elegida](#7-r1-complejidad-y-justificacion)
8. [R1: Tests y validacion](#8-r1-tests-y-validacion)

**R2 — Distancia entre Clientes (BFS)**
9. [R2: Planteo del problema](#9-r2-planteo-del-problema)
10. [R2: Analisis del problema — recorrido de grafos](#10-r2-analisis-del-problema)
11. [R2: Solucion — BFS (Breadth-First Search)](#11-r2-solucion)
12. [R2: Ejemplo paso a paso](#12-r2-ejemplo-paso-a-paso)
13. [R2: TDAs auxiliares dentro del BFS](#13-r2-tdas-auxiliares)
14. [R2: Implementacion en el codigo](#14-r2-implementacion-en-el-codigo)
15. [R2: Complejidad O(V + E)](#15-r2-complejidad)
16. [R2: Tests y validacion](#16-r2-tests-y-validacion)

**Transversales**
17. [Persistencia JSON (R3)](#17-persistencia-json)
18. [Diseno de TDAs e Invariantes de Representacion](#18-diseno-de-tdas-e-invariantes)
19. [Vistas: Visualizacion interactiva para el docente](#19-vistas)
20. [Archivos modificados](#20-archivos-modificados)

**Anexo: Base de Datos de Practica (15 clientes)**
21. [Mapa de relaciones y ejercicios BFS](#21-mapa-de-relaciones)

---

# CONTEXTO

---

## 1. Resumen de Requerimientos

La iteracion 3 extiende la red social con dos requerimientos funcionales y uno
transversal:

| Req. | Nombre | Descripcion |
|------|--------|-------------|
| **R1** | Relaciones Generales | Gestionar relaciones de **amistad bidireccional** entre clientes: agregar, eliminar, consultar vecinos. Operaciones O(1). |
| **R2** | Distancia entre Clientes | Calcular la **distancia minima** (numero de saltos) entre dos clientes en la red. |
| **R3** | Persistencia y Pruebas | Las amistades deben persistirse en JSON y contar con tests unitarios. |

Adicionalmente, la consigna pide **"agregar el diseno de los tipos abstractos
de datos e invariantes de representacion"**, lo cual se documenta en la
[seccion 18](#18-diseno-de-tdas-e-invariantes).

---

## 2. El Grafo como Modelo de una Red Social

Antes de abordar R1 y R2, es necesario entender el concepto teorico que
subyace a ambos: el **grafo**.

### Que es un grafo

Un **grafo** G = (V, E) es una estructura matematica compuesta por:
- **V (vertices/nodos):** entidades del dominio. En nuestro caso, cada **Cliente** es un vertice.
- **E (aristas/edges):** conexiones entre vertices. En nuestro caso, las relaciones entre clientes.

### Tipos de grafos relevantes

| Tipo | Definicion | Ejemplo en la red |
|------|-----------|-------------------|
| **Dirigido (digrafo)** | Las aristas tienen direccion. A -> B no implica B -> A. | "A sigue a B" |
| **No dirigido** | Las aristas son simetricas. A — B implica que ambos se conocen. | "A y B son amigos" |
| **Ponderado** | Las aristas tienen un peso numerico. | No aplica en nuestro sistema |
| **No ponderado** | Todas las aristas valen lo mismo (1 salto). | Nuestro caso |

### Nuestro sistema: dos grafos superpuestos

La red social mantiene **dos grafos** sobre el mismo conjunto de vertices:

```
    Clientes: {1, 2, 3, 4, 5}

    GRAFO 1 — DIRIGIDO (seguimientos):      GRAFO 2 — NO DIRIGIDO (amistades):

        1 -----> 2                               1 ----- 2
        |        |                                       |
        v        v                               3 ----- 2
        3        4                               3 ----- 5
                 |
                 v
                 5

    "1 sigue a 2" (no implica que 2 siga a 1)    "1 y 2 son amigos" (simetrico)
    Usado en: R2 (calcular distancia)             Usado en: R1 (amistades)
```

- El **grafo dirigido** (iter 1-2) modela los seguimientos. Es asimetrico.
- El **grafo no dirigido** (iter 3, R1) modela las amistades. Es simetrico.
- Ambos conviven en los mismos objetos `Cliente`, almacenados en Diccionarios separados.

### El grafo implicito: por que NO hay un TDA Grafo

En muchas implementaciones academicas se crea una clase `Grafo` con una
**matriz de adyacencia** o listas de adyacencia centralizadas. Nosotros **no**
creamos un TDA Grafo separado. En su lugar, el grafo esta **distribuido
implicitamente** dentro de los propios objetos `Cliente`.

```
        GRAFO EXPLICITO (no usado)              GRAFO IMPLICITO (usado)
    +-----------------------------------+   +-----------------------------------+
    |  class Grafo                      |   |  class Cliente                     |
    |    - vertices: Lista<Cliente>      |   |    - siguiendo: Diccionario        |  <- "Mis aristas salientes"
    |    - aristas: int[][]              |   |    - seguidores: Diccionario       |  <- "Mis aristas entrantes"
    |    + agregarArista(A, B)           |   |    - amistades: Diccionario        |  <- "Mis aristas bidireccionales"
    |    + obtenerVecinos(A): Lista      |   +-----------------------------------+
    +-----------------------------------+   Cada Cliente almacena SUS conexiones.
                                            El "grafo" emerge de la union de todos.
```

Esta es una **decision de diseno** basada en el principio **Information Expert**
(GRASP): cada Cliente ya "sabe" a quien sigue, quien lo sigue, y quienes son
sus amigos. No necesitamos duplicar esa informacion en una estructura
centralizada.

**Ventajas del grafo implicito:**

| Aspecto | Grafo explicito (matriz NxN) | Grafo implicito (Diccionarios) |
|---------|------------------------------|-------------------------------|
| Memoria | O(N^2) para N clientes | O(N + E) solo aristas reales |
| Agregar arista | O(1) | O(1) amortizado |
| Consultar vecinos | O(N) recorrer fila | O(k) solo vecinos reales |
| Consultar "A sigue a B?" | O(1) | O(1) via `Diccionario.contiene()` |
| Eliminar cliente | O(N) limpiar fila+columna | O(seg + sig + amigos) solo afectados |

Con 1 millon de clientes, una matriz de adyacencia usaria ~10^12 celdas (~1 TB).
Nuestro enfoque solo almacena las relaciones que realmente existen.

---

# R1 — RELACIONES GENERALES (AMISTADES BIDIRECCIONALES)

---

## 3. R1: Planteo del Problema

### Que se pide

La consigna dice:

> *"Implementar una estructura que permita gestionar relaciones generales
> (amistades) entre los clientes [...] agregar relaciones de amistad [...]
> obtener los vecinos de un cliente"*

Ademas, se pide que las operaciones sean eficientes: **O(1)** para agregar,
eliminar y consultar amistades.

### Que es una "amistad" en la red social

Hasta la iteracion 2, el sistema solo tenia **seguimientos**: relaciones
**unidireccionales** donde "A sigue a B" no implica que "B siga a A". Esto es
un grafo dirigido.

La iteracion 3 introduce un nuevo tipo de relacion: la **amistad**, que es
**bidireccional**. Si A se hace amigo de B, automaticamente B es amigo de A.
Esto es un grafo no dirigido.

```
    SEGUIMIENTO (iter 1-2):              AMISTAD (iter 3):

    A -----seguir-----> B                A <====amigos====> B
    (unidireccional)                     (bidireccional)

    A.siguiendo contiene B               A.amistades contiene B
    B.seguidores contiene A              B.amistades contiene A
    PERO B.siguiendo NO contiene A       SIEMPRE simetrico
```

### El desafio tecnico

El desafio no es conceptual (la idea de "amistad" es simple), sino **estructural**:

1. **Como almacenar las amistades?** Necesitamos una estructura que permita
   agregar, eliminar y consultar amistades en O(1).
2. **Como garantizar la simetria?** Si A agrega a B como amigo, B debe
   automaticamente tener a A como amigo. Si A elimina la amistad, B tambien
   pierde a A.
3. **Como integrar con el sistema existente?** Sin romper los seguimientos
   ni la persistencia.

---

## 4. R1: Analisis del Problema

### El concepto: lista de adyacencia

En teoria de grafos, la forma estandar de representar las conexiones de un
vertice es una **lista de adyacencia**: para cada vertice, almacenamos la
lista de vertices a los que esta conectado.

```
    Lista de adyacencia para un grafo no dirigido de amistades:

    Cliente 1: [2, 5]         <- "1 es amigo de 2 y de 5"
    Cliente 2: [1, 3]         <- "2 es amigo de 1 y de 3"
    Cliente 3: [2]            <- "3 es amigo de 2"
    Cliente 5: [1]            <- "5 es amigo de 1"

    Observar la simetria: 1 aparece en la lista de 2, y 2 en la lista de 1.
```

### Que estructura usar para la lista de adyacencia?

Las operaciones que necesitamos sobre la lista de amigos de un cliente son:

| Operacion | Frecuencia | Requerimiento |
|-----------|-----------|---------------|
| Agregar amigo | Media | O(1) |
| Eliminar amigo | Media | O(1) |
| Consultar "es amigo de X?" | Alta | O(1) |
| Obtener todos los amigos | Baja | O(k) aceptable |

Comparemos las opciones de TDAs disponibles en el proyecto:

| Operacion | **Diccionario (Hash)** | Lista enlazada | ABB | Cola/Pila |
|-----------|----------------------|----------------|-----|-----------|
| Agregar | **O(1) amort.** | O(1) al inicio | O(log k) | O(1) pero no busca |
| Eliminar por ID | **O(1) amort.** | O(k) buscar | O(log k) | No soporta |
| Consultar existencia | **O(1) amort.** | O(k) recorrer | O(log k) | No soporta |
| Obtener todos | O(k) | O(k) | O(k) inorden | O(k) destructivo |

El **Diccionario** es la unica estructura que cumple O(1) en las tres
operaciones criticas. Es la eleccion optima y la que ya utilizamos para
`siguiendo` y `seguidores` en iteraciones previas.

### Diccionario como Conjunto (Set)

Un patron clave de la iteracion 3 es usar `Diccionario<Integer, Boolean>` como
si fuera un **conjunto de enteros**. No tenemos un TDA `Conjunto` dedicado, pero
el Diccionario cumple exactamente el mismo rol:

| Operacion de Set | Equivalente con Diccionario |
|------------------|----------------------------|
| `set.add(x)` | `diccionario.insertar(x, true)` |
| `set.contains(x)` | `diccionario.contiene(x)` |
| `set.remove(x)` | `diccionario.eliminar(x)` |
| `set.size()` | `diccionario.getCantidad()` |
| `set.toArray()` | `diccionario.obtenerClaves()` |

El valor `Boolean` es irrelevante; solo nos importa la **presencia de la clave**.
Esto es exactamente el comportamiento de un conjunto (Set) matematico.

---

## 5. R1: Solucion

### Arquitectura de la solucion

La solucion se divide en dos capas, siguiendo el principio de
**responsabilidad unica (SRP)**:

```
    ┌──────────────────────────────────────────────────┐
    │              GestorRelaciones                      │
    │  (Nivel de Servicio — coordina ambos lados)       │
    │                                                    │
    │  agregarAmistad(idA, idB):                        │
    │    1. Buscar Cliente A y Cliente B en O(1)        │
    │    2. A.agregarAmistad(idB)  ← lado A             │
    │    3. B.agregarAmistad(idA)  ← lado B (simetria)  │
    └────────────┬───────────────────────┬──────────────┘
                 │                       │
    ┌────────────▼────────┐   ┌─────────▼──────────────┐
    │   Cliente A          │   │   Cliente B              │
    │   (Nivel de Modelo)  │   │   (Nivel de Modelo)      │
    │                      │   │                          │
    │   amistades: {B: T}  │   │   amistades: {A: T}      │
    └──────────────────────┘   └──────────────────────────┘
```

- **Cliente (modelo):** Cada cliente tiene un `Diccionario<Integer, Boolean> amistades`
  que es su lista de adyacencia local. Solo conoce "sus" amigos.
  Opera sobre **un solo lado** de la relacion.

- **GestorRelaciones (servicio):** Coordina la operacion en **ambos** clientes,
  garantizando la simetria. Es el unico punto de entrada para modificar amistades.

Esta separacion es importante: el modelo no necesita saber nada sobre simetria,
y el servicio no necesita saber como se almacenan los datos internamente.

---

## 6. R1: Implementacion en el Codigo

### Nivel de modelo: `Cliente.java`

Se agrego un campo y 8 metodos:

```java
// Campo: lista de adyacencia de amistades
private Diccionario<Integer, Boolean> amistades;  // Inicializado en constructor

// ═══════ Operaciones de amistad (un solo lado) ═══════

public boolean agregarAmistad(int idAmigo) {
    if (idAmigo == this.id) return false;           // Invariante: no auto-amistad
    if (amistades.contiene(idAmigo)) return false;  // Ya son amigos
    amistades.insertar(idAmigo, true);              // O(1) amortizado
    return true;
}

public boolean eliminarAmistad(int idAmigo) {
    if (amistades.contiene(idAmigo)) {
        amistades.eliminar(idAmigo);                // O(1) amortizado
        return true;
    }
    return false;
}

public boolean esAmigoDE(int idOtro) {
    return amistades.contiene(idOtro);              // O(1) — lookup en hash table
}

public int[] obtenerAmigos() {
    return parsearClaves(amistades);                // O(k) — reutiliza helper existente
}

public int getCantidadAmigos() {
    return amistades.getCantidad();                 // O(1) — contador interno del Diccionario
}

// ═══════ Persistencia ═══════

public int[] getAmistadesSerialized() {
    return obtenerAmigos();                         // Para guardar en JSON
}

public void cargarAmistades(int[] ids) {            // Para cargar desde JSON
    if (ids == null) return;
    for (int idAmigo : ids) {
        if (idAmigo != this.id) {
            amistades.insertar(idAmigo, true);
        }
    }
}
```

### Nivel de servicio: `GestorRelaciones.java`

Se agregaron 5 metodos que coordinan la operacion en ambos clientes:

```java
// Garantiza la SIMETRIA: opera en ambos clientes
public boolean agregarAmistad(int idCliente1, int idCliente2) {
    Cliente cliente1 = clientes.obtener(idCliente1);  // O(1)
    Cliente cliente2 = clientes.obtener(idCliente2);  // O(1)
    if (cliente1 == null || cliente2 == null) return false;

    if (cliente1.agregarAmistad(idCliente2)) {  // Lado A -> B
        cliente2.agregarAmistad(idCliente1);    // Lado B -> A
        return true;
    }
    return false;  // Ya eran amigos
}

public boolean eliminarAmistad(int idCliente1, int idCliente2) {
    Cliente cliente1 = clientes.obtener(idCliente1);
    Cliente cliente2 = clientes.obtener(idCliente2);
    if (cliente1 == null || cliente2 == null) return false;

    if (cliente1.eliminarAmistad(idCliente2)) {  // Lado A -> B
        cliente2.eliminarAmistad(idCliente1);    // Lado B -> A
        return true;
    }
    return false;  // No eran amigos
}

public Cliente[] obtenerAmigos(int idCliente) { ... }  // O(k) — obtener vecinos
public int obtenerCantidadAmigos(int idCliente) { ... } // O(1)
public boolean sonAmigos(int id1, int id2) { ... }      // O(1)
```

### Nivel de coordinacion: `GestorClientes.java`

Se agregaron 5 metodos de delegacion que simplemente reenvian al
`GestorRelaciones`:

```java
public boolean agregarAmistad(int id1, int id2)    { return gestorRelaciones.agregarAmistad(id1, id2); }
public boolean eliminarAmistad(int id1, int id2)   { return gestorRelaciones.eliminarAmistad(id1, id2); }
public Cliente[] obtenerAmigos(int id)              { return gestorRelaciones.obtenerAmigos(id); }
public int obtenerCantidadAmigos(int id)            { return gestorRelaciones.obtenerCantidadAmigos(id); }
public boolean sonAmigos(int id1, int id2)          { return gestorRelaciones.sonAmigos(id1, id2); }
```

---

## 7. R1: Complejidad y Justificacion

### Tabla de complejidad

| Operacion | Complejidad | Desglose |
|-----------|-------------|----------|
| `agregarAmistad(A, B)` | **O(1)** amort. | 2x `Diccionario.obtener()` + 2x `Diccionario.insertar()` |
| `eliminarAmistad(A, B)` | **O(1)** amort. | 2x `Diccionario.obtener()` + 2x `Diccionario.eliminar()` |
| `sonAmigos(A, B)` | **O(1)** amort. | 1x `Diccionario.obtener()` + 1x `Diccionario.contiene()` |
| `obtenerCantidadAmigos(A)` | **O(1)** | `Diccionario.getCantidad()` (contador interno) |
| `obtenerAmigos(A)` | **O(k)** | k = cantidad de amigos, recorrer claves del Diccionario |

Todas las operaciones frecuentes (agregar, eliminar, consultar) cumplen con
el **O(1)** que pide la consigna.

### Por que se cumple O(1)

El Diccionario (Hash Table) logra O(1) amortizado porque:

1. **Hash function:** Convierte el ID (entero) en un indice de bucket en O(1).
2. **Factor de carga controlado:** El Diccionario se redimensiona cuando
   alpha > 0.75, manteniendo las cadenas de colision cortas.
3. **Sin busqueda lineal:** `contiene(key)` calcula el hash y accede
   directamente al bucket. En el caso promedio, la cadena tiene 1-2 elementos.

---

## 8. R1: Tests y Validacion

### Tests unitarios: `AmistadTest.java` (7 tests)

| # | Test | Que valida |
|---|------|-----------|
| 1 | Agregar amistad retorna true | La operacion se realiza correctamente |
| 2 | Amistad es bidireccional | `sonAmigos(A,B)` y `sonAmigos(B,A)` ambos true |
| 3 | Eliminar amistad retorna true | La eliminacion funciona |
| 4 | Eliminar es bidireccional | Ambos lados se eliminan |
| 5 | `obtenerAmigos` retorna cantidad correcta | La lista de vecinos es correcta |
| 6 | `sonAmigos` distingue amigos de no-amigos | No hay falsos positivos |
| 7 | No se puede ser amigo de si mismo | Invariante de no auto-amistad |

### Casos de uso: CU-028 a CU-032

| CU | Nombre | Que valida |
|----|--------|-----------|
| CU-028 | Agregar amistad bidireccional | Agregar + verificar bidireccionalidad + no duplicados + no auto-amistad + multiples amigos |
| CU-029 | Eliminar amistad bidireccional | Eliminar + verificar que desaparece de ambos lados + eliminar inexistente retorna false |
| CU-030 | Ver lista de amigos | Lista vacia, 1 amigo (ambos lados), multiples amigos |
| CU-031 | Verificar si son amigos | `sonAmigos` true, false, y bidireccional |
| CU-032 | Obtener cantidad de amigos | 0 amigos, 1 amigo, multiples amigos |

### Ejecucion

```bash
# Tests unitarios
javac -cp "lib/gson-2.10.1.jar" -d out test/AmistadTest.java src/**/*.java
java -ea -cp out:"lib/gson-2.10.1.jar" AmistadTest

# O toda la suite completa
./test.sh
```

---

# R2 — DISTANCIA ENTRE CLIENTES (BFS)

---

## 9. R2: Planteo del Problema

### Que se pide

La consigna dice:

> *"Calcular la distancia entre dos clientes (numero de saltos)"*

### Que significa "distancia" en una red social

En un grafo, la **distancia** entre dos vertices A y B es la **cantidad minima
de aristas** (saltos) que hay que recorrer para ir de A a B.

```
    Ejemplo: Red de seguimientos

    Alice --seguir--> Bob --seguir--> Charlie --seguir--> Diana

    Distancia(Alice, Diana) = 3     (3 saltos: Alice→Bob→Charlie→Diana)
    Distancia(Alice, Bob) = 1       (1 salto directo)
    Distancia(Alice, Alice) = 0     (mismo nodo)
    Distancia(Diana, Alice) = -1    (no hay camino inverso, es dirigido)
```

Observaciones clave:
- La distancia se mide en **numero de saltos**, no en "peso" de aristas (el
  grafo no es ponderado).
- Queremos la distancia **minima**: si hay multiples caminos, nos interesa el
  mas corto.
- Si no hay camino posible, la distancia es **-1** (clientes desconectados).
- El BFS opera sobre el **grafo dirigido** de seguimientos (`siguiendo`),
  no sobre el de amistades.

### El desafio tecnico

Calcular la distancia minima en un grafo requiere un **algoritmo de recorrido**
que explore sistematicamente los nodos. El desafio concreto es:

1. **Encontrar el camino mas corto**, no cualquier camino.
2. **Evitar ciclos infinitos** (A sigue a B, B sigue a A → loop).
3. **Manejar grafos desconectados** (componentes sin camino entre si).
4. **Hacerlo eficientemente** con los TDAs disponibles (sin `java.util`).

---

## 10. R2: Analisis del Problema

### Recorrido de grafos: BFS vs DFS

Para recorrer un grafo existen dos estrategias fundamentales:

**DFS (Depth-First Search) — Busqueda en profundidad:**
Explora tan lejos como sea posible por una rama antes de retroceder.
Usa una **Pila** (LIFO).

```
    DFS desde nodo 1:
    1 -> 2 -> 4 -> 5 -> (retrocede) -> 3
    Encuentra UN camino, pero no necesariamente el mas corto.
```

**BFS (Breadth-First Search) — Busqueda en anchura:**
Explora todos los vecinos a distancia 1, luego todos a distancia 2, etc.
Usa una **Cola** (FIFO).

```
    BFS desde nodo 1:
    Nivel 0: {1}
    Nivel 1: {2, 3}      ← todos los vecinos directos de 1
    Nivel 2: {4, 5}      ← todos los vecinos de 2 y 3
    Explora por niveles: GARANTIZA la distancia minima.
```

### Por que BFS es la solucion correcta

| Criterio | DFS (Pila) | **BFS (Cola)** |
|----------|-----------|---------------|
| Encuentra camino mas corto? | No garantizado | **Si, siempre** |
| Complejidad temporal | O(V + E) | O(V + E) |
| Complejidad espacial | O(V) | O(V) |
| Estructura necesaria | Pila (LIFO) | Cola (FIFO) |

Ambos tienen la misma complejidad, pero solo **BFS garantiza la distancia
minima** en grafos no ponderados. Esto se debe a que BFS explora por niveles:
cuando encuentra el destino por primera vez, necesariamente lo encontro por el
camino mas corto.

**Demostracion intuitiva:** Si BFS encuentra el destino en el nivel k, eso
significa que exploro TODOS los nodos a distancia k-1 sin encontrarlo.
Por lo tanto, no existe un camino de longitud menor a k.

---

## 11. R2: Solucion

### El algoritmo BFS adaptado a nuestra red

El BFS clasico se adapta a nuestra arquitectura de la siguiente manera:

```
    BFS(origen, destino):

        Caso base: si origen == destino, retornar 0

        1. Crear Cola<Integer> vacia            ← frontera de exploracion
        2. Crear Diccionario<Int, Bool> vacio   ← nodos ya visitados
        3. Encolar origen, marcarlo como visitado
        4. distancia = 0

        5. Mientras la Cola no este vacia:
            a. distancia++
            b. cantidadEnNivel = Cola.cantidad
               (esto "congela" la cantidad de nodos del nivel actual)

            c. Para i = 1 hasta cantidadEnNivel:
                I.    Desencolar nodo actual
                II.   Obtener vecinos del nodo: actual.getSiguiendo()
                      (lista de adyacencia del grafo dirigido)
                III.  Para cada vecino:
                      - Si vecino == destino → retornar distancia (ENCONTRADO)
                      - Si vecino no fue visitado:
                        * Marcarlo como visitado
                        * Encolarlo (se procesara en el proximo nivel)

        6. Retornar -1  (Cola se vacio sin encontrar destino → no hay camino)
```

### Detalles clave del algoritmo

**El truco del "congelamiento de nivel":** En la linea 5b, guardamos cuantos
nodos hay en la Cola ANTES de empezar a desencolar. Asi distinguimos los nodos
del nivel actual de los que se van encolando (que pertenecen al nivel siguiente).
Esto es lo que nos permite llevar la cuenta de la distancia.

**Direccion del recorrido:** El BFS sigue las aristas del grafo **dirigido**
de seguimientos (`actual.getSiguiendo()`), no del de amistades. Esto significa
que `calcularDistancia(A, B)` puede ser diferente de `calcularDistancia(B, A)`,
e incluso uno puede ser -1 cuando el otro no.

**Deteccion temprana:** Verificamos si el vecino es el destino **antes** de
encolarlo. Esto evita procesar un nivel extra innecesariamente.

---

## 12. R2: Ejemplo Paso a Paso

### Escenario

```
    Grafo de seguimientos:
    1 sigue a {2, 3}
    2 sigue a {4}
    3 sigue a {5}
    4 sigue a {5}

    Pregunta: calcularDistancia(1, 5) = ?
```

### Ejecucion del BFS

```
    Estado inicial:
    Cola = [1]
    Visitados = {1}
    Distancia = 0

    ──────────────────────────────────────────────────────
    ITERACION 1 — distancia = 1
    ──────────────────────────────────────────────────────
    nodosEnNivel = 1 (solo el nodo 1)

    Desencolar 1.
    Vecinos de 1: {2, 3}
      ¿2 == 5? No. ¿2 visitado? No → Marcar 2, Encolar 2.
      ¿3 == 5? No. ¿3 visitado? No → Marcar 3, Encolar 3.

    Cola = [2, 3]
    Visitados = {1, 2, 3}

    ──────────────────────────────────────────────────────
    ITERACION 2 — distancia = 2
    ──────────────────────────────────────────────────────
    nodosEnNivel = 2 (nodos 2 y 3)

    Desencolar 2.
    Vecinos de 2: {4}
      ¿4 == 5? No. ¿4 visitado? No → Marcar 4, Encolar 4.

    Desencolar 3.
    Vecinos de 3: {5}
      ¿5 == 5? SI → retornar distancia = 2

    ──────────────────────────────────────────────────────
    RESULTADO: calcularDistancia(1, 5) = 2
    Camino encontrado: 1 → 3 → 5 (2 saltos)
    ──────────────────────────────────────────────────────
```

### Observaciones

- El nodo 4 fue encolado pero nunca procesado (el algoritmo termino antes).
- Tambien existe el camino 1 → 2 → 4 → 5 (3 saltos), pero BFS encontro
  primero el de 2 saltos porque explora por niveles.
- Si hubieramos usado DFS, podria haber encontrado el camino de 3 saltos
  primero (dependiendo del orden de exploracion).

---

## 13. R2: TDAs Auxiliares dentro del BFS

El BFS necesita dos estructuras auxiliares. Ambas son TDAs propios del proyecto
(sin usar `java.util`):

### Cola<Integer> — Frontera de exploracion

La Cola implementa la politica **FIFO (First In, First Out)** que es lo que
hace que BFS funcione correctamente:

```
    Sin Cola (usando Pila/LIFO):        Con Cola (FIFO):
    Se convierte en DFS!                BFS correcto

    Explora asi:                        Explora asi:
    1 -> 2 -> 4 -> 5                   1 -> {2, 3} -> {4, 5}
    (profundidad primero)              (nivel por nivel)
    NO garantiza minimo                GARANTIZA minimo
```

Si usaramos una **Pila** en lugar de una Cola, el algoritmo se transformaria
en **DFS** y perderia la garantia de distancia minima.

| Operacion | Complejidad | Uso en BFS |
|-----------|-------------|-----------|
| `encolar(id)` | O(1) | Agregar vecino no visitado a la frontera |
| `desencolar()` | O(1) | Obtener el proximo nodo a explorar (el mas antiguo) |
| `getCantidad()` | O(1) | Saber cuantos nodos hay en el nivel actual |
| `estaVacia()` | O(1) | Condicion de parada (no hay mas nodos por explorar) |

### Diccionario<Integer, Boolean> — Conjunto de visitados

Sin el conjunto de visitados, el BFS podria:
1. **Entrar en ciclos infinitos:** Si A sigue a B y B sigue a A, visitaria
   A, B, A, B... indefinidamente.
2. **Procesar nodos multiples veces:** Desperdiciando tiempo y obteniendo
   distancias incorrectas.

| Operacion | Complejidad | Uso en BFS |
|-----------|-------------|-----------|
| `insertar(id, true)` | O(1) amort. | Marcar nodo como visitado |
| `contiene(id)` | O(1) amort. | Verificar si ya visitamos este nodo |

El Diccionario nos da `contiene()` en **O(1)**, lo cual es critico porque se
consulta **una vez por cada arista** del grafo. Si usaramos una estructura con
busqueda O(k), la complejidad total del BFS subiria a O(V * k + E).

---

## 14. R2: Implementacion en el Codigo

### `GestorRelaciones.java` — Metodo `calcularDistancia`

```java
/**
 * Calcula la distancia (numero de saltos) entre dos clientes.
 * BFS sobre el grafo dirigido de seguimientos.
 * Complejidad: O(V + E)
 *
 * @return distancia en saltos, 0 si mismo cliente, -1 si no hay camino
 */
public int calcularDistancia(int idOrigen, int idDestino) {
    // Validar que ambos clientes existen
    if (clientes.obtener(idOrigen) == null || clientes.obtener(idDestino) == null) {
        return -1;
    }

    // Caso base: mismo cliente
    if (idOrigen == idDestino) {
        return 0;
    }

    // ═══ Estructuras auxiliares del BFS ═══
    tda.Cola<Integer> cola = new tda.Cola<>();                  // Frontera FIFO
    Diccionario<Integer, Boolean> visitados = new Diccionario<>();  // Conjunto visitados

    cola.encolar(idOrigen);
    visitados.insertar(idOrigen, true);

    int distancia = 0;

    while (!cola.estaVacia()) {
        distancia++;
        int nodosEnNivel = cola.getCantidad();  // "Congelar" tamaño del nivel

        for (int i = 0; i < nodosEnNivel; i++) {
            int idActual = cola.desencolar();              // FIFO: el mas antiguo
            Cliente actual = clientes.obtener(idActual);   // O(1) hash lookup
            if (actual == null) continue;

            int[] vecinos = actual.getSiguiendo();         // Lista de adyacencia
            for (int idVecino : vecinos) {
                if (idVecino == 0) continue;               // ID invalido

                if (idVecino == idDestino) {
                    return distancia;                      // ENCONTRADO
                }

                if (!visitados.contiene(idVecino)) {       // O(1) consulta
                    visitados.insertar(idVecino, true);    // Marcar visitado
                    cola.encolar(idVecino);                // Encolar para proximo nivel
                }
            }
        }
    }

    return -1;  // Cola vacia, no hay camino
}
```

### `GestorClientes.java` — Delegacion

```java
public int calcularDistancia(int idOrigen, int idDestino) {
    return gestorRelaciones.calcularDistancia(idOrigen, idDestino);
}
```

---

## 15. R2: Complejidad O(V + E)

### Desglose formal

```
    Cada vertice se desencola exactamente 1 vez         → O(V) desencolamientos
    Para cada vertice, recorremos sus vecinos (aristas)  → O(E) aristas exploradas
    Cada operacion individual es O(1):
      - encolar / desencolar                             → O(1)
      - insertar en visitados / consultar visitados      → O(1) amortizado
      - obtener cliente por ID                           → O(1) amortizado

    Total: O(V) × O(1) + O(E) × O(1) = O(V + E)
```

### Tabla de complejidad

| Aspecto | Complejidad | Justificacion |
|---------|-------------|---------------|
| Tiempo total | **O(V + E)** | Cada vertice visitado 1 vez, cada arista explorada 1 vez |
| Espacio (Cola) | O(V) | Peor caso: todos los vertices en la Cola a la vez |
| Espacio (visitados) | O(V) | Una entrada por vertice visitado |
| Por operacion de Cola | O(1) | `encolar()` y `desencolar()` son O(1) |
| Por consulta de visitado | O(1) amort. | `Diccionario.contiene()` via hash |

Donde:
- **V** = cantidad de vertices (clientes) alcanzables desde el origen
- **E** = cantidad de aristas (relaciones "sigue a") entre esos vertices

### Por que esto es eficiente

En el peor caso (grafo completamente conectado), V = N (todos los clientes) y
E podria ser N * MAX_SEGUIDOS. Con MAX_SEGUIDOS = 2, E ≤ 2N, dando O(3N) = O(N).

En la practica, el BFS suele explorar solo una fraccion del grafo antes de
encontrar el destino o determinar que no hay camino.

---

## 16. R2: Tests y Validacion

### Tests unitarios: `DistanciaTest.java` (7 tests)

| # | Test | Que valida |
|---|------|-----------|
| 1 | Distancia de A a A = 0 | Caso base: mismo nodo |
| 2 | Conexion directa (A → B) = 1 | 1 salto |
| 3 | Camino de 2 saltos (A → B → C) = 2 | Multiples saltos |
| 4 | Sin camino posible = -1 | Clientes desconectados |
| 5 | Cliente inexistente = -1 | Validacion de entrada |
| 6 | Grafo desconectado (dos componentes) = -1 | Componentes separadas |
| 7 | Relaciones son dirigidas (A → B ≠ B → A) | Asimetria del digrafo |

### Casos de uso: CU-033 a CU-035

| CU | Nombre | Que valida |
|----|--------|-----------|
| CU-033 | Calcular distancia | Mismo cliente (0), directo (1), 2 saltos, sin camino (-1), inexistente |
| CU-034 | Encontrar camino mas corto | BFS encuentra el camino mas corto entre alternativas |
| CU-035 | Verificar conectividad | Clientes conectados, desconectados, grafo con 2 componentes |

### Performance: PERF-004

| Test | Que valida |
|------|-----------|
| PERF-004 | Complejidad O(V+E): BFS sobre red de 1000+ nodos en tiempo acotado |

### Ejecucion

```bash
# Tests unitarios
javac -cp "lib/gson-2.10.1.jar" -d out test/DistanciaTest.java src/**/*.java
java -ea -cp out:"lib/gson-2.10.1.jar" DistanciaTest

# O toda la suite completa
./test.sh
```

---

# TRANSVERSALES

---

## 17. Persistencia JSON

Las amistades se persisten en el mismo archivo JSON que el resto del modelo,
siguiendo la estrategia existente de "Load on Startup / Save on Shutdown".

### Cambios en `PersistenciaClientes.java`

**DTO (Data Transfer Object):**
```java
public static class ClienteDTO {
    public int id;
    public String nombre;
    public int scoring;
    public int[] siguiendo;
    public String[] solicitudes;
    public String[] seguidores;
    public int[] amistades;         // ← NUEVO: array de IDs de amigos
}
```

**Carga (al iniciar):**
```java
// Null-safe: soporta JSONs de versiones anteriores sin campo "amistades"
if (dto.amistades != null) {
    c.cargarAmistades(dto.amistades);
}
```

**Guardado (al cerrar):**
```java
dto.amistades = c.getAmistadesSerialized();  // int[] de IDs de amigos
```

### Formato del JSON

```json
{
  "clientes": [
    {
      "id": 1,
      "nombre": "Alice",
      "scoring": 80,
      "siguiendo": [2, 3],
      "seguidores": ["4", "5"],
      "solicitudes": [],
      "amistades": [2, 5]
    }
  ]
}
```

---

## 18. Diseno de TDAs e Invariantes de Representacion

### Diseno de los TDAs empleados en Iteracion 3

La iteracion 3 **reutiliza** los TDAs existentes sin crear nuevas estructuras.
La clave del diseno es usar el **Diccionario como Set** y la **Cola como
estructura de recorrido BFS**.

```
    ┌──────────────┬───────────────────────────────────────┬──────────────────┐
    │ TDA          │ Uso en Iteracion 3                    │ Complejidad      │
    ├──────────────┼───────────────────────────────────────┼──────────────────┤
    │ Diccionario  │ Lista de adyacencia de amistades      │ O(1) por op      │
    │ (Hash Table) │ (campo "amistades" en cada Cliente)   │                  │
    │              ├───────────────────────────────────────┼──────────────────┤
    │              │ Conjunto de visitados en BFS           │ O(1) por op      │
    │              │ (variable local en calcularDistancia)  │                  │
    │              ├───────────────────────────────────────┼──────────────────┤
    │              │ Acceso a clientes por ID durante BFS   │ O(1) por op      │
    │              │ (clientes.obtener(id) en cada paso)   │                  │
    ├──────────────┼───────────────────────────────────────┼──────────────────┤
    │ Cola (FIFO)  │ Cola de recorrido BFS                 │ O(1) enc/desenc  │
    │              │ (procesar nodos nivel por nivel)       │                  │
    └──────────────┴───────────────────────────────────────┴──────────────────┘
```

### Invariantes de Representacion de `Cliente`

Las invariantes son las propiedades que **SIEMPRE** deben cumplirse sobre el
estado interno de un objeto `Cliente`. Si alguna se violara, el sistema estaria
en un estado inconsistente. Estan documentadas en el codigo fuente en
`Cliente.java` (lineas 7-24).

**Invariantes basicas (Iteraciones 1-2):**

| # | Invariante | Garantia |
|---|-----------|----------|
| 1 | `id > 0` | Validado en constructor con `IllegalArgumentException` |
| 2 | `nombre != null && !nombre.trim().isEmpty()` | Validado por `Validador.validarNombre()` |
| 3 | `scoring` en rango `[0, 100]` | Validado por `Validador.validarScoring()` |
| 4 | `siguiendo != null` | Inicializado en constructor como `new Diccionario<>()` |
| 5 | `seguidores != null` | Inicializado en constructor como `new Diccionario<>()` |
| 6 | `solicitudesPendientes != null` | Inicializado en constructor como `new Cola<>()` |
| 7 | Sin duplicados en siguiendo/seguidores | Garantizado por Diccionario (claves unicas) |
| 8 | Ningun cliente se sigue a si mismo | Validado en `seguir()`: `if (idObjetivo == this.id) return false` |

**Invariantes de Iteracion 3 (Amistades):**

| # | Invariante | Garantia |
|---|-----------|----------|
| 9 | `amistades != null` | Inicializado en constructor como `new Diccionario<>()` |
| 10 | Sin duplicados en amistades | Garantizado por Diccionario (claves unicas) |
| 11 | Ningun cliente es amigo de si mismo | Validado en `agregarAmistad()`: `if (idAmigo == this.id) return false` |
| 12 | **Simetria de amistades**: si `A.amistades.contiene(B)` entonces `B.amistades.contiene(A)` | Garantizado por `GestorRelaciones.agregarAmistad()` y `eliminarAmistad()` que operan en **ambos** clientes |
| 13 | **Simetria de seguidores**: si `A.siguiendo.contiene(B)` entonces `B.seguidores.contiene(A)` | Garantizado por `GestorRelaciones.seguir()` y `dejarDeSeguir()` que actualizan ambos lados |

### Como se preservan las invariantes

**En tiempo de ejecucion:**
- `GestorRelaciones` es el **unico punto de entrada** para modificar relaciones
- `agregarAmistad(idA, idB)` inserta en `A.amistades` y `B.amistades` en la misma operacion
- `eliminarAmistad(idA, idB)` elimina de `A.amistades` y `B.amistades` en la misma operacion
- Si el primer `agregarAmistad` retorna false (ya eran amigos), el segundo no se ejecuta

**En persistencia:**
- `guardarCambios()` serializa el campo `amistades` de cada cliente como `int[]`
- `cargarDesdeArchivo()` recarga cada lista de amistades individualmente
- La simetria se preserva porque ambos lados fueron serializados al guardar

**En eliminacion de clientes:**
- `eliminarCliente()` limpia referencias en cascada:
  1. Elimina al cliente del Diccionario principal
  2. Recorre sus `seguidores` y les quita el follow
  3. Recorre sus `siguiendo` y les quita el seguidor
  4. Recorre sus `amistades` y les quita la amistad
- Complejidad total: O(seguidores + siguiendo + amigos)

---

## 19. Vistas: Visualizacion Interactiva para el Docente

Ademas de la logica de negocio, la iteracion 3 agrega vistas interactivas que
permiten **demostrar visualmente** el funcionamiento de los grafos, las listas
de adyacencia y las matrices. Estas vistas estan pensadas para que el docente
pueda verificar de manera tangible que la implementacion es correcta.

### 19.1 Seleccion de base de datos (`Main.java`)

Al iniciar la aplicacion, el usuario elige con que dataset trabajar:

```
+------------------------------------------------+
|        RED SOCIAL - UADE - TPO AyED II         |
+------------------------------------------------+

  Seleccionar base de datos:

  1. Produccion (1M clientes)
  2. Demo Iteracion 3 (10 clientes)
     Ideal para visualizar matrices de adyacencia

  0. Salir
```

**Por que dos bases de datos:**

| Base | Clientes | Proposito |
|------|----------|-----------|
| `data/clientes_1M.json` | ~1,000,000 | Demuestra escalabilidad: O(1) en hash, carga en ~2s |
| `data/clientes_iteracion_3.json` | 10 | Demuestra correctitud: matrices legibles, grafos visualizables |

La DB demo contiene 10 clientes con relaciones especificamente disenadas:
- **2 componentes conexas** en amistades: `{Alice, Bob, Charlie, Diana, Eva, Frank}` y `{Hugo, Ivan, Julia}`
- **1 nodo aislado**: `Grace` (sin amigos ni seguidos)
- **Cadenas de seguimiento**: `Alice→Bob→Diana→Eva→Frank`, `Hugo→Ivan→Julia`
- La **simetria** de las amistades es visible en la matriz

Tecnicamente, `Main.java` pasa el path seleccionado al constructor
`Menu(String dbPath)`, que lo propaga a `GestorClientes(dbPath)`. Toda la
arquitectura funciona identicamente con cualquier tamaño de dataset.

### 19.2 Menu de amistades (Opcion 4)

Desde `Amigos & Red Social > Amistades`, el usuario puede:

1. **Ver tabla de amigos actuales** del usuario logueado (ID, nombre, scoring)
2. **Agregar amistad** por ID — ejecuta `GestorRelaciones.agregarAmistad()` que
   inserta en ambos Diccionarios (bidireccional)
3. **Eliminar amistad** por ID — ejecuta `GestorRelaciones.eliminarAmistad()` que
   elimina de ambos Diccionarios

La vista valida: no auto-amistad, no duplicados, existencia del ID. Cada
operacion muestra feedback inmediato confirmando la bidireccionalidad.

### 19.3 Calculo de distancia BFS (Opcion 5)

Desde `Amigos & Red Social > Calcular Distancia`, el usuario ingresa un ID
origen y un ID destino. La vista:

1. Ejecuta `GestorRelaciones.calcularDistancia()` (BFS)
2. Mide el tiempo de ejecucion en nanosegundos
3. Muestra el resultado en un recuadro visual:

```
+-------------------------------------------+
|         RESULTADO BFS - DISTANCIA          |
+-------------------------------------------+
| Origen:  @Alice (ID:1)                    |
| Destino: @Eva (ID:5)                      |
+-------------------------------------------+
| Distancia: 2 salto(s)                     |
| Tiempo BFS: 0.042 ms                      |
+-------------------------------------------+
```

Esto permite al docente verificar que el BFS retorna la distancia correcta y
observar el tiempo de ejecucion (tipicamente < 1ms incluso en la DB de 1M).

### 19.4 Visualizacion de matrices de adyacencia (Opcion 6)

Esta es la vista mas rica de la iteracion 3. Desde
`Amigos & Red Social > Visualizar Grafo`, el sistema muestra **4 pantallas
secuenciales** que recorren la representacion del grafo desde lo abstracto
hasta lo concreto.

**Logica de carga:** Si la DB actual tiene 20 clientes o menos, usa esa misma
data. Si tiene mas (ej: 1M), carga automaticamente la DB demo para que la
matriz sea legible.

**Pantalla 1 — Tabla de clientes:**

Muestra los 10 clientes con sus relaciones en formato tabular:

```
+------+----------+---------+------------+------------+
| ID   | Nombre   | Scoring | Siguiendo  | Amigos     |
+------+----------+---------+------------+------------+
| 1    | Alice    | 90      | 2, 3       | 2, 5       |
| 2    | Bob      | 85      | 4          | 1, 3       |
| ...  | ...      | ...     | ...        | ...        |
| 7    | Grace    | 45      | -          | -          |
| ...  | ...      | ...     | ...        | ...        |
+------+----------+---------+------------+------------+
```

Aqui ya se puede observar: Grace no tiene relaciones (nodo aislado), Alice
tiene 2 seguidos y 2 amigos, etc.

**Pantalla 2 — Listas de adyacencia (texto):**

Muestra la representacion **por vertice** de cada grafo, que es exactamente
como se almacenan internamente en los Diccionarios:

```
LISTAS DE ADYACENCIA — SEGUIMIENTOS (grafo dirigido)

  Alice    (ID  1): [ Bob, Charlie ]
  Bob      (ID  2): [ Diana ]
  Charlie  (ID  3): [ Eva, Frank ]
  ...
  Grace    (ID  7): [ ]  (sin conexiones)
  Hugo     (ID  8): [ Ivan, Julia ]
  ...

LISTAS DE ADYACENCIA — AMISTADES (grafo no dirigido)

  Alice    (ID  1): [ Bob, Eva ]
  Bob      (ID  2): [ Alice, Charlie ]
  ...
  Grace    (ID  7): [ ]  (sin amigos)
  ...
```

Esta es la forma mas directa de ver el **grafo implicito**: cada linea
corresponde a un `Diccionario<Integer, Boolean>` dentro de un `Cliente`.
El docente puede verificar que las amistades aparecen en ambos lados
(Alice lista a Bob, Bob lista a Alice).

**Pantalla 3 — Matriz de seguimientos (dirigida):**

```
MATRIZ DE ADYACENCIA — SEGUIMIENTOS (grafo dirigido)
Lectura: fila i SIGUE A columna j

                1   2   3   4   5   6   7   8   9  10
             +-----------------------------------------+
  Alice     1 |  .   X   X   .   .   .   .   .   .   . |
  Bob       2 |  .   .   .   X   .   .   .   .   .   . |
  Charlie   3 |  .   .   .   .   X   X   .   .   .   . |
  Diana     4 |  .   .   .   .   X   .   .   .   .   . |
  Eva       5 |  .   .   .   .   .   X   .   .   .   . |
  Frank     6 |  .   .   .   .   .   .   .   .   .   . |
  Grace     7 |  .   .   .   .   .   .   .   .   .   . |
  Hugo      8 |  .   .   .   .   .   .   .   .   X   X |
  Ivan      9 |  .   .   .   .   .   .   .   .   .   X |
  Julia    10 |  .   .   .   .   .   .   .   .   .   . |
             +-----------------------------------------+

Nota: Esta matriz es ASIMETRICA (grafo dirigido).
```

Puntos que el docente puede verificar visualmente:
- **Asimetria:** `[1,2]=X` (Alice sigue a Bob) pero `[2,1]=.` (Bob no sigue a Alice)
- **Filas vacias:** Frank(6), Grace(7), Julia(10) no siguen a nadie
- **Dos componentes:** Bloque superior-izquierdo (1-6) y bloque inferior-derecho (8-10)

**Pantalla 4 — Matriz de amistades (no dirigida):**

```
MATRIZ DE ADYACENCIA — AMISTADES (grafo no dirigido)
Lectura: fila i ES AMIGO DE columna j

                1   2   3   4   5   6   7   8   9  10
             +-----------------------------------------+
  Alice     1 |  .   X   .   .   X   .   .   .   .   . |
  Bob       2 |  X   .   X   .   .   .   .   .   .   . |
  Charlie   3 |  .   X   .   X   .   .   .   .   .   . |
  Diana     4 |  .   .   X   .   .   .   .   .   .   . |
  Eva       5 |  X   .   .   .   .   X   .   .   .   . |
  Frank     6 |  .   .   .   .   X   .   .   .   .   . |
  Grace     7 |  .   .   .   .   .   .   .   .   .   . |
  Hugo      8 |  .   .   .   .   .   .   .   .   X   X |
  Ivan      9 |  .   .   .   .   .   .   .   X   .   X |
  Julia    10 |  .   .   .   .   .   .   .   X   X   . |
             +-----------------------------------------+

Nota: Esta matriz es SIMETRICA respecto a la diagonal.
```

Puntos que el docente puede verificar visualmente:
- **Simetria perfecta:** La matriz es identica si se refleja sobre la diagonal.
  `[1,2]=X` y `[2,1]=X` (Alice y Bob son amigos mutuamente).
- **Diagonal toda en `.`:** Ningun cliente es amigo de si mismo (invariante 11).
- **Grace (fila 7):** Toda en `.` — nodo aislado, sin amigos.
- **Bloque 8-9-10:** Hugo, Ivan y Julia forman un triangulo completo (todos
  amigos entre si) — visible como un bloque 3x3 de X's en la esquina inferior derecha.

### 19.5 Justificacion teorica de la matriz de adyacencia

La **matriz de adyacencia** es una de las dos formas clasicas de representar
un grafo (la otra son las listas de adyacencia). Para un grafo G = (V, E) con
N vertices, la matriz M es de tamaño N x N donde:

```
    M[i][j] = 1   si existe arista de i a j
    M[i][j] = 0   si no existe arista de i a j
```

**Propiedades matematicas que se demuestran en las vistas:**

| Propiedad | Grafo dirigido | Grafo no dirigido |
|-----------|---------------|-------------------|
| Simetria | M[i][j] ≠ M[j][i] (en general) | **M[i][j] = M[j][i] siempre** |
| Diagonal | Puede tener 1's (self-loops) | M[i][i] = 0 (sin auto-amistad) |
| Grado de un vertice | Grado saliente = suma de fila i | Grado = suma de fila i (o columna i) |
| Componentes | Bloques no necesariamente visibles | Bloques simetricos visibles |

En nuestro sistema, **no almacenamos** la matriz de adyacencia en memoria
(seria O(N^2) = 10^12 para 1M clientes). La vista la **construye dinamicamente**
recorriendo los Diccionarios de cada Cliente. El almacenamiento real es via
listas de adyacencia (Diccionarios), que son O(N + E).

La vista de la matriz es exclusivamente una **herramienta de visualizacion** para
datasets pequenos, no una estructura de datos del sistema.

---

## 20. Archivos Modificados

### Modelo

| Archivo | Cambio | Lineas |
|---------|--------|--------|
| `src/modelo/Cliente.java` | Campo `amistades` + 8 metodos + invariantes actualizadas | +75 |

### Servicio

| Archivo | Cambio | Lineas |
|---------|--------|--------|
| `src/servicio/GestorRelaciones.java` | BFS `calcularDistancia()` + 5 metodos de amistad | +180 |
| `src/servicio/GestorClientes.java` | 6 delegaciones + limpieza cascada amistades | +65 |
| `src/servicio/PersistenciaClientes.java` | DTO `amistades` + carga/guardado | +10 |

### Vista

| Archivo | Cambio | Lineas |
|---------|--------|--------|
| `src/Main.java` | Pantalla de seleccion de base de datos | +60 |
| `src/vista/Menu.java` | Constructor `Menu(String dbPath)` + info de clientes cargados | +8 |
| `src/vista/MenuSolicitudes.java` | Opcion 4: Amistades + Opcion 5: Distancia BFS + Opcion 6: Matrices | +390 |

### Datos

| Archivo | Descripcion |
|---------|-------------|
| `data/clientes_iteracion_3.json` | 10 clientes con relaciones disenadas para demo visual |

### Tests

| Archivo | Descripcion |
|---------|-------------|
| `test/AmistadTest.java` | 7 tests: agregar, bidireccionalidad, eliminar, obtener, verificar, consigo mismo |
| `test/DistanciaTest.java` | 7 tests: mismo cliente, 1 hop, 2 hops, sin camino, inexistente, desconectado, dirigido |
| `test/casos_de_uso/CU_028` a `CU_035` | 8 casos de uso de amistades + distancia |
| `test/casos_de_uso/performance/PERF_004` | Validacion O(V+E) del BFS |

---

## 21. Mapa de Relaciones

> Archivo: `data/clientes_iteracion_3.json` — 15 clientes, 24 seguimientos, 14 amistades, 5 solicitudes.
> Validado contra todas las reglas de negocio (MAX_SEGUIDOS=2, MAX_AMIGOS=2, bidireccionalidad).

### 21.1 Tabla de clientes

| ID | Nombre | Scoring | Siguiendo | Seguidores | Amigos | Solicitudes pendientes |
|----|--------|---------|-----------|------------|--------|------------------------|
| 1  | Alice  | 90      | 2, 3      | 12         | 7, 10  | —                      |
| 2  | Bob    | 42      | 4, 5      | 1          | 9, 14  | de Oscar(15)           |
| 3  | Charlie| 71      | 6, 7      | 1          | 11, 15 | —                      |
| 4  | Diana  | 18      | 6, 8      | 2          | 9, 12  | —                      |
| 5  | Eva    | 83      | 9, 11     | 2          | 8, 13  | —                      |
| 6  | Frank  | 55      | 8, 10     | 3, 4       | 11, 15 | de Julia(10)           |
| 7  | Grace  | 97      | 11, 12    | 3          | 1, 14  | de Nora(14)            |
| 8  | Hugo   | 29      | 10, 14    | 4, 6       | 5, 12  | de Kevin(11)           |
| 9  | Ivan   | 64      | 13, 14    | 5          | 2, 4   | —                      |
| 10 | Julia  | 36      | 15        | 6, 8       | 1, 13  | —                      |
| 11 | Kevin  | 78      | 13        | 5, 7       | 3, 6   | —                      |
| 12 | Laura  | 51      | 1, 14     | 7          | 4, 8   | de Marco(13)           |
| 13 | Marco  | 12      | 15        | 9, 11      | 5, 10  | —                      |
| 14 | Nora   | 88      | 15        | 8, 9, 12   | 2, 7   | —                      |
| 15 | Oscar  | 45      | —         | 10, 13, 14 | 3, 6   | —                      |

### 21.2 Grafo de seguimientos (dirigido)

Todas las flechas indican "A sigue a B". Cada cliente sigue a maximo 2.

```
            12 ──→ 1 ──→ 2 ──→ 4 ──→ 6 ──→ 8 ──→ 10 ──→ 15
            ↑      ↓      ↓      ↓            ↓      ↑      ↑
            7 ←── 3      5      8 ──→ 14 ──→ 15     |      |
            ↓             ↓                           |      |
           11 ──→ 13 ──→ 15           9 ──→ 13 ──→ 15      |
            ↑      ↑                   ↓                     |
            5      9 ──→ 14 ──→ 15    |                     |
                                       └── 14                |
           12 ──→ 14 ──→ 15                                 |
                                              6 ──→ 10 ─────┘
```

**Resumen de aristas dirigidas (24 total):**

```
1→2    2→4    3→6    4→6    5→9     6→8     7→11    8→10
1→3    2→5    3→7    4→8    5→11    6→10    7→12    8→14
                                    9→13    10→15   11→13
                                    9→14    12→1    13→15
                                            12→14   14→15
```

### 21.3 Grafo de amistades (no dirigido)

Cada linea indica "A y B son amigos mutuamente". Cada cliente tiene maximo 2 amigos.

```
Grace(7) ── Alice(1) ── Julia(10) ── Marco(13) ── Eva(5) ── Hugo(8) ── Laura(12) ── Diana(4)
  |                                                                                    |
Nora(14) ── Bob(2) ── Ivan(9)                                                       Ivan(9)
                         |
                      Diana(4)

Oscar(15) ── Frank(6) ── Kevin(11) ── Charlie(3) ── Oscar(15)
```

**Nota:** ningun par de amigos coincide con un par de seguimiento.
Los grafos de seguimiento y amistad son 100% independientes.

**Los 15 pares de amistad:**

```
1↔7    1↔10   2↔9    2↔14   3↔11   3↔15   4↔9    4↔12
5↔8    5↔13   6↔11   6↔15   7↔14   8↔12   10↔13
```

### 21.4 Ejercicios de BFS (camino mas corto por seguimientos)

Estos son los resultados que deberias obtener al ejecutar "Distancia BFS" en el sistema.
El BFS recorre las aristas dirigidas de `siguiendo`.

| Origen | Destino | Saltos | Camino que encuentra BFS |
|--------|---------|--------|--------------------------|
| Alice(1) | Oscar(15) | **4** | Alice→Charlie→Frank→Julia→Oscar (1→3→6→10→15) |
| Alice(1) | Nora(14) | **4** | Alice→Bob→Diana→Hugo→Nora (1→2→4→8→14) |
| Alice(1) | Marco(13) | **4** | Alice→Bob→Eva→Ivan→Marco (1→2→5→9→13) |
| Alice(1) | Hugo(8) | **3** | Alice→Bob→Diana→Hugo (1→2→4→8) |
| Laura(12) | Oscar(15) | **2** | Laura→Nora→Oscar (12→14→15) |
| Grace(7) | Oscar(15) | **3** | Grace→Kevin→Marco→Oscar (7→11→13→15) |
| Eva(5) | Oscar(15) | **3** | Eva→Ivan→Marco→Oscar (5→9→13→15) |
| Oscar(15) | Alice(1) | **-1** | **No hay camino** (Oscar no sigue a nadie) |
| Oscar(15) | Bob(2) | **-1** | **No hay camino** |
| Alice(1) | Alice(1) | **0** | Mismo cliente |

**Para pensar:**
- ¿Por que Alice→Oscar es 4 y no 5? Porque existe el camino 1→3→6→10→15 (4 saltos, via Charlie→Frank→Julia) ademas del mas largo 1→2→4→8→10→15 (5 saltos). BFS siempre encuentra el mas corto.
- ¿Por que Oscar→Alice es -1? Porque el grafo es **dirigido**: Oscar.siguiendo esta vacio, no tiene aristas de salida.
- ¿Que pasa si Laura busca a Alice? Laura→Alice = 1 salto (12→1). Pero Alice→Laura por seguimientos es mas largo porque Alice no sigue directamente a Laura.

### 21.5 Solicitudes pendientes

| Destinatario | Solicitante | Si se acepta... |
|-------------|-------------|-----------------|
| Bob(2)      | Oscar(15)   | Oscar pasa a seguir a Bob. Oscar: 0→1 seguidos. Bob gana 1 seguidor. |
| Frank(6)    | Julia(10)   | Julia pasa a seguir a Frank. Julia: 1→2 seguidos (alcanza MAX). |
| Grace(7)    | Nora(14)    | Nora pasa a seguir a Grace. Nora: 1→2 seguidos (alcanza MAX). |
| Hugo(8)     | Kevin(11)   | Kevin pasa a seguir a Hugo. Kevin: 1→2 seguidos (alcanza MAX). |
| Laura(12)   | Marco(13)   | Marco pasa a seguir a Laura. Marco: 1→2 seguidos (alcanza MAX). |

### 21.6 Verificacion de reglas de negocio

| Regla | Estado |
|-------|--------|
| MAX_SEGUIDOS=2: ningun cliente sigue a mas de 2 | OK (todos ≤ 2) |
| MAX_AMIGOS=2: ningun cliente tiene mas de 2 amigos | OK (todos = 2, excepto ninguno con mas) |
| Seguidores sin limite: Oscar tiene 3 seguidores, Nora tiene 3 | OK |
| Bidireccionalidad seguimientos: A.siguiendo[B] ↔ B.seguidores[A] | OK (24/24 pares verificados) |
| Bidireccionalidad amistades: A.amistades[B] ↔ B.amistades[A] | OK (15/15 pares verificados) |
| No auto-seguimiento ni auto-amistad | OK (0 violaciones) |
| Solicitudes validas: solicitante tiene espacio y no ya sigue al destino | OK (5/5 validas) |
| Scoring en rango 0-100 | OK (min=12 Marco, max=97 Grace) |
| Independencia de grafos: ningun par de amigos coincide con seguimiento | OK (0/15 coincidencias) |
