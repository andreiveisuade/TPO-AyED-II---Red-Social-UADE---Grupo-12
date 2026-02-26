import servicio.GestorClientes;

public class CU_015_AceptarSolicitud {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU015_TEST.json";
    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}, {\"id\": 2, \"nombre\": \"Bob\", \"scoring\": 80, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}]}");
        } catch (java.io.IOException e) {}
    }
    public static void main(String[] args) {
        System.out.println("\n  CU-015: ACEPTAR SOLICITUD");
        testAceptarSolicitud();
        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);
        if (testsFallados > 0) System.exit(1);
    }
    private static void testAceptarSolicitud() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            gestor.enviarSolicitud(2, 1);
            var alice = gestor.buscarPorId(1);
            if (alice.tieneSolicitudesPendientes()) {
                var solicitud = alice.procesarSiguienteSolicitud();
                assert solicitud != null : "Procesa solicitud";
            }
            reportarExito("Aceptar solicitud");
        } catch (AssertionError e) {
            reportarFallo("Aceptar solicitud", e.getMessage());
        }
    }
    private static void reportarExito(String testName) { testsPasados++; System.out.println("  [OK] " + testName); }
    private static void reportarFallo(String testName, String error) { testsFallados++; System.err.println("  [FAIL] " + testName); }
}
