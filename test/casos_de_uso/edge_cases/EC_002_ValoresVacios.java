import servicio.GestorClientes;

public class EC_002_ValoresVacios {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_EC002_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  EC-002: VALORES VACÍOS                    ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        testNombreVacio();
        testBuscarEnVacio();
        testListarVacio();

        System.out.println("\n" + "─".repeat(50));
        System.out.printf("EC-002 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("─".repeat(50) + "\n");

        if (testsFallados > 0) System.exit(1);
    }

    private static void testNombreVacio() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id = gestor.agregarCliente("", 50);
            assert id == -1 : "Rechaza nombre vacío";
            reportarExito("Nombre vacío");
        } catch (AssertionError e) {
            reportarFallo("Nombre vacío", e.getMessage());
        }
    }

    private static void testBuscarEnVacio() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var clientes = gestor.buscarPorNombre("Alice");
            assert clientes.length == 0 : "Busca en sistema vacío";
            reportarExito("Buscar en vacío");
        } catch (AssertionError e) {
            reportarFallo("Buscar en vacío", e.getMessage());
        }
    }

    private static void testListarVacio() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var clientes = gestor.obtenerTodosLosClientes();
            assert clientes.length == 0 : "Lista vacía";
            assert gestor.getCantidadClientes() == 0 : "Cantidad 0";
            reportarExito("Listar vacío");
        } catch (AssertionError e) {
            reportarFallo("Listar vacío", e.getMessage());
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
