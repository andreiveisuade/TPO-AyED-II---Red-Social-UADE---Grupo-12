import servicio.GestorClientes;
import modelo.Cliente;
import modelo.Sesion;
import modelo.Accion;

/**
 * Test exhaustivo para debuggear la funcionalidad de Undo/Redo.
 */
public class UndoDebugTest {
    
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_UNDO_TEST.json";
    
    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {}
    }
    
    private static boolean contiene(int[] array, int valor) {
        if (array == null) return false;
        for (int v : array) {
            if (v == valor) return true;
        }
        return false;
    }
    
    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║         TEST DE DEBUG: SISTEMA DE UNDO/REDO                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        
        testUndoSeguirBasico();
        testUndoSeguir();
        testUndoDejarDeSeguir();
        testUndoMultiplesAcciones();
        testUndoConsistenciaBidireccional();
        
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.printf("║  RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        
        if (testsFallados > 0) System.exit(1);
    }
    
    private static void testUndoSeguirBasico() {
        try {
            System.out.println("TEST 1: Undo Seguir (básico)");
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 95);
            int idBob = gestor.agregarCliente("Bob", 87);

            Cliente alice = gestor.buscarPorId(idAlice);
            Sesion.getInstancia().iniciarSesion(alice);

            gestor.seguir(idAlice, idBob);
            System.out.println("  ✓ Alice sigue a Bob");
            assert alice.sigueA(idBob) : "Alice debe seguir a Bob";

            Accion accionDeshecha = gestor.deshacer();
            System.out.println("  ✓ Undo ejecutado: " + accionDeshecha.getTipo());

            assert accionDeshecha != null : "Deshacer debe retornar la acción";
            assert !alice.sigueA(idBob) : "Alice no debe seguir a Bob después del undo";
            System.out.println("  ✓ Relación deshecha correctamente\n");

            testsPasados++;
        } catch (Exception e) {
            System.out.println("  ✗ Error: " + e.getMessage() + "\n");
            testsFallados++;
        }
    }
    
    private static void testUndoSeguir() {
        try {
            System.out.println("TEST 2: Undo Seguir");
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 95);
            int idBob = gestor.agregarCliente("Bob", 87);
            
            Cliente alice = gestor.buscarPorId(idAlice);
            Sesion.getInstancia().iniciarSesion(alice);
            gestor.seguir(idAlice, idBob);
            System.out.println("  ✓ Alice sigue a Bob");
            assert alice.sigueA(idBob) : "Alice debe seguir a Bob";
            
            Accion accionDeshecha = gestor.deshacer();
            System.out.println("  ✓ Undo ejecutado: " + accionDeshecha.getTipo());
            
            assert !alice.sigueA(idBob) : "Alice no debe seguir a Bob después del undo";
            System.out.println("  ✓ Relación deshecha correctamente\n");
            
            testsPasados++;
        } catch (Exception e) {
            System.out.println("  ✗ Error: " + e.getMessage() + "\n");
            testsFallados++;
        }
    }
    
    private static void testUndoDejarDeSeguir() {
        try {
            System.out.println("TEST 3: Undo Dejar de Seguir");
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 95);
            int idBob = gestor.agregarCliente("Bob", 87);
            
            Cliente alice = gestor.buscarPorId(idAlice);
            Sesion.getInstancia().iniciarSesion(alice);
            gestor.seguir(idAlice, idBob);
            gestor.dejarDeSeguir(idAlice, idBob);
            System.out.println("  ✓ Alice deja de seguir a Bob");
            assert !alice.sigueA(idBob) : "Alice no debe seguir a Bob";
            
            Accion accionDeshecha = gestor.deshacer();
            System.out.println("  ✓ Undo ejecutado: " + accionDeshecha.getTipo());
            
            assert alice.sigueA(idBob) : "Alice debe seguir a Bob después del undo";
            System.out.println("  ✓ Relación restaurada correctamente\n");
            
            testsPasados++;
        } catch (Exception e) {
            System.out.println("  ✗ Error: " + e.getMessage() + "\n");
            testsFallados++;
        }
    }
    
    private static void testUndoMultiplesAcciones() {
        try {
            System.out.println("TEST 4: Undo Múltiples Acciones");
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int id1 = gestor.agregarCliente("A1", 95);
            int id2 = gestor.agregarCliente("A2", 87);
            int id3 = gestor.agregarCliente("A3", 75);
            
            Cliente c1 = gestor.buscarPorId(id1);
            Sesion.getInstancia().iniciarSesion(c1);
            gestor.seguir(id1, id2);
            System.out.println("  ✓ A1 sigue a A2");
            gestor.seguir(id1, id3);
            System.out.println("  ✓ A1 sigue a A3");
            
            Accion undo1 = gestor.deshacer();
            System.out.println("  ✓ Primer undo: " + undo1.getTipo());
            assert c1.sigueA(id2) : "A1 debe seguir a A2";
            assert !c1.sigueA(id3) : "A1 no debe seguir a A3";
            
            Accion undo2 = gestor.deshacer();
            System.out.println("  ✓ Segundo undo: " + undo2.getTipo());
            assert !c1.sigueA(id2) : "A1 no debe seguir a A2";
            assert !c1.sigueA(id3) : "A1 no debe seguir a A3";
            System.out.println("  ✓ Múltiples undos ejecutados correctamente\n");
            
            testsPasados++;
        } catch (Exception e) {
            System.out.println("  ✗ Error: " + e.getMessage() + "\n");
            testsFallados++;
        }
    }
    
    private static void testUndoConsistenciaBidireccional() {
        try {
            System.out.println("TEST 5: Consistencia Bidireccional");
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int idAlice = gestor.agregarCliente("Alice", 95);
            int idBob = gestor.agregarCliente("Bob", 87);
            
            Cliente alice = gestor.buscarPorId(idAlice);
            Cliente bob = gestor.buscarPorId(idBob);
            Sesion.getInstancia().iniciarSesion(alice);
            gestor.seguir(idAlice, idBob);
            System.out.println("  ✓ Seguimiento establecido:");
            System.out.println("    - Alice sigue a Bob: " + alice.sigueA(idBob));
            System.out.println("    - Bob tiene Alice como seguidor: " + contiene(bob.getSeguidores(), idAlice));
            
            assert alice.sigueA(idBob) : "Alice debe seguir a Bob";
            assert contiene(bob.getSeguidores(), idAlice) : "Bob debe tener a Alice como seguidor";
            
            gestor.deshacer();
            System.out.println("  ✓ Undo ejecutado");
            System.out.println("    - Alice sigue a Bob: " + alice.sigueA(idBob));
            System.out.println("    - Bob tiene Alice como seguidor: " + contiene(bob.getSeguidores(), idAlice));
            
            assert !alice.sigueA(idBob) : "Alice no debe seguir a Bob";
            assert !contiene(bob.getSeguidores(), idAlice) : "Bob no debe tener a Alice como seguidor";
            System.out.println("  ✓ Consistencia bidireccional mantenida\n");
            
            testsPasados++;
        } catch (Exception e) {
            System.out.println("  ✗ Error: " + e.getMessage() + "\n");
            testsFallados++;
        }
    }
}
