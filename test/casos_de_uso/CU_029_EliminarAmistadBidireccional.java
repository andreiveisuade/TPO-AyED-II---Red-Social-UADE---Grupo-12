import servicio.GestorClientes;
import modelo.Cliente;

public class CU_029_EliminarAmistadBidireccional {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU029_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  CU-029: ELIMINAR AMISTAD BIDIRECCIONAL");

        testEliminarAmistadRetornaTrue();
        testEliminacionEsBidireccional();
        testEliminarAmistadInexistente();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    private static void testEliminarAmistadRetornaTrue() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            gestor.agregarAmistad(a, b);

            boolean resultado = gestor.eliminarAmistad(a, b);
            afirmar(resultado, "eliminarAmistad debe retornar true");
            afirmar(!gestor.sonAmigos(a, b), "ya no deben ser amigos");
            reportarExito("eliminarAmistad retorna true");
        } catch (Exception e) {
            reportarFallo("eliminarAmistad retorna true", e.getMessage());
        }
    }

    private static void testEliminacionEsBidireccional() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            gestor.agregarAmistad(a, b);
            gestor.eliminarAmistad(a, b);

            afirmar(!gestor.sonAmigos(a, b), "A no debe ser amigo de B");
            afirmar(!gestor.sonAmigos(b, a), "B no debe ser amigo de A");
            reportarExito("eliminacion es bidireccional");
        } catch (Exception e) {
            reportarFallo("eliminacion es bidireccional", e.getMessage());
        }
    }

    private static void testEliminarAmistadInexistente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);

            boolean resultado = gestor.eliminarAmistad(a, b);
            afirmar(!resultado, "eliminar amistad inexistente debe retornar false");
            reportarExito("eliminar amistad inexistente retorna false");
        } catch (Exception e) {
            reportarFallo("eliminar amistad inexistente retorna false", e.getMessage());
        }
    }

    private static void afirmar(boolean cond, String msg) throws Exception { if (!cond) throw new Exception(msg); }
    private static void reportarExito(String t) { testsPasados++; System.out.println("  [OK] " + t); }
    private static void reportarFallo(String t, String e) { testsFallados++; System.err.println("  [FAIL] " + t + ": " + e); }
}
