package tda;

/*
Nodo para la estructura de Cola.
Almacena un dato genérico y referencia al siguiente nodo.
 */
public class NodoCola<T> {
    
    /* Atributos */
    private T dato;
    private NodoCola<T> siguiente;

    /*
    Constructor que inicializa el nodo con un dato.
    */
    public NodoCola(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    public T getDato() {
        return dato;
    }

    public NodoCola<T> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoCola<T> siguiente) {
        this.siguiente = siguiente;
    }
}
