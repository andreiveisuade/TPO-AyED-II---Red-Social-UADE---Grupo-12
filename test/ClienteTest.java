package test;

import modelo.Cliente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class ClienteTest {
    
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("Alice", 95);
    }

    @Test
    void testCrearClienteValido() {
        assertEquals("Alice", cliente.getNombre());
        assertEquals(95, cliente.getScoring());
        assertEquals(0, cliente.getCantidadSiguiendo());
    }

    @Test
    void testCrearClienteNombreNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Cliente(null, 50));
    }

    @Test
    void testCrearClienteNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> new Cliente("", 50));
    }

    @Test
    void testCrearClienteScoringInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Cliente("Test", 150));
        assertThrows(IllegalArgumentException.class, () -> new Cliente("Test", -1));
    }

    @Test
    void testSeguirCliente() {
        assertTrue(cliente.seguir("Bob"));
        assertEquals(1, cliente.getCantidadSiguiendo());
        assertTrue(cliente.sigueA("Bob"));
    }

    @Test
    void testSeguirMaximoDos() {
        assertTrue(cliente.seguir("Bob"));
        assertTrue(cliente.seguir("Charlie"));
        assertFalse(cliente.seguir("David"));
        assertEquals(2, cliente.getCantidadSiguiendo());
    }

    @Test
    void testNoSeguirseASiMismo() {
        assertFalse(cliente.seguir("Alice"));
        assertEquals(0, cliente.getCantidadSiguiendo());
    }

    @Test
    void testNoSeguirDuplicado() {
        assertTrue(cliente.seguir("Bob"));
        assertFalse(cliente.seguir("Bob"));
        assertEquals(1, cliente.getCantidadSiguiendo());
    }

    @Test
    void testDejarDeSeguir() {
        cliente.seguir("Bob");
        assertTrue(cliente.dejarDeSeguir("Bob"));
        assertEquals(0, cliente.getCantidadSiguiendo());
        assertFalse(cliente.sigueA("Bob"));
    }

    @Test
    void testDejarDeSeguirInexistente() {
        assertFalse(cliente.dejarDeSeguir("Bob"));
    }
}
