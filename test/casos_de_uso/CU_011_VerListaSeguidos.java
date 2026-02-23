import servicio.GestorClientes;

public class CU_011_VerListaSeguidos {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU011_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [2], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}, " +
                    "{\"id\": 2, \"nombre\": \"Bob\", \"scoring\": 80, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [1], \"amistades\": []}]}");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  CU-011: VER LISTA SEGUIDOS                ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        testVerSeguidos();
        testVacio();

        System.out.println("\n" + "─".repeat(50));
        System.out.printf("CU-011 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("─".repeat(50) + "\n");

        if (testsFallados > 0) System.exit(1);
    }

    private static void testVerSeguidos() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var alice = gestor.buscarPorId(1);
            assert alice.getCantidadSiguiendo() == 1 : "Alice sigue 1";
            reportarExito("Ver seguidos");
        } catch (AssertionError e) {
            reportarFallo("Ver seguidos", e.getMessage());
        }
    }

    private static void testVacio() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var bob = gestor.buscarPorId(2);
            assert bob.getCantidadSiguiendo() == 0 : "Bob no sigue";
            reportarExito("Vacío");
        } catch (AssertionError e) {
            reportarFallo("Vacío", e.getMessage());
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
