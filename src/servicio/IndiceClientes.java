package servicio;

import modelo.Cliente;
import tda.ArbolBinarioBusqueda;
import tda.Cola;
import tda.Diccionario;

/**
 * Gestiona los índices secundarios de clientes para búsquedas eficientes.
 *
 * RESPONSABILIDAD: Mantener sincronizados los índices de scoring y nombre
 * con el diccionario principal de clientes.
 *
 * ÍNDICES MANTENIDOS:
 * - Scoring: ABB<Integer, Cola<Cliente>> — máx 101 nodos, lazy loading
 * - Nombre:  Diccionario<String, Cola<Cliente>> — hash O(1), eager loading
 *
 * PRINCIPIOS SOLID:
 * - SRP: Solo gestiona indexación, no CRUD ni relaciones
 *
 * PRINCIPIOS GRASP:
 * - Information Expert: Conoce las estructuras de indexación y cómo mantenerlas
 * - Low Coupling: No conoce persistencia, historial ni relaciones
 * - High Cohesion: Todos los métodos están relacionados con indexación
 */
public class IndiceClientes {

    // Índice secundario por scoring — ABB con Cola por nodo (lazy loading)
    private ArbolBinarioBusqueda<Integer, Cola<Cliente>> indiceScoring;
    private boolean scoringIndexConstructed = false;

    // Índice secundario por nombre — Hash con Cola por clave (eager loading)
    private Diccionario<String, Cola<Cliente>> indiceNombre;

    // Referencia al diccionario principal (para construir índice scoring lazy)
    private Diccionario<Integer, Cliente> clientes;

