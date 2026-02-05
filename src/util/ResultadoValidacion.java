package util;

/*
Representa el resultado de una validación.
Permite retornar éxito o error con mensaje descriptivo.

GRASP: Information Expert - Conoce si la validación fue exitosa y el mensaje
 */
public class ResultadoValidacion {
    
    /* Atributos */
    private final boolean valido;
    private final String mensajeError;
    
    /*
    Constructor privado para forzar uso de métodos estáticos.
    */
    private ResultadoValidacion(boolean valido, String mensajeError) {
        this.valido = valido;
        this.mensajeError = mensajeError;
    }
    
    /*
    Crea un resultado exitoso.
    */
    public static ResultadoValidacion ok() {
        return new ResultadoValidacion(true, null);
    }
    
    /*
    Crea un resultado de error con mensaje.
    */
    public static ResultadoValidacion error(String mensaje) {
        return new ResultadoValidacion(false, mensaje);
    }
    
    /*
    Verifica si la validación fue exitosa.
    */
    public boolean esValido() {
        return valido;
    }
    
    /*
    Obtiene el mensaje de error (null si fue exitoso).
    */
    public String getMensajeError() {
        return mensajeError;
    }
    
    @Override
    public String toString() {
        return valido ? "Validación exitosa" : "Error: " + mensajeError;
    }
}
