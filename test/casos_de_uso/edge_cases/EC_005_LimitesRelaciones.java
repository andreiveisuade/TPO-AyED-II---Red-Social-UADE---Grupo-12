import servicio.GestorClientes;
import modelo.Cliente;

/*
EC-005: LÍMITES DE RELACIONES

Valida los límites de negocio:
- MAX_SEGUIDOS = 2 (no se puede seguir a mas de 2)
- Seguidores: sin limite (cualquier cantidad puede seguirte)
- MAX_AMIGOS = 2 (no se puede tener mas de 2 amigos)
- Aceptar solicitud respeta MAX_SEGUIDOS
*/

public class EC_005_LimitesRelaciones {
    private static int testsPasados = 0, testsFallados = 0;
    private static final String TEST_DB = "data/clientes_EC005_TEST.json";

    private static void initTestDB() {
        try (java.io.FileWriter writer = new java.io.FileWriter(TEST_DB)) {
            writer.write("{ \"clientes\": [] }");
        } catch (java.io.IOException e) {}
    }

    public static void main(String[] args) {
        System.out.println("\n  EC-005: LIMITES DE RELACIONES");

        testSeguirExactamenteAlLimite();
        testSeguirSobreElLimite();
        testSeguidoresSinLimite();
        testAmistadExactamenteAlLimite();
        testAmistadSobreElLimite();
        testAmistadFallaNoCorrompeEstado();
        testSeguirLlenoYDejarDeSeguirLibera();
        testAmistadLlenaYEliminarLibera();
        testAceptarSolicitudConLimiteLleno();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) System.exit(1);
    }

    // Seguir exactamente a MAX_SEGUIDOS funciona
    private static void testSeguirExactamenteAlLimite() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);

            afirmar(gestor.seguir(a, b), "Seguir 1 de 2 debe funcionar");
            afirmar(gestor.seguir(a, c), "Seguir 2 de 2 debe funcionar");
            afirmar(gestor.buscarPorId(a).getCantidadSiguiendo() == 2, "Debe tener exactamente 2 seguidos");

            reportarExito("Seguir al limite (" + Cliente.MAX_SEGUIDOS + ")");
        } catch (Exception e) {
            reportarFallo("Seguir al limite", e.getMessage());
        }
    }

    // No se puede seguir a un tercero
    private static void testSeguirSobreElLimite() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);

            gestor.seguir(a, b);
            gestor.seguir(a, c);
            afirmar(!gestor.seguir(a, d), "Tercero debe fallar");
            afirmar(gestor.buscarPorId(a).getCantidadSiguiendo() == 2, "Sigue teniendo 2");
            afirmar(gestor.buscarPorId(d).getCantidadSeguidores() == 0, "Diana no gano seguidor");

            reportarExito("Seguir sobre el limite rechazado");
        } catch (Exception e) {
            reportarFallo("Seguir sobre el limite", e.getMessage());
        }
    }

    // Muchos usuarios pueden seguir al mismo cliente
    private static void testSeguidoresSinLimite() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int objetivo = gestor.agregarCliente("Popular", 90);
            int cantidad = 20;

            for (int i = 0; i < cantidad; i++) {
                int seguidor = gestor.agregarCliente("Fan" + i, 50);
                afirmar(gestor.seguir(seguidor, objetivo), "Fan" + i + " debe poder seguir");
            }

            afirmar(gestor.buscarPorId(objetivo).getCantidadSeguidores() == cantidad,
                "Debe tener " + cantidad + " seguidores");

            reportarExito("Seguidores sin limite (" + cantidad + ")");
        } catch (Exception e) {
            reportarFallo("Seguidores sin limite", e.getMessage());
        }
    }

    // Amistad exactamente al limite funciona
    private static void testAmistadExactamenteAlLimite() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);

            afirmar(gestor.agregarAmistad(a, b), "Amistad 1 de 2 debe funcionar");
            afirmar(gestor.agregarAmistad(a, c), "Amistad 2 de 2 debe funcionar");
            afirmar(gestor.obtenerCantidadAmigos(a) == 2, "Debe tener exactamente 2 amigos");

            reportarExito("Amistad al limite (" + Cliente.MAX_AMIGOS + ")");
        } catch (Exception e) {
            reportarFallo("Amistad al limite", e.getMessage());
        }
    }

    // No se puede agregar un tercer amigo
    private static void testAmistadSobreElLimite() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);

            gestor.agregarAmistad(a, b);
            gestor.agregarAmistad(a, c);
            afirmar(!gestor.agregarAmistad(a, d), "3ra amistad debe fallar");
            afirmar(!gestor.sonAmigos(a, d), "A y D no deben ser amigos");
            afirmar(gestor.obtenerCantidadAmigos(d) == 0, "D no gano amigo");

            reportarExito("Amistad sobre el limite rechazada");
        } catch (Exception e) {
            reportarFallo("Amistad sobre el limite", e.getMessage());
        }
    }

    // Si la amistad falla, ninguno de los dos se modifica
    private static void testAmistadFallaNoCorrompeEstado() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);

            // B llena sus amigos
            gestor.agregarAmistad(b, c);
            gestor.agregarAmistad(b, d);

            int amigosA = gestor.obtenerCantidadAmigos(a);
            int amigosB = gestor.obtenerCantidadAmigos(b);

            // A intenta, debe fallar por B lleno
            gestor.agregarAmistad(a, b);

            afirmar(gestor.obtenerCantidadAmigos(a) == amigosA, "A no cambio");
            afirmar(gestor.obtenerCantidadAmigos(b) == amigosB, "B no cambio");

            reportarExito("Fallo no corrompe estado");
        } catch (Exception e) {
            reportarFallo("Fallo no corrompe estado", e.getMessage());
        }
    }

    // Dejar de seguir libera cupo para seguir a otro
    private static void testSeguirLlenoYDejarDeSeguirLibera() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);

            gestor.seguir(a, b);
            gestor.seguir(a, c);
            afirmar(!gestor.seguir(a, d), "Lleno, no puede");

            // Deja de seguir a Bob
            gestor.dejarDeSeguir(a, b);
            afirmar(gestor.buscarPorId(a).getCantidadSiguiendo() == 1, "Ahora tiene 1");

            // Ahora puede seguir a Diana
            afirmar(gestor.seguir(a, d), "Debe poder seguir tras liberar cupo");
            afirmar(gestor.buscarPorId(a).getCantidadSiguiendo() == 2, "Vuelve a 2");

            reportarExito("Dejar de seguir libera cupo");
        } catch (Exception e) {
            reportarFallo("Dejar de seguir libera cupo", e.getMessage());
        }
    }

    // Eliminar amistad libera cupo para otra amistad
    private static void testAmistadLlenaYEliminarLibera() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);

            gestor.agregarAmistad(a, b);
            gestor.agregarAmistad(a, c);
            afirmar(!gestor.agregarAmistad(a, d), "Lleno, no puede");

            // Elimina amistad con Bob
            gestor.eliminarAmistad(a, b);
            afirmar(gestor.obtenerCantidadAmigos(a) == 1, "Ahora tiene 1");

            // Ahora puede con Diana
            afirmar(gestor.agregarAmistad(a, d), "Debe poder tras liberar cupo");
            afirmar(gestor.obtenerCantidadAmigos(a) == 2, "Vuelve a 2");

            reportarExito("Eliminar amistad libera cupo");
        } catch (Exception e) {
            reportarFallo("Eliminar amistad libera cupo", e.getMessage());
        }
    }

    // Aceptar solicitud cuando ya se tiene MAX_SEGUIDOS debe fallar
    private static void testAceptarSolicitudConLimiteLleno() {
        try {
            initTestDB();
            GestorClientes gestor = new GestorClientes(TEST_DB);
            int a = gestor.agregarCliente("Alice", 80);
            int b = gestor.agregarCliente("Bob", 70);
            int c = gestor.agregarCliente("Charlie", 60);
            int d = gestor.agregarCliente("Diana", 50);

            // Alice ya sigue a 2 personas
            gestor.seguir(a, b);
            gestor.seguir(a, c);
            afirmar(gestor.buscarPorId(a).getCantidadSiguiendo() == 2, "Alice llena");

            // Diana envia solicitud a Alice, Alice la acepta -> Alice seguiria a Diana
            // Pero Alice ya esta llena, seguir() debe retornar false
            boolean resultado = gestor.seguir(a, d);
            afirmar(!resultado, "No debe poder seguir con limite lleno");
            afirmar(gestor.buscarPorId(a).getCantidadSiguiendo() == 2, "Sigue con 2");

            reportarExito("Aceptar solicitud con limite lleno");
        } catch (Exception e) {
            reportarFallo("Aceptar solicitud con limite lleno", e.getMessage());
        }
    }

    private static void afirmar(boolean cond, String msg) throws Exception { if (!cond) throw new Exception(msg); }
    private static void reportarExito(String t) { testsPasados++; System.out.println("  [OK] " + t); }
    private static void reportarFallo(String t, String e) { testsFallados++; System.err.println("  [FAIL] " + t + ": " + e); }
}
