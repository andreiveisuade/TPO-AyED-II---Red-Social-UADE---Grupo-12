package modelo;

import tda.Cola;
import util.ResultadoValidacion;
import util.Validador;

/*
Representa un cliente del sistema con capacidad de seguir a otros clientes
y establecer relaciones de amistad bidireccional.

INVARIANTE DE REPRESENTACIÓN:
- id > 0
- nombre != null && !nombre.trim().isEmpty()
- 0 <= scoring <= 100
- siguiendo != null (Diccionario<Integer,Boolean> — lista de adyacencia dirigida)
- seguidores != null (Diccionario<Integer,Boolean> — lista de adyacencia inversa)
- amistades != null (Diccionario<Integer,Boolean> — lista de adyacencia no dirigida, Iteración 3)
- solicitudesPendientes != null
- No existen duplicados en siguiendo, seguidores ni amistades (garantizado por Diccionario)
- Ningún cliente se sigue a sí mismo: !siguiendo.contiene(id)
- Ningún cliente es amigo de sí mismo: !amistades.contiene(id)
- Consistencia bidireccional de amistades: si A.amistades.contiene(B) → B.amistades.contiene(A)
  (garantizado por GestorRelaciones.agregarAmistad/eliminarAmistad que opera en ambos lados)
- Consistencia bidireccional de seguidores: si A.siguiendo.contiene(B) → B.seguidores.contiene(A)
  (garantizado por GestorRelaciones.seguir/dejarDeSeguir que opera en ambos lados)
 */
public class Cliente {
    
    /* Constantes */
    /*
     * Límite máximo de usuarios que un cliente puede seguir.
     * Restricción impuesta por regla de negocio Iteración 1.
     * En iteraciones futuras puede incrementarse o eliminarse.
     */
    public static final int MAX_SEGUIDOS = 2;
    public static final int MAX_AMIGOS = 2;
    
    /* Atributos */
    private int id;
    private String nombre;
    private int scoring;
    private tda.Diccionario<Integer, Boolean> siguiendo;  // Diccionario de usuarios que sigue
    private tda.Diccionario<Integer, Boolean> seguidores;  // Diccionario de usuarios que lo siguen
    private tda.Diccionario<Integer, Boolean> amistades;  // Diccionario de amigos (relaciones bidireccionales - Iteración 3)
    private Cola<SolicitudSeguimiento> solicitudesPendientes;  // Cola de solicitudes recibidas

    // Cache de arrays para evitar reparsear claves del Diccionario en cada llamada.
    // null = invalidado, se reconstruye en el próximo get.
    private int[] cacheSiguiendo;
    private int[] cacheSeguidores;

    /*
    Constructor que inicializa un cliente con ID, nombre y scoring.
    Valida las precondiciones.
    */
    public Cliente(int id, String nombre, int scoring) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        
        ResultadoValidacion validacionNombre = Validador.validarNombre(nombre);
        if (!validacionNombre.esValido()) {
            throw new IllegalArgumentException(validacionNombre.getMensajeError());
        }
        
        ResultadoValidacion validacionScoring = Validador.validarScoring(scoring);
        if (!validacionScoring.esValido()) {
            throw new IllegalArgumentException(validacionScoring.getMensajeError());
        }
        
        this.id = id;
        this.nombre = nombre;
        this.scoring = scoring;
        this.siguiendo = new tda.Diccionario<>(); // Inicializar diccionario vacío
        this.seguidores = new tda.Diccionario<>(); // Inicializar diccionario vacío de seguidores
        this.amistades = new tda.Diccionario<>(); // Inicializar diccionario vacío de amistades (Iteración 3)
        this.solicitudesPendientes = new Cola<>();  // Inicializar cola vacía
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getScoring() {
        return scoring;
    }

    /*
    Modifica el scoring del cliente, validando el rango.
    */
    public void setScoring(int scoring) {
        ResultadoValidacion validacion = Validador.validarScoring(scoring);
        if (!validacion.esValido()) {
            throw new IllegalArgumentException(validacion.getMensajeError());
        }
        this.scoring = scoring;
    }

