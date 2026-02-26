import servicio.GestorClientes;

/**
 * Pruebas unitarias para calcularDistancia() en la red social.
 *
 * REQUISITO: Distancia entre Clientes
 * Calcula el número de saltos entre dos clientes en la red.
 * Usa BFS sobre el grafo de seguimientos.
 *
 * Ejecución: java -ea -cp out:lib/gson-2.10.1.jar DistanciaTest
 */
public class DistanciaTest {

    private static int testsPasados = 0;
    private static int testsFallidos = 0;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║         TESTS: DISTANCIA ENTRE CLIENTES (BFS)          ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        testMismoCliente();
        testConexionDirecta();
        testDosHops();
        testSinCamino();
        testClienteInexistente();
        testGrafoDesconectado();
        testDistanciaInversa();

        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                    RESUMEN DE TESTS                    ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.printf("║ ✓ Passed: %-47d ║\n", testsPasados);
        System.out.printf("║ ✗ Failed: %-47d ║\n", testsFallidos);
        System.out.println("╚════════════════════════════════════════════════════════╝");

        if (testsFallidos == 0) {
            System.out.println("\n✅ ¡TODOS LOS TESTS PASARON! calcularDistancia está lista.");
        } else {
            System.out.println("\n❌ Hay errores a corregir.");
        }
    }

    /**
     * TEST 1: Distancia de un cliente a sí mismo debe ser 0.
     */
    private static void testMismoCliente() {
        System.out.println("TEST 1: Mismo cliente → distancia 0");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);

            int distancia = gestor.calcularDistancia(a, a);
            afirmar(distancia == 0, "Distancia de A a A debe ser 0, era: " + distancia);

            System.out.println("  ✓ calcularDistancia(A, A) = 0\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Mismo cliente", e);
            testsFallidos++;
        }
    }

    /**
     * TEST 2: Conexión directa (1 hop).
     * A → B  →  distancia = 1
     */
    private static void testConexionDirecta() {
        System.out.println("TEST 2: Conexión directa → distancia 1");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob",   70);
            gestor.seguir(a, b); // A → B

            int distancia = gestor.calcularDistancia(a, b);
            afirmar(distancia == 1, "Distancia A→B debe ser 1, era: " + distancia);

            System.out.println("  ✓ calcularDistancia(A, B) = 1  [A→B]\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Conexión directa", e);
            testsFallidos++;
        }
    }

    /**
     * TEST 3: Camino de 2 saltos.
     * A → B → C  →  distancia(A, C) = 2
     */
    private static void testDosHops() {
        System.out.println("TEST 3: Camino de 2 saltos → distancia 2");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice",   80);
            int b = gestor.agregarCliente("Bob",     70);
            int c = gestor.agregarCliente("Charlie", 60);
            gestor.seguir(a, b); // A → B
            gestor.seguir(b, c); // B → C

            int distancia = gestor.calcularDistancia(a, c);
            afirmar(distancia == 2, "Distancia A→B→C debe ser 2, era: " + distancia);

            System.out.println("  ✓ calcularDistancia(A, C) = 2  [A→B→C]\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Dos saltos", e);
            testsFallidos++;
        }
    }

    /**
     * TEST 4: Sin camino posible entre clientes.
     * A → B, C aislado  →  distancia(A, C) = -1
     */
    private static void testSinCamino() {
        System.out.println("TEST 4: Sin camino posible → distancia -1");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice",   80);
            int b = gestor.agregarCliente("Bob",     70);
            int c = gestor.agregarCliente("Charlie", 60);
            gestor.seguir(a, b); // Solo A → B  (C queda aislado)

            int distancia = gestor.calcularDistancia(a, c);
            afirmar(distancia == -1, "Distancia a C (aislado) debe ser -1, era: " + distancia);

            System.out.println("  ✓ calcularDistancia(A, C) = -1  [C no alcanzable]\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Sin camino", e);
            testsFallidos++;
        }
    }

    /**
     * TEST 5: Cliente que no existe → -1.
     */
    private static void testClienteInexistente() {
        System.out.println("TEST 5: Cliente inexistente → distancia -1");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);

            int distancia = gestor.calcularDistancia(a, 9999);
            afirmar(distancia == -1, "Distancia a ID inexistente debe ser -1, era: " + distancia);

            System.out.println("  ✓ calcularDistancia(A, 9999) = -1  [ID inexistente]\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Cliente inexistente", e);
            testsFallidos++;
        }
    }

    /**
     * TEST 6: Grafo desconectado — dos componentes separadas.
     *   A → B     C → D   → distancia(A, D) = -1
     */
    private static void testGrafoDesconectado() {
        System.out.println("TEST 6: Grafo desconectado → distancia -1");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice",   80);
            int b = gestor.agregarCliente("Bob",     70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("David",   50);
            gestor.seguir(a, b); // A → B
            gestor.seguir(c, d); // C → D (componente separada)

            int distancia = gestor.calcularDistancia(a, d);
            afirmar(distancia == -1, "Distancia entre componentes desconectadas debe ser -1, era: " + distancia);

            System.out.println("  ✓ calcularDistancia(A, D) = -1  [componentes desconectadas]\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Grafo desconectado", e);
            testsFallidos++;
        }
    }

    /**
     * TEST 7: La distancia es dirigida — A→B no implica B→A.
     * A → B  →  distancia(B, A) = -1  (no hay camino inverso)
     */
    private static void testDistanciaInversa() {
        System.out.println("TEST 7: Relación dirigida → camino inverso = -1");
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob",   70);
            gestor.seguir(a, b); // Solo A → B (no B → A)

            int directa = gestor.calcularDistancia(a, b);
            int inversa  = gestor.calcularDistancia(b, a);

            afirmar(directa == 1, "Distancia directa A→B debe ser 1, era: " + directa);
            afirmar(inversa == -1, "Distancia inversa B→A debe ser -1 (dirigido), era: " + inversa);

            System.out.println("  ✓ calcularDistancia(A, B) = 1  [A sigue a B]");
            System.out.println("  ✓ calcularDistancia(B, A) = -1 [B no sigue a A]\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Relación dirigida", e);
            testsFallidos++;
        }
    }

    // ════════════════════════════════════════════════════════
    // UTILIDADES
    // ════════════════════════════════════════════════════════

    private static void afirmar(boolean condicion, String mensaje) throws Exception {
        if (!condicion) throw new Exception(mensaje);
    }

    private static void printError(String test, Exception e) {
        System.out.println("  ✗ ERROR en '" + test + "': " + e.getMessage() + "\n");
    }
}
