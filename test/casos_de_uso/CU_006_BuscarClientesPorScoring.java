import servicio.GestorClientes;

public class CU_006_BuscarClientesPorScoring {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU006_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}, " +
                    "{\"id\": 2, \"nombre\": \"Bob\", \"scoring\": 80, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}]}");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  CU-006: BUSCAR POR SCORING");

        testBuscarScoring95();
        testBuscarScoring80();
        testBuscarInexistente();
        testLazyLoading();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testBuscarScoring95() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var clientes = gestor.buscarPorScoring(95);
            assert clientes.length == 1 : "Encuentra 95";
            reportarExito("Buscar 95");
        } catch (AssertionError e) {
            reportarFallo("Buscar 95", e.getMessage());
        }
    }

    private static void testBuscarScoring80() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var clientes = gestor.buscarPorScoring(80);
            assert clientes.length == 1 : "Encuentra 80";
            reportarExito("Buscar 80");
        } catch (AssertionError e) {
            reportarFallo("Buscar 80", e.getMessage());
        }
    }

    private static void testBuscarInexistente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var clientes = gestor.buscarPorScoring(50);
            assert clientes.length == 0 : "No encuentra 50";
            reportarExito("Buscar inexistente");
        } catch (AssertionError e) {
            reportarFallo("Buscar inexistente", e.getMessage());
        }
    }

    private static void testLazyLoading() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var c1 = gestor.buscarPorScoring(95);
            var c2 = gestor.buscarPorScoring(95);
            assert c1.length == c2.length : "Lazy loading funciona";
            reportarExito("Lazy loading");
        } catch (AssertionError e) {
            reportarFallo("Lazy loading", e.getMessage());
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
