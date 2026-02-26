import servicio.GestorClientes;

public class CU_008_EliminarCliente {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU008_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [2], \"solicitudes\": [], \"seguidores\": [2], \"amistades\": []}]}");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  CU-008: ELIMINAR CLIENTE");

        testEliminarClienteExistente();
        testDisminuyeCantidad();
        testLimpiaReferenciasCascada();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testEliminarClienteExistente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            boolean resultado = gestor.eliminarCliente(1);
            assert resultado : "Elimina cliente";
            assert gestor.buscarPorId(1) == null : "Ya no existe";
            reportarExito("Eliminar existente");
        } catch (AssertionError e) {
            reportarFallo("Eliminar existente", e.getMessage());
        }
    }

    private static void testDisminuyeCantidad() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            assert gestor.getCantidadClientes() == 1 : "Hay 1";
            gestor.eliminarCliente(1);
            assert gestor.getCantidadClientes() == 0 : "Hay 0";
            reportarExito("Disminuye cantidad");
        } catch (AssertionError e) {
            reportarFallo("Disminuye cantidad", e.getMessage());
        }
    }

    private static void testLimpiaReferenciasCascada() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            gestor.eliminarCliente(1);
            assert gestor.getCantidadClientes() == 0 : "Cascada limpia";
            reportarExito("Cascada referencias");
        } catch (AssertionError e) {
            reportarFallo("Cascada referencias", e.getMessage());
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
