package tda;

/*
Nodo para la estructura de Diccionario.
Almacena un par clave-valor genérico y referencia al siguiente nodo.
 */
public class NodoDiccionario<K, V> {
    
    /* Atributos */
    private K clave;
    private V valor;
    private NodoDiccionario<K, V> siguiente;

    /*
    Constructor que inicializa el nodo con una clave y valor.
    */
    public NodoDiccionario(K clave, V valor) {
        this.clave = clave;
        this.valor = valor;
        this.siguiente = null;
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

    public NodoDiccionario<K, V> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoDiccionario<K, V> siguiente) {
        this.siguiente = siguiente;
    }
}
