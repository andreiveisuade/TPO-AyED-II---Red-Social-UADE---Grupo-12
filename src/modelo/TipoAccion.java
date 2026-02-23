package modelo;

/*
Enumeración de los tipos de acciones del usuario en el sistema.
Utilizado para el registro de historial y undo/redo.
Solo incluye acciones que el usuario ejecuta desde las vistas.
 */
public enum TipoAccion {
    SEGUIR,
    DEJAR_DE_SEGUIR
}
