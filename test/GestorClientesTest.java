package test;

import servicio.GestorClientes;
import modelo.Cliente;
import modelo.Accion;
import modelo.TipoAccion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class GestorClientesTest {
    
    private GestorClientes gestor;

    private static final String TIMING_TEST_DB = "data/clientes_JUNIT.json";

    @BeforeEach
    void setUp() {
        // Reset del archivo para aislar los tests (evitar que datos de un test afecten a otro)
        try (java.io.FileWriter writer = new java.io.FileWriter(TIMING_TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {
            System.err.println("Error resetando DB de test: " + e.getMessage());
        }
        
        // Usar la base de datos pequeña para tests rápidos
        gestor = new GestorClientes(TIMING_TEST_DB);
    }

    @Test
    void testAgregarClienteValido() {
        assertTrue(gestor.agregarCliente("Alice", 95));
        assertEquals(1, gestor.getCantidadClientes());
    }

    @Test
    void testAgregarClienteDuplicado() {
        gestor.agregarCliente("Alice", 95);
        assertFalse(gestor.agregarCliente("Alice", 80));
        assertEquals(1, gestor.getCantidadClientes());
    }

    @Test
    void testAgregarClienteNombreVacio() {
        assertFalse(gestor.agregarCliente("", 50));
        assertFalse(gestor.agregarCliente(null, 50));
        assertEquals(0, gestor.getCantidadClientes());
    }

    @Test
    void testAgregarClienteScoringInvalido() {
        assertFalse(gestor.agregarCliente("Test", 150));
        assertFalse(gestor.agregarCliente("Test", -1));
        assertEquals(0, gestor.getCantidadClientes());
    }

    @Test
    void testBuscarPorNombre() {
        gestor.agregarCliente("Alice", 95);
        Cliente encontrado = gestor.buscarPorNombre("Alice");
        assertNotNull(encontrado);
        assertEquals("Alice", encontrado.getNombre());
    }

    @Test
    void testBuscarInexistente() {
        assertNull(gestor.buscarPorNombre("ZZZ"));
    }

    @Test
    void testSeguir() {
        gestor.agregarCliente("Alice", 95);
        gestor.agregarCliente("Bob", 88);
        assertTrue(gestor.seguir("Alice", "Bob"));
        
        Cliente alice = gestor.buscarPorNombre("Alice");
        assertTrue(alice.sigueA("Bob"));
    }

    @Test
    void testSeguirClienteInexistente() {
        gestor.agregarCliente("Alice", 95);
        assertFalse(gestor.seguir("Alice", "ZZZ"));
        assertFalse(gestor.seguir("ZZZ", "Alice"));
    }

    @Test
    void testSeguirAutoLazo() {
        gestor.agregarCliente("Alice", 95);
        // Debe retornar false porque está prohibido seguirse a sí mismo
        assertFalse(gestor.seguir("Alice", "Alice")); 
    }

    @Test
    void testSeguirLimiteExcedido() {
        gestor.agregarCliente("Alice", 90);
        gestor.agregarCliente("B1", 10);
        gestor.agregarCliente("B2", 10);
        gestor.agregarCliente("B3", 10);
        
        assertTrue(gestor.seguir("Alice", "B1"));
        assertTrue(gestor.seguir("Alice", "B2"));
        assertFalse(gestor.seguir("Alice", "B3")); // Límite de 2 alcanzado
    }

    @Test
    void testDeshacerAgregarCliente() {
        gestor.agregarCliente("Alice", 95);
        assertEquals(1, gestor.getCantidadClientes());
        
        Accion accion = gestor.deshacer();
        assertNotNull(accion);
        assertEquals(TipoAccion.AGREGAR_CLIENTE, accion.getTipo());
        assertEquals(0, gestor.getCantidadClientes());
        assertNull(gestor.buscarPorNombre("Alice"));
    }

    @Test
    void testDeshacerSeguir() {
        gestor.agregarCliente("Alice", 95);
        gestor.agregarCliente("Bob", 88);
        gestor.seguir("Alice", "Bob");
        
        gestor.deshacer();
        
        Cliente alice = gestor.buscarPorNombre("Alice");
        assertFalse(alice.sigueA("Bob"));
    }
    
    @Test
    void testUndoEliminarClienteConCascada() {
        gestor.agregarCliente("Alice", 90);
        gestor.agregarCliente("Bob", 80);
        gestor.seguir("Alice", "Bob");
        
        assertTrue(gestor.buscarPorNombre("Alice").sigueA("Bob"));
        
        // Eliminar Bob -> Alice debe dejar de seguirlo automáticamente
        gestor.eliminarCliente("Bob");
        assertNull(gestor.buscarPorNombre("Bob"));
        assertFalse(gestor.buscarPorNombre("Alice").sigueA("Bob"));
        
        // Undo -> Debe volver Bob y Alice debe volver a seguirlo
        gestor.deshacer();
        assertNotNull(gestor.buscarPorNombre("Bob"));
        boolean sigueABob = gestor.buscarPorNombre("Alice").sigueA("Bob");
        assertTrue(sigueABob, "Undo falló en restaurar la relación de seguimiento");
    }

    @Test
    void testDeshacerHistorialVacio() {
        assertNull(gestor.deshacer());
    }

    @Test
    void testVerUltimaAccion() {
        gestor.agregarCliente("Alice", 95);
        Accion accion = gestor.verUltimaAccion();
        assertNotNull(accion);
        assertEquals(TipoAccion.AGREGAR_CLIENTE, accion.getTipo());
    }
}
