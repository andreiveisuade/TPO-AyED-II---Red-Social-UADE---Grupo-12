package servicio;

import modelo.Cliente;
import modelo.Accion;
import modelo.TipoAccion;
import util.Validador;
import modelo.Sesion;
import util.ResultadoValidacion;
import tda.Diccionario;
import tda.Pila;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/*
Gestiona los clientes del sistema usando IDs como identificadores únicos.

INVARIANTE DE REPRESENTACIÓN:
- clientes != null
- Todos los IDs son > 0
- Todos los clientes tienen scoring entre 0 y 100

GRASP: Creator - crea instancias de Cliente y Accion
GRASP: Information Expert - conoce el Diccionario de clientes
SOLID: SRP - solo gestiona clientes
 */
public class GestorClientes {
    
    // Esta clase una un diccionario como TDA, donde la clave es el id del cliente, y el valor es el cliente
    private Diccionario<Integer, Cliente> clientes;
    private boolean registrarEnHistorial;
    private int proximoId;
    private final String archivoPath;
    
    /* Constantes */
    private static final String DEFAULT_PATH = "data/clientes_1M.json";

    public GestorClientes() {
        this(DEFAULT_PATH);
    }
    
    /*
    Constructor principal.
    Carga los clientes desde el archivo JSON especificado.
    */
    public GestorClientes(String dbPath) {
        this.archivoPath = dbPath;
        this.registrarEnHistorial = true;
        this.proximoId = 1001;
        cargarDesdeArchivo();
    }
    
    /*
    Carga los clientes desde un archivo JSON.
    Si falla, inicia con un sistema vacío.
    */
    private void cargarDesdeArchivo() {
        System.out.println("Cargando clientes...");
        try (FileReader reader = new FileReader(archivoPath)) {
            Gson gson = new Gson();
            ClientesWrapper wrapper = gson.fromJson(reader, ClientesWrapper.class);
            
            this.clientes = new Diccionario<>(1000003);
            
            if (wrapper != null && wrapper.clientes != null) {
                for (ClienteDTO dto : wrapper.clientes) {
                    Cliente c = new Cliente(dto.id, dto.nombre, dto.scoring);
                    c.cargarSiguiendo(dto.siguiendo);
                    c.cargarSolicitudes(dto.solicitudes);
                    
                    clientes.insertar(c.getId(), c);
                    if(c.getId() >= proximoId) proximoId = c.getId() + 1;
                }
            }
        } catch (IOException e) {
            System.err.println("Error cargando datos (iniciando vacío): " + e.getMessage());
            this.clientes = new Diccionario<>(1000003);
        }
    }

