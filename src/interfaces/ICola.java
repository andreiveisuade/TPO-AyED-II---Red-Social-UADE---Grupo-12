package interfaces;

/*
Interfaz genérica para estructura FIFO (First In, First Out).
Define el contrato para el TDA Cola.

SOLID: ISP - Interfaz específica para operaciones de cola
SOLID: DIP - Permite depender de la abstracción
*/
public interface ICola<T> {

    /*
    Agrega un elemento al final de la cola.
    */
    void encolar(T dato);

    /*
    Remueve y retorna el elemento del frente de la cola.
    Retorna null si está vacía.
    */
    T desencolar();

    /*
    Consulta el elemento del frente sin removerlo.
    Retorna null si está vacía.
    */
    T verFrente();

    /*
    Verifica si la cola está vacía.
    */
    boolean estaVacia();

    /*
    Retorna la cantidad de elementos en la cola.
    */
    int getCantidad();
}
