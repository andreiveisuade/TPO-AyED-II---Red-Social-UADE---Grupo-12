import servicio.GestorClientes;

public class CU_010_DejarDeSeguir {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU010_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [2], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}, " +
                    "{\"id\": 2, \"nombre\": \"Bob\", \"scoring\": 80, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [1], \"amistades\": []}]}");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  CU-010: DEJAR DE SEGUIR");

        testDejarDeSeguirValido();
        testActualizaBidireccional();
        testDejarDeSeguirNoExistente();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testDejarDeSeguirValido() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var alice = gestor.buscarPorId(1);
            assert alice.getCantidadSiguiendo() == 1 : "Alice sigue 1";
            boolean resultado = gestor.dejarDeSeguir(1, 2);
            assert resultado : "Deja de seguir";
            assert alice.getCantidadSiguiendo() == 0 : "Alice sigue 0";
            reportarExito("Dejar de seguir válido");
        } catch (AssertionError e) {
            reportarFallo("Dejar de seguir válido", e.getMessage());
        }
    }

    private static void testActualizaBidireccional() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var bob = gestor.buscarPorId(2);
            assert bob.getCantidadSeguidores() == 1 : "Bob tiene 1 seguidor";
            gestor.dejarDeSeguir(1, 2);
            assert bob.getCantidadSeguidores() == 0 : "Bob tiene 0 seguidores";
            reportarExito("Actualiza bidireccional");
        } catch (AssertionError e) {
            reportarFallo("Actualiza bidireccional", e.getMessage());
        }
    }

    private static void testDejarDeSeguirNoExistente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            boolean resultado = gestor.dejarDeSeguir(1, 9999);
            assert !resultado : "No puede dejar de seguir inexistente";
            reportarExito("No existente");
        } catch (AssertionError e) {
            reportarFallo("No existente", e.getMessage());
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
