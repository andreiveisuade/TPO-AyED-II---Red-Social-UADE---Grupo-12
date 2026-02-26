import servicio.GestorClientes;
import modelo.Cliente;

public class CU_035_VerificarConectividad {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU035_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  CU-035: VERIFICAR CONECTIVIDAD");

        testClientesConectados();
        testClientesDesconectados();
        testGrafoDesconectadoComponentes();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testClientesConectados() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            gestor.seguir(a, b);

            int dist = gestor.calcularDistancia(a, b);
            afirmar(dist >= 0, "clientes conectados deben tener distancia >= 0, era: " + dist);
            reportarExito("clientes conectados: distancia " + dist);
        } catch (Exception e) {
            reportarFallo("clientes conectados", e.getMessage());
        }
    }

    private static void testClientesDesconectados() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            // Sin relacion entre ellos

            int dist = gestor.calcularDistancia(a, b);
            afirmar(dist == -1, "clientes sin conexion deben tener distancia -1, era: " + dist);
            reportarExito("clientes desconectados: distancia -1");
        } catch (Exception e) {
            reportarFallo("clientes desconectados", e.getMessage());
        }
    }

    private static void testGrafoDesconectadoComponentes() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);
            // Componente 1: A -> B
            gestor.seguir(a, b);
            // Componente 2: C -> D
            gestor.seguir(c, d);

            // Entre componentes no hay camino
            int distAD = gestor.calcularDistancia(a, d);
            afirmar(distAD == -1, "componentes desconectadas: A->D debe ser -1, era: " + distAD);

            // Dentro de componente si hay camino
            int distAB = gestor.calcularDistancia(a, b);
            afirmar(distAB == 1, "misma componente: A->B debe ser 1, era: " + distAB);
            reportarExito("grafo con 2 componentes desconectadas");
        } catch (Exception e) {
            reportarFallo("grafo desconectado", e.getMessage());
        }
    }

    private static void afirmar(boolean cond, String msg) throws Exception { if (!cond) throw new Exception(msg); }
    private static void reportarExito(String t) { testsPasados++; System.out.println("  [OK] " + t); }
    private static void reportarFallo(String t, String e) { testsFallados++; System.err.println("  [FAIL] " + t + ": " + e); }
}
