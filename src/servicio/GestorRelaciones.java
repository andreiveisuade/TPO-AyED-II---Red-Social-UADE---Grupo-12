package servicio;

import modelo.Cliente;
import tda.ArbolBinarioBusqueda;
import tda.Diccionario;

/**
 * Gestiona relaciones entre clientes en la red social.
 *
 * RESPONSABILIDAD: Encapsular toda la lógica de relaciones (seguimientos, seguidores, árbol de conexiones).
 *
 * SOLID:
 * - SRP: Única responsabilidad es gestionar relaciones entre clientes
 * - DIP: Depende de abstracciones (Cliente)
 *
 * GRASP:
 * - Information Expert: Conoce las relaciones entre clientes
 * - Low Coupling: No conoce detalles de persistencia ni acciones
 */
public class GestorRelaciones {

    private Diccionario<Integer, Cliente> clientes;  // Referencia al diccionario de clientes

    /**
     * Constructor que recibe la referencia al diccionario de clientes.
     * Complejidad: O(1)
     */
    public GestorRelaciones(Diccionario<Integer, Cliente> clientes) {
        this.clientes = clientes;
    }

    /**
     * Registra que un cliente sigue a otro.
     * Actualiza la relación bidireccional.
     *
     * Complejidad: O(1) amortizado
     *
     * @param idSolicitante ID del cliente que inicia el seguimiento
     * @param idObjetivo ID del cliente a seguir
     * @return true si se estableció la relación, false si falló
     */
    public boolean seguir(int idSolicitante, int idObjetivo) {
        Cliente clienteSolicitante = clientes.obtener(idSolicitante);
        Cliente clienteObjetivo = clientes.obtener(idObjetivo);

        if (clienteSolicitante == null || clienteObjetivo == null) {
            return false;
        }

        if (clienteSolicitante.seguir(idObjetivo)) {
            clienteObjetivo.agregarSeguidor(idSolicitante);
            return true;
        }
        return false;
    }

    /**
     * Registra que un cliente deja de seguir a otro.
     * Actualiza la relación bidireccional.
     *
     * Complejidad: O(1) amortizado
     *
     * @param idSolicitante ID del cliente que deja de seguir
     * @param idObjetivo ID del cliente que ya no será seguido
     * @return true si se desvincul la relación, false si falló
     */
    public boolean dejarDeSeguir(int idSolicitante, int idObjetivo) {
        Cliente solicitante = clientes.obtener(idSolicitante);
        Cliente objetivo = clientes.obtener(idObjetivo);
        if (solicitante == null || objetivo == null) return false;

        if (solicitante.dejarDeSeguir(idObjetivo)) {
            objetivo.eliminarSeguidor(idSolicitante);
            return true;
        }
        return false;
    }

    /**
     * Obtiene los clientes que un cliente específico está siguiendo (vecinos directos).
     *
     * Complejidad: O(k) donde k = cantidad de clientes que sigue
     *
     * @param idCliente ID del cliente
     * @return Array de clientes que sigue (vecinos)
     */
    public Cliente[] obtenerVecinos(int idCliente) {
        Cliente cliente = clientes.obtener(idCliente);
        if (cliente == null) return new Cliente[0];

        int[] idsSiguiendo = cliente.getSiguiendo();
        Cliente[] vecinos = new Cliente[idsSiguiendo.length];
        int count = 0;

        for (int idSeguido : idsSiguiendo) {
            Cliente seguido = clientes.obtener(idSeguido);
            if (seguido != null) {
                vecinos[count++] = seguido;
            }
        }

        // Redimensionar array si es necesario
        if (count < vecinos.length) {
            Cliente[] resultado = new Cliente[count];
            System.arraycopy(vecinos, 0, resultado, 0, count);
            return resultado;
        }
        return vecinos;
    }

    /**
     * Construye un Árbol Binario de Búsqueda con los seguidores de un cliente.
     * Ordena por scoring para facilitar búsquedas eficientes.
     *
     * ITERACIÓN 2: Utilizado para "Consultas de Conexiones".
     * Carga los datos de los clientes que lo siguen e imprime en nivel 4.
     *
     * Complejidad: O(k log k) donde k = cantidad de seguidores
     *
     * @param idCliente ID del cliente
     * @return ABB con seguidores ordenados por scoring
     */
    public ArbolBinarioBusqueda<Integer, Cliente> construirArbolRelaciones(int idCliente) {
        Cliente cliente = clientes.obtener(idCliente);
        if (cliente == null) return new ArbolBinarioBusqueda<>();

        ArbolBinarioBusqueda<Integer, Cliente> arbol = new ArbolBinarioBusqueda<>();
        int[] idsSeguidores = cliente.getSeguidores();

        // Construir árbol ordenado por scoring (lo que define el nivel)
        for (int idSeguidor : idsSeguidores) {
            Cliente seguidor = clientes.obtener(idSeguidor);
            if (seguidor != null) {
                arbol.insertar(seguidor.getScoring(), seguidor);
            }
        }

        return arbol;
    }

