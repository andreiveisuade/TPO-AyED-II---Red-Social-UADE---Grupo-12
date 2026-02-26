package servicio;

import modelo.Cliente;
import modelo.Accion;
import modelo.TipoAccion;
import util.Validador;
import util.NumerosPrimos;
import modelo.Sesion;
import util.ResultadoValidacion;
import tda.Diccionario;
import tda.ArbolBinarioBusqueda;

/**
 * Coordinador centralizado de operaciones sobre clientes en la red social.
 *
 * RESPONSABILIDAD: Coordinar CRUD, búsquedas y undo de clientes.
 * Delega responsabilidades especializadas a servicios dedicados.
 *
 * ARQUITECTURA MODULAR:
 * ┌─────────────────────────────────────────────────┐
 * │          GestorClientes (Coordinador)           │
 * ├─────────────────────────────────────────────────┤
 * │ - CRUD de clientes                              │
 * │ - Undo de acciones                              │
 * │ - Delegación a servicios especializados         │
 * └─────────────────────────────────────────────────┘
 *     ↓ delega          ↓ delega          ↓ delega
 * IndiceClientes   GestorRelaciones  PersistenciaClientes
 * (Búsquedas)      (Relaciones)      (JSON I/O)
 *
 * PRINCIPIOS SOLID:
 * - SRP: Coordina CRUD y undo; delega índices, relaciones y persistencia
 * - DIP: Depende de abstracciones (Cliente, servicios)
 *
 * PRINCIPIOS GRASP:
 * - Controller: Coordina acciones entre servicios
 * - Creator: Crea instancias de Cliente
 * - Indirection: Intermediario entre Vista y servicios especializados
 */
public class GestorClientes {

    // ÍNDICE PRIMARIO
    private Diccionario<Integer, Cliente> clientes;  // Índice primario por ID - O(1)

    // SERVICIOS DELEGADOS
    private IndiceClientes indices;                   // Índices secundarios (scoring + nombre)
    private GestorRelaciones gestorRelaciones;        // Gestión de relaciones entre clientes
    private PersistenciaClientes persistencia;        // Persistencia en JSON

    // ESTADO
    private int proximoId;

    // CONSTANTES
    private static final String DEFAULT_PATH = "data/clientes_1M.json";

    // ════════════════════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ════════════════════════════════════════════════════════════════════════════════════

    public GestorClientes() {
        this(DEFAULT_PATH);
    }

    /**
     * Constructor principal.
     * Inicializa servicios delegados y carga datos desde archivo.
     *
     * Complejidad: O(N) donde N = cantidad de clientes
     */
    public GestorClientes(String dbPath) {
        this.proximoId = 1001;

        // Cargar clientes desde persistencia (capacidad dinámica basada en datos reales)
        this.persistencia = new PersistenciaClientes(dbPath);
        this.clientes = persistencia.cargarDesdeArchivo();

        // Inicializar servicios delegados (capacidad proporcional al dataset)
        this.indices = new IndiceClientes(clientes, NumerosPrimos.capacidadOptima(clientes.getCantidad()));
        this.gestorRelaciones = new GestorRelaciones(clientes);

        // Actualizar proximoId basado en clientes cargados
        actualizarProximoId();
    }

