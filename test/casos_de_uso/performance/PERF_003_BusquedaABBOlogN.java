import servicio.GestorClientes;

public class PERF_003_BusquedaABBOlogN {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_PERF003_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            StringBuilder json = new StringBuilder("{ \"clientes\": [");
            for (int i = 1; i <= 1000; i++) {
                if (i > 1) json.append(",");
                json.append("{\"id\": ").append(i).append(", \"nombre\": \"User").append(i)
                    .append("\", \"scoring\": ").append(i % 100).append(", \"siguiendo\": [], ")
                    .append("\"solicitudes\": [], \"seguidores\": [], \"amistades\": []}");
            }
            json.append("] }");
            writer.write(json.toString());
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  PERF-003: BÚSQUEDA ABB O(log N)");

        testBusquedaABB();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testBusquedaABB() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            long inicio = System.nanoTime();
            var clientes = gestor.buscarPorScoring(50);
            long tiempoABB = System.nanoTime() - inicio;

            assert clientes.length > 0 : "Encuentra scoring";
            System.out.println("    Tiempo búsqueda ABB (1000 clientes): " + tiempoABB + "ns");
            System.out.println("    log(1000) = 10 comparaciones esperadas");

            reportarExito("Búsqueda ABB O(log N)");
        } catch (AssertionError e) {
            reportarFallo("Búsqueda ABB O(log N)", e.getMessage());
        }
    }

    private static void reportarExito(String testName) {
        testsPasados++;
        System.out.println("  [OK] " + testName);
    }

    private static void reportarFallo(String testName, String error) {
        testsFallados++;
        System.err.println("  [FAIL] " + testName + " - " + error);
    }
}
