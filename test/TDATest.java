import tda.*;
import modelo.*;
import servicio.*;

/**
 * Tests unitarios básicos para los TDAs de Iteración 1.
 * 
 * Para ejecutar: compilar y ejecutar este archivo.
 * Los tests usan assertions - ejecutar con java -ea TDATest
 */
public class TDATest {

    private static int testsPasados = 0;
    private static int testsFallados = 0;

    private static final String TEST_DB = "data/clientes_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {
            System.err.println("Error creating test DB: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        initTestDB();
        System.out.println("═══════════════════════════════════════════");
        System.out.println("   TESTS UNITARIOS (MODO TEST DB)      ");
        System.out.println("═══════════════════════════════════════════\n");

        testPilaOperacionesBasicas();
        testPilaEdgeCases();
        testColaFIFO();
        testColaEdgeCases();
        testDiccionarioBasico();
        testDiccionarioColisiones();
        testDiccionarioMuchosElementos();
        testConjuntoBasico();
        testGestorClientesBasico();
        testGestorClientesEliminacionCascada();
        testHistorialUndo();
        
        // Nuevos tests para casos borde y stress
        testDiccionarioVaciado();
        testDiccionarioValores();
        testGestorClientesEdgeCases();
        testPersistenciaReal();
        testBuscarPorScoring();
        testPersistenciaUnfollow();

        System.out.println("\n═══════════════════════════════════════════");
        System.out.printf("RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("═══════════════════════════════════════════");

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TESTS DE PILA
    // ═══════════════════════════════════════════════════════════════════

    private static void testPilaOperacionesBasicas() {
        try {
            Pila<Integer> p = new Pila<>();
            assert p.estaVacia() : "Pila nueva debe estar vacía";
            assert p.getCantidad() == 0 : "Cantidad debe ser 0";

            p.apilar(1);
            p.apilar(2);
            p.apilar(3);
            
            assert !p.estaVacia() : "Pila con elementos no debe estar vacía";
            assert p.getCantidad() == 3 : "Cantidad debe ser 3";
            assert p.verTope() == 3 : "VerTope debe retornar 3";
            assert p.desapilar() == 3 : "Desapilar debe retornar 3";
            assert p.desapilar() == 2 : "Desapilar debe retornar 2 (LIFO)";
            assert p.desapilar() == 1 : "Desapilar debe retornar 1";
            assert p.estaVacia() : "Pila vacía después de desapilar";

            reportarExito("Pila - Operaciones básicas");
        } catch (AssertionError e) {
            reportarFallo("Pila - Operaciones básicas", e.getMessage());
        }
    }

    private static void testPilaEdgeCases() {
        try {
            Pila<String> p = new Pila<>();
            
            assert p.desapilar() == null : "Desapilar en pila vacía debe retornar null";
            assert p.verTope() == null : "VerTope en pila vacía debe retornar null";
            
            p.apilar("A");
            assert p.verTope().equals("A") : "VerTope no debe modificar pila";
            assert p.verTope().equals("A") : "Segundo verTope debe retornar mismo valor";
            assert p.getCantidad() == 1 : "Cantidad sigue siendo 1";

            reportarExito("Pila - Casos borde");
        } catch (AssertionError e) {
            reportarFallo("Pila - Casos borde", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TESTS DE COLA
    // ═══════════════════════════════════════════════════════════════════

    private static void testColaFIFO() {
        try {
            Cola<String> c = new Cola<>();
            assert c.estaVacia() : "Cola nueva debe estar vacía";

            c.encolar("A");
            c.encolar("B");
            c.encolar("C");

            assert c.getCantidad() == 3 : "Cantidad debe ser 3";
            assert c.desencolar().equals("A") : "Primer elemento debe ser A (FIFO)";
            assert c.desencolar().equals("B") : "Segundo elemento debe ser B";
            assert c.desencolar().equals("C") : "Tercer elemento debe ser C";
            assert c.estaVacia() : "Cola vacía después de desencolar todos";

            reportarExito("Cola - FIFO");
        } catch (AssertionError e) {
            reportarFallo("Cola - FIFO", e.getMessage());
        }
    }

    private static void testColaEdgeCases() {
        try {
            Cola<Integer> c = new Cola<>();
            
            assert c.desencolar() == null : "Desencolar en cola vacía retorna null";
            assert c.verFrente() == null : "VerFrente en cola vacía retorna null";

            c.encolar(5);
            assert c.verFrente() == 5 : "VerFrente retorna primer elemento";
            assert c.verFrente() == 5 : "VerFrente no modifica cola";
            assert c.getCantidad() == 1 : "Cantidad sigue siendo 1";

            reportarExito("Cola - Casos borde");
        } catch (AssertionError e) {
            reportarFallo("Cola - Casos borde", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TESTS DE DICCIONARIO
    // ═══════════════════════════════════════════════════════════════════

    private static void testDiccionarioBasico() {
        try {
            Diccionario<String, Integer> d = new Diccionario<>();
            assert d.estaVacio() : "Diccionario nuevo debe estar vacío";

            d.insertar("uno", 1);
            d.insertar("dos", 2);
            d.insertar("tres", 3);

            assert d.getCantidad() == 3 : "Cantidad debe ser 3";
            assert d.contiene("dos") : "Debe contener 'dos'";
            assert !d.contiene("cuatro") : "No debe contener 'cuatro'";
            assert d.obtener("uno") == 1 : "Obtener 'uno' debe retornar 1";
            
            d.eliminar("dos");
            assert !d.contiene("dos") : "Después de eliminar no debe contener 'dos'";
            assert d.getCantidad() == 2 : "Cantidad debe ser 2";

            reportarExito("Diccionario - Operaciones básicas");
        } catch (AssertionError e) {
            reportarFallo("Diccionario - Operaciones básicas", e.getMessage());
        }
    }

    private static void testDiccionarioColisiones() {
        try {
            Diccionario<String, String> d = new Diccionario<>();
            
            // Insertar varios elementos para forzar colisiones
            for (int i = 0; i < 10; i++) {
                d.insertar("key" + i, "val" + i);
            }
            
            assert d.getCantidad() == 10 : "Cantidad debe ser 10";
            assert d.obtener("key5").equals("val5") : "Debe manejar colisiones correctamente";
            assert d.obtener("key9").equals("val9") : "Último elemento accesible";

            reportarExito("Diccionario - Manejo de colisiones");
        } catch (AssertionError e) {
            reportarFallo("Diccionario - Manejo de colisiones", e.getMessage());
        }
    }

    private static void testDiccionarioMuchosElementos() {
        try {
            Diccionario<String, Integer> d = new Diccionario<>();
            
            // Insertar 20 elementos para verificar manejo de colisiones
            for (int i = 0; i < 20; i++) {
                d.insertar("k" + i, i);
            }
            
            assert d.getCantidad() == 20 : "Cantidad debe ser 20";
            
            // Verificar que todos los elementos son accesibles
            for (int i = 0; i < 20; i++) {
                assert d.contiene("k" + i) : "Elemento k" + i + " debe existir";
                assert d.obtener("k" + i) == i : "Valor de k" + i + " debe ser correcto";
            }

            reportarExito("Diccionario - Muchos elementos");
        } catch (AssertionError e) {
            reportarFallo("Diccionario - Muchos elementos", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TESTS DE CONJUNTO
    // ═══════════════════════════════════════════════════════════════════

    private static void testConjuntoBasico() {
        try {
            Conjunto c = new Conjunto();
            assert c.estaVacio() : "Conjunto nuevo debe estar vacío";

            c.agregar("A");
            c.agregar("B");
            c.agregar("A"); // Duplicado

            assert c.getCantidad() == 2 : "Conjunto no debe tener duplicados";
            assert c.contiene("A") : "Debe contener A";
            assert c.contiene("B") : "Debe contener B";
            assert !c.contiene("C") : "No debe contener C";

            c.eliminar("A");
            assert !c.contiene("A") : "A eliminado correctamente";
            assert c.getCantidad() == 1 : "Cantidad debe ser 1";

            reportarExito("Conjunto - Operaciones básicas");
        } catch (AssertionError e) {
            reportarFallo("Conjunto - Operaciones básicas", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TESTS DE GESTOR CLIENTES (API ACTUALIZADA)
    // ═══════════════════════════════════════════════════════════════════

    private static void testGestorClientesBasico() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            int idAlice = gestor.agregarCliente("Alice", 80);
            assert idAlice != -1 : "Agregar Alice debe funcionar";
            
            int idBob = gestor.agregarCliente("Bob", 60);
            assert idBob != -1 : "Agregar Bob debe funcionar";
            
            // Probar invalidos
            assert gestor.agregarCliente("", 50) == -1 : "No debe permitir nombre vacío";
            assert gestor.agregarCliente("Charlie", 150) == -1 : "No debe permitir scoring > 100";
            
            assert gestor.getCantidadClientes() >= 2 : "Cantidad debe ser al menos 2";
            assert gestor.existeCliente(idAlice) : "Alice debe existir por ID";
            assert !gestor.existeCliente(999999999) : "ID inexistente no debe existir";
            
            Cliente[] resultados = gestor.buscarPorNombre("Alice");
            assert resultados != null && resultados.length > 0 : "Buscar Alice debe devolver resultados";
            // Checkeo aproximado de ID porque la búsqueda por nombre podría devolver homónimos
            boolean encontrado = false;
            for(Cliente c : resultados) {
                if (c.getId() == idAlice) { encontrado = true; break; }
            }
            assert encontrado : "El cliente Alice agregado debe aparecer en la búsqueda por nombre";

            reportarExito("GestorClientes - Operaciones básicas");
        } catch (AssertionError e) {
            reportarFallo("GestorClientes - Operaciones básicas", e.getMessage());
        }
    }

    private static void testGestorClientesEliminacionCascada() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);

            int idAlice = gestor.agregarCliente("Alice", 80);
            int idBob = gestor.agregarCliente("Bob", 60);
            
            // Alice sigue a Bob
            gestor.seguir(idAlice, idBob);
            Cliente alice = gestor.buscarPorId(idAlice);
            assert alice.sigueA(idBob) : "Alice debe seguir a Bob";
            
            // Eliminar Bob
            assert gestor.eliminarCliente(idBob) : "Eliminar Bob debe funcionar";
            assert !gestor.existeCliente(idBob) : "Bob no debe existir";
            
            assert !alice.sigueA(idBob) : "Alice no debe seguir a Bob tras eliminación";
            
            reportarExito("GestorClientes - Eliminación en cascada");
        } catch (AssertionError e) {
            reportarFallo("GestorClientes - Eliminación en cascada", e.getMessage());
        }
    }

    private static void testHistorialUndo() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            
            int idAlice = gestor.agregarCliente("Alice", 80);
            int idBob = gestor.agregarCliente("Bob", 60);
            
            // Simular sesión para que el historial funcione
            Cliente alice = gestor.buscarPorId(idAlice);
            Sesion.getInstancia().iniciarSesion(alice);
            gestor.activarHistorial(); // Reiniciar/activar historial
            
            // Acción de Seguir
            gestor.seguir(idAlice, idBob);
            assert alice.sigueA(idBob) : "Debe seguir a Bob";
            
            // Undo
            gestor.deshacer();
            assert !alice.sigueA(idBob) : "Undo debe revertir el seguimiento";
            
            reportarExito("Historial - Undo (Seguir)");
        } catch (AssertionError e) {
            reportarFallo("Historial - Undo (Seguir)", e.getMessage());
        }
    }

    private static void testDiccionarioVaciado() {
        try {
            Diccionario<String, Integer> d = new Diccionario<>();
            int cantidad = 100;
            for (int i = 0; i < cantidad; i++) d.insertar("k" + i, i);
            
            assert d.getCantidad() == cantidad : "Debe tener " + cantidad + " elementos";
            
            for (int i = 0; i < cantidad; i++) {
                assert d.eliminar("k" + i) != null : "Eliminar k" + i + " falló";
            }
            
            assert d.estaVacio() : "Diccionario debe quedar vacío tras eliminar todo";
            assert d.getCantidad() == 0 : "Cantidad debe ser 0";

            reportarExito("Diccionario - Vaciado Completo (Stress)");
        } catch (AssertionError e) {
            reportarFallo("Diccionario - Vaciado Completo (Stress)", e.getMessage());
        }
    }

    private static void testDiccionarioValores() {
        try {
            Diccionario<String, Integer> d = new Diccionario<>();
            d.insertar("A", 1);
            d.insertar("B", 2);
            d.insertar("C", 3);
            
            Object[] valores = d.obtenerValores();
            assert valores.length == 3 : "Debe traer 3 valores";
            
            int suma = 0;
            for(Object v : valores) suma += (Integer)v;
            assert suma == 6 : "Suma de valores debe ser 1+2+3=6";

            reportarExito("Diccionario - Obtener Valores");
        } catch (AssertionError e) {
            reportarFallo("Diccionario - Obtener Valores", e.getMessage());
        }
    }

    private static void testGestorClientesEdgeCases() {
        try {
            initTestDB();
            GestorClientes g = new GestorClientes(TEST_DB);
            int idA = g.agregarCliente("A", 10);
            
            assert !g.seguir(idA, idA) : "Autoseguimiento debe estar prohibido (retorna false)";
            
            reportarExito("GestorClientes - Casos Borde");
        } catch (AssertionError e) {
            reportarFallo("GestorClientes - Casos Borde", e.getMessage());
        }
    }

    private static void testPersistenciaReal() {
        try {
            // 1. Limpiar DB
            initTestDB();
            
            // 2. Crear datos y guardar
            GestorClientes g1 = new GestorClientes(TEST_DB);
            int idUser = g1.agregarCliente("PersistentUser", 99);
            g1.guardarCambios(); // Forzar guardado
            
            // 3. Reiniciar gestor (Simular reinicio app)
            GestorClientes g2 = new GestorClientes(TEST_DB);
            
            // 4. Verificar que datos existen
            assert g2.getCantidadClientes() == 1 : "Debe haber 1 cliente tras recarga";
            Cliente c = g2.buscarPorId(idUser);
            assert c != null : "Cliente debe existir";
            assert c.getNombre().equals("PersistentUser") : "Nombre debe coincidir";
            assert c.getScoring() == 99 : "Scoring debe coincidir";
            
            reportarExito("Persistencia - Recarga de Datos");
        } catch (AssertionError e) {
            reportarFallo("Persistencia - Recarga de Datos", e.getMessage());
        }
    }

    private static void testBuscarPorScoring() {
        try {
            initTestDB();
            GestorClientes g = new GestorClientes(TEST_DB);
            g.agregarCliente("High1", 99);
            g.agregarCliente("High2", 99);
            g.agregarCliente("Low", 10);
            
            Cliente[] resultados = g.buscarPorScoring(99);
            assert resultados.length == 2 : "Debe encontrar 2 usuarios con scoring 99";
            
            boolean found1 = false, found2 = false;
            for(Cliente c : resultados) {
                if(c.getNombre().equals("High1")) found1 = true;
                if(c.getNombre().equals("High2")) found2 = true;
            }
            assert found1 && found2 : "Debe encontrar a ambos usuarios";
            
            assert g.buscarPorScoring(10).length == 1 : "Debe encontrar 1 con scoring 10";
            assert g.buscarPorScoring(50).length == 0 : "No debe encontrar scoring inexistente";
            
            reportarExito("Búsqueda - Por Scoring");
        } catch (AssertionError e) {
            reportarFallo("Búsqueda - Por Scoring", e.getMessage());
        }
    }

    private static void testPersistenciaUnfollow() {
        try {
            initTestDB();
            GestorClientes g1 = new GestorClientes(TEST_DB);
            int idA = g1.agregarCliente("A", 50);
            int idB = g1.agregarCliente("B", 50);
            g1.guardarCambios(); // Commit initial state
            
            // 1. Seguir (Esto guarda automáticamente)
            g1.seguir(idA, idB);
            
            // 2. Dejar de seguir - AQUÍ ES DONDE PROBABLEMENTE FALLE SI NO IMPLEMENTAMOS EL FIX
            // Esperamos que falle si no hay persistencia automatica aun
            boolean resultado = g1.dejarDeSeguir(idA, idB);
            assert resultado : "Dejar de seguir debe retornar true";
            
            // 3. Reiniciar
            GestorClientes g2 = new GestorClientes(TEST_DB);
            Cliente a = g2.buscarPorId(idA);
            
            // 4. Verificar
            if (a.sigueA(idB)) {
                // Si sigue a B, significa que el 'dejarDeSeguir' solo afectó memoria y no disco
                throw new AssertionError("La acción 'Dejar de Seguir' NO persistió tras reinicio");
            }
            
            reportarExito("Persistencia - Unfollow");
        } catch (AssertionError e) {
            reportarFallo("Persistencia - Unfollow", e.getMessage());
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
