import servicio.GestorClientes;
import modelo.Cliente;
import modelo.Accion;
import modelo.TipoAccion;
import modelo.Sesion;

/**
 * Tests unitarios para GestorClientes.
 *
 * Usa assertions nativas (ejecutar con java -ea GestorClientesTest).
 */
public class GestorClientesTest {

    private static int testsPasados = 0;
    private static int testsFallados = 0;

    private static final String TEST_DB = "data/clientes_GESTOR_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {
            System.err.println("Error creating test DB: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("   TESTS DE GESTOR CLIENTES               ");
        System.out.println("═══════════════════════════════════════════\n");

        testAgregarClienteValido();
        testAgregarClienteDuplicado();
        testAgregarClienteNombreVacio();
        testAgregarClienteScoringInvalido();
        testBuscarPorNombre();
        testBuscarInexistente();
        testSeguir();
        testSeguirClienteInexistente();
        testSeguirAutoLazo();
        testSeguirLimiteExcedido();
        testDeshacerAgregarCliente();
        testDeshacerSeguir();
        testUndoEliminarClienteConCascada();
        testDeshacerHistorialVacio();
        testVerUltimaAccion();
        testBuscarPorNombreCaseInsensitive();
        testBuscarPorNombreDuplicados();
        testBuscarPorNombrePostEliminacion();

        System.out.println("\n═══════════════════════════════════════════");
        System.out.printf("RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("═══════════════════════════════════════════");

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void testAgregarClienteValido() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id = gestor.agregarCliente("Alice", 95);
            assert id != -1 : "Agregar Alice debe retornar un ID válido";
            assert gestor.getCantidadClientes() == 1 : "Cantidad debe ser 1";

            reportarExito("Agregar cliente válido");
        } catch (AssertionError e) {
            reportarFallo("Agregar cliente válido", e.getMessage());
        }
    }

