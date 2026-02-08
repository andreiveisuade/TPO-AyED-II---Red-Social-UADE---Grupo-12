# Especificación de Tipos de Datos Abstractos (TDAs)

Este documento justifica técnica y teóricamente la elección de las estructuras de datos, detallando su implementación en Java, complejidad asintótica y aplicación en el dominio del problema.

---

## 1. Resumen de Complejidad

Se ha priorizado un límite superior asintótico de **O(1)** para las operaciones más frecuentes del sistema. Todas las estructuras son **genéricas** para maximizar la reutilización.

| Estructura | Interfaz | Implementación | Acceso | Inserción | Eliminación | Uso Principal |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Diccionario** | `IDiccionario<K,V>` | Hash Table (Addressable) | O(1)* | O(1)* | O(1)* | Indexación de clientes y relaciones. |
| **Pila** | `IPila<T>` | Linked List (LIFO) | O(1) | O(1) | O(1) | Gestión del historial (Undo). |
| **Cola** | `ICola<T>` | Linked List (FIFO) | O(1) | O(1) | O(1) | Buffer de solicitudes de seguimiento. |
| **Conjunto** | `IConjunto` | Hash Set Adapter | O(1)* | O(1)* | O(1)* | Colecciones de elementos únicos (Strings). |

*\* Amortizado promedio.*

---

## 2. Diccionario (Hash Table)

### 2.1. Definición
Estructura asociativa genérica `Diccionario<K, V>` que almacena pares clave-valor, permitiendo la recuperación eficiente de valores a partir de su clave única.

### 2.2. Implementación Técnica
*   **Estrategia**: Open Hashing (Encadenamiento separado).
*   **Estructura Interna**: Arreglo de nodos (`NodoDiccionario<K, V>[] table`).
*   **Función Hash**: Modular aritmética (`Math.abs(key.hashCode()) % capacidad`).
*   **Capacidad**:
    *   **Por defecto**: 64 buckets.
    *   **Optimizada**: En `GestorClientes`, se inicializa con **1,000,003** buckets (número primo) para minimizar colisiones con el dataset de 1M de clientes.
*   **Colisiones**: Resueltas mediante listas enlazadas simples en cada bucket.
*   **Sin Rehashing**: La capacidad es fija tras la instanciación para garantizar previsibilidad en el consumo de memoria.

### 2.3. Análisis de Complejidad
*   **Caso Promedio**: O(1), gracias a la distribución uniforme de claves y la capacidad optimizada.
*   **Peor Caso**: O(n), degenerando en una lista enlazada si todas las claves colisionan (mitigado por el tamaño de la tabla).

### 2.4. Aplicación en el Sistema
*   **Gestión de Clientes**: `GestorClientes` utiliza `Diccionario<Integer, Cliente>` para mapear IDs a instancias de objetos.
*   **Relaciones**: Cada `Cliente` mantiene un `Diccionario<Integer, Boolean>` llamado `siguiendo` para verificar relaciones de seguimiento en tiempo constante O(1).

---

## 3. Pila (Stack)

### 3.1. Definición
Colección lineal genérica `Pila<T>` que sigue la política **LIFO** (Last In, First Out).

### 3.2. Implementación Técnica
*   **Estructura**: Lista enlazada simple de nodos (`NodoPila<T>`).
*   **Punteros**: Referencia única al nodo `tope`.
*   **Tipado**: Genérico, permite almacenar cualquier tipo de objeto.

### 3.3. Análisis de Complejidad
Todas las operaciones primitivas (`apilar`, `desapilar`, `verTope`) manipulan únicamente la referencia al tope, garantizando una complejidad temporal constante **O(1)** independiente del tamaño de la pila.

### 3.4. Aplicación en el Sistema
Se utiliza implementar el patrón **Command** para la funcionalidad de deshacer:
*   **Sesion**: Mantiene una instancia de `HistorialAcciones` que encapsula una `Pila<Accion>`.
*   **Undo**: Las acciones realizadas (`Accion`) se apilan y, al deshacer, se desapilan para revertir el estado.

---

## 4. Cola (Queue)

### 4.1. Definición
Colección lineal genérica `Cola<T>` que sigue la política **FIFO** (First In, First Out).

### 4.2. Implementación Técnica
*   **Estructura**: Lista enlazada simple de nodos (`NodoCola<T>`).
*   **Punteros**: Referencias explícitas a `frente` (inicio) y `fin` (final) para permitir inserciones al final y eliminaciones al frente sin recorrer la estructura.

### 4.3. Análisis de Complejidad
*   **Encolar**: Inserción al final usando el puntero `fin` → **O(1)**.
*   **Desencolar**: Eliminación del frente usando el puntero `frente` → **O(1)**.

### 4.4. Aplicación en el Sistema
*   **Solicitudes Pendientes**: Cada `Cliente` tiene una `Cola<SolicitudSeguimiento>` para gestionar las peticiones recibidas. Garantiza que las solicitudes se procesen en el orden estricto de llegada.

---

## 5. Conjunto (Set)

### 5.1. Definición
Colección de elementos únicos sin orden específico.

### 5.2. Implementación Técnica
*   **Patrón Adapter**: La clase `Conjunto` utiliza internamente un `Diccionario<String, Boolean>`.
*   **Unicidad**: Garantizada por la propiedad de claves únicas del Diccionario subyacente.
*   **Valor Dummy**: Se almacena `true` como valor para todas las claves.

---

## 6. Abstracción y Polimorfismo

El sistema adhiere al **Principio de Inversión de Dependencias (DIP)** mediante la definición de interfaces para cada estructura en el paquete `interfaces`:

*   `IDiccionario<K,V>`
*   `IPila<T>`
*   `ICola<T>`
*   `IConjunto`

Esto desacopla la implementación concreta (listas enlazadas, arreglos, buckets) de la lógica de negocio (Gestores, Modelos), facilitando el mantenimiento y las pruebas.

---

## 7. Diagrama de Estructuras

A continuación se esquematiza la jerarquía y composición de los TDAs en el modelo de objetos actual:

```mermaid
classDiagram
    class GestorClientes
    class Cliente
    class Sesion
    class HistorialAcciones
    
    class "Diccionario<Integer, Cliente>" as DicClientes
    class "Diccionario<Integer, Boolean>" as DicSeguidos
    class "Cola<SolicitudSeguimiento>" as ColaSolicitudes
    class "Pila<Accion>" as PilaHistorial

    GestorClientes --> DicClientes : usa (clientes)
    Cliente --> DicSeguidos : usa (siguiendo)
    Cliente --> ColaSolicitudes : usa (solicitudesPendientes)
    Sesion --> HistorialAcciones : posee
    HistorialAcciones --> PilaHistorial : usa (pilaAcciones)
```

## 8. Conclusión

La selección de estas estructuras de datos y su implementación manual en Java (sin usar Collections Framework) permite un control total sobre la gestión de memoria y la complejidad algorítmica. La optimización de capacidad en el Diccionario principal es clave para procesar 1.000.000 de registros con tiempos de respuesta inmediatos.
