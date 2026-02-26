import servicio.GestorClientes;
import modelo.Cliente;

public class CU_032_ObtenerCantidadAmigos {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU032_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  CU-032: OBTENER CANTIDAD DE AMIGOS");
        System.out.println("╚════════════════════════════════════════════╝\n");

        testCantidadCero();
        testCantidadUno();
        testCantidadMultiple();

        System.out.println("\n" + "-".repeat(50));
        System.out.printf("CU-032 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("-".repeat(50) + "\n");

        if (testsFallados > 0) System.exit(1);
    }

    private static void testCantidadCero() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);

            int cantidad = gestor.obtenerCantidadAmigos(a);
            afirmar(cantidad == 0, "sin amigos debe ser 0, era: " + cantidad);
            reportarExito("cantidad 0 sin amigos");
        } catch (Exception e) {
            reportarFallo("cantidad 0 sin amigos", e.getMessage());
        }
    }

    private static void testCantidadUno() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            gestor.agregarAmistad(a, b);

            int cantidadA = gestor.obtenerCantidadAmigos(a);
            int cantidadB = gestor.obtenerCantidadAmigos(b);
            afirmar(cantidadA == 1, "Alice debe tener 1 amigo, tiene: " + cantidadA);
            afirmar(cantidadB == 1, "Bob debe tener 1 amigo, tiene: " + cantidadB);
            reportarExito("cantidad 1 despues de agregar amistad");
        } catch (Exception e) {
            reportarFallo("cantidad 1", e.getMessage());
        }
    }

    private static void testCantidadMultiple() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);
            gestor.agregarAmistad(a, b);
            gestor.agregarAmistad(a, c);
            gestor.agregarAmistad(a, d);

            int cantidad = gestor.obtenerCantidadAmigos(a);
            afirmar(cantidad == 3, "Alice debe tener 3 amigos, tiene: " + cantidad);
            reportarExito("cantidad 3 con multiples amistades");
        } catch (Exception e) {
            reportarFallo("cantidad multiple", e.getMessage());
        }
    }

    private static void afirmar(boolean cond, String msg) throws Exception { if (!cond) throw new Exception(msg); }
    private static void reportarExito(String t) { testsPasados++; System.out.println("  [OK] " + t); }
    private static void reportarFallo(String t, String e) { testsFallados++; System.err.println("  [FAIL] " + t + ": " + e); }
}
