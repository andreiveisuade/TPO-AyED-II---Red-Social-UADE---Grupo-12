
package tda;

import interfaces.IDiccionario;

/*
TDA Diccionario implementado como Tabla Hash (Abierta/Encadenamiento).

Estructura: Array de listas enlazadas (buckets).
Función Hash: Math.abs(clave.hashCode()) % capacidad.

Complejidad: O(1) amortizado para insertar/buscar/eliminar.
SOLID: DIP - Implementa interfaz IDiccionario
*/
public class Diccionario<K, V> implements IDiccionario<K, V> {
    
    /* Atributos */
    private static final int CAPACIDAD_INICIAL = 64; // Potencia de 2 para mejor distribución
    private NodoDiccionario<K, V>[] tabla;
    private int cantidad;

    /*
    Constructor que inicializa la tabla hash.
    */
    @SuppressWarnings("unchecked")
    public Diccionario() {
        this(CAPACIDAD_INICIAL);
    }

    /*
    Constructor que inicializa la tabla hash con una capacidad específica.
    Permite optimizar el rendimiento cuando se conoce el volumen de datos.
    */
    @SuppressWarnings("unchecked")
    public Diccionario(int capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor a 0");
        }
        this.tabla = (NodoDiccionario<K, V>[]) new NodoDiccionario[capacidad];
        this.cantidad = 0;
    }

    /*
    Función hash privada para calcular el índice.
    Valida que la clave no sea null.
    */
    private int hash(K clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser null");
        }
        return Math.abs(clave.hashCode()) % tabla.length;
    }

    /*
    Inserta un par clave-valor en O(1).
    Maneja colisiones agregando al inicio de la lista del bucket.
    */
    @Override
    public void insertar(K clave, V valor) {
        int indice = hash(clave);
        NodoDiccionario<K, V> actual = tabla[indice];

        // 1. Buscar si ya existe para actualizar
        while (actual != null) {
            if (actual.getClave().equals(clave)) {
                actual.setValor(valor);
                return;
            }
            actual = actual.getSiguiente();
        }

        // 2. No existe, agregar al inicio del bucket (O(1))
        NodoDiccionario<K, V> nuevo = new NodoDiccionario<>(clave, valor);
        nuevo.setSiguiente(tabla[indice]);
        tabla[indice] = nuevo;
        cantidad++;
    }

    /*
    Obtiene el valor asociado a una clave en O(1).
    */
    @Override
    public V obtener(K clave) {
        int indice = hash(clave);
        NodoDiccionario<K, V> actual = tabla[indice];
        
        while (actual != null) {
            if (actual.getClave().equals(clave)) {
                return actual.getValor();
            }
            actual = actual.getSiguiente();
        }
        return null; // No encontrado
    }

    /*
    Verifica si existe una clave en O(1).
    */
    @Override
    public boolean contiene(K clave) {
        return obtener(clave) != null;
    }

    /*
    Elimina un par clave-valor en O(1).
    */
    @Override
    public V eliminar(K clave) {
        int indice = hash(clave);
        NodoDiccionario<K, V> actual = tabla[indice];
        NodoDiccionario<K, V> anterior = null;

        while (actual != null) {
            if (actual.getClave().equals(clave)) {
                if (anterior == null) {
                    tabla[indice] = actual.getSiguiente(); // Eliminar primero del bucket
                } else {
                    anterior.setSiguiente(actual.getSiguiente()); // Eliminar del medio/fin
                }
                cantidad--;
                return actual.getValor();
            }
            anterior = actual;
            actual = actual.getSiguiente();
        }
        return null;
    }

    @Override
    public int getCantidad() {
        return cantidad;
    }

    @Override
    public boolean estaVacio() {
        return cantidad == 0;
    }

    /*
    Retorna todas las claves del diccionario.
    Complejidad: O(n) - recorre todos los buckets.
    */
    @Override
    public String[] obtenerClaves() {
        String[] claves = new String[cantidad];
        int indice = 0;
        
        for (int i = 0; i < tabla.length; i++) {
            NodoDiccionario<K, V> actual = tabla[i];
            while (actual != null) {
                claves[indice++] = actual.getClave().toString();
                actual = actual.getSiguiente();
            }
        }
        return claves;
    }

    /*
    NUEVO: Obtiene todos los valores del diccionario.
    Optimización: Permite iterar valores sin buscar por clave repetidamente.
    Complejidad: O(n)
    */
    @Override
    @SuppressWarnings("unchecked")
    public Object[] obtenerValores() {
        Object[] valores = new Object[cantidad];
        int indice = 0;
        
        for (int i = 0; i < tabla.length; i++) {
            NodoDiccionario<K, V> actual = tabla[i];
            while (actual != null) {
                valores[indice++] = actual.getValor();
                actual = actual.getSiguiente();
            }
        }
        return valores;
    }
}
