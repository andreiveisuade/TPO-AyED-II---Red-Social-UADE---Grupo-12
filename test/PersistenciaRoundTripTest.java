import servicio.GestorClientes;
import modelo.Cliente;

/**
 * Test de round-trip de persistencia.
 *
 * Verifica que los datos sobreviven al ciclo completo:
 *   crear → guardar (JSON) → recargar → verificar
 *
 * Detecta problemas como:
 * - Campos que se serializan pero no se deserializan (o viceversa)
 * - Pérdida de bidireccionalidad en amistades al recargar
 * - Corrupción de relaciones de seguimiento
 * - Pérdida de solicitudes pendientes
 *
 * Ejecución: java -ea -Dtest.quiet=true -cp out:lib/gson-2.10.1.jar PersistenciaRoundTripTest
 */
public class PersistenciaRoundTripTest {

    private static int testsPasados = 0;
    private static int testsFallados = 0;
    private static final String TEST_DB = "data/clientes_ROUNDTRIP_TEST.json";

    public static void main(String[] args) {
        System.out.println("── PersistenciaRoundTripTest ─────────────────────");

        testRoundTripAmistades();
        testRoundTripAmistadesMultiples();
        testRoundTripAmistadesYSeguimientos();
        testRoundTripClienteSinRelaciones();
        testRoundTripBidireccionalidadAmistades();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        // Limpiar archivo de test
        new java.io.File(TEST_DB).delete();

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    /**
     * Round-trip básico: crear 2 clientes, hacerlos amigos, guardar, recargar, verificar.
     */
    private static void testRoundTripAmistades() {
        try {
            // 1. CREAR: Armar datos en memoria
            initEmptyDB();
            GestorClientes gestor1 = new GestorClientes(TEST_DB);
            int alice = gestor1.agregarCliente("Alice", 80);
            int bob = gestor1.agregarCliente("Bob", 70);
            gestor1.agregarAmistad(alice, bob);

            // 2. GUARDAR: Persistir a JSON
            gestor1.guardarCambios();

            // 3. RECARGAR: Nueva instancia que lee del mismo archivo
            GestorClientes gestor2 = new GestorClientes(TEST_DB);

            // 4. VERIFICAR: Los datos sobrevivieron
            afirmar(gestor2.sonAmigos(alice, bob),
                "Amistad Alice→Bob debe persistir tras round-trip");
            afirmar(gestor2.sonAmigos(bob, alice),
                "Amistad Bob→Alice debe persistir (bidireccional)");
            afirmar(gestor2.obtenerCantidadAmigos(alice) == 1,
                "Alice debe tener 1 amigo tras recargar, tiene: " + gestor2.obtenerCantidadAmigos(alice));
            afirmar(gestor2.obtenerCantidadAmigos(bob) == 1,
                "Bob debe tener 1 amigo tras recargar, tiene: " + gestor2.obtenerCantidadAmigos(bob));

            reportarExito("Round-trip amistades basico");
        } catch (Exception e) {
            reportarFallo("Round-trip amistades basico", e.getMessage());
        }
    }

    /**
     * Round-trip con múltiples amistades: A-B, A-C. Verifica que todas persisten.
     */
    private static void testRoundTripAmistadesMultiples() {
        try {
            initEmptyDB();
            GestorClientes gestor1 = new GestorClientes(TEST_DB);
            int a = gestor1.agregarCliente("Alice", 80);
            int b = gestor1.agregarCliente("Bob", 70);
            int c = gestor1.agregarCliente("Charlie", 60);

            gestor1.agregarAmistad(a, b);
            gestor1.agregarAmistad(a, c);
            gestor1.guardarCambios();

            GestorClientes gestor2 = new GestorClientes(TEST_DB);

            afirmar(gestor2.sonAmigos(a, b), "A-B debe persistir");
            afirmar(gestor2.sonAmigos(a, c), "A-C debe persistir");
            afirmar(!gestor2.sonAmigos(b, c), "B-C no deben ser amigos");
            afirmar(gestor2.obtenerCantidadAmigos(a) == 2,
                "Alice debe tener 2 amigos, tiene: " + gestor2.obtenerCantidadAmigos(a));

            reportarExito("Round-trip amistades multiples");
        } catch (Exception e) {
            reportarFallo("Round-trip amistades multiples", e.getMessage());
        }
    }

    /**
     * Round-trip mixto: amistades + seguimientos coexisten sin interferirse.
     */
    private static void testRoundTripAmistadesYSeguimientos() {
        try {
            initEmptyDB();
            GestorClientes gestor1 = new GestorClientes(TEST_DB);
            int a = gestor1.agregarCliente("Alice", 80);
            int b = gestor1.agregarCliente("Bob", 70);
            int c = gestor1.agregarCliente("Charlie", 60);

            // Alice y Bob son amigos
            gestor1.agregarAmistad(a, b);
            // Alice sigue a Charlie (pero no son amigos)
            gestor1.seguir(a, c);

            gestor1.guardarCambios();

            GestorClientes gestor2 = new GestorClientes(TEST_DB);

            // Verificar amistades
            afirmar(gestor2.sonAmigos(a, b), "Amistad A-B debe persistir");
            afirmar(!gestor2.sonAmigos(a, c), "A y C no deben ser amigos");

            // Verificar seguimientos
            Cliente aliceRecargada = gestor2.buscarPorId(a);
            afirmar(aliceRecargada != null, "Alice debe existir");
            afirmar(aliceRecargada.sigueA(c), "Alice debe seguir a Charlie");
            afirmar(!aliceRecargada.sigueA(b), "seguir y amistad son independientes");

            reportarExito("Round-trip amistades + seguimientos");
        } catch (Exception e) {
            reportarFallo("Round-trip amistades + seguimientos", e.getMessage());
        }
    }

    /**
     * Round-trip de cliente sin relaciones: no debe inventar amistades fantasma.
     */
    private static void testRoundTripClienteSinRelaciones() {
        try {
            initEmptyDB();
            GestorClientes gestor1 = new GestorClientes(TEST_DB);
            int a = gestor1.agregarCliente("Alice", 80);

            gestor1.guardarCambios();

            GestorClientes gestor2 = new GestorClientes(TEST_DB);

            afirmar(gestor2.obtenerCantidadAmigos(a) == 0,
                "Cliente sin amigos debe tener 0 tras round-trip");
            Cliente alice = gestor2.buscarPorId(a);
            afirmar(alice != null, "Alice debe existir");
            afirmar(alice.getCantidadSiguiendo() == 0, "No debe tener seguidos fantasma");

            reportarExito("Round-trip cliente sin relaciones");
        } catch (Exception e) {
            reportarFallo("Round-trip cliente sin relaciones", e.getMessage());
        }
    }

    /**
     * Round-trip de consistencia bidireccional:
     * verifica que al recargar, si A tiene a B como amigo,
     * entonces B también tiene a A como amigo.
     *
     * Este test es clave porque el JSON guarda las amistades
     * de cada cliente por separado. Si hay un bug en guardarCambios()
     * o cargarDesdeArchivo(), podría romper la bidireccionalidad.
     */
    private static void testRoundTripBidireccionalidadAmistades() {
        try {
            initEmptyDB();
            GestorClientes gestor1 = new GestorClientes(TEST_DB);
            int a = gestor1.agregarCliente("Alice", 80);
            int b = gestor1.agregarCliente("Bob", 70);
            int c = gestor1.agregarCliente("Charlie", 60);

            gestor1.agregarAmistad(a, b);
            gestor1.agregarAmistad(b, c);
            gestor1.guardarCambios();

            GestorClientes gestor2 = new GestorClientes(TEST_DB);

            // Verificar desde ambos lados
            afirmar(gestor2.sonAmigos(a, b) && gestor2.sonAmigos(b, a),
                "A↔B bidireccional tras round-trip");
            afirmar(gestor2.sonAmigos(b, c) && gestor2.sonAmigos(c, b),
                "B↔C bidireccional tras round-trip");
            afirmar(!gestor2.sonAmigos(a, c),
                "A y C no deben ser amigos");

            // Verificar conteos consistentes
            afirmar(gestor2.obtenerCantidadAmigos(a) == 1, "A tiene 1 amigo");
            afirmar(gestor2.obtenerCantidadAmigos(b) == 2, "B tiene 2 amigos");
            afirmar(gestor2.obtenerCantidadAmigos(c) == 1, "C tiene 1 amigo");

            reportarExito("Round-trip bidireccionalidad amistades");
        } catch (Exception e) {
            reportarFallo("Round-trip bidireccionalidad amistades", e.getMessage());
        }
    }

    // ─── Helpers ──────────────────────────────────────────────

    private static void initEmptyDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {
            throw new RuntimeException("No se pudo crear DB de test", e);
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
