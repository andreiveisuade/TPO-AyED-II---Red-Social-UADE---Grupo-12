import servicio.GestorClientes;
import modelo.Cliente;
import modelo.Sesion;

/*
CU-001: Iniciar Sesión

Descripción: Usuario ingresa su ID y el sistema lo autentica.
Flujo: Usuario → ID válido → Sesión activa → Bienvenida
Complejidad: O(1)
*/

public class CU_001_IniciarSesion {

    private static int testsPasados = 0;
    private static int testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU001_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}]}");
        } catch (java.io.IOException e) {
            System.err.println("Error creating test DB: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  CU-001: INICIAR SESIÓN                    ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        testIniciarSesionConIDValido();
        testIniciarSesionConIDInvalido();
        testSesionSingleton();
        testObtenerDatosUsuarioAutenticado();

        System.out.println("\n" + "─".repeat(50));
        System.out.printf("CU-001 RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("─".repeat(50) + "\n");

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void testIniciarSesionConIDValido() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion = Sesion.getInstancia();

            // Usuario busca cliente por ID
            Cliente alice = gestor.buscarPorId(1);
            assert alice != null : "Alice debe existir";

            // Usuario inicia sesión
            sesion.iniciarSesion(alice);
            assert sesion.estaAutenticado() : "Sesión debe estar activa";
            assert sesion.getUsuarioActual().getId() == 1 : "Usuario debe ser Alice (ID 1)";
            assert sesion.getNombreUsuarioActual().equals("Alice") : "Nombre debe ser Alice";

            reportarExito("Iniciar sesión con ID válido");
        } catch (AssertionError e) {
            reportarFallo("Iniciar sesión con ID válido", e.getMessage());
        }
    }

    private static void testIniciarSesionConIDInvalido() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion = Sesion.getInstancia();
            sesion.cerrarSesion();

            // Usuario intenta ID inválido
            Cliente noExiste = gestor.buscarPorId(9999);
            assert noExiste == null : "ID 9999 no debe existir";

            reportarExito("Iniciar sesión con ID inválido");
        } catch (AssertionError e) {
            reportarFallo("Iniciar sesión con ID inválido", e.getMessage());
        }
    }

    private static void testSesionSingleton() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion1 = Sesion.getInstancia();
            Sesion sesion2 = Sesion.getInstancia();

            assert sesion1 == sesion2 : "Singleton debe retornar la misma instancia";

            Cliente alice = gestor.buscarPorId(1);
            sesion1.iniciarSesion(alice);

            assert sesion2.estaAutenticado() : "Ambas referencias ven el mismo estado";
            assert sesion2.getNombreUsuarioActual().equals("Alice") : "Estado compartido";

            reportarExito("Sesión es Singleton");
        } catch (AssertionError e) {
            reportarFallo("Sesión es Singleton", e.getMessage());
        }
    }

    private static void testObtenerDatosUsuarioAutenticado() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion = Sesion.getInstancia();
            sesion.cerrarSesion();

            Cliente alice = gestor.buscarPorId(1);
            sesion.iniciarSesion(alice);

            // Obtener datos del usuario autenticado
            assert sesion.getIdUsuarioActual() == 1 : "ID debe ser 1";
            assert sesion.getNombreUsuarioActual().equals("Alice") : "Nombre debe ser Alice";
            assert sesion.getUsuarioActual().getScoring() == 95 : "Scoring debe ser 95";

            reportarExito("Obtener datos usuario autenticado");
        } catch (AssertionError e) {
            reportarFallo("Obtener datos usuario autenticado", e.getMessage());
        }
    }

    private static void reportarExito(String testName) {
        testsPasados++;
        System.out.println("  [✓] " + testName);
    }

    private static void reportarFallo(String testName, String error) {
        testsFallados++;
        System.err.println("  [✗] " + testName);
        System.err.println("      Error: " + error);
    }
}
