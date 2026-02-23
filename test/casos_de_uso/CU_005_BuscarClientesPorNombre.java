import servicio.GestorClientes;

/*
CU-005: Buscar Clientes por Nombre
Complejidad: O(1) búsqueda + O(k) iteración
*/

public class CU_005_BuscarClientesPorNombre {

    private static int testsPasados = 0;
    private static int testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU005_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}, " +
                    "{\"id\": 2, \"nombre\": \"Bob\", \"scoring\": 80, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}]}");
        } catch (java.io.IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  CU-005: BUSCAR POR NOMBRE                 ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        testBuscarExistente();
        testBuscarInexistente();
        testCaseInsensitive();
        testMultiplesResultados();

        System.out.println("\n" + "─".repeat(50));
        System.out.printf("CU-005 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("─".repeat(50) + "\n");

        if (testsFallados > 0) System.exit(1);
    }

    private static void testBuscarExistente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var clientes = gestor.buscarPorNombre("Alice");
            assert clientes.length == 1 : "Encuentra Alice";
            reportarExito("Buscar existente");
        } catch (AssertionError e) {
            reportarFallo("Buscar existente", e.getMessage());
        }
    }

    private static void testBuscarInexistente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var clientes = gestor.buscarPorNombre("Inexistente");
            assert clientes.length == 0 : "No encuentra";
            reportarExito("Buscar inexistente");
        } catch (AssertionError e) {
            reportarFallo("Buscar inexistente", e.getMessage());
        }
    }

    private static void testCaseInsensitive() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var clientes = gestor.buscarPorNombre("alice");
            assert clientes.length == 1 : "Case insensitive";
            reportarExito("Case insensitive");
        } catch (AssertionError e) {
            reportarFallo("Case insensitive", e.getMessage());
        }
    }

    private static void testMultiplesResultados() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            gestor.agregarCliente("Alice", 90);
            var clientes = gestor.buscarPorNombre("Alice");
            assert clientes.length == 2 : "Múltiples resultados";
            reportarExito("Múltiples resultados");
        } catch (AssertionError e) {
            reportarFallo("Múltiples resultados", e.getMessage());
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
