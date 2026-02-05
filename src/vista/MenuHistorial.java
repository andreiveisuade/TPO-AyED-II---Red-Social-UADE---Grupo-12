package vista;

import modelo.Accion;
import servicio.GestorClientes;
import modelo.Sesion;
import java.util.Scanner;
import static vista.Terminal.*;

/*
Submenú para gestión del historial.
Usa Sesion.getInstancia() para acceder a la sesión Singleton.
 */
public class MenuHistorial {
    
    /* Atributos */
    private final GestorClientes gestor;
    private final Scanner scanner;
    private final MenuUtils utils;
    private final util.PerformanceTimer timer;

    public MenuHistorial(GestorClientes gestor, Scanner scanner) {
        this.gestor = gestor;
        this.scanner = scanner;
        this.utils = new MenuUtils(scanner);
        this.timer = new util.PerformanceTimer();
    }

    public void mostrar() {
        int opcion;
        String mensaje = "";
        do {
            limpiarPantalla();
            utils.mostrarCabecera("Inicio", "Historial");
            
            System.out.println(" 1. Ver ultima accion");
            System.out.println(" 2. Deshacer ultima accion");
            System.out.println(" 3. Ver historial completo");
            System.out.println(" 0. <- Volver");
            imprimirSeparador(MenuUtils.ANCHO);
            
            if (!mensaje.isEmpty()) {
                System.out.println(mensaje);
                mensaje = "";
            }
            
            System.out.print("Opción: ");
            opcion = utils.leerEntero();

            switch (opcion) {
                case 1:
                    mensaje = verUltimaAccion();
                    break;
                case 2:
                    mensaje = deshacerAccion();
                    break;
                case 3:
                    verHistorialCompleto();
                    pausar(scanner);
                    break;
            }
        } while (opcion != 0);
    }

    private String verUltimaAccion() {
        timer.iniciar();
        Accion accion = gestor.verUltimaAccion();
        timer.detener();
        String tiempo = timer.obtenerTiempoFormateado();
        
        if (accion != null) {
            return "Ultima accion: " + accion + " (" + tiempo + ")";
        } else {
            return "[AVISO] Historial vacio";
        }
    }

    private String deshacerAccion() {
        timer.iniciar();
        Accion accion = gestor.deshacer();
        timer.detener();
        String tiempo = timer.obtenerTiempoFormateado();
        
        if (accion != null) {
            return "[OK] Deshecho: " + accion + " (" + tiempo + ")";
        } else {
            return "[AVISO] No hay acciones para deshacer";
        }
    }

    private void verHistorialCompleto() {
        limpiarPantalla();
        utils.mostrarCabecera("Inicio", "Historial", "Completo");
        
        timer.iniciar();
        Accion[] acciones = gestor.obtenerHistorialCompleto();
        timer.detener();
        String tiempo = timer.obtenerTiempoFormateado();
        
        if (acciones.length == 0) {
            imprimirAviso("El historial esta vacio");
            return;
        }
        
        System.out.println("Mostrando desde la mas reciente a la mas antigua:");
        System.out.println();
        
        for (int i = 0; i < acciones.length; i++) {
            Accion a = acciones[i];
            String marcador = (i == 0) ? "-> " : "  ";
            System.out.println(marcador + (i + 1) + ". " + a);
        }
        
        System.out.println();
        System.out.println("Total: " + acciones.length + " acciones");
        System.out.println("Tiempo: " + tiempo);
    }
}
