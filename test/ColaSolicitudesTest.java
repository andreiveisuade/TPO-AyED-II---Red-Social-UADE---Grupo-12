import modelo.Cliente;
import modelo.SolicitudSeguimiento;

/**
 * Tests unitarios para el sistema de solicitudes de seguimiento.
 *
 * Testea la cola de solicitudes integrada en Cliente:
 * recibirSolicitud(), procesarSiguienteSolicitud(), etc.
 * Usa assertions nativas (ejecutar con java -ea ColaSolicitudesTest).
 */
public class ColaSolicitudesTest {

    private static int testsPasados = 0;
    private static int testsFallados = 0;

    public static void main(String[] args) {
        System.out.println("── ColaSolicitudesTest ──────────────────────────");

        testRecibirSolicitud();
        testProcesarSolicitudFIFO();
        testColaVacia();
        testSolicitudesPendientes();
        testVerSiguienteSinProcesar();
        testMultiplesSolicitudes();

        System.out.printf("%n  %d pasados, %d fallados%n", testsPasados, testsFallados);

        if (testsFallados > 0) {
            System.exit(1);
        }
    }

    private static void testRecibirSolicitud() {
        try {
            Cliente bob = new Cliente(2, "Bob", 80);
            SolicitudSeguimiento solicitud = new SolicitudSeguimiento("1", "2");

            bob.recibirSolicitud(solicitud);
            assert bob.tieneSolicitudesPendientes() : "Debe tener solicitudes pendientes";
            assert bob.getCantidadSolicitudesPendientes() == 1 : "Debe tener 1 solicitud";

            reportarExito("Recibir solicitud");
        } catch (AssertionError e) {
            reportarFallo("Recibir solicitud", e.getMessage());
        }
    }

    private static void testProcesarSolicitudFIFO() {
        try {
            Cliente bob = new Cliente(2, "Bob", 80);

            SolicitudSeguimiento sol1 = new SolicitudSeguimiento("1", "2"); // Alice -> Bob
            SolicitudSeguimiento sol2 = new SolicitudSeguimiento("3", "2"); // Carol -> Bob
            SolicitudSeguimiento sol3 = new SolicitudSeguimiento("4", "2"); // Dave -> Bob

            bob.recibirSolicitud(sol1);
            bob.recibirSolicitud(sol2);
            bob.recibirSolicitud(sol3);

            assert bob.getCantidadSolicitudesPendientes() == 3 : "Debe tener 3 solicitudes";

            // Procesar en orden FIFO
            SolicitudSeguimiento primera = bob.procesarSiguienteSolicitud();
            assert primera != null : "Primera no debe ser null";
            assert "1".equals(primera.getSolicitante()) : "Primera debe ser de Alice (ID 1)";

            SolicitudSeguimiento segunda = bob.procesarSiguienteSolicitud();
            assert segunda != null : "Segunda no debe ser null";
            assert "3".equals(segunda.getSolicitante()) : "Segunda debe ser de Carol (ID 3)";

            SolicitudSeguimiento tercera = bob.procesarSiguienteSolicitud();
            assert tercera != null : "Tercera no debe ser null";
            assert "4".equals(tercera.getSolicitante()) : "Tercera debe ser de Dave (ID 4)";

            assert bob.getCantidadSolicitudesPendientes() == 0 : "No deben quedar solicitudes";

            reportarExito("Procesar solicitudes FIFO");
        } catch (AssertionError e) {
            reportarFallo("Procesar solicitudes FIFO", e.getMessage());
        }
    }

    private static void testColaVacia() {
        try {
            Cliente bob = new Cliente(2, "Bob", 80);

            assert !bob.tieneSolicitudesPendientes() : "Sin solicitudes inicialmente";
            assert bob.getCantidadSolicitudesPendientes() == 0 : "Cantidad debe ser 0";

            SolicitudSeguimiento resultado = bob.procesarSiguienteSolicitud();
            assert resultado == null : "Procesar cola vacía debe retornar null";

            SolicitudSeguimiento ver = bob.verSiguienteSolicitud();
            assert ver == null : "Ver cola vacía debe retornar null";

            reportarExito("Cola vacía");
        } catch (AssertionError e) {
            reportarFallo("Cola vacía", e.getMessage());
        }
    }

    private static void testSolicitudesPendientes() {
        try {
            Cliente bob = new Cliente(2, "Bob", 80);

            bob.recibirSolicitud(new SolicitudSeguimiento("1", "2"));
            bob.recibirSolicitud(new SolicitudSeguimiento("3", "2"));

            assert bob.tieneSolicitudesPendientes() : "Debe tener pendientes";
            assert bob.getCantidadSolicitudesPendientes() == 2 : "Debe tener 2";

            bob.procesarSiguienteSolicitud();
            assert bob.getCantidadSolicitudesPendientes() == 1 : "Debe quedar 1";

            bob.procesarSiguienteSolicitud();
            assert !bob.tieneSolicitudesPendientes() : "Ya no debe tener pendientes";

            reportarExito("Solicitudes pendientes (conteo)");
        } catch (AssertionError e) {
            reportarFallo("Solicitudes pendientes (conteo)", e.getMessage());
        }
    }

    private static void testVerSiguienteSinProcesar() {
        try {
            Cliente bob = new Cliente(2, "Bob", 80);
            bob.recibirSolicitud(new SolicitudSeguimiento("1", "2"));

            // Ver sin procesar no debe modificar la cola
            SolicitudSeguimiento vista = bob.verSiguienteSolicitud();
            assert vista != null : "Debe retornar solicitud";
            assert "1".equals(vista.getSolicitante()) : "Solicitante debe ser 1";
            assert bob.getCantidadSolicitudesPendientes() == 1 : "Cantidad no debe cambiar";

            // Segundo ver debe retornar lo mismo
            SolicitudSeguimiento vista2 = bob.verSiguienteSolicitud();
            assert "1".equals(vista2.getSolicitante()) : "Sigue siendo la misma solicitud";

            reportarExito("Ver siguiente sin procesar");
        } catch (AssertionError e) {
            reportarFallo("Ver siguiente sin procesar", e.getMessage());
        }
    }

    private static void testMultiplesSolicitudes() {
        try {
            Cliente bob = new Cliente(2, "Bob", 80);

            // Recibir, procesar, y volver a recibir
            bob.recibirSolicitud(new SolicitudSeguimiento("1", "2"));
            bob.procesarSiguienteSolicitud();

            // Puede volver a recibir del mismo solicitante
            bob.recibirSolicitud(new SolicitudSeguimiento("1", "2"));
            assert bob.getCantidadSolicitudesPendientes() == 1 : "Debe tener 1 solicitud";

            reportarExito("Múltiples solicitudes (re-agregar)");
        } catch (AssertionError e) {
            reportarFallo("Múltiples solicitudes (re-agregar)", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // UTILIDADES DE REPORTE
    // ═══════════════════════════════════════════════════════════════════

    private static void reportarExito(String nombre) {
        System.out.println("  [OK] " + nombre);
        testsPasados++;
    }

    private static void reportarFallo(String nombre, String mensaje) {
        System.out.println("  [FAIL] " + nombre + ": " + mensaje);
        testsFallados++;
    }
}
