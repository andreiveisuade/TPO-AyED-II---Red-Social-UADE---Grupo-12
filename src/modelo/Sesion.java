package modelo;

import servicio.HistorialAcciones;

/*
TDA Sesion - Representa la sesión única del usuario en el sistema.

PATRÓN: Singleton - Garantiza una única instancia de sesión.

INVARIANTE DE REPRESENTACIÓN:
- instancia es única (garantizado por el patrón Singleton)
- Si estaAutenticado == true, entonces usuarioActual != null
- Si estaAutenticado == false, entonces usuarioActual == null
 */
public class Sesion {
    
    /* Singleton: instancia única, por eso static*/
    private static Sesion instancia;
    
    /* Atributos */
    private Cliente usuarioActual;
    private boolean estaAutenticado;
    private HistorialAcciones historial;
    
    /*
    Constructor privado (Singleton).
    */
    private Sesion() {
        this.usuarioActual = null;
        this.estaAutenticado = false;
        this.historial = new HistorialAcciones();
    }
    
    /*
    Obtiene la instancia única de Sesion (Singleton).
    */
    public static Sesion getInstancia() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }
    
    /*
    Inicia sesión con un cliente.
    */
    public void iniciarSesion(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser null");
        }
        this.usuarioActual = cliente;
        this.estaAutenticado = true;
        this.historial = new HistorialAcciones();
    }
    
    /*
    Cierra la sesión actual.
    */
    public void cerrarSesion() {
        this.usuarioActual = null;
        this.estaAutenticado = false;
        this.historial = new HistorialAcciones();
    }
    
    /*
    Verifica si hay un usuario autenticado.
    */
    public boolean estaAutenticado() {
        return estaAutenticado;
    }
    
    /*
    Retorna el usuario actualmente autenticado (o null).
    */
    public Cliente getUsuarioActual() {
        return usuarioActual;
    }
    
    public int getIdUsuarioActual() {
        return estaAutenticado ? usuarioActual.getId() : -1;
    }
    
    public String getNombreUsuarioActual() {
        return estaAutenticado ? usuarioActual.getNombre() : "Invitado";
    }
    
    public boolean tieneSolicitudesPendientes() {
        return estaAutenticado && usuarioActual.tieneSolicitudesPendientes();
    }
    
    public int getCantidadSolicitudesPendientes() {
        return estaAutenticado ? usuarioActual.getCantidadSolicitudesPendientes() : 0;
    }
    
    /*
    Retorna el historial de acciones de la sesión actual.
    */
    public HistorialAcciones getHistorial() {
        if (!estaAutenticado) throw new IllegalStateException("No hay sesión activa");
        return historial;
    }

    @Override
    public String toString() {
        return estaAutenticado 
            ? "Sesión activa: " + usuarioActual.getNombre() + " (ID: " + usuarioActual.getId() + ")"
            : "Sin sesión activa";
    }
}
