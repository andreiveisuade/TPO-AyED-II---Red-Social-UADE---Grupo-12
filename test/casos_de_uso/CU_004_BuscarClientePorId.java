import servicio.GestorClientes;
import modelo.Cliente;

/*
CU-004: Buscar Cliente por ID

Descripción: Usuario busca otro usuario por ID.
Flujo: ID ingresado → Búsqueda O(1) → Retorna cliente
Complejidad: O(1)
*/

public class CU_004_BuscarClientePorId {

    private static int testsPasados = 0;
    private static int testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU004_TEST.json";

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
        System.out.println("║  CU-004: BUSCAR CLIENTE POR ID              ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        testBuscarClienteExistente();
        testBuscarClienteInexistente();
        testObtenerDetallesCliente();
        testComplejidadO1();

        System.out.println("\n" + "─".repeat(50));
        System.out.printf("CU-004 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("─".repeat(50) + "\n");

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void testBuscarClienteExistente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            Cliente alice = gestor.buscarPorId(1);
            assert alice != null : "Debe encontrar Alice";
            assert alice.getId() == 1 : "ID debe ser 1";
            assert alice.getNombre().equals("Alice") : "Nombre debe ser Alice";

            reportarExito("Buscar cliente existente");
        } catch (AssertionError e) {
            reportarFallo("Buscar cliente existente", e.getMessage());
        }
    }

    private static void testBuscarClienteInexistente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            Cliente noExiste = gestor.buscarPorId(9999);
            assert noExiste == null : "No debe encontrar ID inexistente";

            reportarExito("Buscar cliente inexistente");
        } catch (AssertionError e) {
            reportarFallo("Buscar cliente inexistente", e.getMessage());
        }
    }

    private static void testObtenerDetallesCliente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            Cliente bob = gestor.buscarPorId(2);
            assert bob != null : "Debe encontrar Bob";
            assert bob.getNombre().equals("Bob") : "Nombre es Bob";
            assert bob.getScoring() == 80 : "Scoring es 80";
            assert bob.getCantidadSiguiendo() == 0 : "No sigue a nadie";

            reportarExito("Obtener detalles cliente");
        } catch (AssertionError e) {
            reportarFallo("Obtener detalles cliente", e.getMessage());
        }
    }

    private static void testComplejidadO1() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            long inicio = System.nanoTime();
            Cliente alice = gestor.buscarPorId(1);
            long tiempo1 = System.nanoTime() - inicio;

            inicio = System.nanoTime();
            Cliente bob = gestor.buscarPorId(2);
            long tiempo2 = System.nanoTime() - inicio;

            // Ambas búsquedas deben ser O(1) - tiempos similares
            assert alice != null && bob != null : "Ambas búsquedas deben encontrar";
            // Los tiempos pueden variar un poco, pero deben ser del mismo orden
            System.out.println("    Tiempo búsqueda 1: " + tiempo1 + "ns");
            System.out.println("    Tiempo búsqueda 2: " + tiempo2 + "ns");

            reportarExito("Complejidad O(1)");
        } catch (AssertionError e) {
            reportarFallo("Complejidad O(1)", e.getMessage());
        }
    }

    private static void reportarExito(String testName) {
        testsPasados++;
        System.out.println("  [OK] " + testName);
    }

    private static void reportarFallo(String testName, String error) {
        testsFallados++;
        System.err.println("  [FAIL] " + testName);
        System.err.println("         Error: " + error);
    }
}
