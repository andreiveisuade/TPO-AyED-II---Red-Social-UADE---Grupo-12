package servicio;

import modelo.Cliente;
import modelo.Accion;
import modelo.TipoAccion;
import util.Validador;
import modelo.Sesion;
import util.ResultadoValidacion;
import tda.Diccionario;
import tda.ArbolBinarioBusqueda;
import tda.Cola;

/**
 * Gestión centralizada de clientes en la red social.
 *
 * RESPONSABILIDAD PRINCIPAL: Coordinar operaciones CRUD de clientes y búsquedas.
 * Delega operaciones especializadas a servicios específicos.
 *
 * ARQUITECTURA MODULAR:
 * ┌─────────────────────────────────────────────────┐
 * │          GestorClientes (Coordinador)           │
 * ├─────────────────────────────────────────────────┤
 * │ - CRUD de clientes                              │
 * │ - Búsquedas (ID, nombre, scoring)              │
 * │ - Gestiona índices                              │
 * │ - Delegación a servicios                         │
 * └─────────────────────────────────────────────────┘
 *         ↓ delega                  ↓ delega
 *    GestorRelaciones          PersistenciaClientes
 *    (Relaciones)              (JSON I/O)
 *
 * INVARIANTES DE REPRESENTACIÓN:
 * - clientes != null
 * - Todos los IDs son > 0
 * - Todos los clientes tienen scoring entre 0 y 100
 * - Índices son consistentes con diccionario principal
 *
 * PRINCIPIOS SOLID:
 * - SRP: Solo coordina clientes y búsquedas
 * - DIP: Depende de abstracciones (Cliente)
 *
 * PRINCIPIOS GRASP:
 * - Information Expert: Conoce el diccionario de clientes
 * - Creator: Crea instancias de Cliente
 * - Controller: Coordina acciones sobre clientes
 */
public class GestorClientes {

    // ÍNDICES PRIMARIOS Y SECUNDARIOS
    private Diccionario<Integer, Cliente> clientes;  // Índice primario por ID - O(1)
    private tda.ArbolBinarioBusqueda<Integer, Cliente> indiceScoring;  // Índice secundario por scoring (lazy loading)
    private boolean scoringIndexConstructed = false;  // Flag para lazy loading
    private Diccionario<String, Cola<Cliente>> indiceNombre;  // Índice secundario por nombre - O(1)

    // SERVICIOS DELEGADOS
    private GestorRelaciones gestorRelaciones;  // Gestión de relaciones entre clientes
    private PersistenciaClientes persistencia;  // Persistencia en JSON

    // ESTADO
    private boolean registrarEnHistorial;
    private int proximoId;

    // CONSTANTES
    private static final String DEFAULT_PATH = "data/clientes_1M.json";
    private static final int CAPACIDAD_DICCIONARIO = 1000003;

    // ════════════════════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ════════════════════════════════════════════════════════════════════════════════════

    public GestorClientes() {
        this(DEFAULT_PATH);
    }

    /**
     * Constructor principal.
     * Inicializa todos los índices y servicios, carga datos desde archivo.
     *
     * Complejidad: O(N) donde N = cantidad de clientes
     */
    public GestorClientes(String dbPath) {
        this.registrarEnHistorial = true;
        this.proximoId = 1001;
        this.indiceScoring = new ArbolBinarioBusqueda<>();
        this.indiceNombre = new Diccionario<>(CAPACIDAD_DICCIONARIO);

        // Inicializar servicios
        this.persistencia = new PersistenciaClientes(dbPath);

        // Cargar clientes
        this.clientes = persistencia.cargarDesdeArchivo(CAPACIDAD_DICCIONARIO);

        // Construir índice de nombre (rápido - O(1) por cliente)
        construirIndiceNombre();

        // Actualizar proximoId basado en clientes cargados
        actualizarProximoId();

        // Inicializar gestor de relaciones
        this.gestorRelaciones = new GestorRelaciones(clientes);
    }

    /**
     * Construye el índice secundario de nombre.
     * Complejidad: O(N) donde N = cantidad de clientes
     */
    private void construirIndiceNombre() {
        Object[] valores = clientes.obtenerValores();
        for (Object obj : valores) {
            Cliente c = (Cliente) obj;
            agregarAlIndiceNombre(c);
        }
    }

