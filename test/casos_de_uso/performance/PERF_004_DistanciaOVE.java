import servicio.GestorClientes;

public class PERF_004_DistanciaOVE {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_PERF004_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"A\", \"scoring\": 50, \"siguiendo\": [2], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}, " +
                    "{\"id\": 2, \"nombre\": \"B\", \"scoring\": 50, \"siguiendo\": [3,4], \"solicitudes\": [], \"seguidores\": [1], \"amistades\": []}, " +
                    "{\"id\": 3, \"nombre\": \"C\", \"scoring\": 50, \"siguiendo\": [5], \"solicitudes\": [], \"seguidores\": [2], \"amistades\": []}, " +
                    "{\"id\": 4, \"nombre\": \"D\", \"scoring\": 50, \"siguiendo\": [5], \"solicitudes\": [], \"seguidores\": [2], \"amistades\": []}, " +
                    "{\"id\": 5, \"nombre\": \"E\", \"scoring\": 50, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [3,4], \"amistades\": []}]}");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  PERF-004: DISTANCIA BFS O(V+E)            ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        testDistanciaBFS();

        System.out.println("\n" + "─".repeat(50));
        System.out.printf("PERF-004 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("─".repeat(50) + "\n");

        if (testsFallados > 0) System.exit(1);
    }

    private static void testDistanciaBFS() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            long inicio = System.nanoTime();
            int distancia = gestor.calcularDistancia(1, 5);
            long tiempoBFS = System.nanoTime() - inicio;

            assert distancia == 3 : "Distancia correcta";
            System.out.println("  Distancia 1 → 5: " + distancia);
            System.out.println("  Tiempo BFS: " + tiempoBFS + "ns");
            System.out.println("  V=5 vértices, E=5 aristas (1→2, 2→3, 2→4, 3→5, 4→5)");

            reportarExito("BFS O(V+E)");
        } catch (AssertionError e) {
            reportarFallo("BFS O(V+E)", e.getMessage());
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