    private static void testAgregarClienteDuplicado() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 95);
            assert idAlice != -1 : "Primer agregar debe funcionar";
            // Nota: GestorClientes actual permite nombres duplicados (diferentes IDs)
            // Esto es un cambio respecto al diseño original: cada agregar genera nuevo ID
            int idAlice2 = gestor.agregarCliente("Alice", 80);
            assert idAlice2 != -1 : "Segundo agregar con mismo nombre genera nuevo ID";
            assert idAlice != idAlice2 : "IDs deben ser distintos";

            reportarExito("Agregar cliente duplicado (IDs distintos)");
        } catch (AssertionError e) {
            reportarFallo("Agregar cliente duplicado", e.getMessage());
        }
    }

    private static void testAgregarClienteNombreVacio() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            assert gestor.agregarCliente("", 50) == -1 : "Nombre vacío debe retornar -1";
            assert gestor.agregarCliente(null, 50) == -1 : "Nombre null debe retornar -1";
            assert gestor.getCantidadClientes() == 0 : "Cantidad debe ser 0";

            reportarExito("Agregar cliente con nombre vacío/null");
        } catch (AssertionError e) {
            reportarFallo("Agregar cliente con nombre vacío/null", e.getMessage());
        }
    }

    private static void testAgregarClienteScoringInvalido() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            assert gestor.agregarCliente("Test", 150) == -1 : "Scoring 150 debe retornar -1";
            assert gestor.agregarCliente("Test", -1) == -1 : "Scoring -1 debe retornar -1";
            assert gestor.getCantidadClientes() == 0 : "Cantidad debe ser 0";

            reportarExito("Agregar cliente con scoring inválido");
        } catch (AssertionError e) {
            reportarFallo("Agregar cliente con scoring inválido", e.getMessage());
        }
    }

    private static void testBuscarPorNombre() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 95);
            Cliente[] encontrados = gestor.buscarPorNombre("Alice");
            assert encontrados != null : "Resultado no debe ser null";
            assert encontrados.length > 0 : "Debe encontrar al menos 1";

            boolean found = false;
            for (Cliente c : encontrados) {
                if (c.getId() == idAlice && "Alice".equals(c.getNombre())) {
                    found = true;
                    break;
                }
            }
            assert found : "Debe encontrar a Alice con el ID correcto";

            reportarExito("Buscar por nombre");
        } catch (AssertionError e) {
            reportarFallo("Buscar por nombre", e.getMessage());
        }
    }

    private static void testBuscarInexistente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Cliente[] resultado = gestor.buscarPorNombre("ZZZ");
            assert resultado != null : "Resultado no debe ser null";
            assert resultado.length == 0 : "No debe encontrar nada";

            reportarExito("Buscar inexistente");
        } catch (AssertionError e) {
            reportarFallo("Buscar inexistente", e.getMessage());
        }
    }

    private static void testSeguir() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 95);
            int idBob = gestor.agregarCliente("Bob", 88);
            assert gestor.seguir(idAlice, idBob) : "Alice debe poder seguir a Bob";

            Cliente alice = gestor.buscarPorId(idAlice);
            assert alice.sigueA(idBob) : "Alice debe seguir a Bob";

            // Verificar que Bob tiene a Alice como seguidor
            Cliente bob = gestor.buscarPorId(idBob);
            assert bob.getCantidadSeguidores() == 1 : "Bob debe tener 1 seguidor";

            reportarExito("Seguir");
        } catch (AssertionError e) {
            reportarFallo("Seguir", e.getMessage());
        }
    }

    private static void testSeguirClienteInexistente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 95);
            assert !gestor.seguir(idAlice, 999999) : "No debe poder seguir ID inexistente";
            assert !gestor.seguir(999999, idAlice) : "ID inexistente no debe poder seguir";

            reportarExito("Seguir cliente inexistente");
        } catch (AssertionError e) {
            reportarFallo("Seguir cliente inexistente", e.getMessage());
        }
    }

    private static void testSeguirAutoLazo() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 95);
            assert !gestor.seguir(idAlice, idAlice) : "No debe poder seguirse a sí mismo";

            reportarExito("Seguir auto-lazo");
        } catch (AssertionError e) {
            reportarFallo("Seguir auto-lazo", e.getMessage());
        }
    }

    private static void testSeguirLimiteExcedido() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 90);
            int idB1 = gestor.agregarCliente("B1", 10);
            int idB2 = gestor.agregarCliente("B2", 10);
            int idB3 = gestor.agregarCliente("B3", 10);

            assert gestor.seguir(idAlice, idB1) : "Primer seguido OK";
            assert gestor.seguir(idAlice, idB2) : "Segundo seguido OK";
            assert !gestor.seguir(idAlice, idB3) : "Tercer seguido debe fallar (MAX_SEGUIDOS=2)";

            reportarExito("Seguir límite excedido");
        } catch (AssertionError e) {
            reportarFallo("Seguir límite excedido", e.getMessage());
        }
    }

    private static void testDeshacerAgregarCliente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion.getInstancia().iniciarSesion(new Cliente(999, "Admin", 100));
            gestor.activarHistorial();

            int id = gestor.agregarCliente("Alice", 95);
            assert gestor.getCantidadClientes() == 1 : "Debe haber 1 cliente";

            Accion accion = gestor.deshacer();
            assert accion != null : "Deshacer debe retornar una acción";
            assert accion.getTipo() == TipoAccion.AGREGAR_CLIENTE : "Tipo debe ser AGREGAR_CLIENTE";
            assert gestor.buscarPorNombre("Alice").length == 0 : "Alice no debe existir tras undo";

            reportarExito("Deshacer agregar cliente");
        } catch (AssertionError e) {
            reportarFallo("Deshacer agregar cliente", e.getMessage());
        }
    }

    private static void testDeshacerSeguir() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 95);
            int idBob = gestor.agregarCliente("Bob", 88);

            Cliente alice = gestor.buscarPorId(idAlice);
            Sesion.getInstancia().iniciarSesion(alice);
            gestor.activarHistorial();

            gestor.seguir(idAlice, idBob);
            assert alice.sigueA(idBob) : "Alice debe seguir a Bob";

            gestor.deshacer();
            assert !alice.sigueA(idBob) : "Undo debe revertir el seguimiento";

            reportarExito("Deshacer seguir");
        } catch (AssertionError e) {
            reportarFallo("Deshacer seguir", e.getMessage());
        }
    }

    private static void testUndoEliminarClienteConCascada() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 90);
            int idBob = gestor.agregarCliente("Bob", 80);

            Cliente alice = gestor.buscarPorId(idAlice);
            Sesion.getInstancia().iniciarSesion(alice);
            gestor.activarHistorial();

            gestor.seguir(idAlice, idBob);
            assert alice.sigueA(idBob) : "Alice debe seguir a Bob";

            // Eliminar Bob -> Alice debe dejar de seguirlo
            gestor.eliminarCliente(idBob);
            assert !gestor.existeCliente(idBob) : "Bob eliminado";
            assert !alice.sigueA(idBob) : "Alice ya no sigue a Bob";

            // Undo -> Debe restaurar Bob y la relación
            gestor.deshacer();
            assert gestor.existeCliente(idBob) : "Bob debe volver a existir";

            reportarExito("Undo eliminar con cascada");
        } catch (AssertionError e) {
            reportarFallo("Undo eliminar con cascada", e.getMessage());
        }
    }

    private static void testDeshacerHistorialVacio() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion.getInstancia().iniciarSesion(new Cliente(999, "Admin", 100));

            Accion accion = gestor.deshacer();
            assert accion == null : "Deshacer con historial vacío debe retornar null";

            reportarExito("Deshacer historial vacío");
        } catch (AssertionError e) {
            reportarFallo("Deshacer historial vacío", e.getMessage());
        }
    }

    private static void testVerUltimaAccion() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion.getInstancia().iniciarSesion(new Cliente(999, "Admin", 100));
            gestor.activarHistorial();

            gestor.agregarCliente("Alice", 95);
            Accion accion = gestor.verUltimaAccion();
            assert accion != null : "Debe retornar última acción";
            assert accion.getTipo() == TipoAccion.AGREGAR_CLIENTE : "Tipo debe ser AGREGAR_CLIENTE";

            reportarExito("Ver última acción");
        } catch (AssertionError e) {
            reportarFallo("Ver última acción", e.getMessage());
        }
    }

    private static void testBuscarPorNombreCaseInsensitive() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            gestor.agregarCliente("Alice", 95);

            // Buscar con distintas variantes de mayúsculas/minúsculas
            assert gestor.buscarPorNombre("alice").length == 1 : "Debe encontrar con minúsculas";
            assert gestor.buscarPorNombre("ALICE").length == 1 : "Debe encontrar con mayúsculas";
            assert gestor.buscarPorNombre("Alice").length == 1 : "Debe encontrar con caso original";
            assert gestor.buscarPorNombre("aLiCe").length == 1 : "Debe encontrar con caso mixto";

            reportarExito("Buscar por nombre case-insensitive (índice hash)");
        } catch (AssertionError e) {
            reportarFallo("Buscar por nombre case-insensitive", e.getMessage());
        }
    }

    private static void testBuscarPorNombreDuplicados() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id1 = gestor.agregarCliente("Alice", 95);
            int id2 = gestor.agregarCliente("Alice", 80);

            Cliente[] encontrados = gestor.buscarPorNombre("Alice");
            assert encontrados.length == 2 : "Debe encontrar 2 Alices, encontró: " + encontrados.length;

            // Verificar que ambos IDs están presentes
            boolean found1 = false, found2 = false;
            for (Cliente c : encontrados) {
                if (c.getId() == id1) found1 = true;
                if (c.getId() == id2) found2 = true;
            }
            assert found1 && found2 : "Debe contener ambas Alices";

            reportarExito("Buscar por nombre con duplicados (índice hash)");
        } catch (AssertionError e) {
            reportarFallo("Buscar por nombre con duplicados", e.getMessage());
        }
    }

    private static void testBuscarPorNombrePostEliminacion() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            Sesion.getInstancia().iniciarSesion(new Cliente(999, "Admin", 100));
            gestor.activarHistorial();

            int id1 = gestor.agregarCliente("Alice", 95);
            int id2 = gestor.agregarCliente("Alice", 80);

            assert gestor.buscarPorNombre("Alice").length == 2 : "Debe haber 2 Alices";

            gestor.eliminarCliente(id1);
            Cliente[] postElim = gestor.buscarPorNombre("Alice");
            assert postElim.length == 1 : "Debe quedar 1 Alice, quedaron: " + postElim.length;
            assert postElim[0].getId() == id2 : "La Alice restante debe ser id2";

            reportarExito("Buscar por nombre post-eliminación (índice hash)");
        } catch (AssertionError e) {
            reportarFallo("Buscar por nombre post-eliminación", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // UTILIDADES DE REPORTE
    // ═══════════════════════════════════════════════════════════════════

    private static void reportarExito(String nombre) {
        System.out.printf("✅ %s%n", nombre);
        testsPasados++;
    }

    private static void reportarFallo(String nombre, String mensaje) {
        System.out.printf("❌ %s: %s%n", nombre, mensaje);
        testsFallados++;
    }
}
