package test;

import tda.Diccionario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class DiccionarioTest {
    
    private Diccionario<String, Integer> diccionario;

    @BeforeEach
    void setUp() {
        diccionario = new Diccionario<>();
    }

    @Test
    void testDiccionarioVacioInicial() {
        assertTrue(diccionario.estaVacio());
        assertEquals(0, diccionario.getCantidad());
    }

    @Test
    void testInsertar() {
        diccionario.insertar("clave1", 100);
        assertFalse(diccionario.estaVacio());
        assertEquals(1, diccionario.getCantidad());
    }

    @Test
    void testInsertarMultiples() {
        diccionario.insertar("a", 1);
        diccionario.insertar("b", 2);
        diccionario.insertar("c", 3);
        assertEquals(3, diccionario.getCantidad());
    }

    @Test
    void testObtener() {
        diccionario.insertar("clave", 42);
        assertEquals(42, diccionario.obtener("clave"));
    }

    @Test
    void testObtenerInexistente() {
        assertNull(diccionario.obtener("noExiste"));
    }

    @Test
    void testContiene() {
        diccionario.insertar("existe", 1);
        assertTrue(diccionario.contiene("existe"));
        assertFalse(diccionario.contiene("noExiste"));
    }

    @Test
    void testActualizarValor() {
        diccionario.insertar("clave", 100);
        diccionario.insertar("clave", 200); // Actualiza
        
        assertEquals(200, diccionario.obtener("clave"));
        assertEquals(1, diccionario.getCantidad());
    }

    @Test
    void testEliminar() {
        diccionario.insertar("a", 1);
        diccionario.insertar("b", 2);
        
        Integer eliminado = diccionario.eliminar("a");
        assertEquals(1, eliminado);
        assertEquals(1, diccionario.getCantidad());
        assertFalse(diccionario.contiene("a"));
    }

    @Test
    void testEliminarInexistente() {
        assertNull(diccionario.eliminar("noExiste"));
    }

    @Test
    void testObtenerClaves() {
        diccionario.insertar("a", 1);
        diccionario.insertar("b", 2);
        diccionario.insertar("c", 3);
        
        String[] claves = diccionario.obtenerClaves();
        assertEquals(3, claves.length);
    }
    
    @Test
    void testObtenerValores() {
        diccionario.insertar("a", 1);
        diccionario.insertar("b", 2);
        
        Object[] valores = diccionario.obtenerValores();
        assertEquals(2, valores.length);
        
        boolean tiene1 = false, tiene2 = false;
        for(Object v : valores) {
            if((Integer)v == 1) tiene1 = true;
            if((Integer)v == 2) tiene2 = true;
        }
        assertTrue(tiene1 && tiene2);
    }

    @Test
    void testColisionesHash() {
        // Insertar muchos elementos para forzar colisiones
        for (int i = 0; i < 20; i++) {
            diccionario.insertar("clave" + i, i);
        }
        
        assertEquals(20, diccionario.getCantidad());
        
        for (int i = 0; i < 20; i++) {
            assertEquals(i, diccionario.obtener("clave" + i));
        }
    }

    @Test
    void testEliminarConColisiones() {
        for (int i = 0; i < 10; i++) {
            diccionario.insertar("clave" + i, i);
        }
        
        diccionario.eliminar("clave5");
        assertEquals(9, diccionario.getCantidad());
        assertNull(diccionario.obtener("clave5"));
        
        // Verificar que otros siguen accesibles
        assertEquals(0, diccionario.obtener("clave0"));
        assertEquals(9, diccionario.obtener("clave9"));
    }
    
    @Test
    void testVaciarDiccionario() {
        // Caso borde: Llenar y vaciar completamente para probar todos los casos de borrado en buckets
        int cantidad = 100;
        for(int i=0; i<cantidad; i++) {
            diccionario.insertar("k"+i, i);
        }
        assertEquals(cantidad, diccionario.getCantidad());
        
        for(int i=0; i<cantidad; i++) {
            assertNotNull(diccionario.eliminar("k"+i));
        }
        
        assertTrue(diccionario.estaVacio());
        assertEquals(0, diccionario.getCantidad());
    }
}
