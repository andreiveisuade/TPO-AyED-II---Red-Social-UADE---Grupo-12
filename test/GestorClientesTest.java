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
        System.out.println("── GestorClientesTest ───────────────────────────");

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
        testDeshacerSeguirConTipo();
        testDeshacerSeguir();
        testUndoDejarDeSeguir();
        testDeshacerHistorialVacio();
        testVerUltimaAccion();
        testBuscarPorNombreCaseInsensitive();
        testBuscarPorNombreDuplicados();
        testBuscarPorNombrePostEliminacion();
        testBuscarPorScoring();
        testBuscarPorScoringDuplicados();
        testBuscarPorScoringInexistente();
        testObtenerClientesEnNivel();
        testSeguirActualizaSeguidores();
        testUndoSeguirRevierteSeguidores();
        testBuscarPorScoringMuchosDuplicados();
        testAgregarClientePostBuildScoring();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

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

    private static void testDeshacerSeguirConTipo() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 95);
            int idBob = gestor.agregarCliente("Bob", 88);

            Cliente alice = gestor.buscarPorId(idAlice);
            Sesion.getInstancia().iniciarSesion(alice);

            gestor.seguir(idAlice, idBob);
            assert alice.sigueA(idBob) : "Alice debe seguir a Bob";

            Accion accion = gestor.deshacer();
            assert accion != null : "Deshacer debe retornar una acción";
            assert accion.getTipo() == TipoAccion.SEGUIR : "Tipo debe ser SEGUIR";
            assert !alice.sigueA(idBob) : "Alice no debe seguir a Bob tras undo";

            reportarExito("Deshacer seguir (verifica tipo SEGUIR)");
        } catch (AssertionError e) {
            reportarFallo("Deshacer seguir (verifica tipo SEGUIR)", e.getMessage());
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

            gestor.seguir(idAlice, idBob);
            assert alice.sigueA(idBob) : "Alice debe seguir a Bob";

            gestor.deshacer();
            assert !alice.sigueA(idBob) : "Undo debe revertir el seguimiento";

            reportarExito("Deshacer seguir");
        } catch (AssertionError e) {
            reportarFallo("Deshacer seguir", e.getMessage());
        }
    }

    private static void testUndoDejarDeSeguir() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 90);
            int idBob = gestor.agregarCliente("Bob", 80);

            Cliente alice = gestor.buscarPorId(idAlice);
            Cliente bob = gestor.buscarPorId(idBob);
            Sesion.getInstancia().iniciarSesion(alice);

            gestor.seguir(idAlice, idBob);
            assert alice.sigueA(idBob) : "Alice debe seguir a Bob";

            gestor.dejarDeSeguir(idAlice, idBob);
            assert !alice.sigueA(idBob) : "Alice ya no sigue a Bob";
            assert bob.getCantidadSeguidores() == 0 : "Bob debe tener 0 seguidores";

            // Undo DEJAR_DE_SEGUIR -> Debe restaurar la relación
            Accion accion = gestor.deshacer();
            assert accion != null : "Deshacer debe retornar acción";
            assert accion.getTipo() == TipoAccion.DEJAR_DE_SEGUIR : "Tipo debe ser DEJAR_DE_SEGUIR";
            assert alice.sigueA(idBob) : "Alice debe volver a seguir a Bob";
            assert bob.getCantidadSeguidores() == 1 : "Bob debe tener 1 seguidor";

            reportarExito("Undo dejar de seguir");
        } catch (AssertionError e) {
            reportarFallo("Undo dejar de seguir", e.getMessage());
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
            int idAlice = gestor.agregarCliente("Alice", 95);
            int idBob = gestor.agregarCliente("Bob", 88);

            Cliente alice = gestor.buscarPorId(idAlice);
            Sesion.getInstancia().iniciarSesion(alice);

            gestor.seguir(idAlice, idBob);
            Accion accion = gestor.verUltimaAccion();
            assert accion != null : "Debe retornar última acción";
            assert accion.getTipo() == TipoAccion.SEGUIR : "Tipo debe ser SEGUIR";

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
    // TESTS DE ABB SCORING E ITERACIÓN 2
    // ═══════════════════════════════════════════════════════════════════

    private static void testBuscarPorScoring() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            gestor.agregarClienteConId(1, "Alice", 95);
            gestor.agregarClienteConId(2, "Bob", 80);

            Cliente[] encontrados = gestor.buscarPorScoring(95);
            assert encontrados.length == 1 : "Debe encontrar 1 con scoring 95";
            assert encontrados[0].getNombre().equals("Alice") : "Debe ser Alice";

            reportarExito("Buscar por scoring");
        } catch (AssertionError e) {
            reportarFallo("Buscar por scoring", e.getMessage());
        }
    }

    private static void testBuscarPorScoringDuplicados() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            gestor.agregarClienteConId(1, "Alice", 80);
            gestor.agregarClienteConId(2, "Bob", 80);
            gestor.agregarClienteConId(3, "Carol", 90);

            Cliente[] encontrados = gestor.buscarPorScoring(80);
            assert encontrados.length == 2 : "Debe encontrar 2 con scoring 80, encontró: " + encontrados.length;

            reportarExito("Buscar por scoring con duplicados");
        } catch (AssertionError e) {
            reportarFallo("Buscar por scoring con duplicados", e.getMessage());
        }
    }

    private static void testBuscarPorScoringInexistente() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            gestor.agregarClienteConId(1, "Alice", 80);

            Cliente[] encontrados = gestor.buscarPorScoring(99);
            assert encontrados.length == 0 : "No debe encontrar nada con scoring 99";

            reportarExito("Buscar por scoring inexistente");
        } catch (AssertionError e) {
            reportarFallo("Buscar por scoring inexistente", e.getMessage());
        }
    }

    private static void testObtenerClientesEnNivel() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            // Insertar con scorings que formen un ABB con al menos 4 niveles
            // Raíz: 50, nivel 1: 30, 70, nivel 2: 20, 40, 60, 80, nivel 3: 10, 25, 35, 45...
            gestor.agregarClienteConId(1, "C50", 50);  // nivel 0 (raíz)
            gestor.agregarClienteConId(2, "C30", 30);  // nivel 1
            gestor.agregarClienteConId(3, "C70", 70);  // nivel 1
            gestor.agregarClienteConId(4, "C20", 20);  // nivel 2
            gestor.agregarClienteConId(5, "C40", 40);  // nivel 2
            gestor.agregarClienteConId(6, "C60", 60);  // nivel 2
            gestor.agregarClienteConId(7, "C80", 80);  // nivel 2
            gestor.agregarClienteConId(8, "C10", 10);  // nivel 3 (cuarto nivel)
            gestor.agregarClienteConId(9, "C25", 25);  // nivel 3

            // Nivel 0 (raíz)
            Cliente[] nivel0 = gestor.obtenerClientesEnNivel(0);
            assert nivel0.length == 1 : "Nivel 0 debe tener 1, tiene: " + nivel0.length;

            // Nivel 3 (cuarto nivel) - debe tener C10 y C25
            Cliente[] nivel3 = gestor.obtenerClientesEnNivel(3);
            assert nivel3.length == 2 : "Nivel 3 debe tener 2, tiene: " + nivel3.length;

            reportarExito("Obtener clientes en nivel (cuarto nivel ABB)");
        } catch (AssertionError e) {
            reportarFallo("Obtener clientes en nivel (cuarto nivel ABB)", e.getMessage());
        }
    }

    private static void testSeguirActualizaSeguidores() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 90);
            int idBob = gestor.agregarCliente("Bob", 80);

            gestor.seguir(idAlice, idBob);

            Cliente bob = gestor.buscarPorId(idBob);
            assert bob.getCantidadSeguidores() == 1 : "Bob debe tener 1 seguidor";

            Cliente alice = gestor.buscarPorId(idAlice);
            assert alice.sigueA(idBob) : "Alice debe seguir a Bob";

            // Dejar de seguir también debe actualizar seguidores
            gestor.dejarDeSeguir(idAlice, idBob);
            assert bob.getCantidadSeguidores() == 0 : "Bob debe tener 0 seguidores tras dejar de seguir";

            reportarExito("Seguir/dejar de seguir actualiza seguidores");
        } catch (AssertionError e) {
            reportarFallo("Seguir/dejar de seguir actualiza seguidores", e.getMessage());
        }
    }

    private static void testUndoSeguirRevierteSeguidores() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 90);
            int idBob = gestor.agregarCliente("Bob", 80);

            Cliente alice = gestor.buscarPorId(idAlice);
            Sesion.getInstancia().iniciarSesion(alice);

            gestor.seguir(idAlice, idBob);
            Cliente bob = gestor.buscarPorId(idBob);
            assert bob.getCantidadSeguidores() == 1 : "Bob debe tener 1 seguidor";

            // Undo debe revertir AMBOS lados
            gestor.deshacer();
            assert !alice.sigueA(idBob) : "Alice ya no debe seguir a Bob";
            assert bob.getCantidadSeguidores() == 0 : "Bob debe tener 0 seguidores tras undo";

            reportarExito("Undo seguir revierte seguidores (bidireccional)");
        } catch (AssertionError e) {
            reportarFallo("Undo seguir revierte seguidores (bidireccional)", e.getMessage());
        }
    }

    private static void testBuscarPorScoringMuchosDuplicados() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            // Insertar 100 clientes con el mismo scoring
            for (int i = 1; i <= 100; i++) {
                gestor.agregarClienteConId(i, "User" + i, 50);
            }
            // Insertar uno con scoring distinto
            gestor.agregarClienteConId(101, "Distinto", 99);

            Cliente[] encontrados = gestor.buscarPorScoring(50);
            assert encontrados.length == 100 : "Debe encontrar 100 con scoring 50, encontró: " + encontrados.length;

            Cliente[] otro = gestor.buscarPorScoring(99);
            assert otro.length == 1 : "Debe encontrar 1 con scoring 99, encontró: " + otro.length;

            reportarExito("Buscar por scoring con muchos duplicados (100 clientes)");
        } catch (AssertionError e) {
            reportarFallo("Buscar por scoring con muchos duplicados", e.getMessage());
        }
    }

    private static void testAgregarClientePostBuildScoring() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            gestor.agregarClienteConId(1, "Alice", 80);

            // Trigger lazy build del índice de scoring
            Cliente[] antes = gestor.buscarPorScoring(80);
            assert antes.length == 1 : "Debe encontrar 1 antes";

            // Agregar nuevo cliente DESPUÉS de que el índice ya fue construido
            gestor.agregarClienteConId(2, "Bob", 80);
            gestor.agregarClienteConId(3, "Carol", 90);

            Cliente[] despues80 = gestor.buscarPorScoring(80);
            assert despues80.length == 2 : "Debe encontrar 2 con scoring 80 post-build, encontró: " + despues80.length;

            Cliente[] despues90 = gestor.buscarPorScoring(90);
            assert despues90.length == 1 : "Debe encontrar 1 con scoring 90 post-build, encontró: " + despues90.length;

            reportarExito("Agregar cliente post-build scoring index");
        } catch (AssertionError e) {
            reportarFallo("Agregar cliente post-build scoring index", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // UTILIDADES DE REPORTE
    // ═══════════════════════════════════════════════════════════════════

    private static void reportarExito(String nombre) {
        System.out.println("  [OK] " + nombre);
        testsPasados++;
    }

    private static void reportarFallo(String nombre, String mensaje) {
        System.out.println("  [FAIL] " + nombre + ": " + mensaje);
        testsFallados++;
    }
}
