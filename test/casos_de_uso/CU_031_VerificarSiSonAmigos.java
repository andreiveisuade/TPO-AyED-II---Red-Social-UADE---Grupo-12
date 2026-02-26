import servicio.GestorClientes;
import modelo.Cliente;

public class CU_031_VerificarSiSonAmigos {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU031_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  CU-031: VERIFICAR SI SON AMIGOS");

        testSonAmigosTrue();
        testSonAmigosFalse();
        testSonAmigosBidireccional();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testSonAmigosTrue() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            gestor.agregarAmistad(a, b);

            afirmar(gestor.sonAmigos(a, b), "sonAmigos(A,B) debe ser true");
            reportarExito("sonAmigos retorna true para amigos");
        } catch (Exception e) {
            reportarFallo("sonAmigos retorna true", e.getMessage());
        }
    }

    private static void testSonAmigosFalse() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);

            afirmar(!gestor.sonAmigos(a, b), "sonAmigos(A,B) debe ser false sin amistad");
            reportarExito("sonAmigos retorna false para no-amigos");
        } catch (Exception e) {
            reportarFallo("sonAmigos retorna false", e.getMessage());
        }
    }

    private static void testSonAmigosBidireccional() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            gestor.agregarAmistad(a, b);

            afirmar(gestor.sonAmigos(a, b), "sonAmigos(A,B) = true");
            afirmar(gestor.sonAmigos(b, a), "sonAmigos(B,A) = true (bidireccional)");
            reportarExito("sonAmigos es bidireccional");
        } catch (Exception e) {
            reportarFallo("sonAmigos bidireccional", e.getMessage());
        }
    }

    private static void afirmar(boolean cond, String msg) throws Exception { if (!cond) throw new Exception(msg); }
    private static void reportarExito(String t) { testsPasados++; System.out.println("  [OK] " + t); }
    private static void reportarFallo(String t, String e) { testsFallados++; System.err.println("  [FAIL] " + t + ": " + e); }
}
