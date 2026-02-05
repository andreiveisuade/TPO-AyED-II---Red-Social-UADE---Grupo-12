package modelo;

/*
Enumeración de los tipos de acciones posibles en el sistema.
Utilizado para el registro de historial y undo/redo.
 */
public enum TipoAccion {
    AGREGAR_CLIENTE,
    ELIMINAR_CLIENTE,
    SEGUIR,
    DEJAR_DE_SEGUIR,
    AGREGAR_CONEXION,
    ELIMINAR_CONEXION
}
