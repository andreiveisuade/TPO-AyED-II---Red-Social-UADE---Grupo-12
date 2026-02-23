import servicio.GestorClientes;
import modelo.Cliente;
import modelo.Sesion;

/*
CU-009: Seguir a un Usuario

Descripción: Usuario autenticado sigue a otro usuario.
Flujo: Usuario A → Busca Usuario B → Envía solicitud → Acepta → Relación creada
Complejidad: O(1)
*/

public class CU_009_SeguidorUsuario {

    private static int testsPasados = 0;
    private static int testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU009_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}, " +
                    "{\"id\": 2, \"nombre\": \"Bob\", \"scoring\": 80, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}]}");
        } catch (java.io.IOException e) {
            System.err.println("Error creating test DB: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  CU-009: SEGUIR A UN USUARIO                ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        testSeguirUsuarioValido();
        testNoAutoSeguimiento();
        testNoSeguirDuplicado();
        testLimiteDe2Seguidos();
        testRelacionBidireccional();

        System.out.println("\n" + "─".repeat(50));
        System.out.printf("CU-009 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("─".repeat(50) + "\n");

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void testSeguirUsuarioValido() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion = Sesion.getInstancia();

            Cliente alice = gestor.buscarPorId(1);
            Cliente bob = gestor.buscarPorId(2);
            sesion.iniciarSesion(alice);

            // Ejecutar acción: Alice sigue a Bob
            boolean resultado = gestor.seguir(alice.getId(), bob.getId());
            assert resultado : "Debe poder seguir a usuario válido";
            assert alice.getCantidadSiguiendo() == 1 : "Alice debe tener 1 seguido";
            assert bob.getCantidadSeguidores() == 1 : "Bob debe tener 1 seguidor";

            reportarExito("Seguir usuario válido");
        } catch (AssertionError e) {
            reportarFallo("Seguir usuario válido", e.getMessage());
        }
    }

    private static void testNoAutoSeguimiento() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion = Sesion.getInstancia();

            Cliente alice = gestor.buscarPorId(1);
            sesion.iniciarSesion(alice);

            // No debe poder seguirse a sí mismo
            boolean resultado = gestor.seguir(1, 1);
            assert !resultado : "No debe poder auto-seguirse";
            assert alice.getCantidadSiguiendo() == 0 : "No debe agregar auto-seguimiento";

            reportarExito("No auto-seguimiento");
        } catch (AssertionError e) {
            reportarFallo("No auto-seguimiento", e.getMessage());
        }
    }

    private static void testNoSeguirDuplicado() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion = Sesion.getInstancia();

            Cliente alice = gestor.buscarPorId(1);
            Cliente bob = gestor.buscarPorId(2);
            sesion.iniciarSesion(alice);

            // Primera vez funciona
            boolean resultado1 = gestor.seguir(1, 2);
            assert resultado1 : "Primera vez debe funcionar";

            // Segunda vez no debe funcionar
            boolean resultado2 = gestor.seguir(1, 2);
            assert !resultado2 : "No debe seguir duplicado";
            assert alice.getCantidadSiguiendo() == 1 : "Sigue siendo 1 seguido";

            reportarExito("No seguir duplicado");
        } catch (AssertionError e) {
            reportarFallo("No seguir duplicado", e.getMessage());
        }
    }

    private static void testLimiteDe2Seguidos() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            // Necesitamos más clientes para este test
            Cliente alice = gestor.buscarPorId(1);
            int id3 = gestor.agregarCliente("Charlie", 70);
            int id4 = gestor.agregarCliente("Diana", 85);

            // Alice sigue a Bob
            boolean r1 = gestor.seguir(1, 2);
            assert r1 : "Seguimiento 1 debe funcionar";

            // Alice sigue a Charlie
            boolean r2 = gestor.seguir(1, id3);
            assert r2 : "Seguimiento 2 debe funcionar";

            // Alice intenta seguir a Diana (tercero, debe fallar)
            boolean r3 = gestor.seguir(1, id4);
            assert !r3 : "Tercera seguimiento debe fallar (límite 2)";
            assert alice.getCantidadSiguiendo() == 2 : "Debe tener exactamente 2 seguidos";

            reportarExito("Límite de 2 seguidos");
        } catch (AssertionError e) {
            reportarFallo("Límite de 2 seguidos", e.getMessage());
        }
    }

    private static void testRelacionBidireccional() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion = Sesion.getInstancia();

            Cliente alice = gestor.buscarPorId(1);
            Cliente bob = gestor.buscarPorId(2);
            sesion.iniciarSesion(alice);

            // Alice sigue a Bob
            gestor.seguir(1, 2);

            // Verificar relación bidireccional
            assert alice.getCantidadSiguiendo() == 1 : "Alice sigue a 1 persona";
            assert bob.getCantidadSeguidores() == 1 : "Bob tiene 1 seguidor";

            reportarExito("Relación bidireccional");
        } catch (AssertionError e) {
            reportarFallo("Relación bidireccional", e.getMessage());
        }
    }

    private static void reportarExito(String testName) {
        testsPasados++;
        System.out.println("  [OK] " + testName);
    }

    private static void reportarFallo(String testName, String error) {
        testsFallados++;
        System.err.println("  [FAIL] " + testName);
        System.err.println("         Error: " + error);
    }
}
