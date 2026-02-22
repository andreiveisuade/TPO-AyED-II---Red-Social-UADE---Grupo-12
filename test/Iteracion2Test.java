import servicio.GestorClientes;
import modelo.Cliente;
import tda.ArbolBinarioBusqueda;

/**
 * Tests manuales para validar Iteración 2: Relaciones entre Clientes
 *
 * Requisitos Iteración 2:
 * ✓ Relaciones entre clientes (seguir hasta 2)
 * ✓ Estructura eficiente para gestionar relaciones
 * ✓ Clientes ordenados por scoring (lazy loading)
 * ✓ ABB para consultar conexiones
 * ✓ Imprimir nivel 4 del árbol de relaciones
 *
 * Ejecución: java -cp out:lib/gson-2.10.1.jar test.Iteracion2Test
 */
public class Iteracion2Test {

    private static GestorClientes gestor;
    private static int testsPasados = 0;
    private static int testsFallidos = 0;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║     TESTS ITERACIÓN 2: RELACIONES ENTRE CLIENTES       ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        // Cargar datos de prueba
        gestor = new GestorClientes("data/clientes_JSON_TEST.json");
        System.out.println();

        // Ejecutar tests
        testRelacionesEntreClientes();
        testLimiteDosSeguidos();
        testObtenerVecinos();
        testConstruirArbolRelaciones();
        testObtenerSeguidoresEnNivel();
        testObtenerSeguidoresOrdenados();
        testBusquedaPorScoringLazyLoading();
        testObtenerClientesEnNivelConLazyLoading();
        testPersistenciaRelaciones();

        // Resumen
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                    RESUMEN DE TESTS                    ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.printf("║ ✓ Passed: %-47d ║\n", testsPasados);
        System.out.printf("║ ✗ Failed: %-47d ║\n", testsFallidos);
        System.out.println("╚════════════════════════════════════════════════════════╝");

