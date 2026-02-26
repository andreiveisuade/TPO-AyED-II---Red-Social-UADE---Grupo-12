import servicio.GestorClientes;
import modelo.Cliente;

/*
CU-033: Calcular Distancia entre Dos Usuarios

Descripción: Calcula la distancia (saltos) entre dos usuarios usando BFS.
Flujo: Usuario A → Usuario B → BFS → Retorna distancia
Complejidad: O(V + E)
*/

public class CU_033_CalcularDistancia {

    private static int testsPasados = 0;
    private static int testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU033_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [" +
                    "{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [2], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}, " +
                    "{\"id\": 2, \"nombre\": \"Bob\", \"scoring\": 80, \"siguiendo\": [3], \"solicitudes\": [], \"seguidores\": [1], \"amistades\": []}, " +
                    "{\"id\": 3, \"nombre\": \"Charlie\", \"scoring\": 70, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [2], \"amistades\": []}]}");
        } catch (java.io.IOException e) {
            System.err.println("Error creating test DB: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  CU-033: CALCULAR DISTANCIA                 ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        testDistanciaMismoUsuario();
        testDistanciaDirecta();
        testDistanciaMultipleSaltos();
        testDistanciaSinCamino();
        testDistanciaUsuarioInexistente();

        System.out.println("\n" + "─".repeat(50));
        System.out.printf("CU-033 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("─".repeat(50) + "\n");

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void testDistanciaMismoUsuario() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            // Distancia de un usuario a sí mismo
            int distancia = gestor.calcularDistancia(1, 1);
            assert distancia == 0 : "Distancia a sí mismo debe ser 0";

            reportarExito("Distancia mismo usuario");
        } catch (AssertionError e) {
            reportarFallo("Distancia mismo usuario", e.getMessage());
        }
    }

    private static void testDistanciaDirecta() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            // Alice sigue a Bob → distancia 1
            int distancia = gestor.calcularDistancia(1, 2);
            assert distancia == 1 : "Distancia directa debe ser 1";

            reportarExito("Distancia directa");
        } catch (AssertionError e) {
            reportarFallo("Distancia directa", e.getMessage());
        }
    }

    private static void testDistanciaMultipleSaltos() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            // Alice → Bob → Charlie → distancia 2
            int distancia = gestor.calcularDistancia(1, 3);
            assert distancia == 2 : "Distancia con 2 saltos debe ser 2";

            reportarExito("Distancia múltiple saltos");
        } catch (AssertionError e) {
            reportarFallo("Distancia múltiple saltos", e.getMessage());
        }
    }

    private static void testDistanciaSinCamino() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            // Charlie no sigue a nadie, por lo que Alice no puede llegar a Charlie desde Charlie
            int distancia = gestor.calcularDistancia(3, 1);
            assert distancia == -1 : "Sin camino debe retornar -1";

            reportarExito("Distancia sin camino");
        } catch (AssertionError e) {
            reportarFallo("Distancia sin camino", e.getMessage());
        }
    }

    private static void testDistanciaUsuarioInexistente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            // Usuario inexistente
            int distancia = gestor.calcularDistancia(1, 9999);
            assert distancia == -1 : "Usuario inexistente debe retornar -1";

            reportarExito("Distancia usuario inexistente");
        } catch (AssertionError e) {
            reportarFallo("Distancia usuario inexistente", e.getMessage());
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
