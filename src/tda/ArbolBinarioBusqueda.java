package tda;

import interfaces.IArbolBinarioBusqueda;

/*
TDA Árbol Binario de Búsqueda (ABB) - Estructura ordenada por clave.

INVARIANTE DE REPRESENTACIÓN:
- Para todo nodo N: claves en subárbol izquierdo < N.clave
- Para todo nodo N: claves en subárbol derecho > N.clave
- Claves duplicadas se agrupan en la Cola del mismo nodo
- cantidad >= 0 siempre (cuenta total de valores, no de nodos)
- (raiz == null) <=> (cantidad == 0)

COMPLEJIDAD TEMPORAL (D = claves distintas):
- Insertar: O(log D) promedio
- Buscar: O(log D + k) donde k = cantidad con misma clave
- Eliminar: O(log D + k)
- Obtener en nivel: O(N)

SOLID: DIP - Implementa interfaz IArbolBinarioBusqueda
*/
public class ArbolBinarioBusqueda<K extends Comparable<K>, V>
    implements IArbolBinarioBusqueda<K, V> {

    /* Atributos */
    private NodoABB<K, V> raiz;
    private int cantidad;  // Total de valores (no de nodos)

    /*
    Constructor que inicializa el árbol vacío.
    */
    public ArbolBinarioBusqueda() {
        this.raiz = null;
        this.cantidad = 0;
    }

    /*
    Inserta un par clave-valor en el árbol.
    Si la clave ya existe, agrega el valor a la Cola del nodo existente.
    */
    @Override
    public void insertar(K clave, V valor) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser null");
        }
        raiz = insertarRecursivo(raiz, clave, valor);
        cantidad++;
    }

    /*
    Método auxiliar recursivo para inserción.
    Agrupa duplicados en el mismo nodo.
    */
    private NodoABB<K, V> insertarRecursivo(NodoABB<K, V> nodo, K clave, V valor) {
        if (nodo == null) {
            return new NodoABB<>(clave, valor);
        }

        int comparacion = clave.compareTo(nodo.getClave());

        if (comparacion < 0) {
            nodo.setIzquierdo(insertarRecursivo(nodo.getIzquierdo(), clave, valor));
        } else if (comparacion > 0) {
            nodo.setDerecho(insertarRecursivo(nodo.getDerecho(), clave, valor));
        } else {
            // Clave duplicada: agregar valor a la Cola del nodo existente
            nodo.agregarValor(valor);
        }

        return nodo;
    }

    /*
    Busca todos los valores asociados a una clave.
    Retorna array de valores encontrados.
    */
    @Override
    public Object[] buscar(K clave) {
        if (clave == null) {
            return new Object[0];
        }

        NodoABB<K, V> nodo = buscarNodo(raiz, clave);
        if (nodo == null) {
            return new Object[0];
        }

        return colaAArray(copiarCola(nodo.getValores()));
    }

    /*
    Busca el nodo con la clave dada.
    */
    private NodoABB<K, V> buscarNodo(NodoABB<K, V> nodo, K clave) {
        if (nodo == null) {
            return null;
        }

        int comparacion = clave.compareTo(nodo.getClave());

        if (comparacion < 0) {
            return buscarNodo(nodo.getIzquierdo(), clave);
        } else if (comparacion > 0) {
            return buscarNodo(nodo.getDerecho(), clave);
        } else {
            return nodo;
        }
    }

    /*
    Elimina un valor específico asociado a una clave.
    Si el nodo queda sin valores, se elimina del árbol.
    */
    @Override
    public boolean eliminar(K clave, V valor) {
        if (clave == null || valor == null) {
            return false;
        }

        NodoABB<K, V> nodo = buscarNodo(raiz, clave);
        if (nodo == null) {
            return false;
        }

        // Buscar y remover el valor de la Cola
        Cola<V> valores = nodo.getValores();
        Cola<V> nueva = new Cola<>();
        boolean encontrado = false;

        while (!valores.estaVacia()) {
            V v = valores.desencolar();
            if (!encontrado && v.equals(valor)) {
                encontrado = true; // Remover solo la primera ocurrencia
            } else {
                nueva.encolar(v);
            }
        }

        // Restaurar la cola (sin el elemento eliminado)
        while (!nueva.estaVacia()) {
            valores.encolar(nueva.desencolar());
        }

        if (!encontrado) {
            return false;
        }

        cantidad--;

        // Si el nodo quedó vacío, eliminarlo del árbol
        if (valores.estaVacia()) {
            raiz = eliminarNodo(raiz, clave);
        }

        return true;
    }

    /*
    Elimina un nodo del árbol (cuando su Cola queda vacía).
    */
    private NodoABB<K, V> eliminarNodo(NodoABB<K, V> nodo, K clave) {
        if (nodo == null) {
            return null;
        }

        int comparacion = clave.compareTo(nodo.getClave());

        if (comparacion < 0) {
            nodo.setIzquierdo(eliminarNodo(nodo.getIzquierdo(), clave));
        } else if (comparacion > 0) {
            nodo.setDerecho(eliminarNodo(nodo.getDerecho(), clave));
        } else {
            // Nodo encontrado - eliminarlo

            // Caso 1: Sin hijos
            if (nodo.getIzquierdo() == null && nodo.getDerecho() == null) {
                return null;
            }

            // Caso 2: Un solo hijo
            if (nodo.getIzquierdo() == null) {
                return nodo.getDerecho();
            }
            if (nodo.getDerecho() == null) {
                return nodo.getIzquierdo();
            }

            // Caso 3: Dos hijos - reemplazar con sucesor inorder
            NodoABB<K, V> sucesor = encontrarMinimo(nodo.getDerecho());
            NodoABB<K, V> reemplazo = new NodoABB<>(sucesor.getClave(), sucesor.getValor());
            // Copiar todos los valores del sucesor
            Cola<V> sucValores = copiarCola(sucesor.getValores());
            // El constructor ya puso el primer valor, desencolar para no duplicar
            sucValores.desencolar();
            while (!sucValores.estaVacia()) {
                reemplazo.agregarValor(sucValores.desencolar());
            }
            reemplazo.setIzquierdo(nodo.getIzquierdo());
            reemplazo.setDerecho(eliminarNodo(nodo.getDerecho(), sucesor.getClave()));
            return reemplazo;
        }

        return nodo;
    }

    /*
    Encuentra el nodo con la clave mínima en un subárbol.
    */
    private NodoABB<K, V> encontrarMinimo(NodoABB<K, V> nodo) {
        while (nodo.getIzquierdo() != null) {
            nodo = nodo.getIzquierdo();
        }
        return nodo;
    }

    @Override
    public boolean estaVacio() {
        return raiz == null;
    }

    @Override
    public int getCantidad() {
        return cantidad;
    }

    /*
    Obtiene todos los valores en el nivel N del árbol.
    Incluye todos los valores de cada nodo en ese nivel.
    */
    @Override
    public Object[] obtenerEnNivel(int nivel) {
        if (nivel < 0 || raiz == null) {
            return new Object[0];
        }

        Cola<V> resultados = new Cola<>();
        Cola<NodoABB<K, V>> cola = new Cola<>();
        cola.encolar(raiz);

        int nivelActual = 0;

        while (!cola.estaVacia()) {
            int nodosEnNivel = cola.getCantidad();

            if (nivelActual == nivel) {
                for (int i = 0; i < nodosEnNivel; i++) {
                    NodoABB<K, V> nodo = cola.desencolar();
                    // Agregar TODOS los valores del nodo
                    Cola<V> copia = copiarCola(nodo.getValores());
                    while (!copia.estaVacia()) {
                        resultados.encolar(copia.desencolar());
                    }
                }
                break;
            }

            for (int i = 0; i < nodosEnNivel; i++) {
                NodoABB<K, V> nodo = cola.desencolar();

                if (nodo.getIzquierdo() != null) {
                    cola.encolar(nodo.getIzquierdo());
                }
                if (nodo.getDerecho() != null) {
                    cola.encolar(nodo.getDerecho());
                }
            }

            nivelActual++;
        }

        return colaAArray(resultados);
    }

    /*
    Retorna la altura del árbol.
    */
    @Override
    public int getAltura() {
        return getAlturaRecursiva(raiz);
    }

    /*
    Método auxiliar recursivo para calcular altura.
    */
    private int getAlturaRecursiva(NodoABB<K, V> nodo) {
        if (nodo == null) {
            return -1;
        }

        int alturaIzq = getAlturaRecursiva(nodo.getIzquierdo());
        int alturaDer = getAlturaRecursiva(nodo.getDerecho());

        return 1 + Math.max(alturaIzq, alturaDer);
    }

    /*
    Copia una Cola sin modificar la original.
    Complejidad: O(k) donde k = cantidad de elementos.
    */
    private Cola<V> copiarCola(Cola<V> original) {
        Cola<V> copia = new Cola<>();
        Cola<V> aux = new Cola<>();

        while (!original.estaVacia()) {
            V v = original.desencolar();
            copia.encolar(v);
            aux.encolar(v);
        }
        // Restaurar la original
        while (!aux.estaVacia()) {
            original.encolar(aux.desencolar());
        }

        return copia;
    }

    /*
    Convierte una Cola propia a Object[].
    Complejidad: O(k) donde k = cantidad de elementos en la cola.
    */
    private Object[] colaAArray(Cola<V> cola) {
        int tam = cola.getCantidad();
        Object[] resultado = new Object[tam];
        for (int i = 0; i < tam; i++) {
            resultado[i] = cola.desencolar();
        }
        return resultado;
    }
}
