package servicio;

import interfaces.IHistorial;
import modelo.Accion;
import tda.Pila;

/*
Gestiona el historial de acciones realizadas en el sistema.

INVARIANTE DE REPRESENTACIÓN:
- historial != null
- Todas las acciones en historial son != null
- Las acciones están ordenadas cronológicamente (más reciente en tope)

GRASP: Information Expert - conoce y gestiona la Pila de acciones
SOLID: SRP - solo gestiona historial, no lógica de negocio
SOLID: DIP - implementa interfaz IHistorial
 */
public class HistorialAcciones implements IHistorial {
    
    /* Atributos */
    private Pila<Accion> historial;

    /*
    Constructor que inicializa la pila de historial.
    */
    public HistorialAcciones() {
        this.historial = new Pila<>();
    }

    /*
    Registra una nueva acción en el historial.
    Complejidad: O(1)
    */
    public void registrar(Accion accion) {
        if (accion != null) {
            historial.apilar(accion);
        }
    }

    /*
    Extrae y retorna la última acción del historial.
    Complejidad: O(1)
    */
    public Accion extraerUltima() {
        return historial.desapilar();
    }

    /*
    Consulta la última acción sin removerla.
    Complejidad: O(1)
    */
    public Accion verUltima() {
        return historial.verTope();
    }

    /*
    Verifica si el historial está vacío.
    Complejidad: O(1)
    */
    public boolean estaVacio() {
        return historial.estaVacia();
    }

    /*
    Retorna la cantidad de acciones en el historial.
    Complejidad: O(1)
    */
    public int getCantidad() {
        return historial.getCantidad();
    }

    /*
    Obtiene todas las acciones del historial sin modificarlo.
    El array retornado está ordenado de más reciente a más antigua.
    Complejidad: O(n) - usa toArray() que recorre una sola vez
    */
    public Accion[] obtenerTodas() {
        if (historial.estaVacia()) {
            return new Accion[0];
        }

        Object[] datos = historial.toArray();
        Accion[] acciones = new Accion[datos.length];
        for (int i = 0; i < datos.length; i++) {
            acciones[i] = (Accion) datos[i];
        }
        return acciones;
    }
}
