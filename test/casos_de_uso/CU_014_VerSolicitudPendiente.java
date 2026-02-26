import servicio.GestorClientes;

public class CU_014_VerSolicitudPendiente {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU014_TEST.json";
    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [], \"solicitudes\": [\"1:2\"], \"seguidores\": [], \"amistades\": []}]}");
        } catch (java.io.IOException e) {}
    }
    public static void main(String[] args) {
        System.out.println("\n  CU-014: VER SOLICITUD PENDIENTE");
        testVerSolicitud();
        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);
        if (testsFallados > 0) System.exit(1);
    }
    private static void testVerSolicitud() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            var alice = gestor.buscarPorId(1);
            assert alice.tieneSolicitudesPendientes() : "Tiene solicitud";
            reportarExito("Ver solicitud");
        } catch (AssertionError e) {
            reportarFallo("Ver solicitud", e.getMessage());
        }
    }
    private static void reportarExito(String testName) { testsPasados++; System.out.println("  [OK] " + testName); }
    private static void reportarFallo(String testName, String error) { testsFallados++; System.err.println("  [FAIL] " + testName); }
}
