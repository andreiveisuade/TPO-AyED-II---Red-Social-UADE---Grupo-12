import servicio.GestorClientes;

public class EC_004_LimitesScoring {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_EC004_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  EC-004: LÍMITES DE SCORING");

        testScoring0();
        testScoring100();
        testScoring101();
        testScoringMaximo();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testScoring0() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id = gestor.agregarCliente("Alice", 0);
            assert id != -1 : "Score 0 es válido";
            reportarExito("Score 0");
        } catch (AssertionError e) {
            reportarFallo("Score 0", e.getMessage());
        }
    }

    private static void testScoring100() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id = gestor.agregarCliente("Alice", 100);
            assert id != -1 : "Score 100 es válido";
            reportarExito("Score 100");
        } catch (AssertionError e) {
            reportarFallo("Score 100", e.getMessage());
        }
    }

    private static void testScoring101() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id = gestor.agregarCliente("Alice", 101);
            assert id == -1 : "Score 101 inválido";
            reportarExito("Score 101");
        } catch (AssertionError e) {
            reportarFallo("Score 101", e.getMessage());
        }
    }

    private static void testScoringMaximo() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id = gestor.agregarCliente("Alice", Integer.MAX_VALUE);
            assert id == -1 : "Score muy grande inválido";
            reportarExito("Score máximo");
        } catch (AssertionError e) {
            reportarFallo("Score máximo", e.getMessage());
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
