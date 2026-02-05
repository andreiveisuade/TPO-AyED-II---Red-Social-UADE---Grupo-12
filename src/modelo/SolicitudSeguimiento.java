package modelo;

/*
Representa una solicitud de seguimiento entre dos clientes.

INVARIANTE DE REPRESENTACIÓN:
- solicitante != null
- objetivo != null
- solicitante != objetivo (un cliente no puede solicitarse seguir a sí mismo)
 */
public class SolicitudSeguimiento {
    
    /* Atributos */
    private String solicitante;
    private String objetivo;

    /*
    Constructor que valida las precondiciones de la solicitud.
    */
    public SolicitudSeguimiento(String solicitante, String objetivo) {
        if (solicitante == null || objetivo == null) {
            throw new IllegalArgumentException("Solicitante y objetivo no pueden ser nulos");
        }
        if (solicitante.equals(objetivo)) {
            throw new IllegalArgumentException("No se puede crear solicitud a sí mismo");
        }
        this.solicitante = solicitante;
        this.objetivo = objetivo;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public String getObjetivo() {
        return objetivo;
    }

    /*
    Genera una clave única para identificar la solicitud.
    Formato: "solicitante:objetivo"
    */
    public String getClave() {
        return solicitante + ":" + objetivo;
    }

    @Override
    public String toString() {
        return solicitante + " → " + objetivo;
    }
}
