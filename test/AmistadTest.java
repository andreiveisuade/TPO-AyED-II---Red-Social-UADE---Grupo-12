import servicio.GestorClientes;
import modelo.Cliente;

/**
 * Pruebas unitarias para amistades bidireccionales (Iteración 3).
 *
 * REQUISITO: Relaciones Generales
 * Las amistades son relaciones bidireccionales entre clientes.
 * Si A es amigo de B, entonces B es amigo de A automáticamente.
 *
 * Ejecución: java -ea -cp out:lib/gson-2.10.1.jar AmistadTest
 */
public class AmistadTest {

    private static int testsPasados = 0;
    private static int testsFallidos = 0;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║      TESTS: AMISTADES BIDIRECCIONALES (ITER 3)         ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        testAgregarAmistad();
        testAmistadesEsBidireccional();
        testEliminarAmistad();
        testEliminarAmistadesEsBidireccional();
        testObtenerAmigos();
        testVerificarAmistad();
        testAmistadConsigoMismo();

        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                    RESUMEN DE TESTS                    ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.printf("║ ✓ Passed: %-47d ║\n", testsPasados);
        System.out.printf("║ ✗ Failed: %-47d ║\n", testsFallidos);
        System.out.println("╚════════════════════════════════════════════════════════╝");

        if (testsFallidos == 0) {
            System.out.println("\n✅ ¡TODOS LOS TESTS PASARON! Amistades implementadas.");
        } else {
            System.out.println("\n❌ Hay errores a corregir.");
        }
    }

    /**
     * TEST 1: Agregar una amistad simple.
     */
    private static void testAgregarAmistad() {
        System.out.println("TEST 1: Agregar amistad");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);

            boolean resultado = gestor.agregarAmistad(a, b);
            afirmar(resultado, "agregarAmistad debería retornar true");
            afirmar(gestor.sonAmigos(a, b), "Alice y Bob deberían ser amigos");

            System.out.println("  ✓ agregarAmistad(Alice, Bob) = true\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Agregar amistad", e);
            testsFallidos++;
        }
    }

    /**
     * TEST 2: Verificar que la amistad es bidireccional.
     * Si A es amigo de B, entonces B es amigo de A.
     */
    private static void testAmistadesEsBidireccional() {
        System.out.println("TEST 2: Amistad es bidireccional");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);

            gestor.agregarAmistad(a, b);

            afirmar(gestor.sonAmigos(a, b), "A debería ser amigo de B");
            afirmar(gestor.sonAmigos(b, a), "B debería ser amigo de A (bidireccional)");

            System.out.println("  ✓ sonAmigos(A, B) = true");
            System.out.println("  ✓ sonAmigos(B, A) = true (bidireccional)\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Bidireccionalidad", e);
            testsFallidos++;
        }
    }

    /**
     * TEST 3: Eliminar una amistad.
     */
    private static void testEliminarAmistad() {
        System.out.println("TEST 3: Eliminar amistad");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);

            gestor.agregarAmistad(a, b);
            boolean resultado = gestor.eliminarAmistad(a, b);

            afirmar(resultado, "eliminarAmistad debería retornar true");
            afirmar(!gestor.sonAmigos(a, b), "Alice y Bob no deberían ser amigos");

            System.out.println("  ✓ eliminarAmistad(Alice, Bob) = true");
            System.out.println("  ✓ sonAmigos(A, B) = false\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Eliminar amistad", e);
            testsFallidos++;
        }
    }

    /**
     * TEST 4: Eliminar amistad también es bidireccional.
     */
    private static void testEliminarAmistadesEsBidireccional() {
        System.out.println("TEST 4: Eliminar amistad es bidireccional");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);

            gestor.agregarAmistad(a, b);
            gestor.eliminarAmistad(a, b);

            afirmar(!gestor.sonAmigos(a, b), "A no debería ser amigo de B");
            afirmar(!gestor.sonAmigos(b, a), "B no debería ser amigo de A");

            System.out.println("  ✓ Eliminar amistad es bidireccional\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Eliminar bidireccional", e);
            testsFallidos++;
        }
    }

    /**
     * TEST 5: Obtener lista de amigos.
     */
    private static void testObtenerAmigos() {
        System.out.println("TEST 5: Obtener amigos");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);

            gestor.agregarAmistad(a, b);
            gestor.agregarAmistad(a, c);

            Cliente[] amigos = gestor.obtenerAmigos(a);
            afirmar(amigos.length == 2, "Alice debería tener 2 amigos, tiene: " + amigos.length);

            System.out.println("  ✓ obtenerAmigos(Alice).length = 2\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Obtener amigos", e);
            testsFallidos++;
        }
    }

    /**
     * TEST 6: Verificar amistad con O(1).
     */
    private static void testVerificarAmistad() {
        System.out.println("TEST 6: Verificar amistad O(1)");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);

            gestor.agregarAmistad(a, b);

            afirmar(gestor.sonAmigos(a, b), "sonAmigos(A, B) debería ser true");
            afirmar(!gestor.sonAmigos(a, c), "sonAmigos(A, C) debería ser false");

            System.out.println("  ✓ sonAmigos(A, B) = true");
            System.out.println("  ✓ sonAmigos(A, C) = false\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Verificar amistad", e);
            testsFallidos++;
        }
    }

    /**
     * TEST 7: No se puede ser amigo de sí mismo.
     */
    private static void testAmistadConsigoMismo() {
        System.out.println("TEST 7: No se puede ser amigo de sí mismo");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);

            boolean resultado = gestor.agregarAmistad(a, a);
            afirmar(!resultado, "agregarAmistad(A, A) debería retornar false");
            afirmar(!gestor.sonAmigos(a, a), "Un cliente no debería ser amigo de sí mismo");

            System.out.println("  ✓ agregarAmistad(Alice, Alice) = false\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Amistad consigo mismo", e);
            testsFallidos++;
        }
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ════════════════════════════════════════════════════════════════════════════════════

    private static void afirmar(boolean condicion, String mensaje) throws Exception {
        if (!condicion) throw new Exception(mensaje);
    }

    private static void printError(String test, Exception e) {
        System.out.println("  ✗ ERROR en '" + test + "': " + e.getMessage() + "\n");
    }
}
