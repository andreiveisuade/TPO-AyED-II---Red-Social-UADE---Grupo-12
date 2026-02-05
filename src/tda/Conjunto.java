package tda;

import interfaces.IConjunto;

/*
TDA Conjunto - Colección de elementos únicos sin orden

INVARIANTE DE REPRESENTACIÓN:
- elementos != null siempre
- No existen elementos duplicados en el conjunto
- Todos los valores del diccionario interno son true
- SOLID: DIP - Implementa interfaz IConjunto
 */
public class Conjunto implements IConjunto {
    
    /* Atributos */
    private Diccionario<String, Boolean> elementos;

    /*
    Constructor que inicializa el conjunto vacío.
    */
    public Conjunto() {
        this.elementos = new Diccionario<>();
    }

    /*
    Agrega un elemento al conjunto.
    */
    @Override
    public void agregar(String elemento) {
        elementos.insertar(elemento, true);
    }

    /*
    Verifica si el conjunto contiene un elemento.
    */
    @Override
    public boolean contiene(String elemento) {
        return elementos.contiene(elemento);
    }

    /*
    Elimina un elemento del conjunto.
    */
    @Override
    public void eliminar(String elemento) {
        elementos.eliminar(elemento);
    }

    /*
    Retorna la cantidad de elementos en el conjunto.
    */
    @Override
    public int getCantidad() {
        return elementos.getCantidad();
    }

    /*
    Verifica si el conjunto está vacío.
    */
    @Override
    public boolean estaVacio() {
        return elementos.estaVacio();
    }

    /*
    Retorna todos los elementos del conjunto.
    */
    @Override
    public String[] obtenerElementos() {
        return elementos.obtenerClaves();
    }
}
