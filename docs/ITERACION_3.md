# Iteracion 3: Amistades Bidireccionales y Distancia BFS

## Indice

1. [Requerimientos](#1-requerimientos)
2. [El Grafo Implicito: como representamos la red social](#2-el-grafo-implicito)
3. [Representacion con listas de adyacencia via Diccionario](#3-representacion-con-listas-de-adyacencia)
4. [Dos grafos coexistentes en el sistema](#4-dos-grafos-coexistentes)
5. [Amistades bidireccionales: grafo no dirigido](#5-amistades-bidireccionales)
6. [Calcular distancia: BFS sobre el grafo dirigido](#6-calcular-distancia-bfs)
7. [TDAs empleados en la Iteracion 3](#7-tdas-empleados)
8. [Analisis de complejidad](#8-analisis-de-complejidad)
9. [Archivos modificados](#9-archivos-modificados)
10. [Tests](#10-tests)

---

## 1. Requerimientos

La iteracion 3 agrega dos funcionalidades a la red social:

**Requerimiento A - Relaciones Generales (Amistades):**
Permitir que dos clientes establezcan una relacion de amistad **bidireccional**.
Si A se hace amigo de B, automaticamente B es amigo de A. A diferencia de los
seguimientos (dirigidos, donde A sigue a B no implica que B siga a A), las
amistades son simetricas.

**Requerimiento B - Distancia entre Clientes:**
Calcular el numero minimo de saltos (hops) necesarios para llegar de un
cliente A a un cliente B a traves del grafo de seguimientos. Esto requiere un
algoritmo de recorrido de grafos.

---

## 2. El Grafo Implicito

### Que es un grafo?

Un **grafo** G = (V, E) es una estructura matematica compuesta por:
- **V (vertices/nodos):** Entidades del dominio. En nuestro caso, cada **Cliente** es un vertice.
- **E (aristas/edges):** Conexiones entre vertices. En nuestro caso, las relaciones "sigue a" o "es amigo de".

Los grafos pueden ser:
- **Dirigidos:** La arista tiene direccion. A -> B no implica B -> A.
- **No dirigidos:** La arista es bidireccional. Si A -- B, entonces A conoce a B y B conoce a A.

### Por que NO tenemos un TDA Grafo explicito?

En muchas implementaciones academicas, se crea una clase `Grafo` con una matriz
de adyacencia o listas de adyacencia. Nosotros **no** hicimos eso. En su lugar,
el grafo esta **distribuido implicitamente** dentro de los propios objetos `Cliente`.

Esto no es una omision ni un error: es una **decision de diseno** basada en el
principio de **Information Expert** (GRASP). Cada Cliente ya "sabe" a quien sigue,
quien lo sigue, y quienes son sus amigos. No necesitamos duplicar esa informacion
en una estructura centralizada.

```
                        GRAFO EXPLICITO (no usado)
                    +-----------------------------------+
                    |  class Grafo                      |
                    |    - vertices: Lista<Cliente>      |
                    |    - aristas: int[][]              |  <- Matriz NxN
                    |    + agregarArista(A, B)           |     o listas de
                    |    + obtenerVecinos(A): Lista      |     adyacencia
                    +-----------------------------------+

        vs.

                        GRAFO IMPLICITO (usado)
                    +-----------------------------------+
                    |  class Cliente                     |
                    |    - siguiendo: Diccionario        |  <- "Mis aristas salientes"
                    |    - seguidores: Diccionario       |  <- "Mis aristas entrantes"
                    |    - amistades: Diccionario        |  <- "Mis aristas bidireccionales"
                    +-----------------------------------+
                    Cada Cliente almacena SUS conexiones.
                    El "grafo" emerge de la union de todos los Clientes.
```

### Ventajas del grafo implicito

| Aspecto | Grafo explicito (matriz) | Grafo implicito (Diccionarios) |
|---------|--------------------------|-------------------------------|
| Memoria | O(N^2) para N clientes | O(N + E) solo aristas reales |
| Agregar arista | O(1) | O(1) amortizado |
| Consultar vecinos | O(N) recorrer fila | O(k) solo vecinos reales |
| Consultar "A sigue a B?" | O(1) | O(1) via Diccionario.contiene() |
| Eliminar cliente | O(N) limpiar fila+columna | O(seg + sig) solo afectados |

Con 1 millon de clientes, una matriz de adyacencia usaria 10^12 celdas (1 TB).
Nuestro enfoque solo almacena las relaciones que realmente existen.

---

## 3. Representacion con Listas de Adyacencia

### Que son las listas de adyacencia?

En teoria de grafos, una **lista de adyacencia** almacena, para cada vertice,
la lista de vertices a los que esta conectado. Es la representacion mas eficiente
en memoria para grafos dispersos (pocos aristas respecto al maximo posible).

Nuestro sistema usa una variante: en vez de una lista enlazada clasica, usamos
un **Diccionario (Hash Table)** como lista de adyacencia. Esto nos da O(1) para
todas las operaciones (insertar, eliminar, consultar existencia), mientras que
una lista enlazada daria O(k) para buscar o eliminar.

```
    Lista de adyacencia clasica          Nuestra implementacion
    (lista enlazada)                     (Diccionario/Hash Table)

    Cliente 1: [2] -> [5] -> [3]         Cliente 1: { 2: true, 5: true, 3: true }
    Cliente 2: [1] -> [4]                Cliente 2: { 1: true, 4: true }
    Cliente 3: [5]                       Cliente 3: { 5: true }

    Buscar si 1 sigue a 5:              Buscar si 1 sigue a 5:
    Recorrer lista: O(k)                 Diccionario.contiene(5): O(1)
```

### Como se ve en el codigo

Cada `Cliente` tiene tres Diccionarios que actuan como listas de adyacencia:

```java
// En Cliente.java
private Diccionario<Integer, Boolean> siguiendo;   // Aristas salientes (dirigidas)
private Diccionario<Integer, Boolean> seguidores;  // Aristas entrantes (dirigidas)
private Diccionario<Integer, Boolean> amistades;   // Aristas bidireccionales
```

La clave es el **ID del otro cliente**. El valor es siempre `true` (solo nos
importa la existencia, no un peso). En esencia, cada Diccionario funciona como
un **conjunto (Set)** implementado sobre Hash Table.

---

## 4. Dos Grafos Coexistentes

El sistema mantiene **dos grafos superpuestos** sobre los mismos vertices:

### Grafo 1: Seguimientos (Dirigido)

```
    A ---seguir---> B          A sigue a B, pero B no sigue a A

    Almacenamiento:
    - A.siguiendo contiene B   (arista saliente de A)
    - B.seguidores contiene A  (arista entrante a B)
```

Este es un **digrafo** (grafo dirigido). Las aristas tienen sentido.
Se usa para calcular distancia (BFS sigue la direccion de las aristas).

### Grafo 2: Amistades (No dirigido)

```
    A ----amigo---- B          A y B son amigos mutuamente

    Almacenamiento:
    - A.amistades contiene B
    - B.amistades contiene A   (simetria garantizada por GestorRelaciones)
```

Este es un **grafo no dirigido**. Si A es amigo de B, entonces B es amigo de A.
La bidireccionalidad se garantiza porque `GestorRelaciones.agregarAmistad()`
siempre inserta en **ambos** diccionarios.

### Diagrama conceptual

```
    Clientes: {1, 2, 3, 4, 5}

    GRAFO DIRIGIDO (seguimientos):       GRAFO NO DIRIGIDO (amistades):

        1 -----> 2                           1 ----- 2
        |        |                                   |
        v        v                           3 ----- 2
        3        4                           3 ----- 5
                 |
                 v
                 5

    Distancia(1, 5) = 3                  sonAmigos(1, 2) = true
    (1 -> 2 -> 4 -> 5)                  sonAmigos(1, 3) = false
    Distancia(5, 1) = -1                sonAmigos(2, 1) = true  (simetrico)
    (no hay camino inverso)
```

---

## 5. Amistades Bidireccionales

### El problema

Necesitamos una relacion simetrica: si A agrega a B como amigo, B automaticamente
tiene a A como amigo. Y si A elimina la amistad, desaparece para ambos.

### La solucion: Diccionario por Cliente + coordinacion en GestorRelaciones

**Nivel de modelo (Cliente.java):**

Cada Cliente tiene un `Diccionario<Integer, Boolean> amistades`. Este diccionario
es la **lista de adyacencia local** del vertice en el grafo de amistades.

```java
// Cliente.java - Operaciones locales (un solo lado)
public boolean agregarAmistad(int idAmigo) {
    if (idAmigo == this.id) return false;          // No amistad consigo mismo
    if (amistades.contiene(idAmigo)) return false;  // Ya son amigos
    amistades.insertar(idAmigo, true);              // O(1) amortizado
    return true;
}

public boolean esAmigoDE(int idOtro) {
    return amistades.contiene(idOtro);              // O(1) - lookup en hash table
}
```

**Nivel de servicio (GestorRelaciones.java):**

El servicio garantiza la **simetria** llamando a ambos clientes:

```java
// GestorRelaciones.java - Coordina ambos lados
public boolean agregarAmistad(int idCliente1, int idCliente2) {
    Cliente cliente1 = clientes.obtener(idCliente1);  // O(1)
    Cliente cliente2 = clientes.obtener(idCliente2);  // O(1)

    if (cliente1 == null || cliente2 == null) return false;

    if (cliente1.agregarAmistad(idCliente2)) {  // Lado A -> B
        cliente2.agregarAmistad(idCliente1);    // Lado B -> A (simetria)
        return true;
    }
    return false;
}
```

### Por que Diccionario y no otra estructura?

| Operacion | Diccionario (Hash) | Lista enlazada | Array ordenado |
|-----------|-------------------|----------------|----------------|
| Agregar amigo | O(1) amort. | O(1) al inicio | O(N) desplazar |
| Eliminar amigo | O(1) amort. | O(k) buscar | O(N) desplazar |
| Consultar "es amigo?" | O(1) amort. | O(k) recorrer | O(log k) binaria |
| Obtener todos | O(k) | O(k) | O(k) |

El Diccionario gana en todas las operaciones frecuentes. La unica desventaja
(overhead de memoria por la tabla hash) es despreciable comparada con la
ganancia en tiempo.

---

## 6. Calcular Distancia: BFS sobre el Grafo Dirigido

### El problema

Dado un cliente origen y un cliente destino, encontrar la **distancia minima**
(numero de saltos) entre ellos siguiendo las aristas dirigidas del grafo de
seguimientos.

### Por que BFS y no DFS?

- **BFS (Breadth-First Search):** Explora por niveles. Primero todos los vecinos
  a distancia 1, luego a distancia 2, etc. **Garantiza encontrar el camino mas
  corto** en grafos sin pesos.

- **DFS (Depth-First Search):** Explora en profundidad. Puede encontrar UN camino,
  pero no necesariamente el mas corto.

Para distancia minima, BFS es la eleccion correcta.

### Como funciona el algoritmo BFS

```
    BFS(origen, destino):
        1. Crear Cola vacia (para recorrer por niveles)
        2. Crear Diccionario "visitados" (para no revisitar nodos)
        3. Encolar origen, marcarlo como visitado
        4. distancia = 0
        5. Mientras la Cola no este vacia:
            a. distancia++
            b. cantidadEnNivel = Cola.cantidad  (cuantos nodos hay en este nivel)
            c. Para cada nodo del nivel actual:
                i.   Desencolar nodo actual
                ii.  Para cada vecino (cliente que el actual SIGUE):
                     - Si vecino == destino: retornar distancia  (encontrado!)
                     - Si vecino no fue visitado:
                       * Marcarlo como visitado
                       * Encolarlo (se procesara en el proximo nivel)
        6. Retornar -1  (no hay camino)
```

### Ejemplo paso a paso

```
    Grafo de seguimientos:
    1 -> 2, 1 -> 3
    2 -> 4
    3 -> 5
    4 -> 5

    calcularDistancia(1, 5):

    Paso 0: Cola = [1], visitados = {1}, distancia = 0

    Paso 1 (distancia = 1):
      Desencolar 1, explorar vecinos de 1: {2, 3}
        2 no es 5, no visitado -> encolar, marcar
        3 no es 5, no visitado -> encolar, marcar
      Cola = [2, 3], visitados = {1, 2, 3}

    Paso 2 (distancia = 2):
      Desencolar 2, explorar vecinos de 2: {4}
        4 no es 5, no visitado -> encolar, marcar
      Desencolar 3, explorar vecinos de 3: {5}
        5 == destino! -> retornar distancia = 2

    Resultado: calcularDistancia(1, 5) = 2
```

### TDAs usados dentro del BFS

El BFS necesita dos estructuras auxiliares. Ambas son TDAs propios del proyecto:

```java
public int calcularDistancia(int idOrigen, int idDestino) {
    // ...

    // TDA Cola<Integer>: Cola FIFO para el recorrido por niveles
    // Sin esta cola, no podriamos procesar los nodos nivel por nivel.
    // La politica FIFO garantiza que procesamos primero los mas cercanos.
    tda.Cola<Integer> cola = new tda.Cola<>();

    // TDA Diccionario<Integer, Boolean>: Conjunto de visitados
    // Sin este conjunto, el BFS podria entrar en ciclos infinitos
    // (ej: A sigue a B, B sigue a A -> loop).
    // Usamos Diccionario como Set: la clave es el ID, el valor es irrelevante.
    Diccionario<Integer, Boolean> visitados = new Diccionario<>();

    cola.encolar(idOrigen);
    visitados.insertar(idOrigen, true);

    int distancia = 0;

    while (!cola.estaVacia()) {
        distancia++;
        int nodosEnNivel = cola.getCantidad();  // Cuantos procesar en ESTE nivel

        for (int i = 0; i < nodosEnNivel; i++) {
            int idActual = cola.desencolar();         // FIFO: el mas antiguo primero
            Cliente actual = clientes.obtener(idActual);  // O(1) lookup en hash
            if (actual == null) continue;

            int[] vecinos = actual.getSiguiendo();    // Lista de adyacencia del nodo
            for (int idVecino : vecinos) {
                if (idVecino == idDestino) return distancia;  // Encontrado!

                if (!visitados.contiene(idVecino)) {  // O(1) consulta en hash
                    visitados.insertar(idVecino, true);  // Marcar visitado O(1)
                    cola.encolar(idVecino);               // Encolar O(1)
                }
            }
        }
    }

    return -1;  // No hay camino
}
```

### Por que la Cola es esencial?

La Cola impone el orden **FIFO (First In, First Out)** que es lo que hace que
BFS funcione correctamente:

```
    Sin Cola (usando Pila/LIFO):    Con Cola (FIFO):
    Se convierte en DFS!            BFS correcto

    Explora asi:                    Explora asi:
    1 -> 2 -> 4 -> 5               1 -> {2, 3} -> {4, 5}
    (profundidad primero)           (nivel por nivel, mas corto primero)
```

Si usaramos una Pila en lugar de una Cola, el algoritmo se transformaria en DFS
y no garantizaria la distancia minima.

### Por que el Diccionario de visitados?

Sin el conjunto de visitados, el BFS podria:
1. **Entrar en ciclos infinitos:** Si A -> B y B -> A, visitaria A, B, A, B...
2. **Procesar nodos multiples veces:** Desperdiciando tiempo y dando distancias incorrectas.

El Diccionario nos da `contiene()` en O(1), lo cual es critico porque se consulta
una vez **por cada arista** del grafo.

---

## 7. TDAs Empleados en la Iteracion 3

### Resumen de uso de cada TDA

```
    +------------------+-------------------------------------------+-------------------+
    | TDA              | Uso en Iteracion 3                        | Complejidad       |
    +------------------+-------------------------------------------+-------------------+
    | Diccionario      | Lista de adyacencia de amistades          | O(1) por op       |
    | (Hash Table)     | (campo "amistades" en cada Cliente)       |                   |
    |                  +-------------------------------------------+-------------------+
    |                  | Conjunto de visitados en BFS              | O(1) por op       |
    |                  | (variable local en calcularDistancia)     |                   |
    |                  +-------------------------------------------+-------------------+
    |                  | Acceso a clientes por ID durante BFS      | O(1) por op       |
    |                  | (clientes.obtener(id) en cada paso)       |                   |
    +------------------+-------------------------------------------+-------------------+
    | Cola             | Cola de recorrido BFS                     | O(1) enc/desenc   |
    | (FIFO)           | (procesar nodos nivel por nivel)          |                   |
    +------------------+-------------------------------------------+-------------------+
```

### Diccionario<Integer, Boolean> como Conjunto (Set)

Un patron recurrente en la iteracion 3 es usar `Diccionario<Integer, Boolean>`
como si fuera un **conjunto de enteros**. No tenemos un TDA `Conjunto` dedicado
para este uso, pero el Diccionario cumple exactamente el mismo rol:

| Operacion de Set | Equivalente con Diccionario |
|------------------|----------------------------|
| `set.add(x)` | `diccionario.insertar(x, true)` |
| `set.contains(x)` | `diccionario.contiene(x)` |
| `set.remove(x)` | `diccionario.eliminar(x)` |
| `set.size()` | `diccionario.getCantidad()` |
| `set.toArray()` | `diccionario.obtenerClaves()` |

El valor `Boolean` es irrelevante; solo nos importa la **presencia de la clave**.
Esto se usa en:

- **`Cliente.amistades`**: "El conjunto de IDs de mis amigos"
- **`visitados` en BFS**: "El conjunto de IDs que ya visite"
- (ya existia en iter 1-2) **`Cliente.siguiendo`**: "El conjunto de IDs que sigo"
- (ya existia en iter 1-2) **`Cliente.seguidores`**: "El conjunto de IDs que me siguen"

### Cola<Integer> en el BFS

La Cola se usa como **frontera de exploracion** del BFS. Su politica FIFO
garantiza el recorrido por niveles:

```
    Nivel 0: [origen]
    Nivel 1: [vecinos directos del origen]
    Nivel 2: [vecinos de los vecinos]
    ...

    La Cola procesa primero los de Nivel 1, luego los de Nivel 2, etc.
    Esto garantiza que cuando encontramos el destino, la distancia es la minima.
```

La Cola se crea **localmente** dentro de `calcularDistancia()`. No es un campo
persistente — existe solo durante la ejecucion del algoritmo y se destruye al
retornar.

---

## 8. Analisis de Complejidad

### Amistades

| Operacion | Complejidad | Justificacion |
|-----------|-------------|---------------|
| `agregarAmistad(A, B)` | O(1) amort. | 2x `Diccionario.insertar()` + 2x `Diccionario.obtener()` |
| `eliminarAmistad(A, B)` | O(1) amort. | 2x `Diccionario.eliminar()` + 2x `Diccionario.obtener()` |
| `sonAmigos(A, B)` | O(1) amort. | 1x `Diccionario.obtener()` + 1x `Diccionario.contiene()` |
| `obtenerAmigos(A)` | O(k) | k = cantidad de amigos, recorrer claves del Diccionario |
| `obtenerCantidadAmigos(A)` | O(1) | `Diccionario.getCantidad()` |

### Distancia BFS

| Aspecto | Complejidad | Justificacion |
|---------|-------------|---------------|
| Tiempo total | O(V + E) | Cada vertice se visita maximo 1 vez, cada arista se explora maximo 1 vez |
| Espacio (Cola) | O(V) | En el peor caso, todos los vertices estan en la Cola a la vez |
| Espacio (visitados) | O(V) | Un entry por cada vertice visitado |
| Por operacion de Cola | O(1) | `encolar()` y `desencolar()` son O(1) en nuestra implementacion |
| Por consulta de visitado | O(1) amort. | `Diccionario.contiene()` es O(1) amortizado |

Donde:
- **V** = cantidad de vertices (clientes) alcanzables desde el origen
- **E** = cantidad de aristas (relaciones "sigue a") entre esos vertices

### Por que O(V + E)?

```
    Cada vertice se desencola exactamente 1 vez          -> O(V) desencolamientos
    Para cada vertice, recorremos sus vecinos (aristas)  -> O(E) aristas exploradas
    Cada operacion individual (encolar, desencolar,
    insertar visitado, consultar visitado) es O(1)       -> O(1) por operacion

    Total: O(V) * O(1) + O(E) * O(1) = O(V + E)
```

---

## 9. Archivos Modificados

### Modelo

| Archivo | Cambio | Lineas |
|---------|--------|--------|
| `src/modelo/Cliente.java` | Campo `amistades: Diccionario<Integer, Boolean>` + 8 metodos | +65 |

Metodos agregados:
- `agregarAmistad(int)`, `eliminarAmistad(int)`, `obtenerAmigos()`,
  `getCantidadAmigos()`, `esAmigoDE(int)`, `getAmistadesSerialized()`,
  `cargarAmistades(int[])`

### Servicio

| Archivo | Cambio | Lineas |
|---------|--------|--------|
| `src/servicio/GestorRelaciones.java` | BFS `calcularDistancia()` + 5 metodos de amistad | +180 |
| `src/servicio/GestorClientes.java` | 6 delegaciones (distancia + amistades) | +55 |
| `src/servicio/PersistenciaClientes.java` | DTO `amistades` + carga/guardado | +10 |

### Vista

| Archivo | Cambio | Lineas |
|---------|--------|--------|
| `src/vista/MenuSolicitudes.java` | Opcion 4 "Amistades" + Opcion 5 "Distancia BFS" | +195 |

### Tests

| Archivo | Descripcion |
|---------|-------------|
| `test/AmistadTest.java` | 7 tests: agregar, bidireccionalidad, eliminar, obtener, verificar, consigo mismo |
| `test/DistanciaTest.java` | 7 tests: mismo cliente, 1 hop, 2 hops, sin camino, inexistente, desconectado, dirigido |
| `test/casos_de_uso/CU_028` a `CU_035` | 8 casos de uso: amistades + distancia |
| `test/casos_de_uso/performance/PERF_004` | Validacion O(V+E) del BFS |

---

## 10. Tests

Ejecutar toda la suite:

```bash
./test.sh
```

Tests especificos de iteracion 3:

```bash
# Amistades
javac -cp "lib/gson-2.10.1.jar" -d out test/AmistadTest.java src/**/*.java
java -ea -cp out:"lib/gson-2.10.1.jar" AmistadTest

# Distancia BFS
javac -cp "lib/gson-2.10.1.jar" -d out test/DistanciaTest.java src/**/*.java
java -ea -cp out:"lib/gson-2.10.1.jar" DistanciaTest
```

### Que validan los tests

**AmistadTest (7 tests):**
1. Agregar una amistad retorna true
2. La amistad es bidireccional (sonAmigos en ambas direcciones)
3. Eliminar una amistad retorna true
4. Eliminar es bidireccional (ambos lados eliminados)
5. obtenerAmigos retorna la cantidad correcta
6. sonAmigos distingue amigos de no-amigos
7. No se puede ser amigo de si mismo

**DistanciaTest (7 tests):**
1. Distancia de A a A = 0
2. Conexion directa (A -> B) = 1
3. Camino de 2 saltos (A -> B -> C) = 2
4. Sin camino posible = -1
5. Cliente inexistente = -1
6. Grafo desconectado (dos componentes) = -1
7. Las relaciones son dirigidas (A -> B != B -> A)