    /*
    Guarda estado actual en archivo. Llamar AL SALIR de la app.
    */
    public void guardarCambios() {
        System.out.println("Guardando datos en " + archivoPath + "...");
        try (FileWriter writer = new FileWriter(archivoPath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            
            ClientesWrapper wrapper = new ClientesWrapper();
            Object[] objs = clientes.obtenerValores();
            wrapper.clientes = new ClienteDTO[objs.length];
            
            for(int i=0; i<objs.length; i++) {
                Cliente c = (Cliente) objs[i];
                ClienteDTO dto = new ClienteDTO();
                dto.id = c.getId();
                dto.nombre = c.getNombre();
                dto.scoring = c.getScoring();
                dto.siguiendo = c.getSiguiendo();
                dto.solicitudes = c.getSolicitudesRecibidasSerialized();
                wrapper.clientes[i] = dto;
            }
            
            gson.toJson(wrapper, writer);
            System.out.println("Datos guardados exitosamente.");
        } catch (IOException e) {
            System.err.println("Error guardando datos: " + e.getMessage());
        }
    }
    
    // DTO para GSON
    private static class ClienteDTO {
        int id;
        String nombre;
        int scoring;
        int[] siguiendo;
        String[] solicitudes;
    }

    // Wrapper interno para GSON
    private static class ClientesWrapper {
        ClienteDTO[] clientes;
    }
    
    public void activarHistorial() {
        this.registrarEnHistorial = true;
    }

    public void desactivarHistorial() {
        this.registrarEnHistorial = false;
    }
    
    private Sesion getSesion() {
        return Sesion.getInstancia();
    }
    
    private boolean sesionValida() {
        return getSesion().estaAutenticado();
    }
    
    /*
    Agrega un nuevo cliente al sistema generando su ID.
    Retorna el ID generado o -1 si falla validación.
    */
    public int agregarCliente(String nombre, int scoring) {
        ResultadoValidacion validacionNombre = Validador.validarNombre(nombre);
        if (!validacionNombre.esValido()) return -1;
        
        ResultadoValidacion validacionScoring = Validador.validarScoring(scoring);
        if (!validacionScoring.esValido()) return -1;
        
        int id = proximoId++;
        Cliente cliente = new Cliente(id, nombre, scoring);
        clientes.insertar(id, cliente);
        
        if (registrarEnHistorial && sesionValida()) {
            Accion accion = new Accion(TipoAccion.AGREGAR_CLIENTE, String.valueOf(id));
            getSesion().getHistorial().registrar(accion);
        }
        
        return id;
    }

    /*
    Agrega un cliente con un ID específico.
    */
    public boolean agregarClienteConId(int id, String nombre, int scoring) {
        if (id <= 0) return false;
        
        ResultadoValidacion validacionNombre = Validador.validarNombre(nombre);
        if (!validacionNombre.esValido()) return false;
        
        ResultadoValidacion validacionScoring = Validador.validarScoring(scoring);
        if (!validacionScoring.esValido()) return false;
        
        if (clientes.contiene(id)) return false;

        Cliente cliente = new Cliente(id, nombre, scoring);
        clientes.insertar(id, cliente);
        
        if (id >= proximoId) {
            proximoId = id + 1;
        }
        return true;
    }

    /*
    Busca un cliente por su ID.
    */
    public Cliente buscarPorId(int id) {
        return clientes.obtener(id);
    }

    /*
    Busca clientes por nombre.
    Retorna array con las coincidencias.
    */
    public Cliente[] buscarPorNombre(String nombre) {
        if (nombre == null) return new Cliente[0];
        
        Object[] todosLosClientes = clientes.obtenerValores();
        String nombreNormalizado = nombre.toLowerCase();
        
        int count = 0;
        for (Object obj : todosLosClientes) {
            Cliente c = (Cliente) obj;
            if (c.getNombre().toLowerCase().equals(nombreNormalizado)) {
                count++;
            }
        }

        Cliente[] resultado = new Cliente[count];
        int index = 0;
        for (Object obj : todosLosClientes) {
            Cliente c = (Cliente) obj;
            if (c.getNombre().toLowerCase().equals(nombreNormalizado)) {
                resultado[index++] = c;
            }
        }
        return resultado;
    }

    /*
    Verifica si existe un cliente con el ID dado.
    */
    public boolean existeCliente(int id) {
        return clientes.contiene(id);
    }

    /*
    Retorna la cantidad total de clientes registrados.
    */
    public int getCantidadClientes() {
        return clientes.getCantidad();
    }

    /*
    Retorna todos los clientes registrados.
    */
    public Cliente[] obtenerTodosLosClientes() {
        Object[] valores = clientes.obtenerValores();
        Cliente[] resultado = new Cliente[valores.length];
        for (int i = 0; i < valores.length; i++) {
            resultado[i] = (Cliente) valores[i];
        }
        return resultado;
    }

    /*
    Busca clientes por su scoring de influencia.
    Recorre el diccionario filtrando por scoring. O(N).
    */
    public Cliente[] buscarPorScoring(int scoring) {
        Object[] todos = clientes.obtenerValores();
        
        // Primer paso: contar coincidencias
        int count = 0;
        for (Object obj : todos) {
            if (((Cliente) obj).getScoring() == scoring) count++;
        }
        
        // Segundo paso: recolectar
        Cliente[] resultado = new Cliente[count];
        int idx = 0;
        for (Object obj : todos) {
            Cliente c = (Cliente) obj;
            if (c.getScoring() == scoring) resultado[idx++] = c;
        }
        return resultado;
    }

    public Diccionario<Integer, Cliente> getClientes() {
        return clientes;
    }

    /*
    Elimina un cliente del sistema por su ID.
    Limpia también las referencias en otros clientes (dejar de seguir).
    */
    public boolean eliminarCliente(int id) {
        Cliente cliente = clientes.obtener(id);
        if (cliente == null) return false;

        // Guardar estado para historial antes de eliminar referencias
        if (registrarEnHistorial && sesionValida()) {
            StringBuilder seguidos = new StringBuilder();
            int[] idsSeguidos = cliente.getSiguiendo();
            for (int i = 0; i < idsSeguidos.length; i++) {
                seguidos.append(idsSeguidos[i]);
                if (i < idsSeguidos.length - 1) seguidos.append(",");
            }
            
            Accion accion = new Accion(TipoAccion.ELIMINAR_CLIENTE, 
                String.valueOf(id), 
                cliente.getNombre(), 
                String.valueOf(cliente.getScoring()),
                seguidos.toString()
            );
            getSesion().getHistorial().registrar(accion);
        }

        clientes.eliminar(id);
        
        // Limpiar referencias en cascada
        Object[] todosLosClientes = clientes.obtenerValores();
        for (Object obj : todosLosClientes) {
            Cliente c = (Cliente) obj;
            c.dejarDeSeguir(id);
        }
        return true;
    }

    /*
    Registra que un cliente sigue a otro.
    */
    public boolean seguir(int idSolicitante, int idObjetivo) {
        Cliente clienteSolicitante = clientes.obtener(idSolicitante);
        Cliente clienteObjetivo = clientes.obtener(idObjetivo);

        if (clienteSolicitante == null || clienteObjetivo == null) {
            return false;
        }

        if (clienteSolicitante.seguir(idObjetivo)) {
            if (registrarEnHistorial && sesionValida()) {
                Accion accion = new Accion(TipoAccion.SEGUIR, 
                                            String.valueOf(idSolicitante), 
                                            String.valueOf(idObjetivo));
                getSesion().getHistorial().registrar(accion);
            }
            // [Simplificacion] Ya no guardamos en disco aquí. Se guarda al salir.
            return true;
        }
        return false;
    }

    /*
    Gestiona el envío de una solicitud de seguimiento.
    */
    public boolean enviarSolicitud(int idSolicitante, int idObjetivo) {
        Cliente solicitante = clientes.obtener(idSolicitante);
        Cliente objetivo = clientes.obtener(idObjetivo);
        
        if (solicitante == null || objetivo == null) return false;
        
        modelo.SolicitudSeguimiento solicitud = new modelo.SolicitudSeguimiento(
            String.valueOf(idSolicitante), 
            String.valueOf(idObjetivo)
        );
        
        objetivo.recibirSolicitud(solicitud);
        
        // [Simplificacion] Ya no guardamos en disco aquí.
        return true;
    }

    /*
    Procesa y acepta una solicitud de seguimiento.
    Encapsula la lógica de negocio y persistencia.
    */
    public boolean aceptarSolicitud(Cliente solicitante, Cliente objetivo, modelo.SolicitudSeguimiento solicitud) {
        if (solicitante == null || objetivo == null || solicitud == null) return false;
        
        // 1. Crear la relación de seguimiento (Esto ya persiste el cambio vía seguir())
        boolean resultado = seguir(solicitante.getId(), objetivo.getId());
        
        // 2. (Opcional) Si hubiera lógica adicional como notificaciones, iría aquí.
        
        return resultado;
    }

    /*
    Registra que un cliente deja de seguir a otro.
    */
    public boolean dejarDeSeguir(int idSolicitante, int idObjetivo) {
        Cliente cliente = clientes.obtener(idSolicitante);
        if (cliente == null) return false;

        if (cliente.dejarDeSeguir(idObjetivo)) {
            if (registrarEnHistorial && sesionValida()) {
                Accion accion = new Accion(TipoAccion.DEJAR_DE_SEGUIR, 
                                            String.valueOf(idSolicitante), 
                                            String.valueOf(idObjetivo));
                getSesion().getHistorial().registrar(accion);
            }
            // [Simplificacion] Ya no guardamos en disco aquí.
            return true;
        }
        return false;
    }

    /*
    Deshace la última acción registrada en el historial de la sesión.
    */
    public Accion deshacer() {
        if (!sesionValida()) return null;
        HistorialAcciones historial = getSesion().getHistorial();
        
        if (historial.estaVacio()) return null;

        Accion accion = historial.extraerUltima();
        ejecutarUndo(accion);
        return accion;
    }


    /*
    Ejecuta la lógica inversa de una acción para deshacerla.
    */
    private void ejecutarUndo(Accion accion) {
        String[] datos = accion.getDatos();

        switch (accion.getTipo()) {
            case AGREGAR_CLIENTE:
                int idEliminar = Integer.parseInt(datos[0]);
                Cliente cEliminar = clientes.obtener(idEliminar);
                if (cEliminar != null) {
                    clientes.eliminar(idEliminar);
                }
                break;
            case ELIMINAR_CLIENTE:
                int idRestaurar = Integer.parseInt(datos[0]);
                Cliente restaurado = new Cliente(idRestaurar, datos[1], Integer.parseInt(datos[2]));
                clientes.insertar(idRestaurar, restaurado);
                if (datos.length > 3 && !datos[3].isEmpty()) {
                    for (String seguido : datos[3].split(",")) {
                        restaurado.seguir(Integer.parseInt(seguido));
                    }
                }
                break;
            case SEGUIR:
                Cliente c1 = clientes.obtener(Integer.parseInt(datos[0]));
                if (c1 != null) c1.dejarDeSeguir(Integer.parseInt(datos[1]));
                break;
            case DEJAR_DE_SEGUIR:
                Cliente c2 = clientes.obtener(Integer.parseInt(datos[0]));
                if (c2 != null) c2.seguir(Integer.parseInt(datos[1]));
                break;
            default:
                break;
        }
    }


    /*
    Retorna la última acción realizada por el usuario actual.
    */
    public Accion verUltimaAccion() {
        if (!sesionValida()) return null;
        return getSesion().getHistorial().verUltima();
    }

    public boolean historialVacio() {
        if (!sesionValida()) return true;
        return getSesion().getHistorial().estaVacio();
    }


    public int getCantidadAcciones() {
        if (!sesionValida()) return 0;
        return getSesion().getHistorial().getCantidad();
    }


    public Accion[] obtenerHistorialCompleto() {
        if (!sesionValida()) return new Accion[0];
        return getSesion().getHistorial().obtenerTodas();
    }

    /* 
    Clases internas eliminadas. Se utiliza ClienteDTO y ClienteDAO en persistencia.
    */
}
