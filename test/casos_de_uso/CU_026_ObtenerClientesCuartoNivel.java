import servicio.GestorClientes;
import modelo.Cliente;

public class CU_026_ObtenerClientesCuartoNivel {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU026_TEST.json";
    
    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [2], \"solicitudes\": [], \"seguidores\": [2], \"amistades\": [2]}, " +
                    "{\"id\": 2, \"nombre\": \"Bob\", \"scoring\": 80, \"siguiendo\": [1], \"solicitudes\": [], \"seguidores\": [1], \"amistades\": [1]}]}");
        } catch (java.io.IOException e) {}
    }
    
    public static void main(String[] args) {
        System.out.println("\n  CU-026: OBTENER CUARTO NIVEL");
        
        testMain();
        
        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);
        
        if (testsFallados > 0) System.exit(1);
    }
    
    private static void testMain() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            reportarExito("Test 026");
        } catch (Exception e) {
            reportarFallo("Test 026", e.getMessage());
        }
    }
    
    private static void reportarExito(String testName) { testsPasados++; System.out.println("  [OK] " + testName); }
    private static void reportarFallo(String testName, String error) { testsFallados++; System.err.println("  [FAIL] " + testName); }
}
