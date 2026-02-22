import servicio.GestorClientes;
import modelo.Cliente;

/**
 * Tests unitarios para la carga de datos desde JSON.
 *
 * Testea la carga real via GestorClientes(path).
 * Usa assertions nativas (ejecutar con java -ea JsonLoaderTest).
 */
public class JsonLoaderTest {

    private static int testsPasados = 0;
    private static int testsFallados = 0;

    private static final String TEST_DB = "data/clientes_JSON_TEST.json";

    private static void escribirJson(String json) {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write(json);
        } catch (java.io.IOException e) {
            System.err.println("Error escribiendo JSON de test: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("   TESTS DE CARGA JSON                    ");
        System.out.println("═══════════════════════════════════════════\n");

        testCargarClientesDesdeJson();
        testCargarClientesConScoring();
        testCargarSiguiendo();
        testCargarSeguidores();
        testJsonVacio();

        System.out.println("\n═══════════════════════════════════════════");
        System.out.printf("RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("═══════════════════════════════════════════");

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void testCargarClientesDesdeJson() {
        try {
            String json = "{ \"clientes\": ["
                + "{ \"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [] },"
                + "{ \"id\": 2, \"nombre\": \"Bob\", \"scoring\": 88, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [] }"
                + "] }";
            escribirJson(json);
            GestorClientes gestor = new GestorClientes(TEST_DB);

            assert gestor.getCantidadClientes() == 2 : "Debe cargar 2 clientes, cargó: " + gestor.getCantidadClientes();

            Cliente alice = gestor.buscarPorId(1);
            assert alice != null : "Alice debe existir";
            assert "Alice".equals(alice.getNombre()) : "Nombre debe ser Alice";

            Cliente bob = gestor.buscarPorId(2);
            assert bob != null : "Bob debe existir";
            assert "Bob".equals(bob.getNombre()) : "Nombre debe ser Bob";

            reportarExito("Cargar clientes desde JSON");
        } catch (AssertionError e) {
            reportarFallo("Cargar clientes desde JSON", e.getMessage());
        }
    }

    private static void testCargarClientesConScoring() {
        try {
            String json = "{ \"clientes\": ["
                + "{ \"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [] },"
                + "{ \"id\": 2, \"nombre\": \"Bob\", \"scoring\": 50, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [] }"
                + "] }";
            escribirJson(json);
            GestorClientes gestor = new GestorClientes(TEST_DB);

            Cliente alice = gestor.buscarPorId(1);
            Cliente bob = gestor.buscarPorId(2);

            assert alice.getScoring() == 95 : "Scoring de Alice debe ser 95";
            assert bob.getScoring() == 50 : "Scoring de Bob debe ser 50";

            reportarExito("Cargar clientes con scoring");
        } catch (AssertionError e) {
            reportarFallo("Cargar clientes con scoring", e.getMessage());
        }
    }

    private static void testCargarSiguiendo() {
        try {
            String json = "{ \"clientes\": ["
                + "{ \"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [2], \"solicitudes\": [], \"seguidores\": [] },"
                + "{ \"id\": 2, \"nombre\": \"Bob\", \"scoring\": 88, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [\"1\"] }"
                + "] }";
            escribirJson(json);
            GestorClientes gestor = new GestorClientes(TEST_DB);

            Cliente alice = gestor.buscarPorId(1);
            assert alice.sigueA(2) : "Alice debe seguir a Bob (ID 2)";
            assert alice.getCantidadSiguiendo() == 1 : "Alice debe seguir a 1 persona";

            reportarExito("Cargar relaciones de seguimiento");
        } catch (AssertionError e) {
            reportarFallo("Cargar relaciones de seguimiento", e.getMessage());
        }
    }

    private static void testCargarSeguidores() {
        try {
            String json = "{ \"clientes\": ["
                + "{ \"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [2], \"solicitudes\": [], \"seguidores\": [] },"
                + "{ \"id\": 2, \"nombre\": \"Bob\", \"scoring\": 88, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [\"1\"] }"
                + "] }";
            escribirJson(json);
            GestorClientes gestor = new GestorClientes(TEST_DB);

            Cliente bob = gestor.buscarPorId(2);
            assert bob.getCantidadSeguidores() == 1 : "Bob debe tener 1 seguidor";

            reportarExito("Cargar seguidores");
        } catch (AssertionError e) {
            reportarFallo("Cargar seguidores", e.getMessage());
        }
    }

    private static void testJsonVacio() {
        try {
            escribirJson("{ \"clientes\": [] }");
            GestorClientes gestor = new GestorClientes(TEST_DB);

            assert gestor.getCantidadClientes() == 0 : "JSON vacío debe dar 0 clientes";

            reportarExito("JSON vacío");
        } catch (AssertionError e) {
            reportarFallo("JSON vacío", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // UTILIDADES DE REPORTE
    // ═══════════════════════════════════════════════════════════════════

    private static void reportarExito(String nombre) {
        System.out.printf("✅ %s%n", nombre);
        testsPasados++;
    }

    private static void reportarFallo(String nombre, String mensaje) {
        System.out.printf("❌ %s: %s%n", nombre, mensaje);
        testsFallados++;
    }
}
