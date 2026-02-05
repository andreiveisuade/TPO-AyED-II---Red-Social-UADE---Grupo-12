package interfaces;

/*
Interfaz genérica para estructura LIFO (Last In, First Out).
Define el contrato para el TDA Pila.

SOLID: ISP - Interfaz específica para operaciones de pila
SOLID: DIP - Permite depender de la abstracción
*/
public interface IPila<T> {
    
    /*
    Agrega un elemento al tope de la pila.
    */
    void apilar(T dato);

    /*
    Remueve y retorna el elemento del tope de la pila.
    Retorna null si está vacía.
    */
    T desapilar();

    /*
    Consulta el elemento del tope sin removerlo.
    Retorna null si está vacía.
    */
    T verTope();

    /*
    Verifica si la pila está vacía.
    */
    boolean estaVacia();

    /*
    Retorna la cantidad de elementos en la pila.
    */
    int getCantidad();

    /*
    Retorna un array con todos los elementos sin modificar la pila.
    Orden: del tope (índice 0) al fondo.
    */
    Object[] toArray();
}
