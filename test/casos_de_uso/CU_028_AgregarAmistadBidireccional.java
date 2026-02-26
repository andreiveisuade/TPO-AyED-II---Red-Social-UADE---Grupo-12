import servicio.GestorClientes;
import modelo.Cliente;

public class CU_028_AgregarAmistadBidireccional {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU028_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  CU-028: AGREGAR AMISTAD BIDIRECCIONAL");

        testAgregarAmistadRetornaTrue();
        testAmistadEsBidireccional();
        testAmistadDuplicadaRetornaFalse();
        testAmistadConsigoMismoRetornaFalse();
        testAmistadConClienteInexistente();
        testLimiteMaxAmigos();
        testLimiteMaxAmigosDelOtroLado();
        testLimiteNoModificaNadaSiFalla();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testAgregarAmistadRetornaTrue() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);

            boolean resultado = gestor.agregarAmistad(a, b);
            afirmar(resultado, "agregarAmistad debe retornar true");
            reportarExito("agregarAmistad retorna true");
        } catch (Exception e) {
            reportarFallo("agregarAmistad retorna true", e.getMessage());
        }
    }

    private static void testAmistadEsBidireccional() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            gestor.agregarAmistad(a, b);

            afirmar(gestor.sonAmigos(a, b), "A debe ser amigo de B");
            afirmar(gestor.sonAmigos(b, a), "B debe ser amigo de A (bidireccional)");
            reportarExito("amistad es bidireccional");
        } catch (Exception e) {
            reportarFallo("amistad es bidireccional", e.getMessage());
        }
    }

    private static void testAmistadDuplicadaRetornaFalse() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            gestor.agregarAmistad(a, b);

            boolean duplicada = gestor.agregarAmistad(a, b);
            afirmar(!duplicada, "amistad duplicada debe retornar false");
            reportarExito("amistad duplicada retorna false");
        } catch (Exception e) {
            reportarFallo("amistad duplicada retorna false", e.getMessage());
        }
    }

    private static void testAmistadConsigoMismoRetornaFalse() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);

            boolean resultado = gestor.agregarAmistad(a, a);
            afirmar(!resultado, "amistad consigo mismo debe retornar false");
            reportarExito("amistad consigo mismo retorna false");
        } catch (Exception e) {
            reportarFallo("amistad consigo mismo retorna false", e.getMessage());
        }
    }

    private static void testAmistadConClienteInexistente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);

            boolean resultado = gestor.agregarAmistad(a, 9999);
            afirmar(!resultado, "amistad con inexistente debe retornar false");
            reportarExito("amistad con cliente inexistente retorna false");
        } catch (Exception e) {
            reportarFallo("amistad con cliente inexistente retorna false", e.getMessage());
        }
    }

    private static void testLimiteMaxAmigos() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);

            afirmar(gestor.agregarAmistad(a, b), "1ra amistad debe funcionar");
            afirmar(gestor.agregarAmistad(a, c), "2da amistad debe funcionar");
            afirmar(!gestor.agregarAmistad(a, d), "3ra amistad debe fallar (limite " + Cliente.MAX_AMIGOS + ")");
            afirmar(!gestor.sonAmigos(a, d), "A y D no deben ser amigos");
            afirmar(gestor.obtenerCantidadAmigos(a) == 2, "A debe tener exactamente 2 amigos");

            reportarExito("limite MAX_AMIGOS=" + Cliente.MAX_AMIGOS);
        } catch (Exception e) {
            reportarFallo("limite MAX_AMIGOS", e.getMessage());
        }
    }

    private static void testLimiteMaxAmigosDelOtroLado() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);

            // B ya tiene 2 amigos
            gestor.agregarAmistad(b, c);
            gestor.agregarAmistad(b, d);

            // A intenta ser amigo de B, pero B ya esta lleno
            afirmar(!gestor.agregarAmistad(a, b), "debe fallar porque B ya tiene " + Cliente.MAX_AMIGOS + " amigos");
            afirmar(!gestor.sonAmigos(a, b), "A y B no deben ser amigos");

            reportarExito("limite MAX_AMIGOS del otro lado");
        } catch (Exception e) {
            reportarFallo("limite MAX_AMIGOS del otro lado", e.getMessage());
        }
    }

    private static void testLimiteNoModificaNadaSiFalla() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);

            gestor.agregarAmistad(b, c);
            gestor.agregarAmistad(b, d);

            int amigosA = gestor.obtenerCantidadAmigos(a);
            int amigosB = gestor.obtenerCantidadAmigos(b);

            gestor.agregarAmistad(a, b); // debe fallar

            afirmar(gestor.obtenerCantidadAmigos(a) == amigosA, "A no debe haber cambiado");
            afirmar(gestor.obtenerCantidadAmigos(b) == amigosB, "B no debe haber cambiado");

            reportarExito("fallo no modifica estado de ninguno");
        } catch (Exception e) {
            reportarFallo("fallo no modifica estado", e.getMessage());
        }
    }

    private static void afirmar(boolean cond, String msg) throws Exception { if (!cond) throw new Exception(msg); }
    private static void reportarExito(String t) { testsPasados++; System.out.println("  [OK] " + t); }
    private static void reportarFallo(String t, String e) { testsFallados++; System.err.println("  [FAIL] " + t + ": " + e); }
}
