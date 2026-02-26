import servicio.GestorClientes;
import modelo.Cliente;
import modelo.Sesion;

/*
CU-002: Cerrar Sesión

Descripción: Usuario cierra sesión y sistema guarda cambios.
Flujo: Usuario activo → Guarda JSON → Limpia sesión → Vuelve a login
Complejidad: O(N)
*/

public class CU_002_CerrarSesion {

    private static int testsPasados = 0;
    private static int testsFallados = 0;
    private static final String TEST_DB = "data/clientes_CU002_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [{\"id\": 1, \"nombre\": \"Alice\", \"scoring\": 95, \"siguiendo\": [2], \"solicitudes\": [], \"seguidores\": [], \"amistades\": []}]}");
        } catch (java.io.IOException e) {
            System.err.println("Error creating test DB: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("\n  CU-002: CERRAR SESIÓN");

        testCerrarSesionLimpia();
        testGuardarCambiosAlCerrar();
        testVolverAPantallaLogin();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void testCerrarSesionLimpia() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion = Sesion.getInstancia();

            Cliente alice = gestor.buscarPorId(1);
            sesion.iniciarSesion(alice);
            assert sesion.estaAutenticado() : "Debe estar autenticado";

            // Cerrar sesión
            sesion.cerrarSesion();
            assert !sesion.estaAutenticado() : "Sesión debe estar limpia";

            reportarExito("Cerrar sesión limpia estado");
        } catch (AssertionError e) {
            reportarFallo("Cerrar sesión limpia estado", e.getMessage());
        }
    }

    private static void testGuardarCambiosAlCerrar() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion = Sesion.getInstancia();

            Cliente alice = gestor.buscarPorId(1);
            sesion.iniciarSesion(alice);

            // Guardar cambios antes de cerrar (como hace la aplicación)
            gestor.guardarCambios();

            // Verificar que el archivo se guardó
            java.io.File file = new java.io.File(TEST_DB);
            assert file.exists() : "Archivo JSON debe existir después de guardar";
            assert file.length() > 0 : "Archivo JSON debe tener contenido";

            reportarExito("Guardar cambios al cerrar");
        } catch (AssertionError e) {
            reportarFallo("Guardar cambios al cerrar", e.getMessage());
        }
    }

    private static void testVolverAPantallaLogin() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion sesion = Sesion.getInstancia();

            Cliente alice = gestor.buscarPorId(1);
            sesion.iniciarSesion(alice);
            sesion.cerrarSesion();

            // Después de cerrar, se puede iniciar una nueva sesión
            Cliente bob = gestor.buscarPorId(1);
            assert bob != null : "Debe poder buscar usuario después de cerrar sesión";

            sesion.iniciarSesion(bob);
            assert sesion.estaAutenticado() : "Debe poder iniciar nueva sesión";

            reportarExito("Volver a pantalla login");
        } catch (AssertionError e) {
            reportarFallo("Volver a pantalla login", e.getMessage());
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
