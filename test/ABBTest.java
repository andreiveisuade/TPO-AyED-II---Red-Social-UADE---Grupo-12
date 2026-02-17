import tda.*;
import modelo.*;

/**
 * Tests unitarios para Árbol Binario de Búsqueda (ABB).
 * 
 * Para ejecutar: compilar y ejecutar este archivo.
 * Los tests usan assertions - ejecutar con java -ea ABBTest
 */
public class ABBTest {

    private static int testsPasados = 0;
    private static int testsFallados = 0;

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("   TESTS DE ÁRBOL BINARIO DE BÚSQUEDA    ");
        System.out.println("═══════════════════════════════════════════\n");

        testInsertarBuscarBasico();
        testDuplicados();
        testBuscarInexistente();
        testObtenerEnNivel();
        testEliminarSinHijos();
        testEliminarConUnHijo();
        testEliminarConDosHijos();
        testEliminarConDuplicados();
        testArbolVacio();
        testAltura();

        System.out.println("\n═══════════════════════════════════════════");
        System.out.printf("RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("═══════════════════════════════════════════");

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TESTS BÁSICOS
    // ═══════════════════════════════════════════════════════════════════

    private static void testInsertarBuscarBasico() {
        try {
            ArbolBinarioBusqueda<Integer, Cliente> abb = new ArbolBinarioBusqueda<>();
            Cliente c1 = new Cliente(1, "Alice", 80);
            Cliente c2 = new Cliente(2, "Bob", 90);
            Cliente c3 = new Cliente(3, "Carol", 70);

            abb.insertar(80, c1);
            abb.insertar(90, c2);
            abb.insertar(70, c3);

            assert abb.getCantidad() == 3 : "Debe tener 3 elementos";

            Object[] resultado80 = abb.buscar(80);
            assert resultado80.length == 1 : "Debe encontrar 1 con scoring 80";
            assert ((Cliente)resultado80[0]).getNombre().equals("Alice");

            Object[] resultado90 = abb.buscar(90);
            assert resultado90.length == 1 : "Debe encontrar 1 con scoring 90";
            assert ((Cliente)resultado90[0]).getNombre().equals("Bob");

            reportarExito("ABB - Inserción y búsqueda básica");
        } catch (AssertionError e) {
            reportarFallo("ABB - Inserción y búsqueda básica", e.getMessage());
        }
    }

    private static void testDuplicados() {
        try {
            ArbolBinarioBusqueda<Integer, Cliente> abb = new ArbolBinarioBusqueda<>();
            Cliente c1 = new Cliente(1, "User1", 75);
            Cliente c2 = new Cliente(2, "User2", 75);
            Cliente c3 = new Cliente(3, "User3", 75);

            abb.insertar(75, c1);
            abb.insertar(75, c2);
            abb.insertar(75, c3);

            assert abb.getCantidad() == 3 : "Debe tener 3 elementos";

            Object[] resultados = abb.buscar(75);
            assert resultados.length == 3 : "Debe encontrar los 3 con scoring 75, encontró: " + resultados.length;

            reportarExito("ABB - Manejo de duplicados");
        } catch (AssertionError e) {
            reportarFallo("ABB - Manejo de duplicados", e.getMessage());
        }
    }

    private static void testBuscarInexistente() {
        try {
            ArbolBinarioBusqueda<Integer, Cliente> abb = new ArbolBinarioBusqueda<>();
            abb.insertar(50, new Cliente(1, "Test", 50));

            Object[] resultado = abb.buscar(99);
            assert resultado.length == 0 : "Buscar inexistente debe retornar array vacío";

            reportarExito("ABB - Buscar inexistente");
        } catch (AssertionError e) {
            reportarFallo("ABB - Buscar inexistente", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TESTS DE NIVELES
    // ═══════════════════════════════════════════════════════════════════

    private static void testObtenerEnNivel() {
        try {
            ArbolBinarioBusqueda<Integer, Cliente> abb = new ArbolBinarioBusqueda<>();
            
            // Construir árbol:
            //        50
            //       /  \
            //      30   70
            //     / \   / \
            //    20 40 60 80
            
            abb.insertar(50, new Cliente(1, "Root", 50));
            abb.insertar(30, new Cliente(2, "L", 30));
            abb.insertar(70, new Cliente(3, "R", 70));
            abb.insertar(20, new Cliente(4, "LL", 20));
            abb.insertar(40, new Cliente(5, "LR", 40));
            abb.insertar(60, new Cliente(6, "RL", 60));
            abb.insertar(80, new Cliente(7, "RR", 80));

            // Nivel 0: Root (1 nodo)
            Object[] nivel0 = abb.obtenerEnNivel(0);
            assert nivel0.length == 1 : "Nivel 0 debe tener 1 nodo";
            assert ((Cliente)nivel0[0]).getScoring() == 50;

            // Nivel 1: L y R (2 nodos)
            Object[] nivel1 = abb.obtenerEnNivel(1);
            assert nivel1.length == 2 : "Nivel 1 debe tener 2 nodos";

            // Nivel 2: LL, LR, RL, RR (4 nodos)
            Object[] nivel2 = abb.obtenerEnNivel(2);
            assert nivel2.length == 4 : "Nivel 2 debe tener 4 nodos";

            // Nivel 3: no existe
            Object[] nivel3 = abb.obtenerEnNivel(3);
            assert nivel3.length == 0 : "Nivel 3 no debe tener nodos";

            reportarExito("ABB - Obtener nodos por nivel");
        } catch (AssertionError e) {
            reportarFallo("ABB - Obtener nodos por nivel", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TESTS DE ELIMINACIÓN
    // ═══════════════════════════════════════════════════════════════════

    private static void testEliminarSinHijos() {
        try {
            ArbolBinarioBusqueda<Integer, Cliente> abb = new ArbolBinarioBusqueda<>();
            Cliente hoja = new Cliente(1, "Hoja", 20);
            
            abb.insertar(50, new Cliente(2, "Root", 50));
            abb.insertar(30, new Cliente(3, "L", 30));
            abb.insertar(20, hoja);

            assert abb.getCantidad() == 3;
            
            boolean eliminado = abb.eliminar(20, hoja);
            assert eliminado : "Debe eliminar correctamente";
            assert abb.getCantidad() == 2 : "Cantidad debe ser 2";
            assert abb.buscar(20).length == 0 : "No debe encontrar 20";

            reportarExito("ABB - Eliminar nodo sin hijos");
        } catch (AssertionError e) {
            reportarFallo("ABB - Eliminar nodo sin hijos", e.getMessage());
        }
    }

    private static void testEliminarConUnHijo() {
        try {
            ArbolBinarioBusqueda<Integer, Cliente> abb = new ArbolBinarioBusqueda<>();
            Cliente nodo30 = new Cliente(1, "Nodo30", 30);
            
            abb.insertar(50, new Cliente(2, "Root", 50));
            abb.insertar(30, nodo30);
            abb.insertar(20, new Cliente(3, "Hijo", 20));

            boolean eliminado = abb.eliminar(30, nodo30);
            assert eliminado : "Debe eliminar correctamente";
            assert abb.getCantidad() == 2;
            assert abb.buscar(20).length == 1 : "Hijo 20 debe seguir existiendo";

            reportarExito("ABB - Eliminar nodo con un hijo");
        } catch (AssertionError e) {
            reportarFallo("ABB - Eliminar nodo con un hijo", e.getMessage());
        }
    }

    private static void testEliminarConDosHijos() {
        try {
            ArbolBinarioBusqueda<Integer, Cliente> abb = new ArbolBinarioBusqueda<>();
            Cliente raiz = new Cliente(1, "Root", 50);
            
            abb.insertar(50, raiz);
            abb.insertar(30, new Cliente(2, "L", 30));
            abb.insertar(70, new Cliente(3, "R", 70));

            boolean eliminado = abb.eliminar(50, raiz);
            assert eliminado : "Debe eliminar correctamente";
            assert abb.getCantidad() == 2;
            assert abb.buscar(30).length == 1 : "Nodo 30 debe seguir";
            assert abb.buscar(70).length == 1 : "Nodo 70 debe seguir";

            reportarExito("ABB - Eliminar nodo con dos hijos");
        } catch (AssertionError e) {
            reportarFallo("ABB - Eliminar nodo con dos hijos", e.getMessage());
        }
    }

    private static void testEliminarConDuplicados() {
        try {
            ArbolBinarioBusqueda<Integer, Cliente> abb = new ArbolBinarioBusqueda<>();
            Cliente c1 = new Cliente(1, "User1", 80);
            Cliente c2 = new Cliente(2, "User2", 80);
            Cliente c3 = new Cliente(3, "User3", 80);

            abb.insertar(80, c1);
            abb.insertar(80, c2);
            abb.insertar(80, c3);

            // Eliminar solo c2
            boolean eliminado = abb.eliminar(80, c2);
            assert eliminado : "Debe eliminar c2";
            assert abb.getCantidad() == 2 : "Debe quedar 2 elementos";

            Object[] resultados = abb.buscar(80);
            assert resultados.length == 2 : "Debe encontrar 2 con scoring 80";

            reportarExito("ABB - Eliminar con duplicados");
        } catch (AssertionError e) {
            reportarFallo("ABB - Eliminar con duplicados", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TESTS DE CASOS BORDE
    // ═══════════════════════════════════════════════════════════════════

    private static void testArbolVacio() {
        try {
            ArbolBinarioBusqueda<Integer, Cliente> abb = new ArbolBinarioBusqueda<>();
            
            assert abb.estaVacio() : "Árbol nuevo debe estar vacío";
            assert abb.getCantidad() == 0 : "Cantidad debe ser 0";
            assert abb.getAltura() == -1 : "Altura de árbol vacío debe ser -1";
            assert abb.buscar(50).length == 0 : "Buscar en vacío retorna array vacío";
            assert abb.obtenerEnNivel(0).length == 0 : "Nivel 0 de árbol vacío está vacío";

            reportarExito("ABB - Árbol vacío");
        } catch (AssertionError e) {
            reportarFallo("ABB - Árbol vacío", e.getMessage());
        }
    }

    private static void testAltura() {
        try {
            ArbolBinarioBusqueda<Integer, Cliente> abb = new ArbolBinarioBusqueda<>();
            
            assert abb.getAltura() == -1 : "Árbol vacío: altura -1";
            
            abb.insertar(50, new Cliente(1, "Root", 50));
            assert abb.getAltura() == 0 : "Solo raíz: altura 0";
            
            abb.insertar(30, new Cliente(2, "L", 30));
            abb.insertar(70, new Cliente(3, "R", 70));
            assert abb.getAltura() == 1 : "Con 2 niveles: altura 1";
            
            abb.insertar(20, new Cliente(4, "LL", 20));
            assert abb.getAltura() == 2 : "Con 3 niveles: altura 2";

            reportarExito("ABB - Cálculo de altura");
        } catch (AssertionError e) {
            reportarFallo("ABB - Cálculo de altura", e.getMessage());
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
