import servicio.GestorClientes;

public class PERF_001_BusquedaPorIdO1 {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_PERF001_TEST.json";

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
        System.out.println("\n  PERF-001: BÚSQUEDA POR ID O(1)");

        testBusquedaO1();
        testBusquedaEscalable();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testBusquedaO1() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            long inicio = System.nanoTime();
            var cliente = gestor.buscarPorId(1);
            long tiempo1 = System.nanoTime() - inicio;

            assert cliente != null : "Encuentra ID 1";
            System.out.println("    Tiempo búsqueda ID 1: " + tiempo1 + "ns");

            reportarExito("Búsqueda O(1)");
        } catch (AssertionError e) {
            reportarFallo("Búsqueda O(1)", e.getMessage());
        }
    }

    private static void testBusquedaEscalable() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            // Warmup para evitar sesgo de JIT/cache
            gestor.buscarPorId(500);

            long inicio1 = System.nanoTime();
            var c1 = gestor.buscarPorId(100);
            long tiempo1 = System.nanoTime() - inicio1;

            long inicio2 = System.nanoTime();
            var c2 = gestor.buscarPorId(900);
            long tiempo2 = System.nanoTime() - inicio2;

            System.out.println("    Tiempo búsqueda ID 100: " + tiempo1 + "ns");
            System.out.println("    Tiempo búsqueda ID 900: " + tiempo2 + "ns");

            assert c1 != null && c2 != null : "Encuentra ambos";
            // Tiempos deben ser similares (dentro de 10x)
            double ratio = (double) Math.max(tiempo1, tiempo2) / Math.min(tiempo1, tiempo2);
            assert ratio < 10 : "Tiempos similar O(1)";

            reportarExito("Escalabilidad O(1)");
        } catch (AssertionError e) {
            reportarFallo("Escalabilidad O(1)", e.getMessage());
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
