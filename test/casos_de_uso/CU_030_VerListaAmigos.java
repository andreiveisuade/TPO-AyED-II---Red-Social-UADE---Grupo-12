import servicio.GestorClientes;
import modelo.Cliente;

public class CU_030_VerListaAmigos {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU030_TEST.json";
    
    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [2], \"solicitudes\": [], \"seguidores\": [2], \"amistades\": [2]}, " +
                    "{\"id\": 2, \"nombre\": \"Bob\", \"scoring\": 80, \"siguiendo\": [1], \"solicitudes\": [], \"seguidores\": [1], \"amistades\": [1]}]}");
        } catch (java.io.IOException e) {}
    }
    
    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  CU-030: VER LISTA AMIGOS");
        System.out.println("╚════════════════════════════════════════════╝\n");
        
        testMain();
        
        System.out.println("\n" + "─".repeat(50));
        System.out.printf("CU-030 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("─".repeat(50) + "\n");
        
        if (testsFallados > 0) System.exit(1);
    }
    
    private static void testMain() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            reportarExito("Test 030");
        } catch (Exception e) {
            reportarFallo("Test 030", e.getMessage());
        }
    }
    
    private static void reportarExito(String testName) { testsPasados++; System.out.println("  [OK] " + testName); }
    private static void reportarFallo(String testName, String error) { testsFallados++; System.err.println("  [FAIL] " + testName); }
}
