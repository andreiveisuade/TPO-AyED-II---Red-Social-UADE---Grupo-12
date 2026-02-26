package vista;

import modelo.SolicitudSeguimiento;
import servicio.GestorClientes;
import modelo.Sesion;
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

    /*
    Muestra el menú principal de solicitudes y amigos.
    */
    public void mostrar() {
        int opcion;
        String mensaje = "";
        do {
            limpiarPantalla();
            utils.mostrarCabecera("Inicio", "Amigos & Red Social");
            
            System.out.println(" 1. Mis Amigos (Siguiendo)");
            System.out.println(" 2. Solicitudes (Pendientes)");
            System.out.println(" 3. Explorar / Buscar usuarios");
            System.out.println(" 4. Amistades (bidireccionales)");
            System.out.println(" 5. Calcular Distancia (BFS)");
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
                    mensaje = listarAmigos();
                    break;
                case 2:
                    mensaje = gestionarSolicitudes();
                    break;
                case 3:
                    mensaje = menuExplorar();
                    break;
                case 4:
                    mensaje = menuAmistades();
                    break;
                case 5:
                    mensaje = calcularDistancia();
                    break;
            }
        } while (opcion != 0);
    }

    /*
    Lista los amigos (usuarios seguidos) del usuario actual con opción de dejar de seguir.
    */
    private String listarAmigos() {
        int opcionAmigos;
        String msg = "";

        do {
            limpiarPantalla();
            utils.mostrarCabecera("Inicio", "Amigos", "Mis Amigos");

            Sesion sesion = getSesion();
            if (!sesion.estaAutenticado()) return "[ERROR] Error: no autenticado";

            Cliente usuario = sesion.getUsuarioActual();
            int[] siguiendo = usuario.getSiguiendo();
            int cantidad = usuario.getCantidadSiguiendo();

            if (cantidad == 0) {
                System.out.println("[AVISO] No sigues a nadie aún.\n");
                System.out.println(" 0. Volver");
                imprimirSeparador(MenuUtils.ANCHO);
            } else {
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
                System.out.println("\nTotal: " + cantidad + " amigos.\n");
                System.out.println(" 1. Dejar de seguir");
                System.out.println(" 0. Volver");
                imprimirSeparador(MenuUtils.ANCHO);
            }

            if(!msg.isEmpty()) {
                System.out.println(msg);
                msg = "";
            }

            System.out.print("Opción: ");
            opcionAmigos = utils.leerEntero();

            if (opcionAmigos == 1 && cantidad > 0) {
                msg = dejarDeSeguirAmigo();
            }
        } while (opcionAmigos != 0);

        return "";
    }

    /*
    Permite al usuario dejar de seguir a un amigo.
    */
    private String dejarDeSeguirAmigo() {
        System.out.print("ID del amigo a dejar de seguir: ");
        int idAmigo = utils.leerEntero();

        Sesion sesion = getSesion();
        if (!sesion.estaAutenticado()) return "[ERROR] Error: no autenticado";
        Cliente usuario = sesion.getUsuarioActual();

        // Validar que el ID esté en la lista de seguidos
        boolean estaEnSiguiendo = false;
        for (int id : usuario.getSiguiendo()) {
            if (id == idAmigo) {
                estaEnSiguiendo = true;
                break;
            }
        }

        if (!estaEnSiguiendo) {
            return "[ERROR] No sigues a este usuario";
        }

        Cliente amigo = gestor.buscarPorId(idAmigo);
        if (amigo == null) {
            return "[ERROR] Usuario no encontrado";
        }

        gestor.dejarDeSeguir(usuario.getId(), idAmigo);
        return "[OK] Ya no sigues a @" + amigo.getNombre();
    }

    /*
    Gestiona la visualización y procesamiento de solicitudes pendientes.
    */
    private String gestionarSolicitudes() {
        int opcionSolo;
        String msg = "";
        do {
            limpiarPantalla();
            utils.mostrarCabecera("Inicio", "Amigos", "Solicitudes");

            System.out.println(verSolicitudesPendientes());
            System.out.println();
            System.out.println(" 1. Aceptar siguiente");
            System.out.println(" 2. Rechazar siguiente");
            System.out.println(" 0. Volver");
            imprimirSeparador(MenuUtils.ANCHO);

            if(!msg.isEmpty()) {
                System.out.println(msg);
                msg = "";
            }

            System.out.print("Opción: ");
            opcionSolo = utils.leerEntero();

            if (opcionSolo == 1) {
                msg = aceptarSolicitud();
            } else if (opcionSolo == 2) {
                msg = rechazarSolicitud();
            }
        } while (opcionSolo != 0);
        return "";
    }

    /*
    Submenú para explorar y buscar otros usuarios en la red.
    */
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
            System.out.println(" 5. Análisis ABB - Cuarto nivel");
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
                case 5:
                    mostrarCuartoNivelABB();
                    pausar(scanner);
                    break;
            }
        } while (opcionEx != 0);
        return "";
    }

    /*
    Busca un usuario por ID y ofrece la opción de enviar solicitud.
    */
    private String buscarPorIdYAgregar() {
        System.out.print("ID de usuario: ");
        int id = utils.leerEntero();
        
        Cliente cliente = gestor.buscarPorId(id);

        if (cliente != null) {
            mostrarDetalleCliente(cliente);
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
            return "[ERROR] ID " + id + " no encontrado";
        }
    }

    /*
    Busca usuarios por nombre con paginación y ofrece la opción de enviar solicitud.
    Muestra resultados de a PAGINA_SIZE (15) con navegación Anterior/Siguiente.

    Complejidad: O(1) por página mostrada (los resultados ya están en memoria)
    */
    private static final int PAGINA_SIZE = 15;

    private String buscarPorNombreYAgregar() {
        System.out.print("Nombre: ");
        String nombre = utils.capitalizarNombre(scanner.nextLine().trim());

        Cliente[] clientes = gestor.buscarPorNombre(nombre);

        if (clientes.length == 0) {
            return "[ERROR] No encontrado";
        }

        int pagina = 0;
        int totalPaginas = (clientes.length + PAGINA_SIZE - 1) / PAGINA_SIZE;
        int opcion;

        do {
            limpiarPantalla();
            utils.mostrarCabecera("Inicio", "Amigos", "Explorar", "Buscar: " + nombre);

            int desde = pagina * PAGINA_SIZE;
            int hasta = Math.min(desde + PAGINA_SIZE, clientes.length);

            System.out.println("Resultados para \"" + nombre + "\" (" + clientes.length
                + " encontrados) - Página " + (pagina + 1) + "/" + totalPaginas + "\n");
            System.out.println("+------+--------------------+---------+");
            System.out.println("| ID   | Usuario            | Influen |");
            System.out.println("+------+--------------------+---------+");

            for (int i = desde; i < hasta; i++) {
                Cliente c = clientes[i];
                String idCol = String.format("%-4d", c.getId());
                String nombreCol = String.format("%-18s", c.getNombre());
                String scoreCol = String.format("%-7d", c.getScoring());
                System.out.println("| " + idCol + " | " + nombreCol + "| " + scoreCol + "|");
            }
            System.out.println("+------+--------------------+---------+");

            System.out.println();
            if (pagina > 0) System.out.println(" 1. << Anterior");
            if (pagina < totalPaginas - 1) System.out.println(" 2. Siguiente >>");
            System.out.println(" 3. Agregar por ID");
            System.out.println(" 0. Volver");
            imprimirSeparador(MenuUtils.ANCHO);
            System.out.print("Opción: ");
            opcion = utils.leerEntero();

            switch (opcion) {
                case 1:
                    if (pagina > 0) pagina--;
                    break;
                case 2:
                    if (pagina < totalPaginas - 1) pagina++;
                    break;
                case 3:
                    System.out.print("Ingrese ID para agregar (0 cancelar): ");
                    int idSel = utils.leerEntero();
                    if (idSel != 0) {
                        Cliente sel = gestor.buscarPorId(idSel);
                        if (sel != null) return enviarSolicitud(sel);
                        return "[ERROR] ID no encontrado";
                    }
                    break;
            }
        } while (opcion != 0);

        return "";
    }

    /*
    Busca usuarios por nivel de scoring/influencia con paginación.
    Muestra resultados de a PAGINA_SIZE (15) con navegación Anterior/Siguiente.

    Complejidad: O(log 101 + k) para buscar, O(1) por página mostrada
    */
    private String buscarPorScoring() {
        System.out.print("Influencia (0-100): ");
        int scoring = utils.leerEntero();

        Cliente[] encontrados = gestor.buscarPorScoring(scoring);

        if (encontrados.length == 0) return "[AVISO] Sin resultados para influencia " + scoring;

        int pagina = 0;
        int totalPaginas = (encontrados.length + PAGINA_SIZE - 1) / PAGINA_SIZE;
        int opcion;

        do {
            limpiarPantalla();
            utils.mostrarCabecera("Inicio", "Amigos", "Explorar", "Influencia: " + scoring);

            int desde = pagina * PAGINA_SIZE;
            int hasta = Math.min(desde + PAGINA_SIZE, encontrados.length);

            System.out.println("Clientes con influencia " + scoring + " (" + encontrados.length
                + " encontrados) - Página " + (pagina + 1) + "/" + totalPaginas + "\n");
            System.out.println("+------+--------------------+---------+");
            System.out.println("| ID   | Usuario            | Influen |");
            System.out.println("+------+--------------------+---------+");

            for (int i = desde; i < hasta; i++) {
                Cliente c = encontrados[i];
                String idCol = String.format("%-4d", c.getId());
                String nombreCol = String.format("%-18s", c.getNombre());
                String scoreCol = String.format("%-7d", c.getScoring());
                System.out.println("| " + idCol + " | " + nombreCol + "| " + scoreCol + "|");
            }
            System.out.println("+------+--------------------+---------+");

            System.out.println();
            if (pagina > 0) System.out.println(" 1. << Anterior");
            if (pagina < totalPaginas - 1) System.out.println(" 2. Siguiente >>");
            System.out.println(" 3. Agregar por ID");
            System.out.println(" 0. Volver");
            imprimirSeparador(MenuUtils.ANCHO);
            System.out.print("Opción: ");
            opcion = utils.leerEntero();

            switch (opcion) {
                case 1:
                    if (pagina > 0) pagina--;
                    break;
                case 2:
                    if (pagina < totalPaginas - 1) pagina++;
                    break;
                case 3:
                    System.out.print("Ingrese ID para agregar (0 cancelar): ");
                    int idSel = utils.leerEntero();
                    if (idSel != 0) {
                        Cliente sel = gestor.buscarPorId(idSel);
                        if (sel != null) return enviarSolicitud(sel);
                        return "[ERROR] ID no encontrado";
                    }
                    break;
            }
        } while (opcion != 0);

        return "";
    }

    /*
    Lista todos los usuarios registrados en el sistema.
    */
    private void listarTodos() {
        Cliente[] clientes = gestor.obtenerTodosLosClientes();

        if (clientes.length == 0) {
            imprimirAviso("No hay usuarios registrados");
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
    }

    /*
    Envía una solicitud de seguimiento a un usuario objetivo.
    */
    private String enviarSolicitud(Cliente objetivo) {
        Sesion sesion = getSesion();
        if (!sesion.estaAutenticado()) return "[ERROR] Error: no autenticado";
        Cliente solicitante = sesion.getUsuarioActual();
        
        if (solicitante.getId() == objetivo.getId()) return "[ERROR] No puedes seguirte a ti mismo";
        
        for(int id : solicitante.getSiguiendo()) {
            if(id == objetivo.getId()) return "[AVISO] Ya sigues a este usuario";
        }

        boolean resultado = gestor.enviarSolicitud(solicitante.getId(), objetivo.getId());
        
        if (!resultado) return "[ERROR] No se pudo enviar solicitud";
        
        return "[OK] Solicitud enviada a @" + objetivo.getNombre();
    }

    /*
    Muestra la próxima solicitud pendiente de aprobación.
    */
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

    /*
    Acepta la solicitud de seguimiento actual.
    */
    private String aceptarSolicitud() {
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
                return "[OK] Solicitud procesada.";
            } else {
                return "[ERROR] No se pudo procesar (limite o error)";
            }
        } catch (Exception e) {
            return "[ERROR] Error procesando solicitud";
        }
    }

    /*
    Rechaza la solicitud de seguimiento actual sin crear la relación.
    */
    private String rechazarSolicitud() {
        Sesion sesion = getSesion();
        if (!sesion.estaAutenticado()) return "[ERROR] Error: no autenticado";
        Cliente usuarioActual = sesion.getUsuarioActual();

        SolicitudSeguimiento solicitud = usuarioActual.procesarSiguienteSolicitud();
        if (solicitud == null) return "[AVISO] No tienes solicitudes";

        try {
            int idSolicitante = Integer.parseInt(solicitud.getSolicitante());
            Cliente solicitante = gestor.buscarPorId(idSolicitante);
            String nombreSolicitante = (solicitante != null) ? solicitante.getNombre() : "ID:" + idSolicitante;

            return "[OK] Solicitud rechazada de @" + nombreSolicitante;
        } catch (Exception e) {
            return "[ERROR] Error rechazando solicitud";
        }
    }
    
    /*
    Muestra los seguidores del usuario logueado ubicados en el cuarto nivel (nivel 3, 0-indexed)
    del ABB construido con sus seguidores ordenados por scoring.

    ITERACIÓN 2: "Utilizar una implementación de ABB cargando los datos de los clientes
    que se siguen e imprimir los clientes que están en el cuarto nivel, para ver quién
    tiene más seguidores."

    Complejidad: O(k log k) para construir el ABB + O(m) para obtener nivel
    donde k = seguidores del usuario, m = nodos en nivel 4
    */
    private void mostrarCuartoNivelABB() {
        limpiarPantalla();
        utils.mostrarCabecera("Inicio", "Amigos", "Explorar", "ABB - Cuarto Nivel");

        Sesion sesion = getSesion();
        if (!sesion.estaAutenticado()) {
            imprimirAviso("Debes iniciar sesión para ver el análisis ABB.");
            return;
        }

        int idUsuario = sesion.getUsuarioActual().getId();

        // Construir el ABB y mostrarlo visualmente
        tda.ArbolBinarioBusqueda<Integer, Cliente> arbol = gestor.construirArbolRelaciones(idUsuario);

        if (arbol.estaVacio()) {
            imprimirAviso("@" + sesion.getUsuarioActual().getNombre() + " no tiene seguidores.");
            return;
        }

        System.out.println("ABB de seguidores de @" + sesion.getUsuarioActual().getNombre()
            + " (ordenado por scoring):\n");
        System.out.println(arbol.representarArbol());
        System.out.println("Altura del arbol: " + arbol.getAltura()
            + " | Total nodos: " + arbol.getCantidad());
        imprimirSeparador(MenuUtils.ANCHO);

        // Obtener nivel 4 (índice 3)
        Object[] resultadosNivel = arbol.obtenerEnNivel(3);

        if (resultadosNivel.length == 0) {
            imprimirAviso("No hay seguidores en el cuarto nivel (Nivel 3).");
            return;
        }

        Cliente[] clientes = new Cliente[resultadosNivel.length];
        for (int i = 0; i < resultadosNivel.length; i++) {
            clientes[i] = (Cliente) resultadosNivel[i];
        }

        // Ordenar por cantidad de seguidores (mayor a menor) - Selection Sort
        for (int i = 0; i < clientes.length - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < clientes.length; j++) {
                if (clientes[j].getCantidadSeguidores() > clientes[maxIdx].getCantidadSeguidores()) {
                    maxIdx = j;
                }
            }
            if (maxIdx != i) {
                Cliente temp = clientes[i];
                clientes[i] = clientes[maxIdx];
                clientes[maxIdx] = temp;
            }
        }

        System.out.println("\nSeguidores en el cuarto nivel (Nivel 3):\n");
        System.out.println("+------+--------------------+---------+------------+");
        System.out.println("| ID   | Usuario            | Scoring | Seguidores |");
        System.out.println("+------+--------------------+---------+------------+");

        for (Cliente c : clientes) {
            String idCol = String.format("%-4d", c.getId());
            String nombreCol = String.format("%-18s", c.getNombre());
            String scoreCol = String.format("%-7d", c.getScoring());
            String segCol = String.format("%-10d", c.getCantidadSeguidores());
            System.out.println("| " + idCol + " | " + nombreCol + "| " + scoreCol + "| " + segCol + " |");
        }
        System.out.println("+------+--------------------+---------+------------+");
        System.out.println("Total: " + clientes.length + " seguidores en el cuarto nivel.");

        if (clientes[0].getCantidadSeguidores() > 0) {
            System.out.println("Mayor influencia: " + clientes[0].getNombre()
                + " (" + clientes[0].getCantidadSeguidores() + " seguidores)");
        }
    }

    /*
    ══════════════════════════════════════════════════════════
    ITERACIÓN 3: AMISTADES (RELACIONES BIDIRECCIONALES)
    ══════════════════════════════════════════════════════════
    */

    /*
    Submenú para gestionar amistades bidireccionales.
    Permite ver, agregar y eliminar amistades.
    */
    private String menuAmistades() {
        int opcion;
        String msg = "";
        do {
            limpiarPantalla();
            utils.mostrarCabecera("Inicio", "Amigos", "Amistades");

            Sesion sesion = getSesion();
            if (!sesion.estaAutenticado()) return "[ERROR] No autenticado";
            Cliente usuario = sesion.getUsuarioActual();

            // Mostrar amigos actuales
            Cliente[] amigos = gestor.obtenerAmigos(usuario.getId());
            int cantAmigos = gestor.obtenerCantidadAmigos(usuario.getId());

            if (cantAmigos == 0) {
                System.out.println("[AVISO] No tienes amistades aun.\n");
            } else {
                System.out.println("+------+--------------------+---------+");
                System.out.println("| ID   | Amigo              | Scoring |");
                System.out.println("+------+--------------------+---------+");
                for (Cliente amigo : amigos) {
                    if (amigo != null) {
                        String idCol = String.format("%-4d", amigo.getId());
                        String nombreCol = String.format("%-18s", amigo.getNombre());
                        String scoreCol = String.format("%-7d", amigo.getScoring());
                        System.out.println("| " + idCol + " | " + nombreCol + "| " + scoreCol + "|");
                    }
                }
                System.out.println("+------+--------------------+---------+");
                System.out.println("Total: " + cantAmigos + " amigo(s).\n");
            }

            System.out.println(" 1. Agregar amistad");
            System.out.println(" 2. Eliminar amistad");
            System.out.println(" 0. Volver");
            imprimirSeparador(MenuUtils.ANCHO);

            if (!msg.isEmpty()) {
                System.out.println(msg);
                msg = "";
            }

            System.out.print("Opcion: ");
            opcion = utils.leerEntero();

            switch (opcion) {
                case 1:
                    msg = agregarAmistad();
                    break;
                case 2:
                    msg = eliminarAmistad();
                    break;
            }
        } while (opcion != 0);
        return "";
    }

    /*
    Agrega una amistad bidireccional entre el usuario actual y otro cliente.
    */
    private String agregarAmistad() {
        Sesion sesion = getSesion();
        if (!sesion.estaAutenticado()) return "[ERROR] No autenticado";

        System.out.print("ID del usuario para ser amigos: ");
        int idAmigo = utils.leerEntero();

        if (idAmigo == sesion.getUsuarioActual().getId()) {
            return "[ERROR] No puedes ser amigo de ti mismo";
        }

        Cliente amigo = gestor.buscarPorId(idAmigo);
        if (amigo == null) {
            return "[ERROR] ID " + idAmigo + " no encontrado";
        }

        if (gestor.sonAmigos(sesion.getUsuarioActual().getId(), idAmigo)) {
            return "[AVISO] Ya son amigos";
        }

        boolean resultado = gestor.agregarAmistad(sesion.getUsuarioActual().getId(), idAmigo);
        if (resultado) {
            return "[OK] Ahora eres amigo de @" + amigo.getNombre() + " (bidireccional)";
        }
        return "[ERROR] No se pudo agregar la amistad";
    }

    /*
    Elimina una amistad bidireccional entre el usuario actual y otro cliente.
    */
    private String eliminarAmistad() {
        Sesion sesion = getSesion();
        if (!sesion.estaAutenticado()) return "[ERROR] No autenticado";

        System.out.print("ID del amigo a eliminar: ");
        int idAmigo = utils.leerEntero();

        Cliente amigo = gestor.buscarPorId(idAmigo);
        if (amigo == null) {
            return "[ERROR] ID " + idAmigo + " no encontrado";
        }

        if (!gestor.sonAmigos(sesion.getUsuarioActual().getId(), idAmigo)) {
            return "[AVISO] No son amigos";
        }

        boolean resultado = gestor.eliminarAmistad(sesion.getUsuarioActual().getId(), idAmigo);
        if (resultado) {
            return "[OK] Amistad eliminada con @" + amigo.getNombre() + " (ambos lados)";
        }
        return "[ERROR] No se pudo eliminar la amistad";
    }

    /*
    ══════════════════════════════════════════════════════════
    ITERACIÓN 3: CALCULAR DISTANCIA (BFS)
    ══════════════════════════════════════════════════════════
    */

    /*
    Calcula la distancia (saltos) entre dos clientes usando BFS.
    Muestra el resultado con detalle visual.
    */
    private String calcularDistancia() {
        limpiarPantalla();
        utils.mostrarCabecera("Inicio", "Amigos", "Distancia BFS");

        Sesion sesion = getSesion();
        if (!sesion.estaAutenticado()) return "[ERROR] No autenticado";

        System.out.println("Calcula el numero de saltos entre dos clientes");
        System.out.println("en el grafo dirigido de seguimientos.\n");

        System.out.print("ID origen (0 = tu ID " + sesion.getUsuarioActual().getId() + "): ");
        int idOrigen = utils.leerEntero();
        if (idOrigen == 0) idOrigen = sesion.getUsuarioActual().getId();

        System.out.print("ID destino: ");
        int idDestino = utils.leerEntero();

        // Validar que existan
        Cliente origen = gestor.buscarPorId(idOrigen);
        Cliente destino = gestor.buscarPorId(idDestino);

        if (origen == null) {
            System.out.println("\n[ERROR] ID origen " + idOrigen + " no existe.");
            pausar(scanner);
            return "";
        }
        if (destino == null) {
            System.out.println("\n[ERROR] ID destino " + idDestino + " no existe.");
            pausar(scanner);
            return "";
        }

        // Calcular distancia
        long inicio = System.nanoTime();
        int distancia = gestor.calcularDistancia(idOrigen, idDestino);
        long tiempoNs = System.nanoTime() - inicio;

        // Mostrar resultado
        System.out.println();
        System.out.println("+-------------------------------------------+");
        System.out.println("|         RESULTADO BFS - DISTANCIA          |");
        System.out.println("+-------------------------------------------+");
        System.out.println("| Origen:  " + String.format("%-33s", "@" + origen.getNombre() + " (ID:" + idOrigen + ")") + "|");
        System.out.println("| Destino: " + String.format("%-33s", "@" + destino.getNombre() + " (ID:" + idDestino + ")") + "|");
        System.out.println("+-------------------------------------------+");

        if (distancia == 0) {
            System.out.println("| Distancia: 0 (mismo cliente)               |");
        } else if (distancia == -1) {
            System.out.println("| Distancia: SIN CAMINO                      |");
            System.out.println("| No existe ruta dirigida entre ellos.        |");
        } else {
            System.out.println("| Distancia: " + String.format("%-31s", distancia + " salto(s)") + "|");
        }

        System.out.println("| Tiempo BFS: " + String.format("%-30s", String.format("%.3f ms", tiempoNs / 1_000_000.0)) + "|");
        System.out.println("+-------------------------------------------+");

        System.out.println();
        pausar(scanner);
        return "";
    }

    /*
    Muestra los detalles de un usuario en pantalla.
    */
    private void mostrarDetalleCliente(Cliente cliente) {
        System.out.println("  ID: " + cliente.getId() + " | @" + cliente.getNombre());
        System.out.println("  Influencia: " + cliente.getScoring());
        System.out.println("  Siguiendo (" + cliente.getCantidadSiguiendo() + "):");

        if (cliente.getCantidadSiguiendo() == 0) {
            System.out.println("    (nadie)");
        } else {
            int[] siguiendo = cliente.getSiguiendo();
            for (int i = 0; i < cliente.getCantidadSiguiendo(); i++) {
                Cliente seguido = gestor.buscarPorId(siguiendo[i]);
                String nombre = (seguido != null) ? seguido.getNombre() : "Desconocido";
                System.out.println("    - ID: " + siguiendo[i] + " | @" + nombre);
            }
        }
    }
}
