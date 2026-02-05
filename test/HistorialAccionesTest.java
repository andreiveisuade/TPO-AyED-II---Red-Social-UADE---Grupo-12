package test;

import servicio.HistorialAcciones;
import modelo.Accion;
import modelo.TipoAccion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class HistorialAccionesTest {
    
    private HistorialAcciones historial;

    @BeforeEach
    void setUp() {
        historial = new HistorialAcciones();
    }

    @Test
    void testHistorialVacioInicial() {
        assertTrue(historial.estaVacio());
        assertEquals(0, historial.getCantidad());
    }

    @Test
    void testRegistrarAccion() {
        Accion accion = new Accion(TipoAccion.AGREGAR_CLIENTE, "Alice", "95");
        historial.registrar(accion);
        
        assertFalse(historial.estaVacio());
        assertEquals(1, historial.getCantidad());
    }

    @Test
    void testRegistrarAccionNula() {
        historial.registrar(null);
        assertTrue(historial.estaVacio());
    }

    @Test
    void testVerUltima() {
        Accion accion1 = new Accion(TipoAccion.AGREGAR_CLIENTE, "Alice", "95");
        Accion accion2 = new Accion(TipoAccion.SEGUIR, "Alice", "Bob");
        
        historial.registrar(accion1);
        historial.registrar(accion2);
        
        Accion ultima = historial.verUltima();
        assertEquals(TipoAccion.SEGUIR, ultima.getTipo());
        assertEquals(2, historial.getCantidad()); // No modifica
    }

    @Test
    void testVerUltimaVacio() {
        assertNull(historial.verUltima());
    }

    @Test
    void testExtraerUltima() {
        Accion accion1 = new Accion(TipoAccion.AGREGAR_CLIENTE, "Alice", "95");
        Accion accion2 = new Accion(TipoAccion.SEGUIR, "Alice", "Bob");
        
        historial.registrar(accion1);
        historial.registrar(accion2);
        
        Accion extraida = historial.extraerUltima();
        assertEquals(TipoAccion.SEGUIR, extraida.getTipo());
        assertEquals(1, historial.getCantidad());
    }

    @Test
    void testExtraerUltimaVacio() {
        assertNull(historial.extraerUltima());
    }

    @Test
    void testOrdenLIFO() {
        historial.registrar(new Accion(TipoAccion.AGREGAR_CLIENTE, "Alice", "95"));
        historial.registrar(new Accion(TipoAccion.AGREGAR_CLIENTE, "Bob", "88"));
        historial.registrar(new Accion(TipoAccion.SEGUIR, "Alice", "Bob"));
        
        assertEquals(TipoAccion.SEGUIR, historial.extraerUltima().getTipo());
        assertEquals("Bob", historial.extraerUltima().getDatos()[0]);
        assertEquals("Alice", historial.extraerUltima().getDatos()[0]);
        assertTrue(historial.estaVacio());
    }

    @Test
    void testObtenerTodas() {
        historial.registrar(new Accion(TipoAccion.AGREGAR_CLIENTE, "Alice", "95"));
        historial.registrar(new Accion(TipoAccion.AGREGAR_CLIENTE, "Bob", "88"));
        historial.registrar(new Accion(TipoAccion.SEGUIR, "Alice", "Bob"));
        
        Accion[] todas = historial.obtenerTodas();
        
        assertEquals(3, todas.length);
        assertEquals(TipoAccion.SEGUIR, todas[0].getTipo()); // Más reciente primero
        assertEquals(3, historial.getCantidad()); // No modifica historial original
    }

    @Test
    void testObtenerTodasVacio() {
        Accion[] todas = historial.obtenerTodas();
        assertEquals(0, todas.length);
    }
}
