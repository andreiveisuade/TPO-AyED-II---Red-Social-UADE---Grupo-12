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
    private static int testsFallados = 0;

    public static void main(String[] args) {
        System.out.println("── DistanciaTest ────────────────────────────────");

        testMismoCliente();
        testConexionDirecta();
        testDosHops();
        testSinCamino();
        testClienteInexistente();
        testGrafoDesconectado();
        testDistanciaInversa();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void testMismoCliente() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);

            int distancia = gestor.calcularDistancia(a, a);
            afirmar(distancia == 0, "Distancia de A a A debe ser 0, era: " + distancia);

            reportarExito("Mismo cliente: distancia 0");
        } catch (Exception e) {
            reportarFallo("Mismo cliente", e.getMessage());
        }
    }

    private static void testConexionDirecta() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob",   70);
            gestor.seguir(a, b);

            int distancia = gestor.calcularDistancia(a, b);
            afirmar(distancia == 1, "Distancia A→B debe ser 1, era: " + distancia);

            reportarExito("Conexión directa: distancia 1");
        } catch (Exception e) {
            reportarFallo("Conexión directa", e.getMessage());
        }
    }

    private static void testDosHops() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice",   80);
            int b = gestor.agregarCliente("Bob",     70);
            int c = gestor.agregarCliente("Charlie", 60);
            gestor.seguir(a, b);
            gestor.seguir(b, c);

            int distancia = gestor.calcularDistancia(a, c);
            afirmar(distancia == 2, "Distancia A→B→C debe ser 2, era: " + distancia);

            reportarExito("Camino 2 saltos: distancia 2");
        } catch (Exception e) {
            reportarFallo("Camino 2 saltos", e.getMessage());
        }
    }

    private static void testSinCamino() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice",   80);
            int b = gestor.agregarCliente("Bob",     70);
            int c = gestor.agregarCliente("Charlie", 60);
            gestor.seguir(a, b);

            int distancia = gestor.calcularDistancia(a, c);
            afirmar(distancia == -1, "Distancia a C (aislado) debe ser -1, era: " + distancia);

            reportarExito("Sin camino: distancia -1");
        } catch (Exception e) {
            reportarFallo("Sin camino", e.getMessage());
        }
    }

    private static void testClienteInexistente() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);

            int distancia = gestor.calcularDistancia(a, 9999);
            afirmar(distancia == -1, "Distancia a ID inexistente debe ser -1, era: " + distancia);

            reportarExito("Cliente inexistente: distancia -1");
        } catch (Exception e) {
            reportarFallo("Cliente inexistente", e.getMessage());
        }
    }

    private static void testGrafoDesconectado() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice",   80);
            int b = gestor.agregarCliente("Bob",     70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("David",   50);
            gestor.seguir(a, b);
            gestor.seguir(c, d);

            int distancia = gestor.calcularDistancia(a, d);
            afirmar(distancia == -1, "Distancia entre componentes desconectadas debe ser -1, era: " + distancia);

            reportarExito("Grafo desconectado: distancia -1");
        } catch (Exception e) {
            reportarFallo("Grafo desconectado", e.getMessage());
        }
    }

    private static void testDistanciaInversa() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob",   70);
            gestor.seguir(a, b);

            int directa = gestor.calcularDistancia(a, b);
            int inversa  = gestor.calcularDistancia(b, a);

            afirmar(directa == 1, "Distancia directa A→B debe ser 1, era: " + directa);
            afirmar(inversa == -1, "Distancia inversa B→A debe ser -1 (dirigido), era: " + inversa);

            reportarExito("Relación dirigida: camino inverso -1");
        } catch (Exception e) {
            reportarFallo("Relación dirigida", e.getMessage());
        }
    }

    private static void afirmar(boolean condicion, String mensaje) throws Exception {
        if (!condicion) throw new Exception(mensaje);
    }

    private static void reportarExito(String nombre) {
        System.out.println("  [OK] " + nombre);
        testsPasados++;
    }

    private static void reportarFallo(String nombre, String mensaje) {
        System.out.println("  [FAIL] " + nombre + ": " + mensaje);
        testsFallados++;
    }
}
