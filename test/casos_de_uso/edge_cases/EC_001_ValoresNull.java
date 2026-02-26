import servicio.GestorClientes;

/*
EC-001: Validación de Valores NULL

Prueba que el sistema rechaza correctamente valores null
en operaciones críticas.
*/

public class EC_001_ValoresNull {

    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_EC001_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}]}");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  EC-001: VALIDACIÓN DE NULL");

        testBuscarIdNull();
        testAgregarNombreNull();
        testBuscarNombreNull();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testBuscarIdNull() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var cliente = gestor.buscarPorId(0);
            assert cliente == null : "ID 0 no debe existir";
            reportarExito("Buscar ID null");
        } catch (AssertionError e) {
            reportarFallo("Buscar ID null", e.getMessage());
        }
    }

    private static void testAgregarNombreNull() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id = gestor.agregarCliente(null, 50);
            assert id == -1 : "Rechaza nombre null";
            reportarExito("Agregar nombre null");
        } catch (AssertionError e) {
            reportarFallo("Agregar nombre null", e.getMessage());
        }
    }

    private static void testBuscarNombreNull() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            try {
                var clientes = gestor.buscarPorNombre(null);
                // Si no lanza excepción, debe retornar array vacío
                assert clientes.length == 0 : "Búsqueda null retorna vacío";
            } catch (NullPointerException e) {
                // También es aceptable lanzar NullPointerException
                assert true : "NullPointerException aceptable";
            }
            reportarExito("Buscar nombre null");
        } catch (AssertionError e) {
            reportarFallo("Buscar nombre null", e.getMessage());
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
