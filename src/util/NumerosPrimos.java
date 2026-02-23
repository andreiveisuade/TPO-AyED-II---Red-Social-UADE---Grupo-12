package util;

/**
 * Utilidad para cálculos con números primos, usada para dimensionar
 * tablas hash con capacidad óptima.
 *
 * Factor de carga objetivo: alpha = 0.75
 * - Búsqueda exitosa promedio: 1 + alpha/2 = 1.375 comparaciones
 * - Búsqueda fallida promedio: 1 + alpha = 1.75 comparaciones
 */
public class NumerosPrimos {

    private static final int CAPACIDAD_MINIMA = 17;
    private static final double FACTOR_CARGA = 0.75;

    private NumerosPrimos() {}

    /**
     * Verifica si un número es primo.
     * Complejidad: O(sqrt(n))
     */
    public static boolean esPrimo(int n) {
        if (n < 2) return false;
        if (n == 2 || n == 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    /**
     * Encuentra el menor primo >= n.
     * Complejidad: O(n * sqrt(n)) en peor caso, pero los primos son densos
     */
    public static int siguientePrimo(int n) {
        if (n <= 2) return 2;
        int candidato = (n % 2 == 0) ? n + 1 : n;
        while (!esPrimo(candidato)) {
            candidato += 2;
        }
        return candidato;
    }

    /**
     * Calcula la capacidad óptima de un Diccionario para N elementos.
     * Fórmula: siguientePrimo(ceil(N / 0.75)) = siguientePrimo(ceil(N * 4/3))
     * Mínimo: 17
     *
     * @param cantidadElementos Cantidad esperada de elementos
     * @return Capacidad primo óptima para alpha = 0.75
     */
    public static int capacidadOptima(int cantidadElementos) {
        if (cantidadElementos <= 0) return CAPACIDAD_MINIMA;
        int capacidadCruda = (int) Math.ceil(cantidadElementos / FACTOR_CARGA);
        int capacidad = siguientePrimo(capacidadCruda);
        return Math.max(capacidad, CAPACIDAD_MINIMA);
    }
}
