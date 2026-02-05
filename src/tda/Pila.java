package tda;

import interfaces.IPila;

/*
TDA Pila - Estructura LIFO (Last In, First Out)

INVARIANTE DE REPRESENTACIÓN:
- cantidad >= 0 siempre
- (tope == null) <==> (cantidad == 0)
- Si cantidad > 0, entonces tope != null
- La cantidad de nodos enlazados desde tope es exactamente igual a cantidad
- SOLID: DIP - Implementa interfaz IPila
 */
public class Pila<T> implements IPila<T> {
    
    /* Atributos */
    private NodoPila<T> tope;
    private int cantidad;

    /*
    Constructor que inicializa la pila vacía.
    */
    public Pila() {
        this.tope = null;
        this.cantidad = 0;
    }

    /*
    Agrega un elemento al tope de la pila.
    */
    @Override
    public void apilar(T dato) {
        NodoPila<T> nuevo = new NodoPila<>(dato);
        nuevo.setSiguiente(tope);
        tope = nuevo;
        cantidad++;
    }

    /*
    Remueve y retorna el elemento del tope de la pila.
    */
    @Override
    public T desapilar() {
        if (estaVacia()) {
            return null;
        }
        T dato = tope.getDato();
        tope = tope.getSiguiente();
        cantidad--;
        return dato;
    }

    /*
    Consulta el elemento del tope sin removerlo.
    */
    @Override
    public T verTope() {
        if (estaVacia()) {
            return null;
        }
        return tope.getDato();
    }

    /*
    Verifica si la pila está vacía.
    */
    @Override
    public boolean estaVacia() {
        return tope == null;
    }

    /*
    Retorna la cantidad de elementos en la pila.
    */
    @Override
    public int getCantidad() {
        return cantidad;
    }

    /*
    Retorna un array con todos los elementos sin modificar la pila.
    Recorre los nodos directamente, O(n) con un solo recorrido.
    */
    @Override
    public Object[] toArray() {
        Object[] resultado = new Object[cantidad];
        NodoPila<T> actual = tope;
        int i = 0;
        while (actual != null) {
            resultado[i++] = actual.getDato();
            actual = actual.getSiguiente();
        }
        return resultado;
    }
}