    /**
     * Actualiza el próximo ID basado en clientes existentes.
     * Complejidad: O(N)
     */
    private void actualizarProximoId() {
        Object[] valores = clientes.obtenerValores();
        for (Object obj : valores) {
            Cliente c = (Cliente) obj;
            if (c.getId() >= proximoId) {
                proximoId = c.getId() + 1;
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // PERSISTENCIA (Delegada a PersistenciaClientes)
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Guarda cambios en archivo JSON.
     * Delegado a PersistenciaClientes.
     */
    public void guardarCambios() {
        persistencia.guardarCambios(clientes);
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // CRUD: CREATE, READ, UPDATE, DELETE
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Agrega un nuevo cliente con ID autogenerado.
     *
     * Complejidad: O(1) amortizado
     *
     * @return ID del cliente creado, o -1 si falla validación
     */
    public int agregarCliente(String nombre, int scoring) {
        ResultadoValidacion validacionNombre = Validador.validarNombre(nombre);
        if (!validacionNombre.esValido()) return -1;

        ResultadoValidacion validacionScoring = Validador.validarScoring(scoring);
        if (!validacionScoring.esValido()) return -1;

        int id = proximoId++;
        Cliente cliente = new Cliente(id, nombre, scoring);
        clientes.insertar(id, cliente);
        indices.agregarCliente(cliente);

        return id;
    }

    /**
     * Agrega un cliente con ID específico.
     *
     * Complejidad: O(1) amortizado
     *
     * @return true si se agregó, false si falló
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
        indices.agregarCliente(cliente);

        if (id >= proximoId) {
            proximoId = id + 1;
        }
        return true;
    }

    /**
     * Busca un cliente por su ID.
     * Complejidad: O(1)
     */
    public Cliente buscarPorId(int id) {
        return clientes.obtener(id);
    }

    /**
     * Busca clientes por nombre usando índice hash.
     * Delegado a IndiceClientes.
     * Complejidad: O(1) + O(k) donde k = clientes con ese nombre
     */
    public Cliente[] buscarPorNombre(String nombre) {
        return indices.buscarPorNombre(nombre);
    }

    /**
     * Verifica si existe un cliente con el ID dado.
     * Complejidad: O(1)
     */
    public boolean existeCliente(int id) {
        return clientes.contiene(id);
    }

    /**
     * Retorna la cantidad total de clientes.
     * Complejidad: O(1)
     */
    public int getCantidadClientes() {
        return clientes.getCantidad();
    }

    /**
     * Retorna todos los clientes registrados.
     * Complejidad: O(N)
     */
    public Cliente[] obtenerTodosLosClientes() {
        Object[] valores = clientes.obtenerValores();
        Cliente[] resultado = new Cliente[valores.length];
        for (int i = 0; i < valores.length; i++) {
            resultado[i] = (Cliente) valores[i];
        }
        return resultado;
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // BÚSQUEDAS ESPECIALIZADAS (Delegadas a IndiceClientes)
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Busca clientes por scoring usando ABB con lazy loading.
     * Delegado a IndiceClientes.
     * Complejidad: O(N) en primera llamada (build), O(log 101 + k) en siguientes
     */
    public Cliente[] buscarPorScoring(int scoring) {
        return indices.buscarPorScoring(scoring);
    }

    /**
     * Obtiene clientes en nivel N del árbol de scoring.
     * Delegado a IndiceClientes.
     * Complejidad: O(N) una sola vez
     */
    public Cliente[] obtenerClientesEnNivel(int nivel) {
        return indices.obtenerClientesEnNivel(nivel);
    }

    /**
     * Obtiene los top N clientes por cantidad de seguidores.
     *
     * Usa un buffer ordenado descendente de tamaño fijo (top).
     * Un solo recorrido sobre N clientes: cada uno se compara contra el mínimo
     * del buffer. Si no lo supera, se descarta en O(1). Si lo supera, se inserta
     * en posición ordenada desplazando elementos → O(top) por inserción.
     *
     * Complejidad: O(N) amortizado (la mayoría se descarta en O(1))
     * Peor caso teórico: O(N * top) si todos tienen seguidores crecientes
     */
    public Cliente[] obtenerClientesMasPopulares(int top) {
        Object[] valores = clientes.obtenerValores();
        if (valores.length == 0 || top <= 0) return new Cliente[0];

        int capacidad = Math.min(top, valores.length);
        Cliente[] mejores = new Cliente[capacidad];
        int[] conteos = new int[capacidad];  // seguidores cacheados para evitar recalcular
        int llenos = 0;

        for (Object obj : valores) {
            Cliente c = (Cliente) obj;
            int segs = c.getCantidadSeguidores();

            if (llenos < capacidad) {
                // Buffer no lleno: insertar en posición ordenada descendente
                int pos = llenos;
                while (pos > 0 && segs > conteos[pos - 1]) {
                    mejores[pos] = mejores[pos - 1];
                    conteos[pos] = conteos[pos - 1];
                    pos--;
                }
                mejores[pos] = c;
                conteos[pos] = segs;
                llenos++;
            } else if (segs > conteos[capacidad - 1]) {
                // Supera al mínimo del buffer: insertar desplazando
                int pos = capacidad - 1;
                while (pos > 0 && segs > conteos[pos - 1]) {
                    mejores[pos] = mejores[pos - 1];
                    conteos[pos] = conteos[pos - 1];
                    pos--;
                }
                mejores[pos] = c;
                conteos[pos] = segs;
            }
            // else: no supera el mínimo → descartado en O(1)
        }

        if (llenos < capacidad) {
            Cliente[] resultado = new Cliente[llenos];
            for (int i = 0; i < llenos; i++) {
                resultado[i] = mejores[i];
            }
            return resultado;
        }
        return mejores;
    }

    /**
     * Elimina un cliente del sistema.
     * Limpia índices y referencias en cascada usando índices bidireccionales.
     *
     * Complejidad: O(seguidores + siguiendo + amigos) gracias a índices bidireccionales
     */
    public boolean eliminarCliente(int id) {
        Cliente cliente = clientes.obtener(id);
        if (cliente == null) return false;

        clientes.eliminar(id);
        indices.eliminarCliente(cliente);

        // Limpiar referencias en cascada usando índices bidireccionales
        // Solo tocamos los afectados: O(seguidores + siguiendo) en vez de O(N)

        int[] idsSeguidores = cliente.getSeguidores();
        for (int idSeguidor : idsSeguidores) {
            Cliente seguidor = clientes.obtener(idSeguidor);
            if (seguidor != null) {
                seguidor.dejarDeSeguir(id);
            }
        }

        int[] idsSeguidos = cliente.getSiguiendo();
        for (int idSeguido : idsSeguidos) {
            Cliente seguido = clientes.obtener(idSeguido);
            if (seguido != null) {
                seguido.eliminarSeguidor(id);
            }
        }

        // Limpiar amistades bidireccionales (Iteración 3)
        int[] idsAmigos = cliente.obtenerAmigos();
        for (int idAmigo : idsAmigos) {
            Cliente amigo = clientes.obtener(idAmigo);
            if (amigo != null) {
                amigo.eliminarAmistad(id);
            }
        }

        return true;
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // RELACIONES (Delegadas a GestorRelaciones)
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Registra que un cliente sigue a otro.
     * Delegado a GestorRelaciones. Registra en historial si hay sesión activa.
     * Complejidad: O(1) amortizado
     *
     * @param idSolicitante ID del cliente que inicia el seguimiento
     * @param idObjetivo ID del cliente a seguir
     * @return true si se estableció la relación, false si falló
     */
    public boolean seguir(int idSolicitante, int idObjetivo) {
        boolean resultado = gestorRelaciones.seguir(idSolicitante, idObjetivo);
        if (resultado && sesionValida()) {
            Accion accion = new Accion(TipoAccion.SEGUIR,
                String.valueOf(idSolicitante),
                String.valueOf(idObjetivo));
            getSesion().getHistorial().registrar(accion);
        }
        return resultado;
    }

    /**
     * Registra que un cliente deja de seguir a otro.
     * Delegado a GestorRelaciones. Registra en historial si hay sesión activa.
     * Complejidad: O(1) amortizado
     */
    public boolean dejarDeSeguir(int idSolicitante, int idObjetivo) {
        boolean resultado = gestorRelaciones.dejarDeSeguir(idSolicitante, idObjetivo);
        if (resultado && sesionValida()) {
            Accion accion = new Accion(TipoAccion.DEJAR_DE_SEGUIR,
                String.valueOf(idSolicitante),
                String.valueOf(idObjetivo));
            getSesion().getHistorial().registrar(accion);
        }
        return resultado;
    }

    /**
     * Envía una solicitud de seguimiento a otro cliente.
     */
    public boolean enviarSolicitud(int idSolicitante, int idObjetivo) {
        Cliente solicitante = clientes.obtener(idSolicitante);
        Cliente objetivo = clientes.obtener(idObjetivo);

        if (solicitante == null || objetivo == null) return false;

        modelo.SolicitudSeguimiento solicitud = new modelo.SolicitudSeguimiento(
            String.valueOf(idSolicitante),
            String.valueOf(idObjetivo));

        objetivo.recibirSolicitud(solicitud);
        return true;
    }

    /**
     * Acepta una solicitud de seguimiento.
     */
    public boolean aceptarSolicitud(Cliente solicitante, Cliente objetivo, modelo.SolicitudSeguimiento solicitud) {
        if (solicitante == null || objetivo == null || solicitud == null) return false;
        return seguir(solicitante.getId(), objetivo.getId());
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // ITERACIÓN 2: FUNCIONALIDADES DE RELACIONES (Delegadas a GestorRelaciones)
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Obtiene los clientes que un cliente está siguiendo.
     * Delegado a GestorRelaciones.
     */
    public Cliente[] obtenerVecinos(int idCliente) {
        return gestorRelaciones.obtenerVecinos(idCliente);
    }

    /**
     * Construye un ABB con los seguidores de un cliente.
     * Delegado a GestorRelaciones.
     */
    public ArbolBinarioBusqueda<Integer, Cliente> construirArbolRelaciones(int idCliente) {
        return gestorRelaciones.construirArbolRelaciones(idCliente);
    }

    /**
     * Obtiene seguidores en nivel específico del árbol.
     * Delegado a GestorRelaciones.
     */
    public Cliente[] obtenerSeguidoresEnNivel(int idCliente, int nivel) {
        return gestorRelaciones.obtenerSeguidoresEnNivel(idCliente, nivel);
    }

    /**
     * Obtiene seguidores ordenados por scoring.
     * Delegado a GestorRelaciones.
     */
    public Cliente[] obtenerSeguidoresOrdenados(int idCliente) {
        return gestorRelaciones.obtenerSeguidoresOrdenados(idCliente);
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // ITERACIÓN 3: DISTANCIA (BFS)
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Calcula la distancia (número de saltos) entre dos clientes en la red.
     * Delegado a GestorRelaciones.
     * Complejidad: O(V + E)
     */
    public int calcularDistancia(int idOrigen, int idDestino) {
        return gestorRelaciones.calcularDistancia(idOrigen, idDestino);
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // ITERACIÓN 3: AMISTADES (RELACIONES BIDIRECCIONALES)
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Establece una amistad bidireccional entre dos clientes.
     * Delegado a GestorRelaciones.
     */
    public boolean agregarAmistad(int idCliente1, int idCliente2) {
        return gestorRelaciones.agregarAmistad(idCliente1, idCliente2);
    }

    /**
     * Elimina una amistad bidireccional entre dos clientes.
     * Delegado a GestorRelaciones.
     */
    public boolean eliminarAmistad(int idCliente1, int idCliente2) {
        return gestorRelaciones.eliminarAmistad(idCliente1, idCliente2);
    }

    /**
     * Obtiene todos los amigos de un cliente.
     * Complejidad: O(k) donde k = cantidad de amigos
     */
    public Cliente[] obtenerAmigos(int idCliente) {
        return gestorRelaciones.obtenerAmigos(idCliente);
    }

    /**
     * Obtiene la cantidad de amigos de un cliente.
     * Complejidad: O(1)
     */
    public int obtenerCantidadAmigos(int idCliente) {
        return gestorRelaciones.obtenerCantidadAmigos(idCliente);
    }

    /**
     * Verifica si dos clientes son amigos.
     * Complejidad: O(1)
     */
    public boolean sonAmigos(int idCliente1, int idCliente2) {
        return gestorRelaciones.sonAmigos(idCliente1, idCliente2);
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // UNDO (Historial de Acciones)
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Deshace la última acción registrada en el historial.
     * Complejidad: O(costo de la operación inversa)
     */
    public Accion deshacer() {
        if (!sesionValida()) return null;
        HistorialAcciones historial = getSesion().getHistorial();

        if (historial.estaVacio()) return null;

        Accion accion = historial.extraerUltima();
        ejecutarUndo(accion);
        return accion;
    }

    /**
     * Ejecuta la lógica inversa de una acción.
     * Delega a métodos específicos según el tipo de acción.
     */
    private void ejecutarUndo(Accion accion) {
        switch (accion.getTipo()) {
            case SEGUIR:
                undoSeguir(accion.getDatos());
                break;
            case DEJAR_DE_SEGUIR:
                undoDejarDeSeguir(accion.getDatos());
                break;
            default:
                break;
        }
    }

    /**
     * Undo de SEGUIR: deshace la relación de seguimiento.
     * datos[0] = ID seguidor, datos[1] = ID seguido
     */
    private void undoSeguir(String[] datos) {
        int idSeguidor = Integer.parseInt(datos[0]);
        int idSeguido = Integer.parseInt(datos[1]);
        Cliente seguidor = clientes.obtener(idSeguidor);
        Cliente seguido = clientes.obtener(idSeguido);
        if (seguidor != null) seguidor.dejarDeSeguir(idSeguido);
        if (seguido != null) seguido.eliminarSeguidor(idSeguidor);
    }

    /**
     * Undo de DEJAR_DE_SEGUIR: restaura la relación de seguimiento.
     * datos[0] = ID ex-seguidor, datos[1] = ID ex-seguido
     */
    private void undoDejarDeSeguir(String[] datos) {
        int idSeguidor = Integer.parseInt(datos[0]);
        int idSeguido = Integer.parseInt(datos[1]);
        Cliente seguidor = clientes.obtener(idSeguidor);
        Cliente seguido = clientes.obtener(idSeguido);
        if (seguidor != null) seguidor.seguir(idSeguido);
        if (seguido != null) seguido.agregarSeguidor(idSeguidor);
    }

    /**
     * Obtiene la última acción registrada sin eliminarla.
     */
    public Accion verUltimaAccion() {
        if (!sesionValida()) return null;
        return getSesion().getHistorial().verUltima();
    }

    /**
     * Verifica si el historial de acciones está vacío.
     */
    public boolean historialVacio() {
        if (!sesionValida()) return true;
        return getSesion().getHistorial().estaVacio();
    }

    /**
     * Obtiene la cantidad total de acciones registradas.
     */
    public int getCantidadAcciones() {
        if (!sesionValida()) return 0;
        return getSesion().getHistorial().getCantidad();
    }

    /**
     * Obtiene todas las acciones registradas.
     */
    public Accion[] obtenerHistorialCompleto() {
        if (!sesionValida()) return new Accion[0];
        return getSesion().getHistorial().obtenerTodas();
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // UTILIDADES PRIVADAS
    // ════════════════════════════════════════════════════════════════════════════════════

    private Sesion getSesion() {
        return Sesion.getInstancia();
    }

    private boolean sesionValida() {
        return getSesion().estaAutenticado();
    }

    /**
     * Obtiene acceso directo al diccionario de clientes.
     * Complejidad: O(1)
     */
    public Diccionario<Integer, Cliente> getClientes() {
        return clientes;
    }
}
