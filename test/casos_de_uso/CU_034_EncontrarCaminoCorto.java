import servicio.GestorClientes;
import modelo.Cliente;

public class CU_034_EncontrarCaminoCorto {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU034_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  CU-034: ENCONTRAR CAMINO MAS CORTO (BFS)");
        System.out.println("╚════════════════════════════════════════════╝\n");

        testCaminoDirecto();
        testCaminoMasCortoEntreVarios();
        testCaminoLargo();

        System.out.println("\n" + "-".repeat(50));
        System.out.printf("CU-034 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("-".repeat(50) + "\n");

        if (testsFallados > 0) System.exit(1);
    }

    private static void testCaminoDirecto() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            gestor.seguir(a, b);

            int dist = gestor.calcularDistancia(a, b);
            afirmar(dist == 1, "camino directo debe ser 1, era: " + dist);
            reportarExito("camino directo = 1 salto");
        } catch (Exception e) {
            reportarFallo("camino directo", e.getMessage());
        }
    }

    private static void testCaminoMasCortoEntreVarios() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            // Camino largo: A -> B -> C (2 saltos)
            gestor.seguir(a, b);
            gestor.seguir(b, c);
            // Camino corto: A -> C (1 salto)
            gestor.seguir(a, c);

            int dist = gestor.calcularDistancia(a, c);
            afirmar(dist == 1, "BFS debe encontrar el camino mas corto (1), era: " + dist);
            reportarExito("BFS encuentra camino mas corto entre alternativas");
        } catch (Exception e) {
            reportarFallo("camino mas corto", e.getMessage());
        }
    }

    private static void testCaminoLargo() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            // A -> B -> C  (cadena de 2 saltos)
            gestor.seguir(a, b);
            gestor.seguir(b, c);

            int dist = gestor.calcularDistancia(a, c);
            afirmar(dist == 2, "cadena A->B->C debe ser 2, era: " + dist);
            reportarExito("cadena de 2 saltos");
        } catch (Exception e) {
            reportarFallo("cadena de 2 saltos", e.getMessage());
        }
    }

    private static void afirmar(boolean cond, String msg) throws Exception { if (!cond) throw new Exception(msg); }
    private static void reportarExito(String t) { testsPasados++; System.out.println("  [OK] " + t); }
    private static void reportarFallo(String t, String e) { testsFallados++; System.err.println("  [FAIL] " + t + ": " + e); }
}