    /*
    Retorna los IDs de los usuarios seguidos.
    Usa cache interno; se invalida al modificar la relación.
    Complejidad: O(1) si cache válido, O(k) si necesita reconstruir.
    */
    public int[] getSiguiendo() {
        if (cacheSiguiendo == null) {
            cacheSiguiendo = parsearClaves(siguiendo);
        }
        return cacheSiguiendo;
    }
    
    // Método auxiliar para compatibilidad si alguien necesita el diccionario direct (opcional)
    // public tda.Diccionario<Integer, Boolean> getSiguiendoDiccionario() { return siguiendo; }

    /*
    Retorna la cantidad de usuarios que este cliente sigue.
    */
    public int getCantidadSiguiendo() {
        return siguiendo.getCantidad();
    }

    /*
    Retorna los IDs de los usuarios que siguen a este cliente (seguidores).
    Usa cache interno; se invalida al modificar seguidores.
    Complejidad: O(1) si cache válido, O(k) si necesita reconstruir.
    */
    public int[] getSeguidores() {
        if (cacheSeguidores == null) {
            cacheSeguidores = parsearClaves(seguidores);
        }
        return cacheSeguidores;
    }

    /*
    Retorna la cantidad de seguidores de este cliente.
    */
    public int getCantidadSeguidores() {
        return seguidores.getCantidad();
    }

    /*
    Agrega un seguidor a este cliente (uso interno desde GestorClientes).
    Visibilidad package-private para evitar manipulación directa.
    */
    public void agregarSeguidor(int idSeguidor) {
        seguidores.insertar(idSeguidor, true);
        cacheSeguidores = null;
    }

    /*
    Elimina un seguidor de este cliente (uso interno desde GestorClientes).
    Visibilidad package-private para evitar manipulación directa.
    */
    public void eliminarSeguidor(int idSeguidor) {
        seguidores.eliminar(idSeguidor);
        cacheSeguidores = null;
    }

    /*
    Intenta seguir a otro cliente por ID.
    Retorna false si ya lo sigue, si es él mismo, o si alcanzó el límite MAX_SEGUIDOS.
    */
    public boolean seguir(int idObjetivo) {
        if (idObjetivo == this.id) {
            return false;
        }
        if (siguiendo.contiene(idObjetivo)) {
            return false;
        }
        if (siguiendo.getCantidad() >= MAX_SEGUIDOS) {
            return false;
        }
        siguiendo.insertar(idObjetivo, true);
        cacheSiguiendo = null;
        return true;
    }

    /*
    Deja de seguir a un cliente por ID.
    */
    public boolean dejarDeSeguir(int idObjetivo) {
        if (siguiendo.contiene(idObjetivo)) {
            siguiendo.eliminar(idObjetivo);
            cacheSiguiendo = null;
            return true;
        }
        return false;
    }

    /*
    Verifica si el cliente está siguiendo a otro cliente específico por ID.
    */
    public boolean sigueA(int idObjetivo) {
        return siguiendo.contiene(idObjetivo);
    }

    /*
    Representación compacta: usada por el ABB al imprimir el árbol.
    */
    @Override
    public String toString() {
        return nombre + " (ID:" + id + ")";
    }

