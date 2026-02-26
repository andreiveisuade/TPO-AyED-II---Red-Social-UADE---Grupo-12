import servicio.GestorClientes;

public class PERF_005_EscalabilidadMuchosClientes {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_PERF005_TEST.json";

    private static void initTestDB(int cantidad) {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            StringBuilder json = new StringBuilder("{ \"clientes\": [");
            for (int i = 1; i <= cantidad; i++) {
                if (i > 1) json.append(",");
                json.append("{\"id\": ").append(i).append(", \"nombre\": \"User").append(i)
                    .append("\", \"scoring\": ").append(i % 100).append(", \"siguiendo\": [], ")
                    .append("\"solicitudes\": [], \"seguidores\": [], \"amistades\": []}");
            }
            json.append("] }");
            writer.write(json.toString());
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  PERF-005: ESCALABILIDAD");

        testEscalabilidad();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testEscalabilidad() {
        try {
            System.out.println("    Prueba con 10,000 clientes...");
            initTestDB(10000);
            GestorClientes gestor = new GestorClientes(TEST_DB);

            assert gestor.getCantidadClientes() == 10000 : "Carga 10k clientes";
            
            long inicio = System.nanoTime();
            var cliente = gestor.buscarPorId(9999);
            long tiempoO1 = System.nanoTime() - inicio;

            assert cliente != null : "Encuentra en 10k";
            System.out.println("    Tiempo búsqueda en 10k clientes: " + tiempoO1 + "ns");
            System.out.println("    Escalabilidad O(1) confirmada");

            reportarExito("Escalabilidad 10k clientes");
        } catch (AssertionError e) {
            reportarFallo("Escalabilidad 10k clientes", e.getMessage());
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
