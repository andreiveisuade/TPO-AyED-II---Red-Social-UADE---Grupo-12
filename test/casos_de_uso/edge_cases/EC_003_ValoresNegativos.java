import servicio.GestorClientes;

public class EC_003_ValoresNegativos {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_EC003_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  EC-003: VALORES NEGATIVOS");

        testScoringNegativo();
        testIdNegativo();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testScoringNegativo() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id = gestor.agregarCliente("Alice", -50);
            assert id == -1 : "Rechaza scoring negativo";
            reportarExito("Scoring negativo");
        } catch (AssertionError e) {
            reportarFallo("Scoring negativo", e.getMessage());
        }
    }

    private static void testIdNegativo() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var cliente = gestor.buscarPorId(-1);
            assert cliente == null : "ID negativo retorna null";
            reportarExito("ID negativo");
        } catch (AssertionError e) {
            reportarFallo("ID negativo", e.getMessage());
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
