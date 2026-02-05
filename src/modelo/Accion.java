package modelo;

import java.time.LocalDateTime;

/*
Representa una acción realizada en el sistema, con timestamp automático.

INVARIANTE DE REPRESENTACIÓN:
- tipo != null
- datos != null
- timestamp != null
- timestamp <= LocalDateTime.now() (la acción no puede ser futura)
 */
public class Accion {
    
    /* Atributos */
    private TipoAccion tipo;
    private String[] datos;
    private LocalDateTime timestamp;

    /*
    Constructor que inicializa la acción y su timestamp automáticamente.
    */
    public Accion(TipoAccion tipo, String... datos) {
        this.tipo = tipo;
        this.datos = datos;
        this.timestamp = LocalDateTime.now();
    }

    /*
    Retorna el tipo de acción realizada.
    */
    public TipoAccion getTipo() {
        return tipo;
    }

    /*
    Retorna los datos asociados a la acción.
    */
    public String[] getDatos() {
        return datos;
    }

    /*
    Retorna el instante en que ocurrió la acción.
    */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /*
    Genera un string detallado con los datos de la acción.
    */
    public String getDetalle() {
        StringBuilder sb = new StringBuilder();
        sb.append(tipo.name()).append(": ");
        for (int i = 0; i < datos.length; i++) {
            sb.append(datos[i]);
            if (i < datos.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "[" + timestamp.toLocalTime().withNano(0) + "] " + getDetalle();
    }
}
