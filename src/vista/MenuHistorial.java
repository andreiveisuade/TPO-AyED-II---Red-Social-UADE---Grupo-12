package vista;

import modelo.Accion;
import modelo.Cliente;
import servicio.GestorClientes;
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
            return "Ultima accion:\n  " + formatearAccion(accion);
        } else {
            return "[AVISO] Historial vacio";
        }
    }

    private String deshacerAccion() {
        Accion accion = gestor.deshacer();

        if (accion != null) {
            return "[OK] Deshecho:\n  " + formatearAccion(accion);
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
            System.out.println(marcador + (i + 1) + ". " + formatearAccion(a));
        }

        System.out.println();
        System.out.println("Total: " + acciones.length + " acciones");
    }

    /*
    Formatea una acción para mostrarla de forma más legible con nombres de usuarios
    en lugar de solo IDs.
    */
    private String formatearAccion(Accion accion) {
        String timestamp = String.format("[%s]", accion.getTimestamp().toLocalTime().withNano(0));
        String[] datos = accion.getDatos();
        String tipoAccion = accion.getTipo().name();
        String descripcion = "";

        switch (tipoAccion) {
            case "SEGUIR":
                // datos: [idSeguidor, idSeguido]
                if (datos.length >= 2) {
                    try {
                        Cliente seguidor = gestor.buscarPorId(Integer.parseInt(datos[0]));
                        Cliente seguido = gestor.buscarPorId(Integer.parseInt(datos[1]));
                        String nomSeguidor = (seguidor != null) ? "@" + seguidor.getNombre() : "ID:" + datos[0];
                        String nomSeguido = (seguido != null) ? "@" + seguido.getNombre() : "ID:" + datos[1];
                        descripcion = "SEGUIR: " + nomSeguidor + " → " + nomSeguido;
                    } catch (Exception e) {
                        descripcion = tipoAccion + ": " + String.join(", ", datos);
                    }
                } else {
                    descripcion = tipoAccion + ": " + String.join(", ", datos);
                }
                break;

            case "DEJAR_DE_SEGUIR":
                // datos: [idSeguidor, idSeguido]
                if (datos.length >= 2) {
                    try {
                        Cliente seguidor = gestor.buscarPorId(Integer.parseInt(datos[0]));
                        Cliente seguido = gestor.buscarPorId(Integer.parseInt(datos[1]));
                        String nomSeguidor = (seguidor != null) ? "@" + seguidor.getNombre() : "ID:" + datos[0];
                        String nomSeguido = (seguido != null) ? "@" + seguido.getNombre() : "ID:" + datos[1];
                        descripcion = "DEJAR_DE_SEGUIR: " + nomSeguidor + " ✕ " + nomSeguido;
                    } catch (Exception e) {
                        descripcion = tipoAccion + ": " + String.join(", ", datos);
                    }
                } else {
                    descripcion = tipoAccion + ": " + String.join(", ", datos);
                }
                break;

            default:
                descripcion = tipoAccion + ": " + String.join(", ", datos);
        }

        return timestamp + " " + descripcion;
    }
}
