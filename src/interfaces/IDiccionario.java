package interfaces;

/*
Interfaz genérica para estructura de diccionario (clave-valor).
Define el contrato para el TDA Diccionario.

SOLID: ISP - Interfaz específica para operaciones de diccionario
SOLID: DIP - Permite depender de la abstracción
*/
public interface IDiccionario<K, V> {

    /*
    Inserta un par clave-valor.
    Si la clave ya existe, actualiza el valor.
    */
    void insertar(K clave, V valor);

    /*
    Obtiene el valor asociado a una clave.
    Retorna null si la clave no existe.
    */
    V obtener(K clave);

    /*
    Verifica si existe una clave en el diccionario.
    */
    boolean contiene(K clave);

    /*
    Elimina un par clave-valor asociado a la clave.
    Retorna el valor eliminado o null si no existe.
    */
    V eliminar(K clave);

    /*
    Retorna la cantidad de pares clave-valor en el diccionario.
    */
    int getCantidad();

    /*
    Verifica si el diccionario está vacío.
    */
    boolean estaVacio();

    /*
    Retorna todas las claves del diccionario.
    */
    String[] obtenerClaves();

    /*
    Retorna todos los valores del diccionario.
    */
    Object[] obtenerValores();
}