    /**
     * Actualiza el próximo ID basado en clientes existentes.
     * Complejidad: O(N) donde N = cantidad de clientes
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
     * Registra la acción en historial si está habilitado.
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
        agregarAlIndiceNombre(cliente);

        if (registrarEnHistorial && sesionValida()) {
            Accion accion = new Accion(TipoAccion.AGREGAR_CLIENTE, String.valueOf(id));
            getSesion().getHistorial().registrar(accion);
        }

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
        agregarAlIndiceNombre(cliente);

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
     * Complejidad: O(1) + O(k) donde k = clientes con ese nombre
     */
    public Cliente[] buscarPorNombre(String nombre) {
        if (nombre == null) return new Cliente[0];

        String clave = nombre.toLowerCase();
        Cola<Cliente> cola = indiceNombre.obtener(clave);

        if (cola == null || cola.estaVacia()) {
            return new Cliente[0];
        }

        // Copiar la cola a un array sin modificarla
        int cantidad = cola.getCantidad();
        Cliente[] resultado = new Cliente[cantidad];
        Cola<Cliente> aux = new Cola<>();
        int i = 0;
        while (!cola.estaVacia()) {
            Cliente c = cola.desencolar();
            resultado[i++] = c;
            aux.encolar(c);
        }
        // Restaurar la cola original
        while (!aux.estaVacia()) {
            cola.encolar(aux.desencolar());
        }
        return resultado;
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
    // BÚSQUEDAS ESPECIALIZADAS
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Construye el índice de scoring bajo demanda (lazy loading).
     * Se ejecuta una sola vez la primera vez que se necesita.
     *
     * Complejidad: O(N log N) amortizado en todas las búsquedas
     */
    private void construirIndiceScoringLazy() {
        if (scoringIndexConstructed) return;

        Object[] valores = clientes.obtenerValores();
        for (Object obj : valores) {
            Cliente c = (Cliente) obj;
            indiceScoring.insertar(c.getScoring(), c);
        }
        scoringIndexConstructed = true;
    }

    /**
     * Busca clientes por su scoring usando ABB con lazy loading.
     * Complejidad: O(N log N) en primera llamada, O(log N + k) en siguientes
     */
    public Cliente[] buscarPorScoring(int scoring) {
        construirIndiceScoringLazy();
        Object[] resultados = indiceScoring.buscar(scoring);
        Cliente[] clientes = new Cliente[resultados.length];
        for (int i = 0; i < resultados.length; i++) {
            clientes[i] = (Cliente) resultados[i];
        }
        return clientes;
    }

    /**
     * Obtiene clientes en nivel N del árbol de scoring.
     * Complejidad: O(N) una sola vez
     */
    public Cliente[] obtenerClientesEnNivel(int nivel) {
        if (nivel < 0) return new Cliente[0];

        construirIndiceScoringLazy();
        Object[] resultados = indiceScoring.obtenerEnNivel(nivel);
        Cliente[] clientes = new Cliente[resultados.length];
        for (int i = 0; i < resultados.length; i++) {
            clientes[i] = (Cliente) resultados[i];
        }
        return clientes;
    }

    /**
     * Obtiene los top N clientes por cantidad de seguidores.
     * Complejidad: O(N²) en peor caso, pero típicamente O(N * top)
     */
    public Cliente[] obtenerClientesMasPopulares(int top) {
        Cliente[] todos = obtenerTodosLosClientes();

        // Selection sort - solo top N
        for (int i = 0; i < todos.length - 1 && i < top; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < todos.length; j++) {
                if (todos[j].getCantidadSeguidores() > todos[maxIdx].getCantidadSeguidores()) {
                    maxIdx = j;
                }
            }
            // Swap
            Cliente temp = todos[i];
            todos[i] = todos[maxIdx];
            todos[maxIdx] = temp;
        }

        // Retornar top N
        int cantidad = Math.min(top, todos.length);
        Cliente[] resultado = new Cliente[cantidad];
        for (int i = 0; i < cantidad; i++) {
            resultado[i] = todos[i];
        }
        return resultado;
    }

