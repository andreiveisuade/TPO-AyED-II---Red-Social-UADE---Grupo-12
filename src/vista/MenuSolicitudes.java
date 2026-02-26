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
            System.out.println(" 6. Visualizar Grafo (Matriz de Adyacencia)");
            System.out.println(" 0. Volver");
            imprimirSeparador(MenuUtils.ANCHO);
            
            if (!mensaje.isEmpty()) {
                System.out.println(mensaje);
                mensaje = "";
            }
            
            System.out.print("Opcion: ");
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
                case 6:
                    visualizarMatrizAdyacencia();
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
            if (!sesion.estaAutenticado()) return "[ERROR] No autenticado";

            Cliente usuario = sesion.getUsuarioActual();
            int[] siguiendo = usuario.getSiguiendo();
            int cantidad = usuario.getCantidadSiguiendo();

            if (cantidad == 0) {
                System.out.println("[AVISO] No sigues a nadie\n");
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

            System.out.print("Opcion: ");
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
        if (!sesion.estaAutenticado()) return "[ERROR] No autenticado";
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

            System.out.print("Opcion: ");
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
            
            System.out.print("Opcion: ");
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
            System.out.print("Opcion: ");
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
            System.out.print("Opcion: ");
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
        if (!sesion.estaAutenticado()) return "[ERROR] No autenticado";
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
        if (!sesion.estaAutenticado()) return "[ERROR] No autenticado";
        Cliente usuarioActual = sesion.getUsuarioActual();
        
        if (!usuarioActual.tieneSolicitudesPendientes()) {
            return "[AVISO] No tienes solicitudes pendientes";
        } else {
            SolicitudSeguimiento siguiente = usuarioActual.verSiguienteSolicitud();
            try {
                int idSol = Integer.parseInt(siguiente.getSolicitante());
                Cliente sol = gestor.buscarPorId(idSol);
                String nombreSol = (sol != null) ? sol.getNombre() : "ID:" + idSol;
                
                return "Siguiente: @" + nombreSol + " quiere seguirte (" + usuarioActual.getCantidadSolicitudesPendientes() + " pendientes)";
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
        if (!sesion.estaAutenticado()) return "[ERROR] No autenticado";
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
                return "[OK] Aceptada: ahora sigues a @" + solicitante.getNombre();
            } else {
                return "[ERROR] No se pudo procesar";
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
        if (!sesion.estaAutenticado()) return "[ERROR] No autenticado";
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
            imprimirAviso("No autenticado");
            return;
        }

        int idUsuario = sesion.getUsuarioActual().getId();

        // Construir el ABB y mostrarlo visualmente
        tda.ArbolBinarioBusqueda<Integer, Cliente> arbol = gestor.construirArbolRelaciones(idUsuario);

        if (arbol.estaVacio()) {
            imprimirAviso("Sin seguidores");
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
            imprimirAviso("Sin seguidores en nivel 4");
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
                System.out.println("[AVISO] Sin amistades\n");
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

        Cliente usuario = sesion.getUsuarioActual();
        if (usuario.getCantidadAmigos() >= Cliente.MAX_AMIGOS) {
            return "[ERROR] Alcanzaste el limite de " + Cliente.MAX_AMIGOS + " amigos";
        }
        if (amigo.getCantidadAmigos() >= Cliente.MAX_AMIGOS) {
            return "[ERROR] @" + amigo.getNombre() + " ya tiene " + Cliente.MAX_AMIGOS + " amigos";
        }

        boolean resultado = gestor.agregarAmistad(usuario.getId(), idAmigo);
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

        System.out.println("Saltos entre clientes en el grafo dirigido de seguimientos\n");

        System.out.print("ID origen (0 = tu ID " + sesion.getUsuarioActual().getId() + "): ");
        int idOrigen = utils.leerEntero();
        if (idOrigen == 0) idOrigen = sesion.getUsuarioActual().getId();

        System.out.print("ID destino: ");
        int idDestino = utils.leerEntero();

        // Validar que existan
        Cliente origen = gestor.buscarPorId(idOrigen);
        Cliente destino = gestor.buscarPorId(idDestino);

        if (origen == null) {
            return "[ERROR] ID origen " + idOrigen + " no existe";
        }
        if (destino == null) {
            return "[ERROR] ID destino " + idDestino + " no existe";
        }

        // Calcular distancia
        long inicio = System.nanoTime();
        int distancia = gestor.calcularDistancia(idOrigen, idDestino);
        long tiempoNs = System.nanoTime() - inicio;

        // Mostrar resultado
        System.out.println();
        System.out.println("Origen:  @" + origen.getNombre() + " (ID:" + idOrigen + ")");
        System.out.println("Destino: @" + destino.getNombre() + " (ID:" + idDestino + ")");
        imprimirSeparador(MenuUtils.ANCHO);

        if (distancia == 0) {
            System.out.println("Resultado: 0 saltos (mismo cliente)");
        } else if (distancia == -1) {
            System.out.println("Resultado: No existe camino entre estos clientes");
            System.out.println("           (no hay cadena de seguimientos que los conecte)");
        } else {
            System.out.println("Resultado: " + distancia + " salto(s)");
        }

        System.out.println("Tiempo BFS: " + String.format("%.3f ms", tiempoNs / 1_000_000.0));

        System.out.println();
        pausar(scanner);
        return "";
    }

    /*
    ══════════════════════════════════════════════════════════
    ITERACIÓN 3: VISUALIZACIÓN DE MATRIZ DE ADYACENCIA
    ══════════════════════════════════════════════════════════
    */

    private static final String DEMO_DB = "data/clientes_iteracion_3.json";

    /*
    Submenú para elegir el modo de visualización de la matriz de adyacencia.
    Opción 1: Demo Iteración 3 (10 clientes con relaciones ricas)
    Opción 2: Subgrafo del vecindario de un cliente de la base actual
    */
    private void visualizarMatrizAdyacencia() {
        int totalActual = gestor.getCantidadClientes();

        // Si la base ya es chica (≤ 20), mostrar directo sin submenú
        if (totalActual > 0 && totalActual <= 20) {
            renderizarMatriz(gestor, "base actual (" + totalActual + " clientes)");
            return;
        }

        int opcion;
        do {
            limpiarPantalla();
            utils.mostrarCabecera("Inicio", "Amigos", "Matriz de Adyacencia");

            System.out.println("Base actual: " + totalActual + " clientes (muy grande para visualizar completa)\n");
            System.out.println(" 1. Demo Iteracion 3 (10 clientes con relaciones de ejemplo)");
            System.out.println(" 2. Subgrafo de un cliente (vecindario desde la base actual)");
            System.out.println(" 0. Volver");
            imprimirSeparador(MenuUtils.ANCHO);

            System.out.print("Opcion: ");
            opcion = utils.leerEntero();

            switch (opcion) {
                case 1:
                    visualizarDemo();
                    break;
                case 2:
                    visualizarSubgrafo();
                    break;
            }
        } while (opcion != 0);
    }

    /*
    Carga la base de datos demo (10 clientes) y renderiza las matrices.
    */
    private void visualizarDemo() {
        servicio.GestorClientes gestorDemo = new servicio.GestorClientes(DEMO_DB);
        renderizarMatriz(gestorDemo, "demo Iteracion 3");
    }

    /*
    Permite al usuario elegir un cliente de la base actual y visualiza
    su subgrafo: el cliente + todos sus vecinos directos (siguiendo,
    seguidores y amigos). Máximo ~20 nodos para que la matriz sea legible.
    */
    private void visualizarSubgrafo() {
        limpiarPantalla();
        utils.mostrarCabecera("Inicio", "Amigos", "Matriz", "Subgrafo");

        Sesion sesion = getSesion();
        int idDefault = sesion.estaAutenticado() ? sesion.getUsuarioActual().getId() : 0;

        System.out.println("Se mostrara: el cliente + sus seguidos + seguidores + amigos.\n");

        System.out.print("ID del cliente central (0 = tu ID " + idDefault + "): ");
        int idCentral = utils.leerEntero();
        if (idCentral == 0) idCentral = idDefault;

        Cliente central = gestor.buscarPorId(idCentral);
        if (central == null) {
            imprimirError("ID " + idCentral + " no encontrado.");
            pausar(scanner);
            return;
        }

        // Recolectar IDs únicos del vecindario
        // Usamos un array temporal grande y luego recortamos
        int[] siguiendo = central.getSiguiendo();
        int[] seguidores = central.getSeguidores();
        int[] amigos = central.obtenerAmigos();
        int maxNodos = 1 + siguiendo.length + seguidores.length + amigos.length;
        int[] idsTemp = new int[maxNodos];
        int count = 0;

        // Agregar el cliente central
        idsTemp[count++] = idCentral;

        // Agregar siguiendo
        for (int id : siguiendo) {
            if (!contieneId(idsTemp, count, id)) idsTemp[count++] = id;
        }
        // Agregar seguidores
        for (int id : seguidores) {
            if (!contieneId(idsTemp, count, id)) idsTemp[count++] = id;
        }
        // Agregar amigos
        for (int id : amigos) {
            if (!contieneId(idsTemp, count, id)) idsTemp[count++] = id;
        }

        // Limitar a 20 nodos para que la matriz sea legible
        int MAX_NODOS = 20;
        if (count > MAX_NODOS) {
            System.out.println("\n[AVISO] Vecindario tiene " + count + " nodos, mostrando los primeros " + MAX_NODOS);
            count = MAX_NODOS;
        }

        // Construir array de clientes
        Cliente[] subgrafo = new Cliente[count];
        int validCount = 0;
        for (int i = 0; i < count; i++) {
            Cliente c = gestor.buscarPorId(idsTemp[i]);
            if (c != null) subgrafo[validCount++] = c;
        }

        // Recortar si hubo IDs inválidos
        if (validCount < count) {
            Cliente[] recortado = new Cliente[validCount];
            for (int i = 0; i < validCount; i++) recortado[i] = subgrafo[i];
            subgrafo = recortado;
        }

        if (subgrafo.length == 0) {
            imprimirError("No se encontraron clientes validos en el vecindario.");
            pausar(scanner);
            return;
        }

        // Ordenar por ID
        ordenarPorId(subgrafo);

        System.out.println("\nSubgrafo de @" + central.getNombre() + " (ID:" + idCentral + "): "
            + subgrafo.length + " nodos\n");

        renderizarPantallas(subgrafo, gestor, "subgrafo de @" + central.getNombre());
    }

    /*
    Verifica si un ID ya existe en el array hasta la posición count.
    */
    private boolean contieneId(int[] ids, int count, int id) {
        for (int i = 0; i < count; i++) {
            if (ids[i] == id) return true;
        }
        return false;
    }

    /*
    Ordena un array de clientes por ID usando selection sort.
    */
    private void ordenarPorId(Cliente[] clientes) {
        for (int i = 0; i < clientes.length - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < clientes.length; j++) {
                if (clientes[j].getId() < clientes[minIdx].getId()) {
                    minIdx = j;
                }
            }
            if (minIdx != i) {
                Cliente tmp = clientes[i];
                clientes[i] = clientes[minIdx];
                clientes[minIdx] = tmp;
            }
        }
    }

    /*
    Punto de entrada para renderizar la matriz completa de un gestor.
    Obtiene todos los clientes, los ordena y delega a renderizarPantallas.
    */
    private void renderizarMatriz(servicio.GestorClientes gestorViz, String etiqueta) {
        Cliente[] todos = gestorViz.obtenerTodosLosClientes();

        if (todos.length == 0) {
            limpiarPantalla();
            imprimirError("No se pudo cargar datos para la visualizacion.");
            pausar(scanner);
            return;
        }

        ordenarPorId(todos);
        renderizarPantallas(todos, gestorViz, etiqueta);
    }

    /*
    Renderiza las 4 pantallas de visualización:
    1. Tabla de clientes
    2. Listas de adyacencia (seguimientos + amistades)
    3. Matriz de seguimientos (dirigida)
    4. Matriz de amistades (no dirigida)
    */
    private void renderizarPantallas(Cliente[] todos, servicio.GestorClientes gestorViz, String etiqueta) {
        int n = todos.length;

        // ═══ 1. TABLA DE CLIENTES ═══
        limpiarPantalla();
        utils.mostrarCabecera("Inicio", "Amigos", "Matriz de Adyacencia");
        System.out.println("Fuente: " + etiqueta + "\n");
        System.out.println(n + " clientes cargados:\n");
        System.out.println("+------+----------+---------+------------+------------+");
        System.out.println("| ID   | Nombre   | Scoring | Siguiendo  | Amigos     |");
        System.out.println("+------+----------+---------+------------+------------+");
        for (Cliente c : todos) {
            System.out.printf("| %-4d | %-8s | %-7d | %-10s | %-10s |%n",
                c.getId(),
                c.getNombre(),
                c.getScoring(),
                formatIds(c.getSiguiendo()),
                formatIds(c.obtenerAmigos()));
        }
        System.out.println("+------+----------+---------+------------+------------+");
        System.out.println();
        pausar(scanner);

        // ═══ 2. LISTAS DE ADYACENCIA ═══
        limpiarPantalla();
        utils.mostrarCabecera("Inicio", "Amigos", "Matriz de Adyacencia", "Listas");

        System.out.println("LISTAS DE ADYACENCIA — SEGUIMIENTOS (grafo dirigido)\n");
        for (Cliente c : todos) {
            int[] sig = c.getSiguiendo();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("  %-8s (ID %2d): ", c.getNombre(), c.getId()));
            if (sig.length == 0) {
                sb.append("[ ]");
            } else {
                sb.append("[ ");
                for (int i = 0; i < sig.length; i++) {
                    Cliente dest = gestorViz.buscarPorId(sig[i]);
                    sb.append(dest != null ? dest.getNombre() : "?");
                    if (i < sig.length - 1) sb.append(", ");
                }
                sb.append(" ]");
            }
            System.out.println(sb);
        }

        System.out.println("\nLISTAS DE ADYACENCIA — AMISTADES (grafo no dirigido)\n");
        for (Cliente c : todos) {
            int[] amigos = c.obtenerAmigos();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("  %-8s (ID %2d): ", c.getNombre(), c.getId()));
            if (amigos.length == 0) {
                sb.append("[ ]");
            } else {
                sb.append("[ ");
                for (int i = 0; i < amigos.length; i++) {
                    Cliente a = gestorViz.buscarPorId(amigos[i]);
                    sb.append(a != null ? a.getNombre() : "?");
                    if (i < amigos.length - 1) sb.append(", ");
                }
                sb.append(" ]");
            }
            System.out.println(sb);
        }
        System.out.println();
        pausar(scanner);

        // ═══ 3. MATRIZ DE SEGUIMIENTOS (dirigida) ═══
        limpiarPantalla();
        utils.mostrarCabecera("Inicio", "Amigos", "Matriz de Adyacencia", "Seguimientos");
        System.out.println("MATRIZ DE ADYACENCIA — SEGUIMIENTOS (grafo dirigido)");
        System.out.println("Lectura: fila i SIGUE A columna j\n");
        imprimirMatriz(todos, gestorViz, false);
        System.out.println();
        System.out.println("Asimetrica: X = fila sigue a columna");
        System.out.println();
        pausar(scanner);

        // ═══ 4. MATRIZ DE AMISTADES (no dirigida) ═══
        limpiarPantalla();
        utils.mostrarCabecera("Inicio", "Amigos", "Matriz de Adyacencia", "Amistades");
        System.out.println("MATRIZ DE ADYACENCIA — AMISTADES (grafo no dirigido)");
        System.out.println("Lectura: fila i ES AMIGO DE columna j\n");
        imprimirMatriz(todos, gestorViz, true);
        System.out.println();
        System.out.println("Simetrica: A[i][j] = A[j][i] (bidireccional)");
        System.out.println();
        pausar(scanner);
    }

    /*
    Imprime una matriz de adyacencia NxN en la consola.
    Si esAmistad=true, usa el campo amistades. Si no, usa siguiendo.
    */
    private void imprimirMatriz(Cliente[] clientes, servicio.GestorClientes g, boolean esAmistad) {
        int n = clientes.length;
        int anchoId = 3;
        int anchoNombre = 8;

        // Encabezado: IDs de columna
        System.out.printf("%" + (anchoNombre + anchoId + 4) + "s", "");
        for (Cliente c : clientes) {
            System.out.printf(" %2d ", c.getId());
        }
        System.out.println();

        // Separador superior
        System.out.printf("%" + (anchoNombre + anchoId + 3) + "s+", "");
        for (int i = 0; i < n; i++) System.out.print("----");
        System.out.println("-+");

        // Filas
        for (int i = 0; i < n; i++) {
            Cliente fila = clientes[i];
            System.out.printf("  %-" + anchoNombre + "s %2d |", fila.getNombre(), fila.getId());

            for (int j = 0; j < n; j++) {
                Cliente col = clientes[j];
                boolean hayConexion;

                if (esAmistad) {
                    hayConexion = fila.esAmigoDE(col.getId());
                } else {
                    hayConexion = fila.sigueA(col.getId());
                }

                if (i == j) {
                    System.out.print("  . ");  // diagonal
                } else if (hayConexion) {
                    System.out.print("  X ");
                } else {
                    System.out.print("  . ");
                }
            }
            System.out.println("|");
        }

        // Separador inferior
        System.out.printf("%" + (anchoNombre + anchoId + 3) + "s+", "");
        for (int i = 0; i < n; i++) System.out.print("----");
        System.out.println("-+");
    }

    /*
    Formatea un array de IDs como string compacto: "2, 3" o "-"
    */
    private String formatIds(int[] ids) {
        if (ids.length == 0) return "-";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == 0) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(ids[i]);
        }
        return sb.length() == 0 ? "-" : sb.toString();
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