    /*
    Representación detallada con toda la información del cliente.
    */
    public String toStringDetallado() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(id);
        sb.append(" | Cliente: ").append(nombre);
        sb.append(" | Scoring: ").append(scoring);
        sb.append(" | Sigue a (IDs): ");
        if (siguiendo.estaVacio()) {
            sb.append("nadie");
        } else {
            String[] claves = siguiendo.obtenerClaves();
            for (int i = 0; i < claves.length; i++) {
                if (claves[i] != null) {
                    sb.append(claves[i]);
                    if (i < claves.length - 1) {
                        sb.append(", ");
                    }
                }
            }
        }
        return sb.toString();
    }
    
    /*
    ══════════════════════════════════════════════════════════
    ITERACIÓN 3: AMISTADES (RELACIONES BIDIRECCIONALES)
    ══════════════════════════════════════════════════════════
    */

    /*
    Agrega una amistad con otro cliente.
    Complejidad: O(1)
    Retorna false si ya son amigos, si es él mismo, o si alcanzó el límite MAX_AMIGOS.
    */
    public boolean agregarAmistad(int idAmigo) {
        if (idAmigo == this.id) {
            return false;
        }
        if (amistades.contiene(idAmigo)) {
            return false;
        }
        if (amistades.getCantidad() >= MAX_AMIGOS) {
            return false;
        }
        amistades.insertar(idAmigo, true);
        return true;
    }

    /*
    Elimina una amistad con otro cliente.
    Complejidad: O(1)
    Retorna false si no eran amigos.
    */
    public boolean eliminarAmistad(int idAmigo) {
        if (amistades.contiene(idAmigo)) {
            amistades.eliminar(idAmigo);
            return true;
        }
        return false;
    }

    /*
    Retorna los IDs de todos los amigos de este cliente.
    Complejidad: O(k) donde k es la cantidad de amigos.
    */
    public int[] obtenerAmigos() {
        return parsearClaves(amistades);
    }

    /*
    Retorna la cantidad de amigos de este cliente.
    Complejidad: O(1)
    */
    public int getCantidadAmigos() {
        return amistades.getCantidad();
    }

    /*
    Verifica si es amigo de otro cliente.
    Complejidad: O(1)
    */
    public boolean esAmigoDE(int idOtroCliente) {
        return amistades.contiene(idOtroCliente);
    }

    /*
    ══════════════════════════════════════════════════════════
    GESTIÓN DE SOLICITUDES DE SEGUIMIENTO
    ══════════════════════════════════════════════════════════
    */
    
    /*
    Agrega una solicitud de seguimiento a la cola de pendientes.
    */
    public void recibirSolicitud(SolicitudSeguimiento solicitud) {
        if (solicitud != null) {
            solicitudesPendientes.encolar(solicitud);
        }
    }
    
    /*
    Procesa (desencola) la siguiente solicitud pendiente.
    Retorna null si no hay pendientes.
    */
    public SolicitudSeguimiento procesarSiguienteSolicitud() {
        if (!solicitudesPendientes.estaVacia()) {
            return solicitudesPendientes.desencolar();
        }
        return null;
    }
    
    /*
    Consulta la siguiente solicitud sin procesarla.
    Retorna null si no hay pendientes.
    */
    public SolicitudSeguimiento verSiguienteSolicitud() {
        if (!solicitudesPendientes.estaVacia()) {
            return solicitudesPendientes.verFrente();
        }
        return null;
    }
    
    /*
    Verifica si tiene solicitudes pendientes.
    */
    public boolean tieneSolicitudesPendientes() {
        return !solicitudesPendientes.estaVacia();
    }
    
    /*
    Obtiene la cantidad de solicitudes pendientes.
    */
    public int getCantidadSolicitudesPendientes() {
        return solicitudesPendientes.getCantidad();  // O(1) en lugar de O(n)
    }

    /*
    ══════════════════════════════════════════════════════════
    MÉTODOS DE PERSISTENCIA (SIMULACIÓN ORM)
    ══════════════════════════════════════════════════════════
    */

    /*
    Serializa las solicitudes pendientes para guardarlas en el JSON.
    Complejidad: O(k) donde k es la cantidad de solicitudes pendientes.
    Se debe recorrer la cola (copiándola) para no perder el orden ni los datos.
    */
    public String[] getSolicitudesRecibidasSerialized() {
        if (solicitudesPendientes.estaVacia()) {
            return new String[0];
        }
        
        // Al ser una Cola, no podemos iterar directamente sin desencolar en la implementación estricta
        // Pero para persistencia necesitamos ver todo. 
        // Aprovechamos que es una Cola genérica implementada por nosotros.
        // Asumimos que podemos obtener una copia o usar una estructura auxiliar.
        
        // Estrategia: Desencolar todo a una lista temporal y volver a encolar.
        int cantidad = solicitudesPendientes.getCantidad();
        String[] resultado = new String[cantidad];
        Cola<SolicitudSeguimiento> aux = new Cola<>();
        
        int i = 0;
        while (!solicitudesPendientes.estaVacia()) {
            SolicitudSeguimiento sol = solicitudesPendientes.desencolar();
            resultado[i++] = sol.getSolicitante(); // Solo guardamos ID solicitante (el objetivo es 'this')
            aux.encolar(sol);
        }
        
        // Restaurar estado original
        while (!aux.estaVacia()) {
            solicitudesPendientes.encolar(aux.desencolar());
        }
        
        return resultado;
    }

    /*
    Carga IDs de usuarios seguidos desde la persistencia.
    */
    public void cargarSiguiendo(int[] ids) {
        if (ids == null) return;
        for (int idObjetivo : ids) {
            if (idObjetivo != this.id) {
                this.siguiendo.insertar(idObjetivo, true);
            }
        }
        cacheSiguiendo = null;
    }

    /*
    Carga solicitudes desde la persistencia.
    */
    public void cargarSolicitudes(String[] idsSolicitantes) {
        if (idsSolicitantes == null) return;
        
        for (String idSolicitante : idsSolicitantes) {
            if (idSolicitante != null && !idSolicitante.isEmpty()) {
                // Reconstruimos la solicitud. El objetivo es este cliente.
                try {
                    SolicitudSeguimiento sol = new SolicitudSeguimiento(idSolicitante, String.valueOf(this.id));
                    this.solicitudesPendientes.encolar(sol);
                } catch (IllegalArgumentException e) {
                    // Ignorar datos corruptos
                }
            }
        }
    }

    /*
    Serializa los seguidores para guardarlos en el JSON.
    Complejidad: O(k) donde k es la cantidad de seguidores.
    */
    public String[] getSeguidoresSerialized() {
        int[] ids = getSeguidores();
        String[] resultado = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            resultado[i] = String.valueOf(ids[i]);
        }
        return resultado;
    }

    /*
    Carga seguidores desde la persistencia.
    */
    public void cargarSeguidores(int[] ids) {
        if (ids == null) return;
        for (int idSeguidor : ids) {
            this.seguidores.insertar(idSeguidor, true);
        }
        cacheSeguidores = null;
    }

    /*
    Serializa los amigos para guardarlos en el JSON (Iteración 3).
    Complejidad: O(k) donde k es la cantidad de amigos.
    */
    public int[] getAmistadesSerialized() {
        return obtenerAmigos();
    }

    /*
    Carga amigos desde la persistencia (Iteración 3).
    Complejidad: O(k) donde k es la cantidad de amigos.
    */
    public void cargarAmistades(int[] ids) {
        if (ids == null) return;
        for (int idAmigo : ids) {
            if (idAmigo != this.id) {
                this.amistades.insertar(idAmigo, true);
            }
        }
    }

    /*
    Convierte las claves de un Diccionario<Integer, Boolean> a int[].
    Helper privado para construir el cache de getSiguiendo()/getSeguidores().
    Complejidad: O(k) donde k = cantidad de claves.
    */
    private int[] parsearClaves(tda.Diccionario<Integer, Boolean> diccionario) {
        String[] claves = diccionario.obtenerClaves();
        int[] ids = new int[claves.length];
        for (int i = 0; i < claves.length; i++) {
            if (claves[i] != null) {
                try {
                    ids[i] = Integer.parseInt(claves[i]);
                } catch (NumberFormatException e) {
                    ids[i] = 0;
                }
            }
        }
        return ids;
    }
}

