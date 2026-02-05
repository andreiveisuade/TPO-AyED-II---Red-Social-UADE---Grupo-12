package test;

import tda.Cola;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class ColaTest {
    
    private Cola<String> cola;

    @BeforeEach
    void setUp() {
        cola = new Cola<>();
    }

    @Test
    void testColaVaciaInicial() {
        assertTrue(cola.estaVacia());
        assertEquals(0, cola.getCantidad());
    }

    @Test
    void testEncolar() {
        cola.encolar("A");
        assertFalse(cola.estaVacia());
        assertEquals(1, cola.getCantidad());
    }

    @Test
    void testEncolarMultiples() {
        cola.encolar("A");
        cola.encolar("B");
        cola.encolar("C");
        assertEquals(3, cola.getCantidad());
    }

    @Test
    void testDesencolar() {
        cola.encolar("A");
        cola.encolar("B");
        
        assertEquals("A", cola.desencolar());
        assertEquals(1, cola.getCantidad());
        assertEquals("B", cola.desencolar());
        assertEquals(0, cola.getCantidad());
    }

    @Test
    void testDesencolarColaVacia() {
        assertNull(cola.desencolar());
    }

    @Test
    void testVerFrente() {
        cola.encolar("A");
        cola.encolar("B");
        
        assertEquals("A", cola.verFrente());
        assertEquals(2, cola.getCantidad()); // No modifica la cola
    }

    @Test
    void testVerFrenteColaVacia() {
        assertNull(cola.verFrente());
    }

    @Test
    void testOrdenFIFO() {
        cola.encolar("Primero");
        cola.encolar("Segundo");
        cola.encolar("Tercero");
        
        assertEquals("Primero", cola.desencolar());
        assertEquals("Segundo", cola.desencolar());
        assertEquals("Tercero", cola.desencolar());
        assertTrue(cola.estaVacia());
    }

    @Test
    void testEncolarDesencolarIntercalado() {
        cola.encolar("A");
        cola.encolar("B");
        assertEquals("A", cola.desencolar());
        cola.encolar("C");
        assertEquals("B", cola.desencolar());
        assertEquals("C", cola.desencolar());
        assertTrue(cola.estaVacia());
    }

    @Test
    void testReferenciaFinSeActualiza() {
        cola.encolar("A");
        cola.desencolar();
        assertTrue(cola.estaVacia());
        
        cola.encolar("B");
        assertEquals("B", cola.verFrente());
        assertEquals(1, cola.getCantidad());
    }
}
