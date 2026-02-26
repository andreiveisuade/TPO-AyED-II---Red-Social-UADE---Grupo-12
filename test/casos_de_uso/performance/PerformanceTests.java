/*
PERFORMANCE TESTS - Validar complejidad Big O y tiempos

Mide tiempos de ejecucion para validar:
- O(1): Busqueda por ID
- O(log N): Busqueda en ABB
- O(N): Listar todos, iteracion
*/

public class PerformanceTests {

    private static int testsPasados = 0;
    private static int testsFallados = 0;

    public static void main(String[] args) {
        System.out.println("── Performance ──────────────────────────────────");

        ejecutarTest("PERF_001_BusquedaPorIdO1");
        ejecutarTest("PERF_002_ListarTodosON");
        ejecutarTest("PERF_003_BusquedaABBOlogN");
        ejecutarTest("PERF_004_DistanciaOVE");
        ejecutarTest("PERF_005_EscalabilidadMuchosClientes");

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

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
        } catch (Exception e) {
            testsFallados++;
            System.err.println("  [FAIL] " + nombreClase + ": " + e.getMessage());
        }
    }
}
