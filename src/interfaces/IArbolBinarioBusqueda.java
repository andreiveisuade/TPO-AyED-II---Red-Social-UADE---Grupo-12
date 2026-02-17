package interfaces;

/*
Interfaz genérica para Árbol Binario de Búsqueda (ABB).
Define el contrato para el TDA ABB ordenado por clave.

SOLID: ISP - Interfaz específica para operaciones de árbol
SOLID: DIP - Permite depender de la abstracción
*/
public interface IArbolBinarioBusqueda<K extends Comparable<K>, V> {

    /*
    Inserta un par clave-valor en el árbol.
    Permite duplicados: claves iguales se insertan a la derecha.
    Complejidad: O(log N) promedio, O(N) peor caso (árbol degenerado).
    */
    void insertar(K clave, V valor);

    /*
    Busca todos los valores asociados a una clave.
    Retorna array vacío si la clave no existe.
    Complejidad: O(log N + k) donde k = cantidad de valores con esa clave.
    */
    Object[] buscar(K clave);

    /*
    Elimina un valor específico asociado a una clave.
    Retorna true si se eliminó, false si no se encontró.
    Complejidad: O(log N).
    */
    boolean eliminar(K clave, V valor);

    /*
    Verifica si el árbol está vacío.
    Complejidad: O(1).
    */
    boolean estaVacio();

    /*
    Retorna la cantidad total de nodos en el árbol.
    Complejidad: O(1).
    */
    int getCantidad();

    /*
    Obtiene todos los valores que están en el nivel N del árbol.
    Nivel 0 = raíz, nivel 1 = hijos de raíz, etc.
    Retorna array vacío si el nivel no existe.
    Complejidad: O(N) - debe recorrer hasta ese nivel.
    */
    Object[] obtenerEnNivel(int nivel);

    /*
    Retorna la altura del árbol (máxima distancia raíz-hoja).
    Árbol vacío tiene altura -1, árbol con solo raíz tiene altura 0.
    Complejidad: O(N).
    */
    int getAltura();
}