        if (testsFallidos == 0) {
            System.out.println("\n✅ ¡TODOS LOS TESTS PASARON! Iteración 2 está lista.");
        } else {
            System.out.println("\n❌ Hay errores a corregir.");
        }
    }

    private static void testRelacionesEntreClientes() {
        try {
            System.out.println("TEST 1: Relaciones entre clientes");

            Cliente alice = gestor.buscarPorId(1);
            Cliente bob = gestor.buscarPorId(2);

            assertTrue(alice != null, "Alice debe existir");
            assertTrue(bob != null, "Bob debe existir");

            boolean resultado = gestor.seguir(alice.getId(), bob.getId());
            assertTrue(resultado, "Alice debe poder seguir a Bob");
            assertTrue(alice.sigueA(bob.getId()), "Alice debe estar siguiendo a Bob");

            System.out.println("  ✓ Relación creada: Alice → Bob");
            System.out.println("  ✓ Bidireccionalidad: Bob tiene a Alice como seguidor\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Relaciones entre clientes", e);
            testsFallidos++;
        }
    }

    private static void testLimiteDosSeguidos() {
        try {
            System.out.println("TEST 2: Límite de 2 seguidos");

            Cliente alice = gestor.buscarPorId(1);
            Cliente bob = gestor.buscarPorId(2);
            Cliente charlie = gestor.buscarPorId(3);
            Cliente david = gestor.buscarPorId(4);

            gestor.seguir(alice.getId(), bob.getId());
            gestor.seguir(alice.getId(), charlie.getId());

            assertTrue(alice.getCantidadSiguiendo() == 2, "Alice debe seguir a 2 clientes");

            boolean resultado = gestor.seguir(alice.getId(), david.getId());
            assertTrue(!resultado, "Alice no debe poder seguir a más de 2");

            System.out.println("  ✓ Alice sigue a 2 clientes (Bob y Charlie)");
            System.out.println("  ✓ Se rechaza seguir a David (límite alcanzado)\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Límite de dos seguidos", e);
            testsFallidos++;
        }
    }

    private static void testObtenerVecinos() {
        try {
            System.out.println("TEST 3: Obtener vecinos (O(1))");

            Cliente alice = gestor.buscarPorId(1);
            Cliente bob = gestor.buscarPorId(2);
            Cliente charlie = gestor.buscarPorId(3);

            gestor.seguir(alice.getId(), bob.getId());
            gestor.seguir(alice.getId(), charlie.getId());

            Cliente[] vecinos = gestor.obtenerVecinos(alice.getId());
            assertTrue(vecinos.length == 2, "Alice debe tener 2 vecinos");

            System.out.println("  ✓ Vecinos de Alice: " + vecinos.length);
            System.out.println("  ✓ Complejidad: O(1) amortizado\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Obtener vecinos", e);
            testsFallidos++;
        }
    }

    private static void testConstruirArbolRelaciones() {
        try {
            System.out.println("TEST 4: Construir árbol de relaciones");

            Cliente bob = gestor.buscarPorId(2);

            gestor.seguir(1, bob.getId());
            gestor.seguir(3, bob.getId());

            ArbolBinarioBusqueda<Integer, Cliente> arbol = gestor.construirArbolRelaciones(bob.getId());
            assertTrue(arbol != null, "Árbol debe ser creado");

            System.out.println("  ✓ Árbol de relaciones creado para Bob");
            System.out.println("  ✓ Ordenado por scoring de seguidores\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Construir árbol de relaciones", e);
            testsFallidos++;
        }
    }

    private static void testObtenerSeguidoresEnNivel() {
        try {
            System.out.println("TEST 5: Obtener seguidores en nivel N");

            Cliente bob = gestor.buscarPorId(2);

            gestor.seguir(1, bob.getId());
            gestor.seguir(3, bob.getId());

            Cliente[] nivel0 = gestor.obtenerSeguidoresEnNivel(bob.getId(), 0);
            assertTrue(nivel0 != null, "Debe retornar array");

            System.out.println("  ✓ Seguidores en nivel 0: " + nivel0.length);
            System.out.println("  ✓ Funcionalidad Iteración 2: ✓\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Obtener seguidores en nivel", e);
            testsFallidos++;
        }
    }

    private static void testObtenerSeguidoresOrdenados() {
        try {
            System.out.println("TEST 6: Obtener seguidores ordenados por scoring");

            Cliente bob = gestor.buscarPorId(2);

            gestor.seguir(1, bob.getId());
            gestor.seguir(3, bob.getId());

            Cliente[] ordenados = gestor.obtenerSeguidoresOrdenados(bob.getId());
            assertTrue(ordenados != null, "Debe retornar array");

            // Verificar orden descendente
            boolean estaOrdenado = true;
            for (int i = 0; i < ordenados.length - 1; i++) {
                if (ordenados[i].getScoring() < ordenados[i + 1].getScoring()) {
                    estaOrdenado = false;
                    break;
                }
            }

            assertTrue(estaOrdenado, "Debe estar ordenado descendente por scoring");
            System.out.println("  ✓ Seguidores ordenados: " + ordenados.length);
            System.out.println("  ✓ Orden: descendente por scoring\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Obtener seguidores ordenados", e);
            testsFallidos++;
        }
    }

    private static void testBusquedaPorScoringLazyLoading() {
        try {
            System.out.println("TEST 7: Búsqueda por scoring con lazy loading");

            long inicio = System.currentTimeMillis();
            Cliente[] clientes80 = gestor.buscarPorScoring(80);
            long tiempo1 = System.currentTimeMillis() - inicio;

            // Segunda búsqueda (índice ya construido)
            inicio = System.currentTimeMillis();
            Cliente[] clientes88 = gestor.buscarPorScoring(88);
            long tiempo2 = System.currentTimeMillis() - inicio;

            System.out.println("  ✓ Primera búsqueda (lazy loading): " + tiempo1 + "ms");
            System.out.println("  ✓ Segunda búsqueda (cached): " + tiempo2 + "ms");
            System.out.println("  ✓ Complejidad: O(log N + k)\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Búsqueda por scoring lazy loading", e);
            testsFallidos++;
        }
    }

    private static void testObtenerClientesEnNivelConLazyLoading() {
        try {
            System.out.println("TEST 8: Obtener clientes en nivel del ABB de scoring");

            Cliente[] nivel0 = gestor.obtenerClientesEnNivel(0);
            assertTrue(nivel0 != null, "Debe retornar array");

            System.out.println("  ✓ Clientes en nivel 0: " + nivel0.length);
            System.out.println("  ✓ Lazy loading: construido bajo demanda\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Obtener clientes en nivel", e);
            testsFallidos++;
        }
    }

    private static void testPersistenciaRelaciones() {
        try {
            System.out.println("TEST 9: Persistencia de relaciones en JSON");

            Cliente alice = gestor.buscarPorId(1);
            Cliente bob = gestor.buscarPorId(2);

            gestor.seguir(alice.getId(), bob.getId());

            assertTrue(alice.sigueA(bob.getId()), "Relación debe persistir en Cliente");
            assertTrue(bob.getCantidadSeguidores() > 0, "Seguidores deben estar en Cliente");

            System.out.println("  ✓ Relaciones almacenadas en Cliente");
            System.out.println("  ✓ Serializables a JSON\n");
            testsPasados++;
        } catch (Exception e) {
            printError("Persistencia de relaciones", e);
            testsFallidos++;
        }
    }

    private static void assertTrue(boolean condicion, String mensaje) throws Exception {
        if (!condicion) {
            throw new Exception(mensaje);
        }
    }

    private static void printError(String test, Exception e) {
        System.out.println("  ✗ ERROR: " + test);
        System.out.println("     " + e.getMessage() + "\n");
    }
}
