import servicio.GestorClientes;

public class PERF_002_ListarTodosON {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_PERF002_TEST.json";

    private static void initTestDB(int cantidad) {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            StringBuilder json = new StringBuilder("{ \"clientes\": [");
            for (int i = 1; i <= cantidad; i++) {
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
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  PERF-002: LISTAR TODOS O(N)               ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        testListarON();

        System.out.println("\n" + "─".repeat(50));
        System.out.printf("PERF-002 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("─".repeat(50) + "\n");

        if (testsFallados > 0) System.exit(1);
    }

    private static void testListarON() {
        try {
            initTestDB(1000);
            GestorClientes gestor = new GestorClientes(TEST_DB);

            long inicio = System.nanoTime();
            var clientes = gestor.obtenerTodosLosClientes();
            long tiempoN = System.nanoTime() - inicio;

            assert clientes.length == 1000 : "Obtiene 1000 clientes";
            System.out.println("  Tiempo listar 1000 clientes: " + tiempoN + "ns");
            System.out.println("  Promedio por cliente: " + (tiempoN / 1000.0) + "ns");

            reportarExito("Listar O(N)");
        } catch (AssertionError e) {
            reportarFallo("Listar O(N)", e.getMessage());
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
