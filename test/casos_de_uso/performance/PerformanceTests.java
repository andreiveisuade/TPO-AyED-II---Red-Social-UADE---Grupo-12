/*
PERFORMANCE TESTS - Validar complejidad Big O y tiempos

Mide tiempos de ejecución para validar:
- O(1): Búsqueda por ID
- O(log N): Búsqueda en ABB
- O(N): Listar todos, iteración
- O(V+E): BFS para distancia
*/

public class PerformanceTests {

    private static int testsPasados = 0;
    private static int testsFallados = 0;

    public static void main(String[] args) {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("       PERFORMANCE TESTS - VALIDACIÓN DE COMPLEJIDAD");
        System.out.println("═".repeat(70) + "\n");

        ejecutarTest("PERF_001_BusquedaPorIdO1");
        ejecutarTest("PERF_002_ListarTodosON");
        ejecutarTest("PERF_003_BusquedaABBOlogN");
        ejecutarTest("PERF_004_DistanciaOVE");
        ejecutarTest("PERF_005_EscalabilidadMuchosClientes");

        // Resumen final
        System.out.println("\n" + "═".repeat(70));
        System.out.printf("PERFORMANCE RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("═".repeat(70) + "\n");

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void ejecutarTest(String nombreClase) {
        try {
            Class<?> clazz = Class.forName(nombreClase);
            java.lang.reflect.Method main = clazz.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[]{});
            testsPasados++;
            System.out.println("[OK] " + nombreClase);
        } catch (Exception e) {
            testsFallados++;
            System.err.println("[FAIL] " + nombreClase + ": " + e.getMessage());
        }
    }
}
