import servicio.GestorClientes;
import modelo.Cliente;

public class CU_030_VerListaAmigos {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU030_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  CU-030: VER LISTA DE AMIGOS");

        testListaVacia();
        testListaConAmigos();
        testListaConMultiplesAmigos();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testListaVacia() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);

            Cliente[] amigos = gestor.obtenerAmigos(a);
            afirmar(amigos.length == 0, "sin amigos debe retornar array vacio");
            reportarExito("lista vacia sin amigos");
        } catch (Exception e) {
            reportarFallo("lista vacia sin amigos", e.getMessage());
        }
    }

    private static void testListaConAmigos() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            gestor.agregarAmistad(a, b);

            Cliente[] amigosA = gestor.obtenerAmigos(a);
            afirmar(amigosA.length == 1, "Alice debe tener 1 amigo, tiene: " + amigosA.length);
            afirmar(amigosA[0].getId() == b, "el amigo de Alice debe ser Bob");

            Cliente[] amigosB = gestor.obtenerAmigos(b);
            afirmar(amigosB.length == 1, "Bob debe tener 1 amigo, tiene: " + amigosB.length);
            reportarExito("lista con 1 amigo (ambos lados)");
        } catch (Exception e) {
            reportarFallo("lista con 1 amigo", e.getMessage());
        }
    }

    private static void testListaConMultiplesAmigos() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            gestor.agregarAmistad(a, b);
            gestor.agregarAmistad(a, c);

            Cliente[] amigosA = gestor.obtenerAmigos(a);
            afirmar(amigosA.length == 2, "Alice debe tener 2 amigos, tiene: " + amigosA.length);
            reportarExito("lista con multiples amigos");
        } catch (Exception e) {
            reportarFallo("lista con multiples amigos", e.getMessage());
        }
    }

    private static void afirmar(boolean cond, String msg) throws Exception { if (!cond) throw new Exception(msg); }
    private static void reportarExito(String t) { testsPasados++; System.out.println("  [OK] " + t); }
    private static void reportarFallo(String t, String e) { testsFallados++; System.err.println("  [FAIL] " + t + ": " + e); }
}
