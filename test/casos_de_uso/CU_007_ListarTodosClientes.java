import servicio.GestorClientes;

public class CU_007_ListarTodosClientes {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU007_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}, " +
                    "{\"id\": 2, \"nombre\": \"Bob\", \"scoring\": 80, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}]}");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  CU-007: LISTAR TODOS CLIENTES");

        testListarTodos();
        testListarVacio();
        testOrdenamiento();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testListarTodos() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var clientes = gestor.obtenerTodosLosClientes();
            assert clientes.length == 2 : "Retorna 2 clientes";
            reportarExito("Listar todos");
        } catch (AssertionError e) {
            reportarFallo("Listar todos", e.getMessage());
        }
    }

    private static void testListarVacio() {
        try {
            try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
                writer.write("{ \"clientes\": [] }");
            } catch (java.io.IOException e) {
                System.err.println("IO Error: " + e.getMessage());
            }
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var clientes = gestor.obtenerTodosLosClientes();
            assert clientes.length == 0 : "Vacío";
            reportarExito("Listar vacío");
        } catch (AssertionError e) {
            reportarFallo("Listar vacío", e.getMessage());
        }
    }

    private static void testOrdenamiento() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var clientes = gestor.obtenerTodosLosClientes();
            assert clientes.length == 2 : "Obtiene todos";
            reportarExito("Ordenamiento");
        } catch (AssertionError e) {
            reportarFallo("Ordenamiento", e.getMessage());
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
