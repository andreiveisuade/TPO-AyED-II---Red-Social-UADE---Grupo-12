package vista;

import modelo.SolicitudSeguimiento;
import servicio.GestorClientes;
import modelo.Sesion;
import modelo.Cliente;
import modelo.Cliente;
import java.util.Scanner;
import static vista.Terminal.*;

/*
Submenú unificado para gestión de amigos y exploración de la red.
Usa Sesion.getInstancia() para acceder a la sesión Singleton.
 */
public class MenuSolicitudes {
    
    /* Atributos */
    private final GestorClientes gestor;
    private final Scanner scanner;
    private final MenuUtils utils;

    public MenuSolicitudes(GestorClientes gestor, Scanner scanner) {
        this.gestor = gestor;
        this.scanner = scanner;
        this.utils = new MenuUtils(scanner);
    }
    
    private Sesion getSesion() {
        return Sesion.getInstancia();
    }

    public void mostrar() {
        int opcion;
        String mensaje = "";
        do {
            limpiarPantalla();
            utils.mostrarCabecera("Inicio", "Amigos & Red Social");
            
            System.out.println(" 1. Mis Amigos (Siguiendo)");
            System.out.println(" 2. Solicitudes (Pendientes)");
            System.out.println(" 3. Explorar / Buscar usuarios");
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
                    listarAmigos();
                    pausar(scanner);
                    break;
                case 2:
                    mensaje = gestionarSolicitudes();
                    break;
                case 3:
                    mensaje = menuExplorar();
                    break;
            }
        } while (opcion != 0);
    }

    private void listarAmigos() {
        limpiarPantalla();
        utils.mostrarCabecera("Inicio", "Amigos", "Mis Amigos");
        
        Sesion sesion = getSesion();
        if (!sesion.estaAutenticado()) return;
        
        Cliente usuario = sesion.getUsuarioActual();
        int[] siguiendo = usuario.getSiguiendo();
        int cantidad = usuario.getCantidadSiguiendo();
        
        if (cantidad == 0) {
            imprimirAviso("No sigues a nadie aún.");
            return;
        }
        
        System.out.println("+------+--------------------+");
        System.out.println("| ID   | Usuario            |");
        System.out.println("+------+--------------------+");
        
        for (int i = 0; i < cantidad; i++) {
            int idAmigo = siguiendo[i];
            Cliente amigo = gestor.buscarPorId(idAmigo);
            
            String idStr = String.format("%-4d", idAmigo);
            String nombreStr = (amigo != null) ? amigo.getNombre() : "Desconocido";
            nombreStr = String.format("%-18s", nombreStr);
            
            System.out.println("| " + idStr + " | " + nombreStr + " |");
        }
        System.out.println("+------+--------------------+");
        System.out.println("\nTotal: " + cantidad + " amigos.");
    }

    private String gestionarSolicitudes() {
        int opcionSolo;
        String msg = "";
        do {
            limpiarPantalla();
            utils.mostrarCabecera("Inicio", "Amigos", "Solicitudes");
            
            System.out.println(verSolicitudesPendientes());
            System.out.println();
            System.out.println(" 1. Procesar siguiente");
            System.out.println(" 0. Volver");
            imprimirSeparador(MenuUtils.ANCHO);
            
            if(!msg.isEmpty()) {
                System.out.println(msg);
                msg = "";
            }
            
            System.out.print("Opción: ");
            opcionSolo = utils.leerEntero();
            
            if (opcionSolo == 1) {
                msg = procesarSolicitud();
            }
        } while (opcionSolo != 0);
        return "";
    }

    private String menuExplorar() {
        int opcionEx;
        String msg = "";
        do {
            limpiarPantalla();
            utils.mostrarCabecera("Inicio", "Amigos", "Explorar");
            
            System.out.println(" 1. Buscar por ID (y agregar)");
            System.out.println(" 2. Buscar por Nombre (y agregar)");
            System.out.println(" 3. Buscar por Influencia");
            System.out.println(" 4. Listar todos");
            System.out.println(" 0. Volver");
            imprimirSeparador(MenuUtils.ANCHO);
            
            if(!msg.isEmpty()) {
                System.out.println(msg);
                msg = "";
            }
            
            System.out.print("Opción: ");
            opcionEx = utils.leerEntero();
            
            switch (opcionEx) {
                case 1:
                    msg = buscarPorIdYAgregar();
                    break;
                case 2:
                    msg = buscarPorNombreYAgregar();
                    break;
                case 3:
                    msg = buscarPorScoring();
                    break;
                case 4:
                    listarTodos();
                    pausar(scanner);
                    break;
            }
        } while (opcionEx != 0);
        return "";
    }

    private String buscarPorIdYAgregar() {
        System.out.print("ID de usuario: ");
        int id = utils.leerEntero();
        
        timer.iniciar();
        Cliente cliente = gestor.buscarPorId(id);
        timer.detener();
        String tiempo = timer.obtenerTiempoFormateado();

        if (cliente != null) {
            mostrarDetalleCliente(cliente);
            System.out.println("Tiempo: " + tiempo);
            System.out.println();
            
            Sesion sesion = getSesion();
            if (sesion.estaAutenticado() && cliente.getId() != sesion.getUsuarioActual().getId()) {
                System.out.print("¿Enviar solicitud? (1: Si, 0: No): ");
                if (utils.leerEntero() == 1) {
                    return enviarSolicitud(cliente);
                }
            }
            return "";
        } else {
            return "[ERROR] ID " + id + " no encontrado (" + tiempo + ")";
        }
    }

    private String buscarPorNombreYAgregar() {
        System.out.print("Nombre: ");
        String nombre = utils.capitalizarNombre(scanner.nextLine().trim());
        
        timer.iniciar();
        Cliente[] clientes = gestor.buscarPorNombre(nombre);
        timer.detener();
        String tiempo = timer.obtenerTiempoFormateado();

        if (clientes.length > 0) {
            System.out.println();
            for (Cliente c : clientes) {
                System.out.println(" - ID: " + c.getId() + " | " + c.getNombre() + " (Influencia: " + c.getScoring() + ")");
            }
            System.out.println("Tiempo: " + tiempo);
            System.out.println();
            
            System.out.print("Ingrese ID para agregar (0 cancelar): ");
            int idSel = utils.leerEntero();
            if (idSel != 0) {
                Cliente sel = gestor.buscarPorId(idSel);
                if (sel != null) return enviarSolicitud(sel);
            }
            return "";
        } else {
            return "[ERROR] No encontrado (" + tiempo + ")";
        }
    }

    private String buscarPorScoring() {
        System.out.print("Influencia (0-100): ");
        int scoring = utils.leerEntero();
        
        timer.iniciar();
        Cliente[] encontrados = gestor.buscarPorScoring(scoring);
        timer.detener();
        String tiempo = timer.obtenerTiempoFormateado();
        
        if (encontrados.length == 0) return "[AVISO] Sin resultados (" + tiempo + ")";
        
        System.out.println();
        for (Cliente c : encontrados) {
            System.out.println(" - ID: " + c.getId() + " | " + c.getNombre());
        }
        System.out.println("\nTotal: " + encontrados.length);
        System.out.println("Tiempo: " + tiempo);
        pausar(scanner);
        return "";
    }

    private void listarTodos() {
        timer.iniciar();
        Cliente[] clientes = gestor.obtenerTodosLosClientes();
        timer.detener();
        String tiempo = timer.obtenerTiempoFormateado();

        if (clientes.length == 0) {
            imprimirAviso("No hay usuarios registrados (" + tiempo + ")");
            return;
        }

        System.out.println("+------+--------------------+---------+");
        System.out.println("| ID   | Usuario            | Influen |");
        System.out.println("+------+--------------------+---------+");
        
        for (Cliente c : clientes) {
            String idCol = String.format("%-4d", c.getId());
            String nombreCol = String.format("%-18s", c.getNombre());
            String scoreCol = String.format("%-7d", c.getScoring());
            System.out.println("| " + idCol + " | " + nombreCol + "| " + scoreCol + "|");
        }
        System.out.println("+------+--------------------+---------+");
        System.out.println("Total: " + gestor.getCantidadClientes() + " usuarios");
        System.out.println("Tiempo: " + tiempo);
    }

    private String enviarSolicitud(Cliente objetivo) {
        Sesion sesion = getSesion();
        if (!sesion.estaAutenticado()) return "[ERROR] Error: no autenticado";
        Cliente solicitante = sesion.getUsuarioActual();
        
        if (solicitante.getId() == objetivo.getId()) return "[ERROR] No puedes seguirte a ti mismo";
        
        for(int id : solicitante.getSiguiendo()) {
            if(id == objetivo.getId()) return "[AVISO] Ya sigues a este usuario";
        }

        timer.iniciar();
        boolean resultado = gestor.enviarSolicitud(solicitante.getId(), objetivo.getId());
        timer.detener();
        
        if (!resultado) return "[ERROR] No se pudo enviar solicitud";
        String tiempo = timer.obtenerTiempoFormateado();
        
        return "[OK] Solicitud enviada a @" + objetivo.getNombre() + " (" + tiempo + ")";
    }

    private String verSolicitudesPendientes() {
        Sesion sesion = getSesion();
        if (!sesion.estaAutenticado()) return "[ERROR] Error: no autenticado";
        Cliente usuarioActual = sesion.getUsuarioActual();
        
        if (!usuarioActual.tieneSolicitudesPendientes()) {
            return "[AVISO] No tienes solicitudes pendientes";
        } else {
            SolicitudSeguimiento siguiente = usuarioActual.verSiguienteSolicitud();
            try {
                int idSol = Integer.parseInt(siguiente.getSolicitante());
                Cliente sol = gestor.buscarPorId(idSol);
                String nombreSol = (sol != null) ? sol.getNombre() : "ID:" + idSol;
                
                return "Siguiente: " + nombreSol + " quiere seguirte | Total: " + usuarioActual.getCantidadSolicitudesPendientes() + " pendientes";
            } catch (Exception e) {
                return "Error leyendo solicitud";
            }
        }
    }

    private String procesarSolicitud() {
        Sesion sesion = getSesion();
        if (!sesion.estaAutenticado()) return "[ERROR] Error: no autenticado";
        Cliente usuarioActual = sesion.getUsuarioActual();
        
        SolicitudSeguimiento solicitud = usuarioActual.procesarSiguienteSolicitud();
        if (solicitud == null) return "[AVISO] No tienes solicitudes";

        try {
            int idSolicitante = Integer.parseInt(solicitud.getSolicitante());
            int idObjetivo = Integer.parseInt(solicitud.getObjetivo());
            
            Cliente solicitante = gestor.buscarPorId(idSolicitante);
            
            // Refactor: Usar método del gestor
            boolean resultado = solicitante != null && gestor.aceptarSolicitud(solicitante, usuarioActual, solicitud);
            
            if (resultado) {
                System.out.println("✅ ¡Solicitud aceptada!");
                System.out.println("Ahora sigues a " + solicitante.getNombre());
                return "[OK] Solicitud procesada."; // Added return to match method signature
            } else {
                return "[ERROR] No se pudo procesar (limite o error)";
            }
        } catch (Exception e) {
            return "[ERROR] Error procesando solicitud";
        }
    }
    
    private void mostrarDetalleCliente(Cliente cliente) {
        System.out.println("  ID: " + cliente.getId() + " | @" + cliente.getNombre());
        System.out.println("  Influencia: " + cliente.getScoring());
        System.out.print("  Siguiendo: ");
        
        if (cliente.getCantidadSiguiendo() == 0) {
            System.out.print("nadie");
        } else {
            int[] siguiendo = cliente.getSiguiendo();
            for (int i = 0; i < cliente.getCantidadSiguiendo(); i++) {
                System.out.print(siguiendo[i] + (i < cliente.getCantidadSiguiendo()-1 ? ", " : ""));
            }
        }
        System.out.println();
    }
}
