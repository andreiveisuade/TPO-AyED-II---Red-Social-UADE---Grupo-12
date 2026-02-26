import servicio.GestorClientes;

public class CU_012_VerListaSeguidores {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU012_TEST.json";
    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [2,3], \"amistades\": []}]}");
        } catch (java.io.IOException e) {}
    }
    public static void main(String[] args) {
        System.out.println("\n  CU-012: VER LISTA SEGUIDORES");
        testVerSeguidores();
        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);
        if (testsFallados > 0) System.exit(1);
    }
    private static void testVerSeguidores() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var alice = gestor.buscarPorId(1);
            assert alice.getCantidadSeguidores() == 2 : "Alice tiene 2 seguidores";
            reportarExito("Ver seguidores");
        } catch (AssertionError e) {
            reportarFallo("Ver seguidores", e.getMessage());
        }
    }
    private static void reportarExito(String testName) { testsPasados++; System.out.println("  [OK] " + testName); }
    private static void reportarFallo(String testName, String error) { testsFallados++; System.err.println("  [FAIL] " + testName); }
}
