import servicio.GestorClientes;
import modelo.Cliente;

/*
CU-003: Agregar Cliente

Descripción: Sistema carga y agrega clientes desde JSON.
Flujo: JSON → Cargar clientes → Indexar por ID, Nombre, Scoring
Complejidad: O(1) por cliente, O(N) total
*/

public class CU_003_AgregarCliente {

    private static int testsPasados = 0;
    private static int testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU003_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  CU-003: AGREGAR CLIENTE                   ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        testAgregarClienteValido();
        testIdAutogenerado();
        testValidacionesNombre();
        testValidacionesScoring();
        testCantidadClientes();

        System.out.println("\n" + "─".repeat(50));
        System.out.printf("CU-003 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("─".repeat(50) + "\n");

        if (testsFallados > 0) System.exit(1);
    }

    private static void testAgregarClienteValido() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id = gestor.agregarCliente("Alice", 95);
            assert id != -1 : "ID válido";
            assert gestor.buscarPorId(id) != null : "Cliente existe";
            reportarExito("Agregar cliente válido");
        } catch (AssertionError e) {
            reportarFallo("Agregar cliente válido", e.getMessage());
        }
    }

    private static void testIdAutogenerado() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id1 = gestor.agregarCliente("Alice", 95);
            int id2 = gestor.agregarCliente("Bob", 80);
            assert id1 != id2 : "IDs diferentes";
            assert id1 > 0 && id2 > 0 : "IDs positivos";
            reportarExito("ID autogenerado");
        } catch (AssertionError e) {
            reportarFallo("ID autogenerado", e.getMessage());
        }
    }

    private static void testValidacionesNombre() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id1 = gestor.agregarCliente("", 95);
            assert id1 == -1 : "Rechaza nombre vacío";
            reportarExito("Validaciones nombre");
        } catch (AssertionError e) {
            reportarFallo("Validaciones nombre", e.getMessage());
        }
    }

    private static void testValidacionesScoring() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id1 = gestor.agregarCliente("Alice", -1);
            int id2 = gestor.agregarCliente("Bob", 101);
            assert id1 == -1 || id2 == -1 : "Scoring válido 0-100";
            reportarExito("Validaciones scoring");
        } catch (AssertionError e) {
            reportarFallo("Validaciones scoring", e.getMessage());
        }
    }

    private static void testCantidadClientes() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            assert gestor.getCantidadClientes() == 0 : "Inicia vacío";
            gestor.agregarCliente("Alice", 95);
            assert gestor.getCantidadClientes() == 1 : "1 cliente";
            reportarExito("Cantidad clientes");
        } catch (AssertionError e) {
            reportarFallo("Cantidad clientes", e.getMessage());
        }
    }

    private static void reportarExito(String testName) {
        testsPasados++;
        System.out.println("  [OK] " + testName);
    }

    private static void reportarFallo(String testName, String error) {
        testsFallados++;
        System.err.println("  [FAIL] " + testName + " - " + error);
    }
}
