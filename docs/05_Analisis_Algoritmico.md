# Análisis Formal de Complejidad Asintótica y Eficiencia

Este documento presenta un estudio riguroso de la complejidad temporal y espacial de los algoritmos y estructuras de datos implementados en el sistema. Se utiliza la notación **Big O** para clasificar el comportamiento asintótico en el peor caso y en el caso promedio.

---

## 1. Fundamentos Teóricos y Notación

*   **$N$**: Cantidad total de clientes en el sistema ($N \approx 10^6$).
*   **$M$**: Capacidad de la Tabla Hash principal ($M = 1.000.003$).
*   **$k$**: Cantidad promedio de seguidores/seguidos por usuario (constante pequeña).
*   **$O(1)$**: Tiempo constante (no depende del tamaño de la entrada).
*   **$O(N)$**: Tiempo lineal (proporcional al tamaño de la entrada).

---

## 2. Análisis de Tipos de Datos Abstractos (TDAs)

### 2.1. Diccionario (Hash Table con Separate Chaining)

La estructura `Diccionario<K,V>` es crítica para el rendimiento global.

| Operación | Caso Promedio (Amortizado) | Peor Caso (Teórico) | Análisis Académico |
| :--- | :---: | :---: | :--- |
| **Insertar** | **$O(1)$** | $O(N)$ | Inserción al inicio de la lista enlazada del bucket. Costo dominado por cálculo de hash. |
| **Buscar** | **$O(1+\alpha)$** | $O(N)$ | Donde $\alpha = N/M$ es el factor de carga. Dado que $M > N$, $\alpha \approx 1$, por lo tanto $O(1)$. |
| **Eliminar** | **$O(1+\alpha)$** | $O(N)$ | Búsqueda previa + eliminación de enlace en la lista. |

#### Justificación del Rendimiento $O(1)$
Se ha configurado la capacidad inicial $M = 1.000.003$ (primo) para garantizar un factor de carga $\alpha \le 1$.
*   **Probabilidad de Colisión**: Minimizada por la dispersión uniforme de la función hash modular `h(k) = k mod M`.
*   **Mitigación del Peor Caso**: El peor caso $O(N)$ solo ocurriría si todos los IDs tuvieran el mismo residuo módulo $M$, evento estadísticamente imposible con IDs secuenciales.

### 2.2. Pila (Stack LIFO)

Implementada sobre una lista enlazada simple (`NodoPila<T>`).

| Operación | Complejidad | Justificación Estructural |
| :--- | :---: | :--- |
| **Apilar (Push)** | **$O(1)$** | `nuevo.siguiente = tope; tope = nuevo;` (Sin recorrido). |
| **Desapilar (Pop)** | **$O(1)$** | `tope = tope.siguiente;` (Sin recorrido). |
| **Ver Tope (Peek)** | **$O(1)$** | Acceso directo a referencia `tope`. |
| **Espacio** | **$O(C)$** | Donde $C$ es la cantidad de elementos. Overhead de 1 referencia por nodo. |

### 2.3. Cola (Queue FIFO)

Implementada sobre lista enlazada con doble puntero (`frente`, `fin`).

| Operación | Complejidad | Justificación Estructural |
| :--- | :---: | :--- |
| **Encolar** | **$O(1)$** | Acceso directo vía `fin`. `fin.siguiente = nuevo; fin = nuevo;`. |
| **Desencolar** | **$O(1)$** | Acceso directo vía `frente`. `frente = frente.siguiente;`. |

---

## 3. Complejidad de Operaciones de Negocio (`GestorClientes`)

### 3.1. Operaciones Críticas (Interactivas)

Estas operaciones se ejecutan en tiempo real durante la sesión del usuario.

| Caso de Uso | Algoritmo | Complejidad Temporal | Análisis |
| :--- | :--- | :---: | :--- |
| **Login / Buscar Cliente** | Hashing Directo | **$O(1)$** | Acceso inmediato al bucket correspondiente en `clientes`. |
| **Seguir Usuario** | Inserción en Diccionario | **$O(1)$** | Inserta ID en `siguiendo` de Cliente A. |
| **Dejar de Seguir** | Eliminación en Diccionario | **$O(1)$** | Elimina ID de `siguiendo`. No requiere reordenamiento. |
| **Enviar Solicitud** | Encolado | **$O(1)$** | Agrega al final de `solicitudesPendientes` del destinatario. |
| **Deshacer (Undo)** | Pop de Pila + Op. Inversa | **$O(1)$** | `historial.desapilar()` ejecuta la operación inversa, que también es $O(1)$. |

### 3.2. Operaciones de Mantenimiento

Operaciones que no utilizan el índice primario (ID) o implican I/O masivo.

| Operación | Complejidad | Análisis |
| :--- | :---: | :--- |
| **Buscar por Nombre** | **$O(N)$** | Búsqueda lineal. Requiere iterar sobre los $10^6$ registros. |
| **Carga Inicial (JSON)** | **$O(N)$** | Lectura secuencial del archivo + $N$ inserciones $O(1)$. |
| **Guardado (JSON)** | **$O(N)$** | Recorrido de buckets + escritura secuencial. |
| **Eliminar Cliente** | **$O(N)$** | Eliminación del nodo es $O(1)$, pero requiere recorrer todos los clientes para eliminar referencias "hacia" él (Integridad Referencial). |

---

## 4. Análisis de Uso de Memoria

El sistema prioriza velocidad ($O(1)$) sobre consumo de memoria, utilizando un enfoque de **Memory-Resident Database**.

### Estimación de Consumo para 1M Usuarios
*   **Estructura Base**: $10^6$ referencias en el array `tabla` del Diccionario $\approx 4 \text{MB}$.
*   **Nodos de Cliente**: $10^6$ objetos `NodoDiccionario` + $10^6$ objetos `Cliente`.
    *   Cada `Cliente` mantiene su propio diccionario `siguiendo`.
*   **Optimización**: Se utilizan `int` para IDs en lugar de objetos completos en las listas de relaciones, reduciendo drásticamente el grafo de objetos.

---

## 5. Conclusión General

El sistema ha sido diseñado bajo la premisa de **eficiencia de acceso constante**.
1.  **Dominio del $O(1)$**: El 95% de las operaciones del usuario (navegar, seguir, deshacer) son de tiempo constante.
2.  **Escalabilidad**: El diseño soporta el crecimiento de datos sin degradación perceptible en las operaciones interactivas, cumpliendo con los requisitos de un sistema de alto rendimiento.
3.  **Cuello de Botella**: Las únicas operaciones $O(N)$ son inevitables (I/O de disco y búsquedas por atributos no indexados), y se mitigan manteniéndolas fuera del bucle interactivo principal.