    /**
     * Obtiene los seguidores de un cliente que están en el nivel especificado
     * del árbol de relaciones.
     *
     * ITERACIÓN 2: Usado para "imprimir los clientes que están en el cuarto nivel,
     * para ver quién tiene más seguidores".
     *
     * Complejidad: O(N) una sola vez, donde N = cantidad de seguidores
     *
     * @param idCliente ID del cliente
     * @param nivel Nivel del árbol (0 = raíz)
     * @return Array de seguidores en ese nivel
     */
    public Cliente[] obtenerSeguidoresEnNivel(int idCliente, int nivel) {
        if (nivel < 0) return new Cliente[0];

        ArbolBinarioBusqueda<Integer, Cliente> arbolRelaciones = construirArbolRelaciones(idCliente);
        Object[] resultados = arbolRelaciones.obtenerEnNivel(nivel);
        Cliente[] clientes = new Cliente[resultados.length];

        for (int i = 0; i < resultados.length; i++) {
            clientes[i] = (Cliente) resultados[i];
        }

        return clientes;
    }

    /**
     * Obtiene todos los seguidores de un cliente ordenados por scoring (descendente).
     *
     * Complejidad: O(N log N) donde N = cantidad de seguidores
     *
     * @param idCliente ID del cliente
     * @return Array de seguidores ordenados por scoring descendente
     */
    public Cliente[] obtenerSeguidoresOrdenados(int idCliente) {
        Cliente cliente = clientes.obtener(idCliente);
        if (cliente == null) return new Cliente[0];

        int[] idsSeguidores = cliente.getSeguidores();
        Cliente[] seguidores = new Cliente[idsSeguidores.length];

        for (int i = 0; i < idsSeguidores.length; i++) {
            seguidores[i] = clientes.obtener(idsSeguidores[i]);
        }

        // Ordenar por scoring descendente (más influyentes primero)
        for (int i = 0; i < seguidores.length - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < seguidores.length; j++) {
                if (seguidores[j] != null && seguidores[maxIdx] != null &&
                    seguidores[j].getScoring() > seguidores[maxIdx].getScoring()) {
                    maxIdx = j;
                }
            }
            // Swap
            Cliente temp = seguidores[i];
            seguidores[i] = seguidores[maxIdx];
            seguidores[maxIdx] = temp;
        }

