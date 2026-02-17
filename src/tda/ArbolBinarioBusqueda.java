package tda;

import interfaces.IArbolBinarioBusqueda;

/*
TDA Árbol Binario de Búsqueda (ABB) - Estructura ordenada por clave.

INVARIANTE DE REPRESENTACIÓN:
- Para todo nodo N: claves en subárbol izquierdo < N.clave
- Para todo nodo N: claves en subárbol derecho >= N.clave (permite duplicados a la derecha)
- cantidad >= 0 siempre
- (raiz == null) <=> (cantidad == 0)

COMPLEJIDAD TEMPORAL:
- Insertar: O(log N) promedio, O(N) peor caso
- Buscar: O(log N + k) donde k = cantidad con misma clave
- Eliminar: O(log N)
- Obtener en nivel: O(N)

SOLID: DIP - Implementa interfaz IArbolBinarioBusqueda
*/
public class ArbolBinarioBusqueda<K extends Comparable<K>, V> 
    implements IArbolBinarioBusqueda<K, V> {
    
    /* Atributos */
    private NodoABB<K, V> raiz;
    private int cantidad;

    /*
    Constructor que inicializa el árbol vacío.
    */
    public ArbolBinarioBusqueda() {
        this.raiz = null;
        this.cantidad = 0;
    }

    /*
    Inserta un par clave-valor en el árbol.
    Permite duplicados: valores con la misma clave van a la derecha.
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
    */
    private NodoABB<K, V> insertarRecursivo(NodoABB<K, V> nodo, K clave, V valor) {
        // Caso base: posición vacía encontrada
        if (nodo == null) {
            return new NodoABB<>(clave, valor);
        }

        // Comparar claves
        int comparacion = clave.compareTo(nodo.getClave());
        
        if (comparacion < 0) {
            // Menor: ir a la izquierda
            nodo.setIzquierdo(insertarRecursivo(nodo.getIzquierdo(), clave, valor));
        } else {
            // Mayor o igual: ir a la derecha (permite duplicados)
            nodo.setDerecho(insertarRecursivo(nodo.getDerecho(), clave, valor));
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
        
        // Usar una lista temporal para recolectar resultados
        java.util.ArrayList<V> resultados = new java.util.ArrayList<>();
        buscarRecursivo(raiz, clave, resultados);
        return resultados.toArray();
    }

    /*
    Método auxiliar recursivo para búsqueda.
    Recolecta todos los nodos con la clave coincidente.
    */
    private void buscarRecursivo(NodoABB<K, V> nodo, K clave, java.util.ArrayList<V> resultados) {
        if (nodo == null) {
            return;
        }

        int comparacion = clave.compareTo(nodo.getClave());
        
        if (comparacion < 0) {
            // La clave buscada es menor: solo buscar en izquierda
            buscarRecursivo(nodo.getIzquierdo(), clave, resultados);
        } else if (comparacion > 0) {
            // La clave buscada es mayor: solo buscar en derecha
            buscarRecursivo(nodo.getDerecho(), clave, resultados);
        } else {
            // Clave coincide: agregar este nodo
            resultados.add(nodo.getValor());
            
            // Pueden haber duplicados a la derecha (por nuestra regla de inserción)
            buscarRecursivo(nodo.getDerecho(), clave, resultados);
        }
    }

    /*
    Elimina un valor específico asociado a una clave.
    */
    @Override
    public boolean eliminar(K clave, V valor) {
        if (clave == null || valor == null) {
            return false;
        }
        
        int cantidadAntes = cantidad;
        raiz = eliminarRecursivo(raiz, clave, valor);
        return cantidad < cantidadAntes;
    }

    /*
    Método auxiliar recursivo para eliminación.
    Elimina el nodo que contiene la clave Y el valor específico.
    */
    private NodoABB<K, V> eliminarRecursivo(NodoABB<K, V> nodo, K clave, V valor) {
        if (nodo == null) {
            return null;
        }

        int comparacion = clave.compareTo(nodo.getClave());
        
        if (comparacion < 0) {
            // Buscar en izquierda
            nodo.setIzquierdo(eliminarRecursivo(nodo.getIzquierdo(), clave, valor));
        } else if (comparacion > 0) {
            // Buscar en derecha
            nodo.setDerecho(eliminarRecursivo(nodo.getDerecho(), clave, valor));
        } else {
            // Clave coincide: verificar si el valor también coincide
            if (nodo.getValor().equals(valor)) {
                // Este es el nodo a eliminar
                cantidad--;
                
                // Caso 1: Nodo sin hijos
                if (nodo.getIzquierdo() == null && nodo.getDerecho() == null) {
                    return null;
                }
                
                // Caso 2: Nodo con un solo hijo
                if (nodo.getIzquierdo() == null) {
                    return nodo.getDerecho();
                }
                if (nodo.getDerecho() == null) {
                    return nodo.getIzquierdo();
                }
                
                // Caso 3: Nodo con dos hijos
                // Reemplazar con el menor del subárbol derecho (sucesor inorder)
                NodoABB<K, V> sucesor = encontrarMinimo(nodo.getDerecho());
                nodo = new NodoABB<>(sucesor.getClave(), sucesor.getValor());
                nodo.setDerecho(eliminarMinimo(nodo.getDerecho()));
            } else {
                // Valor no coincide: puede haber duplicado a la derecha
                nodo.setDerecho(eliminarRecursivo(nodo.getDerecho(), clave, valor));
            }
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

    /*
    Elimina el nodo con la clave mínima en un subárbol.
    */
    private NodoABB<K, V> eliminarMinimo(NodoABB<K, V> nodo) {
        if (nodo.getIzquierdo() == null) {
            return nodo.getDerecho();
        }
        nodo.setIzquierdo(eliminarMinimo(nodo.getIzquierdo()));
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
    Utiliza recorrido BFS (Breadth-First Search) por niveles.
    */
    @Override
    public Object[] obtenerEnNivel(int nivel) {
        if (nivel < 0 || raiz == null) {
            return new Object[0];
        }
        
        java.util.ArrayList<V> resultados = new java.util.ArrayList<>();
        Cola<NodoABB<K, V>> cola = new Cola<>();
        cola.encolar(raiz);
        
        int nivelActual = 0;
        
        while (!cola.estaVacia()) {
            int nodosEnNivel = cola.getCantidad();
            
            // Si llegamos al nivel deseado, recolectar todos los nodos
            if (nivelActual == nivel) {
                for (int i = 0; i < nodosEnNivel; i++) {
                    NodoABB<K, V> nodo = cola.desencolar();
                    resultados.add(nodo.getValor());
                }
                break;
            }
            
            // Procesar todos los nodos del nivel actual
            for (int i = 0; i < nodosEnNivel; i++) {
                NodoABB<K, V> nodo = cola.desencolar();
                
                // Encolar hijos para el siguiente nivel
                if (nodo.getIzquierdo() != null) {
                    cola.encolar(nodo.getIzquierdo());
                }
                if (nodo.getDerecho() != null) {
                    cola.encolar(nodo.getDerecho());
                }
            }
            
            nivelActual++;
        }
        
        return resultados.toArray();
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
}
