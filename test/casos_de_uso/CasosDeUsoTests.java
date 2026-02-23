/*
SUITE MAESTRA DE CASOS DE USO

Ejecuta todos los tests de casos de uso del sistema de Red Social.
Cada test valida un flujo completo de usuario desde la perspectiva de un caso de uso.

Nomenclatura: CU_XXX_Test.java (ej: CU_001_IniciarSesion.java para "Iniciar Sesión")
*/

public class CasosDeUsoTests {

    private static int testsPasados = 0;
    private static int testsFallados = 0;

    public static void main(String[] args) {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("         SUITE COMPLETA DE CASOS DE USO - RED SOCIAL UADE");
        System.out.println("═".repeat(70) + "\n");

        System.out.println("GESTIÓN DE SESIÓN");
        System.out.println("─".repeat(70));
        ejecutarTest("CU_001_IniciarSesion");
        ejecutarTest("CU_002_CerrarSesion");

        System.out.println("\nITERACIÓN 1: CRUD BÁSICO");
        System.out.println("─".repeat(70));
        ejecutarTest("CU_003_AgregarCliente");
        ejecutarTest("CU_004_BuscarClientePorId");
        ejecutarTest("CU_005_BuscarClientesPorNombre");
        ejecutarTest("CU_006_BuscarClientesPorScoring");
        ejecutarTest("CU_007_ListarTodosClientes");
        ejecutarTest("CU_008_EliminarCliente");

        System.out.println("\nITERACIÓN 1: RELACIONES SIMPLES");
        System.out.println("─".repeat(70));
        ejecutarTest("CU_009_SeguidorUsuario");
        ejecutarTest("CU_010_DejarDeSeguir");
        ejecutarTest("CU_011_VerListaSeguidos");
        ejecutarTest("CU_012_VerListaSeguidores");

        System.out.println("\nITERACIÓN 1: SOLICITUDES DE SEGUIMIENTO");
        System.out.println("─".repeat(70));
        ejecutarTest("CU_013_EnviarSolicitud");
        ejecutarTest("CU_014_VerSolicitudPendiente");
        ejecutarTest("CU_015_AceptarSolicitud");
        ejecutarTest("CU_016_RechazarSolicitud");
        ejecutarTest("CU_017_VerCantidadSolicitudes");

        System.out.println("\nITERACIÓN 1: HISTORIAL Y UNDO/REDO");
        System.out.println("─".repeat(70));
        ejecutarTest("CU_018_VerHistorial");
        ejecutarTest("CU_019_DeshacerAccion");
        ejecutarTest("CU_020_ReHacerAccion");
        ejecutarTest("CU_021_LimpiarHistorial");

        System.out.println("\nITERACIÓN 2: CONSULTAS AVANZADAS");
        System.out.println("─".repeat(70));
        ejecutarTest("CU_022_ObtenerVecinos");
        ejecutarTest("CU_023_ConstruirArbolRelaciones");
        ejecutarTest("CU_024_ObtenerSeguidoresEnNivel");
        ejecutarTest("CU_025_ObtenerSeguidoresOrdenados");
        ejecutarTest("CU_026_ObtenerClientesCuartoNivel");
        ejecutarTest("CU_027_ConsultarInfluenciaPorScoring");

        System.out.println("\nITERACIÓN 3: RELACIONES BIDIRECCIONALES");
        System.out.println("─".repeat(70));
        ejecutarTest("CU_028_AgregarAmistadBidireccional");
        ejecutarTest("CU_029_EliminarAmistadBidireccional");
        ejecutarTest("CU_030_VerListaAmigos");
        ejecutarTest("CU_031_VerificarSiSonAmigos");
        ejecutarTest("CU_032_ObtenerCantidadAmigos");

        System.out.println("\nITERACIÓN 3: ANÁLISIS DE DISTANCIA");
        System.out.println("─".repeat(70));
        ejecutarTest("CU_033_CalcularDistancia");
        ejecutarTest("CU_034_EncontrarCaminoCorto");
        ejecutarTest("CU_035_VerificarConectividad");

        System.out.println("\nSISTEMA: PERSISTENCIA");
        System.out.println("─".repeat(70));
        ejecutarTest("CU_036_CargarClientesDesdeJson");
        ejecutarTest("CU_037_GuardarCambiosEnJson");
        ejecutarTest("CU_038_ValidarIntegridadDatos");

        // Resumen final
        System.out.println("\n" + "═".repeat(70));
        System.out.printf("RESULTADOS FINALES: %d pasados, %d fallados%n", testsPasados, testsFallados);
        System.out.println("═".repeat(70) + "\n");

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void ejecutarTest(String nombreClase) {
        try {
            Class<?> clazz = Class.forName(nombreClase);
            java.lang.reflect.Method main = clazz.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[]{});
            testsPasados++;
            System.out.println("[OK] " + nombreClase);
        } catch (Exception e) {
            testsFallados++;
            System.err.println("[FAIL] " + nombreClase + ": " + e.getMessage());
        }
    }
}