        return seguidores;
    }

    /**
     * Obtiene todos los seguidores de un cliente.
     *
     * Complejidad: O(k) donde k = cantidad de seguidores
     *
     * @param idCliente ID del cliente
     * @return Array de seguidores
     */
    public Cliente[] obtenerSeguidores(int idCliente) {
        Cliente cliente = clientes.obtener(idCliente);
        if (cliente == null) return new Cliente[0];

        int[] idsSeguidores = cliente.getSeguidores();
        Cliente[] seguidores = new Cliente[idsSeguidores.length];

        for (int i = 0; i < idsSeguidores.length; i++) {
            seguidores[i] = clientes.obtener(idsSeguidores[i]);
        }

        return seguidores;
    }

    /**
     * Obtiene la cantidad de seguidores de un cliente.
     *
     * Complejidad: O(1)
     *
     * @param idCliente ID del cliente
     * @return Cantidad de seguidores
     */
    public int obtenerCantidadSeguidores(int idCliente) {
        Cliente cliente = clientes.obtener(idCliente);
        if (cliente == null) return 0;
        return cliente.getCantidadSeguidores();
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // ITERACIÓN 3: DISTANCIA (BFS)
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Calcula la distancia (número de saltos) entre dos clientes en la red.
     *
     * Utiliza BFS (Breadth-First Search) sobre el grafo de seguimientos.
     * La dirección de búsqueda es la de los seguidos (aristas dirigidas).
     *
     * ALGORITMO BFS:
     * - Cola de pares (idCliente, distancia) para el recorrido por niveles
     * - Diccionario de visitados para evitar ciclos: O(1) por consulta
     *
     * Complejidad: O(V + E) donde V = clientes alcanzables, E = relaciones
     *
     * @param idOrigen  ID del cliente origen
     * @param idDestino ID del cliente destino
     * @return Distancia en saltos, 0 si son el mismo, -1 si no hay camino
     */
    public int calcularDistancia(int idOrigen, int idDestino) {
        if (clientes.obtener(idOrigen) == null || clientes.obtener(idDestino) == null) {
            return -1;
        }

        if (idOrigen == idDestino) {
            return 0;
        }

        tda.Cola<Integer> cola = new tda.Cola<>();
        Diccionario<Integer, Boolean> visitados = new Diccionario<>();

        cola.encolar(idOrigen);
        visitados.insertar(idOrigen, true);

        int distancia = 0;

        while (!cola.estaVacia()) {
            distancia++;
            int nodosEnNivel = cola.getCantidad();

            for (int i = 0; i < nodosEnNivel; i++) {
                int idActual = cola.desencolar();
                Cliente actual = clientes.obtener(idActual);
                if (actual == null) continue;

                int[] vecinos = actual.getSiguiendo();
                for (int idVecino : vecinos) {
                    if (idVecino == 0) continue;

                    if (idVecino == idDestino) {
                        return distancia;
                    }

                    if (!visitados.contiene(idVecino)) {
                        visitados.insertar(idVecino, true);
                        cola.encolar(idVecino);
                    }
                }
            }
        }

        return -1;
    }

    // ════════════════════════════════════════════════════════════════════════════════════
    // ITERACIÓN 3: AMISTADES (RELACIONES BIDIRECCIONALES)
    // ════════════════════════════════════════════════════════════════════════════════════

    /**
     * Agrega una amistad bidireccional entre dos clientes.
     * Si A se hace amigo de B, entonces B automáticamente se hace amigo de A.
     *
     * Complejidad: O(1) amortizado
     *
     * @param idCliente1 ID del primer cliente
     * @param idCliente2 ID del segundo cliente
     * @return true si se estableció la amistad, false si falló
     */
    public boolean agregarAmistad(int idCliente1, int idCliente2) {
        Cliente cliente1 = clientes.obtener(idCliente1);
        Cliente cliente2 = clientes.obtener(idCliente2);

        if (cliente1 == null || cliente2 == null) {
            return false;
        }

        // Verificar que ambos tengan espacio antes de modificar nada
        if (cliente1.getCantidadAmigos() >= Cliente.MAX_AMIGOS) {
            return false;
        }
        if (cliente2.getCantidadAmigos() >= Cliente.MAX_AMIGOS) {
            return false;
        }

        if (cliente1.agregarAmistad(idCliente2)) {
            cliente2.agregarAmistad(idCliente1);
            return true;
        }
        return false;
    }

    /**
     * Elimina una amistad bidireccional entre dos clientes.
     * Si se rompe la amistad entre A y B, se rompe en ambos lados.
     *
     * Complejidad: O(1) amortizado
     *
     * @param idCliente1 ID del primer cliente
     * @param idCliente2 ID del segundo cliente
     * @return true si se eliminó la amistad, false si falló
     */
    public boolean eliminarAmistad(int idCliente1, int idCliente2) {
        Cliente cliente1 = clientes.obtener(idCliente1);
        Cliente cliente2 = clientes.obtener(idCliente2);

        if (cliente1 == null || cliente2 == null) {
            return false;
        }

        if (cliente1.eliminarAmistad(idCliente2)) {
            cliente2.eliminarAmistad(idCliente1);
            return true;
        }
        return false;
    }

    /**
     * Obtiene todos los amigos de un cliente.
     *
     * Complejidad: O(k) donde k = cantidad de amigos
     *
     * @param idCliente ID del cliente
     * @return Array de clientes amigos
     */
    public Cliente[] obtenerAmigos(int idCliente) {
        Cliente cliente = clientes.obtener(idCliente);
        if (cliente == null) return new Cliente[0];

        int[] idsAmigos = cliente.obtenerAmigos();
        Cliente[] amigos = new Cliente[idsAmigos.length];
        int count = 0;

        for (int idAmigo : idsAmigos) {
            Cliente amigo = clientes.obtener(idAmigo);
            if (amigo != null) {
                amigos[count++] = amigo;
            }
        }

        if (count < amigos.length) {
            Cliente[] resultado = new Cliente[count];
            System.arraycopy(amigos, 0, resultado, 0, count);
            return resultado;
        }
        return amigos;
    }

    /**
     * Obtiene la cantidad de amigos de un cliente.
     *
     * Complejidad: O(1)
     *
     * @param idCliente ID del cliente
     * @return Cantidad de amigos
     */
    public int obtenerCantidadAmigos(int idCliente) {
        Cliente cliente = clientes.obtener(idCliente);
        if (cliente == null) return 0;
        return cliente.getCantidadAmigos();
    }

    /**
     * Verifica si dos clientes son amigos.
     *
     * Complejidad: O(1)
     *
     * @param idCliente1 ID del primer cliente
     * @param idCliente2 ID del segundo cliente
     * @return true si son amigos, false en caso contrario
     */
    public boolean sonAmigos(int idCliente1, int idCliente2) {
        Cliente cliente = clientes.obtener(idCliente1);
        if (cliente == null) return false;
        return cliente.esAmigoDE(idCliente2);
    }
}
