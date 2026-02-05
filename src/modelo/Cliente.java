package modelo;

import tda.Cola;
import util.Validador;
import util.ResultadoValidacion;

/*
Representa un cliente del sistema con capacidad de seguir a otros clientes.

INVARIANTE DE REPRESENTACIÓN:
- id > 0
- nombre != null && !nombre.trim().isEmpty()
- 0 <= scoring <= 100
- siguiendo != null
- No existen duplicados en siguiendo (garantizado por Diccionario)
- Ningún cliente se sigue a sí mismo
- solicitudesPendientes != null
 */
public class Cliente {
    
    /* Constantes */
    // Ya no hay límite máximo de usuarios a seguir
    
    /* Atributos */
    private int id;
    private String nombre;
    private int scoring;
    private tda.Diccionario<Integer, Boolean> siguiendo;  // Diccionario para O(1) y sin límite
    private Cola<SolicitudSeguimiento> solicitudesPendientes;  // Cola de solicitudes recibidas

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
    */
    public int[] getSiguiendo() {
        String[] claves = siguiendo.obtenerClaves();
        int[] ids = new int[claves.length];
        for (int i = 0; i < claves.length; i++) {
            if (claves[i] != null) {
                try {
                    ids[i] = Integer.parseInt(claves[i]);
                } catch (NumberFormatException e) {
                    ids[i] = 0; // Should not happen with Integer keys
                }
            }
        }
        return ids;
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
    Intenta seguir a otro cliente por ID.
    Retorna false si ya lo sigue o si es él mismo.
    */
    public boolean seguir(int idObjetivo) {
        if (idObjetivo == this.id) {
            return false;
        }
        if (siguiendo.contiene(idObjetivo)) {
            return false;
        }
        siguiendo.insertar(idObjetivo, true);
        return true;
    }

    /*
    Deja de seguir a un cliente por ID.
    */
    public boolean dejarDeSeguir(int idObjetivo) {
        if (siguiendo.contiene(idObjetivo)) {
            siguiendo.eliminar(idObjetivo);
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

    @Override
    public String toString() {
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
}

