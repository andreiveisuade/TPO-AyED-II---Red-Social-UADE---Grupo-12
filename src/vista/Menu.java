package vista;

import java.util.Scanner;
import modelo.Cliente;
import modelo.Sesion;
import servicio.GestorClientes;
import static vista.Terminal.*;

/*
Menú principal - Coordinador de submenús.
Usa Sesion.getInstancia() para acceder a la sesión Singleton.
 */
public class Menu {
    
    /* Atributos */
    private final GestorClientes gestor;
    private final Scanner scanner;
    private final MenuUtils utils;
    
    /* Submenús */
    private final MenuHistorial menuHistorial;
    private final MenuSolicitudes menuSolicitudes;
    
    private String mensajeEstado;

    public Menu() {
        this.gestor = new GestorClientes();
        this.scanner = new Scanner(System.in);
        this.utils = new MenuUtils(scanner);
        this.mensajeEstado = "";
        
        /* Inicializa los submenús */
        this.menuHistorial = new MenuHistorial(gestor, scanner);
        this.menuSolicitudes = new MenuSolicitudes(gestor, scanner);
    }
    
    /*
    Obtiene la sesión Singleton.
    */
    private Sesion getSesion() {
        return Sesion.getInstancia();
    }

    private boolean mostrarPantallaBienvenida() {
        limpiarPantalla();
        System.out.println("+" + "-".repeat(48) + "+");
        System.out.println("|" + utils.centrar("BIENVENIDO A RED SOCIAL - UADE", 48) + "|");
        System.out.println("+" + "-".repeat(48) + "+");
        System.out.println();
        System.out.println("Para comenzar, ingresa tu ID de usuario (o 0 para Salir):");
        System.out.println("(IDs disponibles: 1 a 1,000,000+)");
        System.out.println();
        
        boolean usuarioValido = false;
        int userId = -1;
        
        while (!usuarioValido) {
            System.out.print("> Tu ID: ");
            userId = utils.leerEntero();
            
            if (userId == 0) {
                return false;
            }
            
            if (userId < 0) {
                System.out.println("[ERROR] El ID debe ser un numero positivo.");
                continue;
            }

            Cliente cliente = gestor.buscarPorId(userId);
            
            if (cliente != null) {
                usuarioValido = true;
                getSesion().iniciarSesion(cliente);
                System.out.println();
                System.out.println("[OK] Hola, " + cliente.getNombre() + "! Bienvenido a la red social.");
                
                if (getSesion().tieneSolicitudesPendientes()) {
                    int cantSolicitudes = getSesion().getCantidadSolicitudesPendientes();
                    System.out.println("[AVISO] Tienes " + cantSolicitudes + " solicitud(es) de seguimiento pendiente(s)");
                }
            } else {
                System.out.println("[ERROR] El ID '" + userId + "' no existe en el sistema.");
                System.out.println("  Por favor ingresa un ID valido registrado.");
                System.out.println();
            }
        }
        
        System.out.println();
        pausar(scanner);
        return true;
    }

    public void iniciar() {
        while (true) {
            if (!mostrarPantallaBienvenida()) {
                break;
            }
            
            int opcion;
            do {
                limpiarPantalla();
                utils.mostrarCabecera("Inicio");
                mostrarEstadoSistema();
                mostrarMenuPrincipal();
                
                opcion = utils.leerEntero();
                procesarOpcionPrincipal(opcion);

            } while (opcion != 0);
            
            if (getSesion().estaAutenticado()) {
                System.out.println("\nCerrando sesion de " + getSesion().getNombreUsuarioActual() + "...");
                getSesion().cerrarSesion();
                pausar(scanner);
            }
        }
        
        // Guardar cambios al terminar la aplicación
        gestor.guardarCambios();
        
        limpiarPantalla();
        imprimirTitulo("Hasta pronto!");
        scanner.close();
    }

    private void mostrarEstadoSistema() {
        Sesion sesion = getSesion();
        String infoUsuario = sesion.getNombreUsuarioActual() + " (ID: " + sesion.getIdUsuarioActual() + ")";
        int padding = Math.max(0, 39 - infoUsuario.length());
        
        System.out.println("+- Usuario: " + infoUsuario + " ".repeat(padding) + "+");
        System.out.println("+- Estado de la Red -----------------------+");
        
        int solicitudesPendientes = sesion.getCantidadSolicitudesPendientes();
        
        System.out.println("| Amigos:   " + sesion.getUsuarioActual().getCantidadSiguiendo() + 
                          " | Solicitudes: " + solicitudesPendientes +
                          " | Acciones: " + gestor.getCantidadAcciones() + " |");
        System.out.println("+-------------------------------------------+");
        System.out.println();
    }

    private void mostrarMenuPrincipal() {
        System.out.println(" 1. Amigos & Red Social - Explorar, seguir y solicitudes");
        System.out.println(" 2. Historial           - Acciones realizadas");
        System.out.println(" 0. Salir");
        imprimirSeparador(MenuUtils.ANCHO);
        
        if (!mensajeEstado.isEmpty()) {
            imprimirAviso(mensajeEstado);
            mensajeEstado = "";
        }
        
        System.out.print("Opción: ");
    }

    private void procesarOpcionPrincipal(int opcion) {
        switch (opcion) {
            case 1:
                menuSolicitudes.mostrar();
                break;
            case 2:
                menuHistorial.mostrar();
                break;
            case 0:
                break;
            default:
                mensajeEstado = "Opción no válida";
        }
    }
}
