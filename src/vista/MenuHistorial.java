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

    public MenuHistorial(GestorClientes gestor, Scanner scanner) {
        this.gestor = gestor;
        this.scanner = scanner;
        this.utils = new MenuUtils(scanner);
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
        Accion accion = gestor.verUltimaAccion();
        
        if (accion != null) {
            return "Ultima accion: " + accion;
        } else {
            return "[AVISO] Historial vacio";
        }
    }

    private String deshacerAccion() {
        Accion accion = gestor.deshacer();
        
        if (accion != null) {
            return "[OK] Deshecho: " + accion;
        } else {
            return "[AVISO] No hay acciones para deshacer";
        }
    }

    private void verHistorialCompleto() {
        limpiarPantalla();
        utils.mostrarCabecera("Inicio", "Historial", "Completo");
        
        Accion[] acciones = gestor.obtenerHistorialCompleto();
        
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
    }
}
