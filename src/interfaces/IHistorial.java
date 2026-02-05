package interfaces;

import modelo.Accion;

/*
Define el contrato para gestionar el historial de acciones.

SOLID: Dependency Inversion Principle - abstracción para el historial
SOLID: Interface Segregation Principle - interfaz específica para historial
GRASP: Pure Fabrication - interfaz que define operaciones de historial
*/
public interface IHistorial {
    
    /*
    Registra una nueva acción en el historial.
    */
    void registrar(Accion accion);
    
    /*
    Extrae y retorna la última acción del historial.
    Retorna null si el historial está vacío.
    */
    Accion extraerUltima();
    
    /*
    Consulta la última acción sin removerla.
    Retorna null si el historial está vacío.
    */
    Accion verUltima();
    
    /*
    Verifica si el historial está vacío.
    */
    boolean estaVacio();
    
    /*
    Retorna la cantidad de acciones en el historial.
    */
    int getCantidad();
    
    /*
    Obtiene todas las acciones del historial sin modificarlo.
    El array retornado está ordenado de más reciente a más antigua.
    */
    Accion[] obtenerTodas();
}
