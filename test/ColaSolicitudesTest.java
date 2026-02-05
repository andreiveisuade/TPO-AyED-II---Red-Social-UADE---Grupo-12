package test;

import servicio.ColaSolicitudes;
import modelo.SolicitudSeguimiento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class ColaSolicitudesTest {
    
    private ColaSolicitudes cola;

    @BeforeEach
    void setUp() {
        cola = new ColaSolicitudes();
    }

    @Test
    void testAgregarSolicitud() {
        assertTrue(cola.agregar("Alice", "Bob"));
        assertEquals(1, cola.getCantidad());
    }

    @Test
    void testAgregarSolicitudDuplicada() {
        assertTrue(cola.agregar("Alice", "Bob"));
        assertFalse(cola.agregar("Alice", "Bob"));
        assertEquals(1, cola.getCantidad());
    }

    @Test
    void testAgregarSolicitudASiMismo() {
        assertFalse(cola.agregar("Alice", "Alice"));
        assertEquals(0, cola.getCantidad());
    }

    @Test
    void testProcesarSolicitud() {
        cola.agregar("Alice", "Bob");
        cola.agregar("Charlie", "David");
        
        SolicitudSeguimiento primera = cola.procesar();
        assertNotNull(primera);
        assertEquals("Alice", primera.getSolicitante());
        assertEquals("Bob", primera.getObjetivo());
        assertEquals(1, cola.getCantidad());
    }

    @Test
    void testProcesarColaVacia() {
        assertNull(cola.procesar());
    }

    @Test
    void testOrdenFIFO() {
        cola.agregar("Alice", "Bob");
        cola.agregar("Charlie", "David");
        cola.agregar("Eve", "Frank");
        
        assertEquals("Alice", cola.procesar().getSolicitante());
        assertEquals("Charlie", cola.procesar().getSolicitante());
        assertEquals("Eve", cola.procesar().getSolicitante());
    }

    @Test
    void testPuedeReagregarDespuesDeProcesar() {
        cola.agregar("Alice", "Bob");
        cola.procesar();
        
        assertTrue(cola.agregar("Alice", "Bob"));
        assertEquals(1, cola.getCantidad());
    }

    @Test
    void testExisteSolicitud() {
        cola.agregar("Alice", "Bob");
        assertTrue(cola.existeSolicitud("Alice", "Bob"));
        assertFalse(cola.existeSolicitud("Bob", "Alice"));
    }
}
