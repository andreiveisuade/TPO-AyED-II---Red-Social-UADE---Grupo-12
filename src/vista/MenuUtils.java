package vista;

import java.util.Scanner;
import static vista.Terminal.*;

/*
Utilidades compartidas para los menús.
Evita duplicación de código entre submenús.
 */
public class MenuUtils {
    
    /* Constantes */
    public static final int ANCHO = 50;
    
    /* Atributos */
    private final Scanner scanner;

    /*
    Constructor que inicializa el scanner.
    */
    public MenuUtils(Scanner scanner) {
        this.scanner = scanner;
    }

    /*
    Muestra la cabecera del menú con breadcrumb de navegación.
    */
    public void mostrarCabecera(String... ruta) {
        System.out.println("+" + "-".repeat(ANCHO - 2) + "+");
        System.out.println("|" + centrar("RED SOCIAL - UADE", ANCHO - 2) + "|");
        System.out.println("+" + "-".repeat(ANCHO - 2) + "+");
        
        // Breadcrumb de navegación
        StringBuilder breadcrumb = new StringBuilder();
        breadcrumb.append(">> ");
        for (int i = 0; i < ruta.length; i++) {
            if (i > 0) breadcrumb.append(" > ");
            breadcrumb.append(ruta[i]);
        }
        System.out.println(breadcrumb.toString());
        System.out.println();
    }
    
    /*
    Centra un texto en un ancho dado.
    */
    public String centrar(String texto, int ancho) {
        int espacios = (ancho - texto.length()) / 2;
        return " ".repeat(Math.max(0, espacios)) + texto + " ".repeat(Math.max(0, ancho - texto.length() - espacios));
    }

    /*
    Lee un entero del scanner, retorna -1 si hay error.
    */
    public int leerEntero() {
        try {
            String linea = scanner.nextLine();
            return Integer.parseInt(linea.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /*
    Formatea un nombre para que tenga la primera letra mayúscula y el resto minúsculas.
    Ej: "boB" -> "Bob"
    */
    public String capitalizarNombre(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return nombre;
        }
        return nombre.substring(0, 1).toUpperCase() + nombre.substring(1).toLowerCase();
    }
}
