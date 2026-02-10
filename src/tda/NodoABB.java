package tda;

/*
 * Nodo para el Árbol Binario de Búsqueda (ABB).
 * Almacena una clave (Comparable) y un valor asociado.
 * Para manejar colisiones (mismo scoring para diferentes clientes),
 * el valor podría ser una lista, pero por simplicidad en esta iteración
 * asumiremos que la lógica de colisión se maneja en el ABB o nivel superior
 * permitiendo nodos duplicados o listas.
 * 
 * En este caso, para soportar múltiples clientes con el mismo scoring sin listas en el nodo,
 * usaremos la inserción convencional de ABB donde valores iguales van a la derecha/izquierda 
 * consistentemente, o almacenaremos una lista de valores.
 * 
 * ESTRATEGIA: Lista de valores por nodo para manejar claves duplicadas eficientemente.
 */
public class NodoABB<K extends Comparable<K>, V> {
    private K clave;
    private V valor; // Payload simple
    // Para simplificar y no depender de TDA Lista ajena, usaremos un modelo de nodo simple
    // Si hay colisión de clave, el ABB insertará un nuevo nodo en la rama correspondiente (derecha >=).
    
    private NodoABB<K, V> izquierdo;
    private NodoABB<K, V> derecho;

    public NodoABB(K clave, V valor) {
        this.clave = clave;
        this.valor = valor;
        this.izquierdo = null;
        this.derecho = null;
    }

    public K getClave() {
        return clave;
    }

    public V getValor() {
        return valor;
    }

    public void setValor(V valor) {
        this.valor = valor;
    }

    public NodoABB<K, V> getIzquierdo() {
        return izquierdo;
    }

    public void setIzquierdo(NodoABB<K, V> izquierdo) {
        this.izquierdo = izquierdo;
    }

    public NodoABB<K, V> getDerecho() {
        return derecho;
    }

    public void setDerecho(NodoABB<K, V> derecho) {
        this.derecho = derecho;
    }
}
