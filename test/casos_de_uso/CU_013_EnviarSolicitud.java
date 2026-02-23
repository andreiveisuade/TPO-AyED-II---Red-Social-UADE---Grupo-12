import servicio.GestorClientes;
import modelo.Cliente;
import modelo.Sesion;

/*
CU-013: Enviar Solicitud de Seguimiento

Descripción: Usuario envía solicitud de seguimiento a otro usuario.
Flujo: Usuario A → Busca Usuario B → Envía solicitud → Cola en Usuario B
Complejidad: O(1)
*/

public class CU_013_EnviarSolicitud {

    private static int testsPasados = 0;
    private static int testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU013_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}, " +
                    "{\"id\": 2, \"nombre\": \"Bob\", \"scoring\": 80, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}]}");
        } catch (java.io.IOException e) {
            System.err.println("Error creating test DB: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  CU-013: ENVIAR SOLICITUD                  ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        testEnviarSolicitudValida();
        testEncoladaEnReceptor();
        testNoAutoSolicitud();
        testMultiplesSolicitudes();

        System.out.println("\n" + "─".repeat(50));
        System.out.printf("CU-013 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("─".repeat(50) + "\n");

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void testEnviarSolicitudValida() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion = Sesion.getInstancia();

            Cliente alice = gestor.buscarPorId(1);
            Cliente bob = gestor.buscarPorId(2);
            sesion.iniciarSesion(alice);

            // Enviar solicitud
            boolean resultado = gestor.enviarSolicitud(1, 2);
            assert resultado : "Debe poder enviar solicitud válida";

            reportarExito("Enviar solicitud válida");
        } catch (AssertionError e) {
            reportarFallo("Enviar solicitud válida", e.getMessage());
        } catch (Exception e) {
            reportarFallo("Enviar solicitud válida", e.getMessage());
        }
    }

    private static void testEncoladaEnReceptor() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion = Sesion.getInstancia();

            Cliente alice = gestor.buscarPorId(1);
            Cliente bob = gestor.buscarPorId(2);
            sesion.iniciarSesion(alice);

            // Enviar solicitud de Alice a Bob
            gestor.enviarSolicitud(1, 2);

            // Verificar que Bob tiene solicitud pendiente
            assert bob.tieneSolicitudesPendientes() : "Bob debe tener solicitudes";
            assert bob.getCantidadSolicitudesPendientes() == 1 : "Bob debe tener 1 solicitud";

            // Ver siguiente solicitud (sin procesar)
            var siguiente = bob.verSiguienteSolicitud();
            assert siguiente != null : "Debe haber solicitud pendiente";
            assert siguiente.getSolicitante().equals("1") : "Solicitante debe ser Alice (ID 1)";

            reportarExito("Encolada en receptor");
        } catch (AssertionError e) {
            reportarFallo("Encolada en receptor", e.getMessage());
        } catch (Exception e) {
            reportarFallo("Encolada en receptor", e.getMessage());
        }
    }

    private static void testNoAutoSolicitud() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion = Sesion.getInstancia();

            Cliente alice = gestor.buscarPorId(1);
            sesion.iniciarSesion(alice);

            // No puede enviar solicitud a sí mismo - debe lanzar excepción
            try {
                gestor.enviarSolicitud(1, 1);
                assert false : "Debe lanzar excepción al intentar auto-solicitud";
            } catch (IllegalArgumentException e) {
                // Se espera esta excepción
            }

            reportarExito("No auto-solicitud");
        } catch (AssertionError e) {
            reportarFallo("No auto-solicitud", e.getMessage());
        } catch (Exception e) {
            reportarFallo("No auto-solicitud", e.getMessage());
        }
    }

    private static void testMultiplesSolicitudes() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            
            Cliente alice = gestor.buscarPorId(1);
            Cliente bob = gestor.buscarPorId(2);
            
            Sesion sesion = Sesion.getInstancia();
            sesion.iniciarSesion(alice);

            // Alice envía solicitud a Bob
            boolean resultado = gestor.enviarSolicitud(1, 2);
            assert resultado : "Solicitud debe ser encolada";

            // Verificar cola
            assert bob.getCantidadSolicitudesPendientes() == 1 : "Bob debe tener 1 solicitud";

            reportarExito("Múltiples solicitudes");
        } catch (AssertionError e) {
            reportarFallo("Múltiples solicitudes", e.getMessage());
        } catch (Exception e) {
            reportarFallo("Múltiples solicitudes", e.getMessage());
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
