import modelo.Cliente;

/**
 * Tests unitarios para la clase Cliente.
 *
 * Usa assertions nativas (ejecutar con java -ea ClienteTest).
 */
public class ClienteTest {

    private static int testsPasados = 0;
    private static int testsFallados = 0;

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("   TESTS DE CLIENTE                       ");
        System.out.println("═══════════════════════════════════════════\n");

        testCrearClienteValido();
        testCrearClienteNombreNulo();
        testCrearClienteNombreVacio();
        testCrearClienteScoringInvalido();
        testSeguirCliente();
        testSeguirMaximoDos();
        testNoSeguirseASiMismo();
        testNoSeguirDuplicado();
        testDejarDeSeguir();
        testDejarDeSeguirInexistente();
        testSeguidores();

        System.out.println("\n═══════════════════════════════════════════");
        System.out.printf("RESULTADOS: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("═══════════════════════════════════════════");

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void testCrearClienteValido() {
        try {
            Cliente cliente = new Cliente(1, "Alice", 95);
            assert "Alice".equals(cliente.getNombre()) : "Nombre debe ser Alice";
            assert cliente.getScoring() == 95 : "Scoring debe ser 95";
            assert cliente.getCantidadSiguiendo() == 0 : "No debe seguir a nadie inicialmente";
            assert cliente.getId() == 1 : "ID debe ser 1";

            reportarExito("Crear cliente válido");
        } catch (AssertionError e) {
            reportarFallo("Crear cliente válido", e.getMessage());
        }
    }

    private static void testCrearClienteNombreNulo() {
        try {
            boolean lanzoExcepcion = false;
            try {
                new Cliente(2, null, 50);
            } catch (IllegalArgumentException e) {
                lanzoExcepcion = true;
            }
            assert lanzoExcepcion : "Debe lanzar IllegalArgumentException con nombre null";

            reportarExito("Crear cliente con nombre nulo");
        } catch (AssertionError e) {
            reportarFallo("Crear cliente con nombre nulo", e.getMessage());
        }
    }

    private static void testCrearClienteNombreVacio() {
        try {
            boolean lanzoExcepcion = false;
            try {
                new Cliente(3, "", 50);
            } catch (IllegalArgumentException e) {
                lanzoExcepcion = true;
            }
            assert lanzoExcepcion : "Debe lanzar IllegalArgumentException con nombre vacío";

            reportarExito("Crear cliente con nombre vacío");
        } catch (AssertionError e) {
            reportarFallo("Crear cliente con nombre vacío", e.getMessage());
        }
    }

    private static void testCrearClienteScoringInvalido() {
        try {
            boolean lanzo150 = false;
            try {
                new Cliente(4, "Test", 150);
            } catch (IllegalArgumentException e) {
                lanzo150 = true;
            }
            assert lanzo150 : "Debe lanzar excepción con scoring 150";

            boolean lanzoNeg = false;
            try {
                new Cliente(5, "Test", -1);
            } catch (IllegalArgumentException e) {
                lanzoNeg = true;
            }
            assert lanzoNeg : "Debe lanzar excepción con scoring -1";

            reportarExito("Crear cliente con scoring inválido");
        } catch (AssertionError e) {
            reportarFallo("Crear cliente con scoring inválido", e.getMessage());
        }
    }

    private static void testSeguirCliente() {
        try {
            Cliente cliente = new Cliente(1, "Alice", 95);
            assert cliente.seguir(2) : "Debe poder seguir al ID 2";
            assert cliente.getCantidadSiguiendo() == 1 : "Cantidad siguiendo debe ser 1";
            assert cliente.sigueA(2) : "Debe seguir al ID 2";

            reportarExito("Seguir cliente");
        } catch (AssertionError e) {
            reportarFallo("Seguir cliente", e.getMessage());
        }
    }

    private static void testSeguirMaximoDos() {
        try {
            Cliente cliente = new Cliente(1, "Alice", 95);
            assert cliente.seguir(2) : "Primer seguido OK";
            assert cliente.seguir(3) : "Segundo seguido OK";
            assert !cliente.seguir(4) : "Tercer seguido debe fallar (MAX_SEGUIDOS=2)";
            assert cliente.getCantidadSiguiendo() == 2 : "Cantidad debe ser 2";

            reportarExito("Seguir máximo 2");
        } catch (AssertionError e) {
            reportarFallo("Seguir máximo 2", e.getMessage());
        }
    }

    private static void testNoSeguirseASiMismo() {
        try {
            Cliente cliente = new Cliente(1, "Alice", 95);
            assert !cliente.seguir(1) : "No debe poder seguirse a sí mismo";
            assert cliente.getCantidadSiguiendo() == 0 : "Cantidad debe ser 0";

            reportarExito("No seguirse a sí mismo");
        } catch (AssertionError e) {
            reportarFallo("No seguirse a sí mismo", e.getMessage());
        }
    }

    private static void testNoSeguirDuplicado() {
        try {
            Cliente cliente = new Cliente(1, "Alice", 95);
            assert cliente.seguir(2) : "Primera vez OK";
            assert !cliente.seguir(2) : "Duplicado debe fallar";
            assert cliente.getCantidadSiguiendo() == 1 : "Cantidad debe ser 1";

            reportarExito("No seguir duplicado");
        } catch (AssertionError e) {
            reportarFallo("No seguir duplicado", e.getMessage());
        }
    }

    private static void testDejarDeSeguir() {
        try {
            Cliente cliente = new Cliente(1, "Alice", 95);
            cliente.seguir(2);
            assert cliente.dejarDeSeguir(2) : "Debe poder dejar de seguir";
            assert cliente.getCantidadSiguiendo() == 0 : "Cantidad debe ser 0";
            assert !cliente.sigueA(2) : "Ya no debe seguir al ID 2";

            reportarExito("Dejar de seguir");
        } catch (AssertionError e) {
            reportarFallo("Dejar de seguir", e.getMessage());
        }
    }

    private static void testDejarDeSeguirInexistente() {
        try {
            Cliente cliente = new Cliente(1, "Alice", 95);
            assert !cliente.dejarDeSeguir(2) : "No debe poder dejar de seguir a alguien que no sigue";

            reportarExito("Dejar de seguir inexistente");
        } catch (AssertionError e) {
            reportarFallo("Dejar de seguir inexistente", e.getMessage());
        }
    }

    private static void testSeguidores() {
        try {
            Cliente cliente = new Cliente(1, "Alice", 95);
            assert cliente.getCantidadSeguidores() == 0 : "Inicialmente sin seguidores";

            cliente.agregarSeguidor(2);
            cliente.agregarSeguidor(3);
            assert cliente.getCantidadSeguidores() == 2 : "Debe tener 2 seguidores";

            cliente.eliminarSeguidor(2);
            assert cliente.getCantidadSeguidores() == 1 : "Debe tener 1 seguidor";

            reportarExito("Gestión de seguidores");
        } catch (AssertionError e) {
            reportarFallo("Gestión de seguidores", e.getMessage());
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
