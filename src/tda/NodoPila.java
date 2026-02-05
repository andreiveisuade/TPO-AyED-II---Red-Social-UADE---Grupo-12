package tda;

/*
Nodo para la estructura de Pila.
Almacena un dato genérico y referencia al siguiente nodo.
 */
public class NodoPila<T> {
    
    /* Atributos */
    private T dato;
    private NodoPila<T> siguiente;

    /*
    Constructor que inicializa el nodo con un dato.
    */
    public NodoPila(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    public T getDato() {
        return dato;
    }

    public NodoPila<T> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoPila<T> siguiente) {
        this.siguiente = siguiente;
    }
}
