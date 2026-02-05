package util;

/*
Validador centralizado para entidades del dominio.
Evita duplicación de lógica de validación.

SOLID: SRP - Solo se encarga de validaciones
GRASP: Pure Fabrication - No representa concepto del dominio, pero provee servicio
 */
public class Validador {
    
    /*
    Valida que un nombre no sea nulo ni vacío.
    */
    public static ResultadoValidacion validarNombre(String nombre) {
        if (nombre == null) {
            return ResultadoValidacion.error("El nombre no puede ser nulo");
        }
        if (nombre.trim().isEmpty()) {
            return ResultadoValidacion.error("El nombre no puede estar vacío");
        }
        return ResultadoValidacion.ok();
    }
    
    /*
    Valida que un scoring esté en el rango [0, 100].
    */
    public static ResultadoValidacion validarScoring(int scoring) {
        if (scoring < 0 || scoring > 100) {
            return ResultadoValidacion.error("El scoring debe estar entre 0 y 100");
        }
        return ResultadoValidacion.ok();
    }
    
    /*
    Valida que dos nombres no sean iguales (case-insensitive).
    */
    public static ResultadoValidacion validarNombresDistintos(String nombre1, String nombre2) {
        if (nombre1 == null || nombre2 == null) {
            return ResultadoValidacion.error("Los nombres no pueden ser nulos");
        }
        if (nombre1.equalsIgnoreCase(nombre2)) {
            return ResultadoValidacion.error("Los nombres deben ser diferentes");
        }
        return ResultadoValidacion.ok();
    }
}