    /**
     * Constructor. Inicializa los índices y construye el índice de nombre.
     * El índice de scoring se construye bajo demanda (lazy loading).
     *
     * Complejidad: O(N) donde N = cantidad de clientes
     *
     * @param clientes Referencia al diccionario principal de clientes
     * @param capacidad Capacidad inicial para el diccionario de nombres
     */
    public IndiceClientes(Diccionario<Integer, Cliente> clientes, int capacidad) {
        this.clientes = clientes;
        this.indiceScoring = new ArbolBinarioBusqueda<>();
        this.indiceNombre = new Diccionario<>(capacidad);
        construirIndiceNombre();
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // CONSTRUCCIÓN DE ÍNDICES
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Construye el índice de nombre recorriendo todos los clientes.
     * Complejidad: O(N)
     */
    private void construirIndiceNombre() {
        Object[] valores = clientes.obtenerValores();
        for (Object obj : valores) {
            Cliente c = (Cliente) obj;
            agregarAlIndiceNombre(c);
        }
    }

    /**
     * Construye el índice de scoring bajo demanda (lazy loading).
     * Cada nodo del ABB almacena una Cola con todos los clientes de ese scoring.
     * Máximo 101 nodos (scoring 0-100) → O(N log 101) = O(N).
     *
     * Complejidad: O(N) donde N = cantidad de clientes
     */
    private void construirIndiceScoringLazy() {
        if (scoringIndexConstructed) return;

        Object[] valores = clientes.obtenerValores();
        for (Object obj : valores) {
            Cliente c = (Cliente) obj;
            agregarAlIndiceScoring(c);
        }
        scoringIndexConstructed = true;
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // OPERACIONES DE MANTENIMIENTO (agregar/eliminar cliente de los índices)
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Agrega un cliente a todos los índices secundarios.
     * Complejidad: O(log 101 + p) donde p = partes del nombre
     */
    public void agregarCliente(Cliente cliente) {
        agregarAlIndiceNombre(cliente);
        if (scoringIndexConstructed) {
            agregarAlIndiceScoring(cliente);
        }
    }

    /**
     * Elimina un cliente de todos los índices secundarios.
     * Complejidad: O(log 101 + p * k)
     */
    public void eliminarCliente(Cliente cliente) {
        eliminarDelIndiceNombre(cliente);
        if (scoringIndexConstructed) {
            eliminarDelIndiceScoring(cliente);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // BÚSQUEDAS
    // ════════════════════════════════════════════════════════════════════════════════════

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

        return colaClientesAArray(cola);
    }

    /**
     * Busca clientes por su scoring usando ABB con lazy loading.
     * Complejidad: O(N) en primera llamada (build), O(log 101 + k) en siguientes
     */
    public Cliente[] buscarPorScoring(int scoring) {
        construirIndiceScoringLazy();
        Object[] resultados = indiceScoring.buscar(scoring);
        if (resultados.length == 0) return new Cliente[0];

        @SuppressWarnings("unchecked")
        Cola<Cliente> cola = (Cola<Cliente>) resultados[0];
        return colaClientesAArray(cola);
    }

    /**
     * Obtiene clientes en nivel N del árbol de scoring.
     * Cada nodo del nivel contiene una Cola; este método aplana todas las Colas.
     * Complejidad: O(N) una sola vez
     */
    public Cliente[] obtenerClientesEnNivel(int nivel) {
        if (nivel < 0) return new Cliente[0];

        construirIndiceScoringLazy();
        Object[] nodos = indiceScoring.obtenerEnNivel(nivel);

        Cola<Cliente> aplanado = new Cola<>();
        for (Object obj : nodos) {
            @SuppressWarnings("unchecked")
            Cola<Cliente> cola = (Cola<Cliente>) obj;
            Cola<Cliente> aux = new Cola<>();
            while (!cola.estaVacia()) {
                Cliente c = cola.desencolar();
                aplanado.encolar(c);
                aux.encolar(c);
            }
            while (!aux.estaVacia()) {
                cola.encolar(aux.desencolar());
            }
        }
        return colaClientesAArray(aplanado);
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // HELPERS PRIVADOS — ÍNDICE DE NOMBRE
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Agrega un cliente al índice de nombre.
     * Indexa por nombre completo y por cada parte individual (nombre, apellido).
     * Complejidad: O(p) amortizado donde p = partes del nombre
     */
    private void agregarAlIndiceNombre(Cliente cliente) {
        String nombreCompleto = cliente.getNombre().toLowerCase();

        agregarAlIndiceNombreClave(nombreCompleto, cliente);

        String[] partes = nombreCompleto.split("\\s+");
        if (partes.length > 1) {
            for (String parte : partes) {
                if (!parte.isEmpty()) {
                    agregarAlIndiceNombreClave(parte, cliente);
                }
            }
        }
    }

    /**
     * Agrega un cliente a una clave específica del índice de nombre.
     * Complejidad: O(1) amortizado
     */
    private void agregarAlIndiceNombreClave(String clave, Cliente cliente) {
        Cola<Cliente> cola = indiceNombre.obtener(clave);
        if (cola == null) {
            cola = new Cola<>();
            indiceNombre.insertar(clave, cola);
        }
        cola.encolar(cliente);
    }

    /**
     * Elimina un cliente del índice de nombre.
     * Complejidad: O(p * k)
     */
    private void eliminarDelIndiceNombre(Cliente cliente) {
        String nombreCompleto = cliente.getNombre().toLowerCase();

        eliminarDelIndiceNombreClave(nombreCompleto, cliente);

        String[] partes = nombreCompleto.split("\\s+");
        if (partes.length > 1) {
            for (String parte : partes) {
                if (!parte.isEmpty()) {
                    eliminarDelIndiceNombreClave(parte, cliente);
                }
            }
        }
    }

    /**
     * Elimina un cliente de una clave específica del índice de nombre.
     * Complejidad: O(k)
     */
    private void eliminarDelIndiceNombreClave(String clave, Cliente cliente) {
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

    // ════════════════════════════════════════════════════════════════════════════════════
    // HELPERS PRIVADOS — ÍNDICE DE SCORING
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Agrega un cliente al índice de scoring (ABB<Integer, Cola<Cliente>>).
     * Si ya existe un nodo con ese scoring, encola en su Cola.
     * Complejidad: O(log 101) = O(1)
     */
    private void agregarAlIndiceScoring(Cliente cliente) {
        int scoring = cliente.getScoring();
        Object[] existente = indiceScoring.buscar(scoring);
        if (existente.length > 0) {
            @SuppressWarnings("unchecked")
            Cola<Cliente> cola = (Cola<Cliente>) existente[0];
            cola.encolar(cliente);
        } else {
            Cola<Cliente> cola = new Cola<>();
            cola.encolar(cliente);
            indiceScoring.insertar(scoring, cola);
        }
    }

    /**
     * Elimina un cliente del índice de scoring.
     * Si la Cola queda vacía, elimina el nodo del ABB.
     * Complejidad: O(log 101 + k)
     */
    private void eliminarDelIndiceScoring(Cliente cliente) {
        int scoring = cliente.getScoring();
        Object[] existente = indiceScoring.buscar(scoring);
        if (existente.length == 0) return;

        @SuppressWarnings("unchecked")
        Cola<Cliente> cola = (Cola<Cliente>) existente[0];
        Cola<Cliente> nueva = new Cola<>();
        while (!cola.estaVacia()) {
            Cliente c = cola.desencolar();
            if (c.getId() != cliente.getId()) {
                nueva.encolar(c);
            }
        }
        if (nueva.estaVacia()) {
            indiceScoring.eliminar(scoring, cola);
        } else {
            while (!nueva.estaVacia()) {
                cola.encolar(nueva.desencolar());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Convierte una Cola<Cliente> a Cliente[] sin destruir la cola.
     * Complejidad: O(k) donde k = tamaño de la cola
     */
    private Cliente[] colaClientesAArray(Cola<Cliente> cola) {
        int cantidad = cola.getCantidad();
        Cliente[] resultado = new Cliente[cantidad];
        Cola<Cliente> aux = new Cola<>();
        int i = 0;
        while (!cola.estaVacia()) {
            Cliente c = cola.desencolar();
            resultado[i++] = c;
            aux.encolar(c);
        }
        while (!aux.estaVacia()) {
            cola.encolar(aux.desencolar());
        }
        return resultado;
    }
}