    /**
     * Elimina un cliente del sistema.
     * Limpia referencias en otros clientes.
     *
     * Complejidad: O(N) donde N = cantidad de clientes
     */
    public boolean eliminarCliente(int id) {
        Cliente cliente = clientes.obtener(id);
        if (cliente == null) return false;

        // Registrar en historial
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
                seguidos.toString());
            getSesion().getHistorial().registrar(accion);
        }

        clientes.eliminar(id);
        if (scoringIndexConstructed) {
            indiceScoring.eliminar(cliente.getScoring(), cliente);
        }
        eliminarDelIndiceNombre(cliente);

        // Limpiar referencias en cascada
        Object[] todosLosClientes = clientes.obtenerValores();
        for (Object obj : todosLosClientes) {
            Cliente c = (Cliente) obj;
            c.dejarDeSeguir(id);
        }
        return true;
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // RELACIONES (Delegadas a GestorRelaciones)
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Registra que un cliente sigue a otro.
     * Delegado a GestorRelaciones.
     */
    /**
     * Registra que un cliente sigue a otro.
     * Delegado a GestorRelaciones y registra en historial si está habilitado.
     * Complejidad: O(1) amortizado
     *
     * @param idSolicitante ID del cliente que inicia el seguimiento
     * @param idObjetivo ID del cliente a seguir
     * @return true si se estableció la relación, false si falló
     */
    public boolean seguir(int idSolicitante, int idObjetivo) {
        boolean resultado = gestorRelaciones.seguir(idSolicitante, idObjetivo);
        if (resultado && registrarEnHistorial && sesionValida()) {
            Accion accion = new Accion(TipoAccion.SEGUIR,
                String.valueOf(idSolicitante),
                String.valueOf(idObjetivo));
            getSesion().getHistorial().registrar(accion);
        }
        return resultado;
    }

    /**
     * Registra que un cliente deja de seguir a otro.
     * Delegado a GestorRelaciones.
     */
    public boolean dejarDeSeguir(int idSolicitante, int idObjetivo) {
        boolean resultado = gestorRelaciones.dejarDeSeguir(idSolicitante, idObjetivo);
        if (resultado && registrarEnHistorial && sesionValida()) {
            Accion accion = new Accion(TipoAccion.DEJAR_DE_SEGUIR,
                String.valueOf(idSolicitante),
                String.valueOf(idObjetivo));
            getSesion().getHistorial().registrar(accion);
        }
        return resultado;
    }

    /**
     * Procesa una solicitud de seguimiento.
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

    /**
     * Calcula la distancia (número de saltos) entre dos clientes en la red.
     * Delegado a GestorRelaciones.
     *
     * Complejidad: O(V + E) donde V = clientes alcanzables, E = relaciones
     *
     * @param idOrigen  ID del cliente de partida
     * @param idDestino ID del cliente destino
     * @return Número de saltos mínimo, 0 si son el mismo, -1 si no hay camino
     */
    public int calcularDistancia(int idOrigen, int idDestino) {
        return gestorRelaciones.calcularDistancia(idOrigen, idDestino);
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // UNDO/REDO (Historial de Acciones)
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Activa el registro de acciones en el historial.
     * Complejidad: O(1)
     */
    public void activarHistorial() {
        this.registrarEnHistorial = true;
    }

    /**
     * Desactiva el registro de acciones en el historial.
     * Complejidad: O(1)
     */
    public void desactivarHistorial() {
        this.registrarEnHistorial = false;
    }

    /**
     * Deshace la última acción.
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
     */
    private void ejecutarUndo(Accion accion) {
        String[] datos = accion.getDatos();

        switch (accion.getTipo()) {
            case AGREGAR_CLIENTE:
                int idEliminar = Integer.parseInt(datos[0]);
                Cliente cEliminar = clientes.obtener(idEliminar);
                if (cEliminar != null) {
                    clientes.eliminar(idEliminar);
                    eliminarDelIndiceNombre(cEliminar);
                }
                break;
            case ELIMINAR_CLIENTE:
                int idRestaurar = Integer.parseInt(datos[0]);
                Cliente restaurado = new Cliente(idRestaurar, datos[1], Integer.parseInt(datos[2]));
                clientes.insertar(idRestaurar, restaurado);
                agregarAlIndiceNombre(restaurado);
                if (datos.length > 3 && !datos[3].isEmpty()) {
                    for (String seguido : datos[3].split(",")) {
                        int idSeguidoR = Integer.parseInt(seguido);
                        restaurado.seguir(idSeguidoR);
                        Cliente seguidoCliente = clientes.obtener(idSeguidoR);
                        if (seguidoCliente != null) seguidoCliente.agregarSeguidor(idRestaurar);
                    }
                }
                break;
            case SEGUIR:
                int idSeguidor = Integer.parseInt(datos[0]);
                int idSeguido = Integer.parseInt(datos[1]);
                Cliente cSeguidor = clientes.obtener(idSeguidor);
                Cliente cSeguido = clientes.obtener(idSeguido);
                if (cSeguidor != null) cSeguidor.dejarDeSeguir(idSeguido);
                if (cSeguido != null) cSeguido.eliminarSeguidor(idSeguidor);
                break;
            case DEJAR_DE_SEGUIR:
                int idExSeguidor = Integer.parseInt(datos[0]);
                int idExSeguido = Integer.parseInt(datos[1]);
                Cliente cExSeguidor = clientes.obtener(idExSeguidor);
                Cliente cExSeguido = clientes.obtener(idExSeguido);
                if (cExSeguidor != null) cExSeguidor.seguir(idExSeguido);
                if (cExSeguido != null) cExSeguido.agregarSeguidor(idExSeguidor);
                break;
            default:
                break;
        }
    }

    /**
     * Obtiene la última acción registrada sin eliminarla.
     * Complejidad: O(1)
     *
     * @return Última acción o null si no hay autenticación o historial vacío
     */
    public Accion verUltimaAccion() {
        if (!sesionValida()) return null;
        return getSesion().getHistorial().verUltima();
    }

    /**
     * Verifica si el historial de acciones está vacío.
     * Complejidad: O(1)
     *
     * @return true si no hay acciones registradas o sin autenticación
     */
    public boolean historialVacio() {
        if (!sesionValida()) return true;
        return getSesion().getHistorial().estaVacio();
    }

    /**
     * Obtiene la cantidad total de acciones registradas.
     * Complejidad: O(1)
     *
     * @return Cantidad de acciones, o 0 si sin autenticación
     */
    public int getCantidadAcciones() {
        if (!sesionValida()) return 0;
        return getSesion().getHistorial().getCantidad();
    }

    /**
     * Obtiene todas las acciones registradas en orden de ejecución.
     * Complejidad: O(N) donde N = cantidad de acciones
     *
     * @return Array de todas las acciones, o array vacío si sin autenticación
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
     * Agrega un cliente al índice secundario por nombre.
     * Complejidad: O(1) amortizado
     */
    private void agregarAlIndiceNombre(Cliente cliente) {
        String clave = cliente.getNombre().toLowerCase();
        Cola<Cliente> cola = indiceNombre.obtener(clave);
        if (cola == null) {
            cola = new Cola<>();
            indiceNombre.insertar(clave, cola);
        }
        cola.encolar(cliente);
    }

    /**
     * Elimina un cliente del índice secundario por nombre.
     * Complejidad: O(k) donde k = clientes con ese nombre
     */
    private void eliminarDelIndiceNombre(Cliente cliente) {
        String clave = cliente.getNombre().toLowerCase();
        Cola<Cliente> cola = indiceNombre.obtener(clave);
        if (cola == null) return;

        Cola<Cliente> nueva = new Cola<>();
        while (!cola.estaVacia()) {
            Cliente c = cola.desencolar();
            if (c.getId() != cliente.getId()) {
                nueva.encolar(c);
            }
        }
        if (nueva.estaVacia()) {
            indiceNombre.eliminar(clave);
        } else {
            indiceNombre.insertar(clave, nueva);
        }
    }

    /**
     * Obtiene acceso directo al diccionario de clientes.
     * Úsese con cautela para no romper invariantes.
     *
     * Complejidad: O(1)
     *
     * @return Diccionario primario de clientes indexado por ID
     */
    public Diccionario<Integer, Cliente> getClientes() {
        return clientes;
    }
}
