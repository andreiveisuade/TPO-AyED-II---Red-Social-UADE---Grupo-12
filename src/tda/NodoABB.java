package tda;

/*
 * Nodo para el Árbol Binario de Búsqueda (ABB).
 * Almacena una clave (Comparable) y una Cola de valores asociados.
 *
 * ESTRATEGIA: Cola de valores por nodo para manejar claves duplicadas eficientemente.
 * Cuando múltiples valores comparten la misma clave, se almacenan en la Cola del nodo
 * en lugar de crear nodos separados encadenados. Esto mantiene la altura del árbol
 * proporcional a la cantidad de claves DISTINTAS, no al total de valores.
 */
public class NodoABB<K extends Comparable<K>, V> {
    private K clave;
    private Cola<V> valores;

    private NodoABB<K, V> izquierdo;
    private NodoABB<K, V> derecho;

    public NodoABB(K clave, V valor) {
        this.clave = clave;
        this.valores = new Cola<>();
        this.valores.encolar(valor);
        this.izquierdo = null;
        this.derecho = null;
    }

    public K getClave() {
        return clave;
    }

    public V getValor() {
        return valores.verFrente();
    }

    public void setValor(V valor) {
        // Reemplaza todos los valores con uno solo
        this.valores = new Cola<>();
        this.valores.encolar(valor);
    }

    public Cola<V> getValores() {
        return valores;
    }

    public void agregarValor(V valor) {
        valores.encolar(valor);
    }

    public int getCantidadValores() {
        return valores.getCantidad();
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
