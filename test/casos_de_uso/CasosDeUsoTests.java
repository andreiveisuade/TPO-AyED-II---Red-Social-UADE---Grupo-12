/*
SUITE MAESTRA DE CASOS DE USO

Ejecuta todos los tests de casos de uso del sistema de Red Social.
Cada test valida un flujo completo de usuario desde la perspectiva de un caso de uso.

Nomenclatura: CU_XXX_Test.java (ej: CU_001_IniciarSesion.java para "Iniciar Sesion")
*/

public class CasosDeUsoTests {

    private static int testsPasados = 0;
    private static int testsFallados = 0;

    public static void main(String[] args) {
        System.out.println("── Casos de Uso ─────────────────────────────────");

        seccion("GESTION DE SESION");
        ejecutarTest("CU_001_IniciarSesion");
        ejecutarTest("CU_002_CerrarSesion");

        seccion("ITERACION 1: CRUD BASICO");
        ejecutarTest("CU_003_AgregarCliente");
        ejecutarTest("CU_004_BuscarClientePorId");
        ejecutarTest("CU_005_BuscarClientesPorNombre");
        ejecutarTest("CU_006_BuscarClientesPorScoring");
        ejecutarTest("CU_007_ListarTodosClientes");
        ejecutarTest("CU_008_EliminarCliente");

        seccion("ITERACION 1: RELACIONES SIMPLES");
        ejecutarTest("CU_009_SeguidorUsuario");
        ejecutarTest("CU_010_DejarDeSeguir");
        ejecutarTest("CU_011_VerListaSeguidos");
        ejecutarTest("CU_012_VerListaSeguidores");

        seccion("ITERACION 1: SOLICITUDES");
        ejecutarTest("CU_013_EnviarSolicitud");
        ejecutarTest("CU_014_VerSolicitudPendiente");
        ejecutarTest("CU_015_AceptarSolicitud");
        ejecutarTest("CU_016_RechazarSolicitud");
        ejecutarTest("CU_017_VerCantidadSolicitudes");

        seccion("ITERACION 1: HISTORIAL Y UNDO/REDO");
        ejecutarTest("CU_018_VerHistorial");
        ejecutarTest("CU_019_DeshacerAccion");
        ejecutarTest("CU_020_ReHacerAccion");
        ejecutarTest("CU_021_LimpiarHistorial");

        seccion("ITERACION 2: CONSULTAS AVANZADAS");
        ejecutarTest("CU_022_ObtenerVecinos");
        ejecutarTest("CU_023_ConstruirArbolRelaciones");
        ejecutarTest("CU_024_ObtenerSeguidoresEnNivel");
        ejecutarTest("CU_025_ObtenerSeguidoresOrdenados");
        ejecutarTest("CU_026_ObtenerClientesCuartoNivel");
        ejecutarTest("CU_027_ConsultarInfluenciaPorScoring");

        seccion("ITERACION 3: RELACIONES BIDIRECCIONALES");
        ejecutarTest("CU_028_AgregarAmistadBidireccional");
        ejecutarTest("CU_029_EliminarAmistadBidireccional");
        ejecutarTest("CU_030_VerListaAmigos");
        ejecutarTest("CU_031_VerificarSiSonAmigos");
        ejecutarTest("CU_032_ObtenerCantidadAmigos");

        seccion("ITERACION 3: ANALISIS DE DISTANCIA");
        ejecutarTest("CU_033_CalcularDistancia");
        ejecutarTest("CU_034_EncontrarCaminoCorto");
        ejecutarTest("CU_035_VerificarConectividad");

        seccion("SISTEMA: PERSISTENCIA");
        ejecutarTest("CU_036_CargarClientesDesdeJson");
        ejecutarTest("CU_037_GuardarCambiosEnJson");
        ejecutarTest("CU_038_ValidarIntegridadDatos");

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void seccion(String nombre) {
        System.out.println("\n  " + nombre);
    }

    private static void ejecutarTest(String nombreClase) {
        try {
            Class<?> clazz = Class.forName(nombreClase);
            java.lang.reflect.Method main = clazz.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[]{});
            testsPasados++;
        } catch (Exception e) {
            testsFallados++;
            System.err.println("  [FAIL] " + nombreClase + ": " + e.getMessage());
        }
    }
}
