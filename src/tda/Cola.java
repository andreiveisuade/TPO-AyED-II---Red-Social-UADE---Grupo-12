package tda;

import interfaces.ICola;

/*
TDA Cola - Estructura FIFO (First In, First Out)

INVARIANTE DE REPRESENTACIÓN:
- cantidad >= 0 siempre
- (frente == null) <==> (fin == null) <==> (cantidad == 0)
- Si cantidad == 1, entonces frente == fin
- Si cantidad > 1, entonces frente != fin
- La cantidad de nodos enlazados desde frente hasta fin es exactamente igual a cantidad
- SOLID: DIP - Implementa interfaz ICola
 */
public class Cola<T> implements ICola<T> {
    
    /* Atributos */
    private NodoCola<T> frente;
    private NodoCola<T> fin;
    private int cantidad;

    /*
    Constructor que inicializa la cola vacía.
    */
    public Cola() {
        this.frente = null;
        this.fin = null;
        this.cantidad = 0;
    }

    /*
    Agrega un elemento al final de la cola.
    */
    @Override
    public void encolar(T dato) {
        NodoCola<T> nuevo = new NodoCola<>(dato);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
        cantidad++;
    }

    /*
    Remueve y retorna el elemento del frente de la cola.
    */
    @Override
    public T desencolar() {
        if (estaVacia()) {
            return null;
        }
        T dato = frente.getDato();
        frente = frente.getSiguiente();
        if (frente == null) {
            fin = null;
        }
        cantidad--;
        return dato;
    }

    /*
    Consulta el elemento del frente sin removerlo.
    */
    @Override
    public T verFrente() {
        if (estaVacia()) {
            return null;
        }
        return frente.getDato();
    }

    /*
    Verifica si la cola está vacía.
    */
    @Override
    public boolean estaVacia() {
        return frente == null;
    }

    /*
    Retorna la cantidad de elementos en la cola.
    */
    @Override
    public int getCantidad() {
        return cantidad;
    }
}
