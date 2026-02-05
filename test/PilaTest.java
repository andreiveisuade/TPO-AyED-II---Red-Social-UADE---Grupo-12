package test;

import tda.Pila;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class PilaTest {
    
    private Pila<String> pila;

    @BeforeEach
    void setUp() {
        pila = new Pila<>();
    }

    @Test
    void testPilaVaciaInicial() {
        assertTrue(pila.estaVacia());
        assertEquals(0, pila.getCantidad());
    }

    @Test
    void testApilar() {
        pila.apilar("A");
        assertFalse(pila.estaVacia());
        assertEquals(1, pila.getCantidad());
    }

    @Test
    void testApilarMultiples() {
        pila.apilar("A");
        pila.apilar("B");
        pila.apilar("C");
        assertEquals(3, pila.getCantidad());
    }

    @Test
    void testDesapilar() {
        pila.apilar("A");
        pila.apilar("B");
        
        assertEquals("B", pila.desapilar());
        assertEquals(1, pila.getCantidad());
        assertEquals("A", pila.desapilar());
        assertEquals(0, pila.getCantidad());
    }

    @Test
    void testDesapilarPilaVacia() {
        assertNull(pila.desapilar());
    }

    @Test
    void testVerTope() {
        pila.apilar("A");
        pila.apilar("B");
        
        assertEquals("B", pila.verTope());
        assertEquals(2, pila.getCantidad()); // No modifica la pila
    }

    @Test
    void testVerTopePilaVacia() {
        assertNull(pila.verTope());
    }

    @Test
    void testOrdenLIFO() {
        pila.apilar("Primero");
        pila.apilar("Segundo");
        pila.apilar("Tercero");
        
        assertEquals("Tercero", pila.desapilar());
        assertEquals("Segundo", pila.desapilar());
        assertEquals("Primero", pila.desapilar());
        assertTrue(pila.estaVacia());
    }

    @Test
    void testApilarDesapilarIntercalado() {
        pila.apilar("A");
        pila.apilar("B");
        assertEquals("B", pila.desapilar());
        pila.apilar("C");
        assertEquals("C", pila.desapilar());
        assertEquals("A", pila.desapilar());
        assertTrue(pila.estaVacia());
    }
}
