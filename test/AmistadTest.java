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
    private static int testsFallados = 0;

    public static void main(String[] args) {
        System.out.println("── AmistadTest ──────────────────────────────────");

        testAgregarAmistad();
        testAmistadesEsBidireccional();
        testEliminarAmistad();
        testEliminarAmistadesEsBidireccional();
        testObtenerAmigos();
        testVerificarAmistad();
        testAmistadConsigoMismo();
        testLimiteMaxAmigos();
        testLimiteMaxAmigosDelOtroLado();
        testLimiteNoModificaNadaSiFalla();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void testAgregarAmistad() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);

            boolean resultado = gestor.agregarAmistad(a, b);
            afirmar(resultado, "agregarAmistad debería retornar true");
            afirmar(gestor.sonAmigos(a, b), "Alice y Bob deberían ser amigos");

            reportarExito("Agregar amistad");
        } catch (Exception e) {
            reportarFallo("Agregar amistad", e.getMessage());
        }
    }

    private static void testAmistadesEsBidireccional() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);

            gestor.agregarAmistad(a, b);

            afirmar(gestor.sonAmigos(a, b), "A debería ser amigo de B");
            afirmar(gestor.sonAmigos(b, a), "B debería ser amigo de A (bidireccional)");

            reportarExito("Amistad es bidireccional");
        } catch (Exception e) {
            reportarFallo("Amistad es bidireccional", e.getMessage());
        }
    }

    private static void testEliminarAmistad() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);

            gestor.agregarAmistad(a, b);
            boolean resultado = gestor.eliminarAmistad(a, b);

            afirmar(resultado, "eliminarAmistad debería retornar true");
            afirmar(!gestor.sonAmigos(a, b), "Alice y Bob no deberían ser amigos");

            reportarExito("Eliminar amistad");
        } catch (Exception e) {
            reportarFallo("Eliminar amistad", e.getMessage());
        }
    }

    private static void testEliminarAmistadesEsBidireccional() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);

            gestor.agregarAmistad(a, b);
            gestor.eliminarAmistad(a, b);

            afirmar(!gestor.sonAmigos(a, b), "A no debería ser amigo de B");
            afirmar(!gestor.sonAmigos(b, a), "B no debería ser amigo de A");

            reportarExito("Eliminar amistad es bidireccional");
        } catch (Exception e) {
            reportarFallo("Eliminar amistad bidireccional", e.getMessage());
        }
    }

    private static void testObtenerAmigos() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);

            gestor.agregarAmistad(a, b);
            gestor.agregarAmistad(a, c);

            Cliente[] amigos = gestor.obtenerAmigos(a);
            afirmar(amigos.length == 2, "Alice debería tener 2 amigos, tiene: " + amigos.length);

            reportarExito("Obtener amigos");
        } catch (Exception e) {
            reportarFallo("Obtener amigos", e.getMessage());
        }
    }

    private static void testVerificarAmistad() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);

            gestor.agregarAmistad(a, b);

            afirmar(gestor.sonAmigos(a, b), "sonAmigos(A, B) debería ser true");
            afirmar(!gestor.sonAmigos(a, c), "sonAmigos(A, C) debería ser false");

            reportarExito("Verificar amistad O(1)");
        } catch (Exception e) {
            reportarFallo("Verificar amistad", e.getMessage());
        }
    }

    private static void testAmistadConsigoMismo() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);

            boolean resultado = gestor.agregarAmistad(a, a);
            afirmar(!resultado, "agregarAmistad(A, A) debería retornar false");
            afirmar(!gestor.sonAmigos(a, a), "Un cliente no debería ser amigo de sí mismo");

            reportarExito("No amistad consigo mismo");
        } catch (Exception e) {
            reportarFallo("No amistad consigo mismo", e.getMessage());
        }
    }

    private static void testLimiteMaxAmigos() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);

            afirmar(gestor.agregarAmistad(a, b), "Primera amistad deberia funcionar");
            afirmar(gestor.agregarAmistad(a, c), "Segunda amistad deberia funcionar");
            afirmar(!gestor.agregarAmistad(a, d), "Tercera amistad deberia fallar (limite " + Cliente.MAX_AMIGOS + ")");
            afirmar(!gestor.sonAmigos(a, d), "A y D no deberian ser amigos");

            reportarExito("Limite MAX_AMIGOS=" + Cliente.MAX_AMIGOS);
        } catch (Exception e) {
            reportarFallo("Limite MAX_AMIGOS", e.getMessage());
        }
    }

    private static void testLimiteMaxAmigosDelOtroLado() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);

            // B ya tiene 2 amigos
            gestor.agregarAmistad(b, c);
            gestor.agregarAmistad(b, d);

            // A intenta ser amigo de B, pero B ya esta lleno
            afirmar(!gestor.agregarAmistad(a, b), "Deberia fallar porque B ya tiene " + Cliente.MAX_AMIGOS + " amigos");
            afirmar(!gestor.sonAmigos(a, b), "A y B no deberian ser amigos");

            reportarExito("Limite MAX_AMIGOS del otro lado");
        } catch (Exception e) {
            reportarFallo("Limite MAX_AMIGOS del otro lado", e.getMessage());
        }
    }

    private static void testLimiteNoModificaNadaSiFalla() {
        try {
            GestorClientes gestor = new GestorClientes("data/clientes_JSON_TEST.json");
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);

            gestor.agregarAmistad(b, c);
            gestor.agregarAmistad(b, d);

            int amigosAAntes = gestor.obtenerCantidadAmigos(a);
            int amigosBAntes = gestor.obtenerCantidadAmigos(b);

            gestor.agregarAmistad(a, b); // deberia fallar

            afirmar(gestor.obtenerCantidadAmigos(a) == amigosAAntes, "A no deberia haber cambiado");
            afirmar(gestor.obtenerCantidadAmigos(b) == amigosBAntes, "B no deberia haber cambiado");

            reportarExito("Fallo no modifica estado de ninguno");
        } catch (Exception e) {
            reportarFallo("Fallo no modifica estado", e.getMessage());
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
