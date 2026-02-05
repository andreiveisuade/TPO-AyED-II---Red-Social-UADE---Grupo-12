package test;

import modelo.Cliente;
import modelo.Sesion;
import modelo.SolicitudSeguimiento;

/**
 * Test para demostrar el uso del TDA Sesion (Singleton) y las solicitudes de seguimiento.
 */
public class TestSesionYSolicitudes {
    
    public static void main(String[] args) {
        System.out.println("=== TEST: TDA Sesion (Singleton) y Solicitudes de Seguimiento ===\n");
        
        // Crear clientes
        Cliente alice = new Cliente(1001, "Alice", 85);
        Cliente bob = new Cliente(1002, "Bob", 70);
        Cliente charlie = new Cliente(1003, "Charlie", 90);
        
        System.out.println("Clientes creados:");
        System.out.println("  - " + alice.getNombre() + " (ID: " + alice.getId() + ")");
        System.out.println("  - " + bob.getNombre() + " (ID: " + bob.getId() + ")");
        System.out.println("  - " + charlie.getNombre() + " (ID: " + charlie.getId() + ")");
        
        // Test 1: Singleton
        System.out.println("\n" + "─".repeat(60));
        System.out.println("Test 1: Patrón Singleton");
        System.out.println("─".repeat(60));
        
        Sesion sesion1 = Sesion.getInstancia();
        Sesion sesion2 = Sesion.getInstancia();
        System.out.println("sesion1 == sesion2: " + (sesion1 == sesion2) + " (debe ser true)");
        System.out.println("Sesión inicial: " + sesion1);
        System.out.println("¿Está autenticado? " + sesion1.estaAutenticado());
        
        // Test 2: Iniciar sesión
        System.out.println("\n" + "─".repeat(60));
        System.out.println("Test 2: Gestión de Sesión");
        System.out.println("─".repeat(60));
        
        sesion1.iniciarSesion(alice);
        System.out.println("\nDespués de iniciar sesión:");
        System.out.println("  " + sesion1);
        System.out.println("  ¿Está autenticado? " + sesion1.estaAutenticado());
        System.out.println("  Usuario actual: " + sesion1.getNombreUsuarioActual());
        System.out.println("  ID: " + sesion1.getIdUsuarioActual());
        
        // Test 3: Solicitudes de seguimiento
        System.out.println("\n" + "─".repeat(60));
        System.out.println("Test 3: Solicitudes de Seguimiento");
        System.out.println("─".repeat(60));
        
        SolicitudSeguimiento solicitud1 = new SolicitudSeguimiento(
            String.valueOf(bob.getId()), 
            String.valueOf(alice.getId())
        );
        SolicitudSeguimiento solicitud2 = new SolicitudSeguimiento(
            String.valueOf(charlie.getId()), 
            String.valueOf(alice.getId())
        );
        
        System.out.println("\nBob y Charlie envían solicitudes a Alice...");
        alice.recibirSolicitud(solicitud1);
        alice.recibirSolicitud(solicitud2);
        
        System.out.println("Solicitudes pendientes de Alice: " + alice.getCantidadSolicitudesPendientes());
        System.out.println("¿Tiene solicitudes? " + alice.tieneSolicitudesPendientes());
        
        // Ver siguiente solicitud sin procesar
        System.out.println("\nVer siguiente solicitud (sin procesar):");
        SolicitudSeguimiento siguiente = alice.verSiguienteSolicitud();
        if (siguiente != null) {
            System.out.println("  De: ID " + siguiente.getSolicitante());
            System.out.println("  Para: ID " + siguiente.getObjetivo());
        }
        System.out.println("Solicitudes pendientes: " + alice.getCantidadSolicitudesPendientes());
        
        // Procesar solicitud
        System.out.println("\nProcesar siguiente solicitud:");
        SolicitudSeguimiento procesada = alice.procesarSiguienteSolicitud();
        if (procesada != null) {
            System.out.println("  ✓ Solicitud procesada de ID " + procesada.getSolicitante());
        }
        System.out.println("Solicitudes pendientes restantes: " + alice.getCantidadSolicitudesPendientes());
        
        // Test 4: Integración con Sesion Singleton
        System.out.println("\n" + "─".repeat(60));
        System.out.println("Test 4: Integración Sesion + Solicitudes");
        System.out.println("─".repeat(60));
        
        Sesion sesion = Sesion.getInstancia();  // Obtenemos la misma instancia
        System.out.println("\nConsultar solicitudes desde la sesión:");
        System.out.println("  ¿Tiene solicitudes pendientes? " + sesion.tieneSolicitudesPendientes());
        System.out.println("  Cantidad: " + sesion.getCantidadSolicitudesPendientes());
        
        // Procesar última solicitud
        System.out.println("\nProcesar última solicitud:");
        Cliente usuarioActual = sesion.getUsuarioActual();
        SolicitudSeguimiento ultima = usuarioActual.procesarSiguienteSolicitud();
        if (ultima != null) {
            System.out.println("  ✓ Solicitud procesada de ID " + ultima.getSolicitante());
        }
        System.out.println("  Solicitudes restantes: " + sesion.getCantidadSolicitudesPendientes());
        
        // Test 5: Cerrar sesión
        System.out.println("\n" + "─".repeat(60));
        System.out.println("Test 5: Cerrar Sesión");
        System.out.println("─".repeat(60));
        
        System.out.println("\nAntes de cerrar: " + sesion);
        sesion.cerrarSesion();
        System.out.println("Después de cerrar: " + sesion);
        System.out.println("¿Está autenticado? " + sesion.estaAutenticado());
        System.out.println("Nombre usuario: " + sesion.getNombreUsuarioActual());
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✓ Todos los tests completados exitosamente");
        System.out.println("=".repeat(60));
    }
}
