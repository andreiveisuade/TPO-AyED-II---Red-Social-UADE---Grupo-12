package interfaces;

/*
Interfaz para estructura de conjunto (elementos únicos).
Define el contrato para el TDA Conjunto.

SOLID: ISP - Interfaz específica para operaciones de conjunto
SOLID: DIP - Permite depender de la abstracción
*/
public interface IConjunto {

    /*
    Agrega un elemento al conjunto.
    Si el elemento ya existe, no hace nada (o actualiza, dependiendo de implementación interna, pero conceptualmente es idempotente).
    */
    void agregar(String elemento);

    /*
    Verifica si el conjunto contiene un elemento.
    */
    boolean contiene(String elemento);

    /*
    Elimina un elemento del conjunto.
    */
    void eliminar(String elemento);

    /*
    Retorna la cantidad de elementos en el conjunto.
    */
    int getCantidad();

    /*
    Verifica si el conjunto está vacío.
    */
    boolean estaVacio();

    /*
    Retorna todos los elementos del conjunto.
    */
    String[] obtenerElementos();
}
