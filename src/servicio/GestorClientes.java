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
    
    /* Atributos */
    private Diccionario<Integer, Cliente> clientes;
    private boolean registrarEnHistorial;
    private int proximoId;
    private final String archivoPath;
    
    /* Constantes */
    private static final String DEFAULT_PATH = "data/clientes_1M.json";

    public GestorClientes() {
        this(DEFAULT_PATH);
    }
    
    // Constructor
    public GestorClientes(String dbPath) {
        this.archivoPath = dbPath;
        this.registrarEnHistorial = true;
        this.proximoId = 1001;
        cargarDesdeArchivo();
    }
    
    private void cargarDesdeArchivo() {
        System.out.println("Cargando clientes...");
        try (FileReader reader = new FileReader(archivoPath)) {
            Gson gson = new Gson();
            // Estructura auxiliar para leer el formato actual
            ClientesWrapper wrapper = gson.fromJson(reader, ClientesWrapper.class);
            
            this.clientes = new Diccionario<>(1000003); // Capacidad optimizada
            
            if (wrapper != null && wrapper.clientes != null) {
                for (Cliente c : wrapper.clientes) {
                    clientes.insertar(c.getId(), c);
                    // Actualizar proximoId
                    if(c.getId() >= proximoId) proximoId = c.getId() + 1;
                }
            }
        } catch (IOException e) {
            System.err.println("Error cargando datos (iniciando vacío): " + e.getMessage());
            this.clientes = new Diccionario<>(1000003);
        }
    }

    /**
     * Guarda estado actual en archivo. Llamar AL SALIR de la app.
     */
    public void guardarCambios() {
        System.out.println("Guardando datos en " + archivoPath + "...");
        try (FileWriter writer = new FileWriter(archivoPath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            
            ClientesWrapper wrapper = new ClientesWrapper();
            // Convertir valores del diccionario a array para GSON
            Object[] objs = clientes.obtenerValores();
            wrapper.clientes = new Cliente[objs.length];
            for(int i=0; i<objs.length; i++) {
                wrapper.clientes[i] = (Cliente) objs[i];
            }
            
            gson.toJson(wrapper, writer);
            System.out.println("Datos guardados exitosamente.");
        } catch (IOException e) {
            System.err.println("Error guardando datos: " + e.getMessage());
        }
    }
    
    // Wrapper interno para GSON
    private static class ClientesWrapper {
        Cliente[] clientes;
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
    
    public int agregarCliente(String nombre, int scoring) {
        ResultadoValidacion validacionNombre = Validador.validarNombre(nombre);
        if (!validacionNombre.esValido()) return -1;
        
        ResultadoValidacion validacionScoring = Validador.validarScoring(scoring);
        if (!validacionScoring.esValido()) return -1;
        
        int id = proximoId++;
        Cliente cliente = new Cliente(id, nombre, scoring);
        clientes.insertar(id, cliente);
        return id;
    }

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

    public Cliente buscarPorId(int id) {
        return clientes.obtener(id);
    }

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

    public boolean existeCliente(int id) {
        return clientes.contiene(id);
    }

    public int getCantidadClientes() {
        return clientes.getCantidad();
    }

    public Cliente[] obtenerTodosLosClientes() {
        Object[] valores = clientes.obtenerValores();
        Cliente[] resultado = new Cliente[valores.length];
        for (int i = 0; i < valores.length; i++) {
            resultado[i] = (Cliente) valores[i];
        }
        return resultado;
    }

    public Cliente[] buscarPorScoring(int scoring) {
        Object[] todosLosClientes = clientes.obtenerValores();
        
        int count = 0;
        for (Object obj : todosLosClientes) {
            Cliente c = (Cliente) obj;
            if (c.getScoring() == scoring) count++;
        }

        Cliente[] resultado = new Cliente[count];
        int index = 0;
        for (Object obj : todosLosClientes) {
            Cliente c = (Cliente) obj;
            if (c.getScoring() == scoring) {
                resultado[index++] = c;
            }
        }
        return resultado;
    }

    public Diccionario<Integer, Cliente> getClientes() {
        return clientes;
    }

    public boolean eliminarCliente(int id) {
        Cliente cliente = clientes.obtener(id);
        if (cliente == null) return false;

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

    /**
     * Gestiona el envío de una solicitud de seguimiento y persiste el cambio.
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

    /**
     * Procesa y acepta una solicitud de seguimiento.
     * Encapsula la lógica de negocio y persistencia.
     */
    public boolean aceptarSolicitud(Cliente solicitante, Cliente objetivo, modelo.SolicitudSeguimiento solicitud) {
        if (solicitante == null || objetivo == null || solicitud == null) return false;
        
        // 1. Crear la relación de seguimiento (Esto ya persiste el cambio vía seguir())
        boolean resultado = seguir(solicitante.getId(), objetivo.getId());
        
        // 2. (Opcional) Si hubiera lógica adicional como notificaciones, iría aquí.
        
        return resultado;
    }

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


    public Accion deshacer() {
        if (!sesionValida()) return null;
        HistorialAcciones historial = getSesion().getHistorial();
        
        if (historial.estaVacio()) return null;

        Accion accion = historial.extraerUltima();
        ejecutarUndo(accion);
        return accion;
    }


    private void ejecutarUndo(Accion accion) {
        String[] datos = accion.getDatos();

        switch (accion.getTipo()) {
            case AGREGAR_CLIENTE:
                clientes.eliminar(Integer.parseInt(datos[0]));
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
