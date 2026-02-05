package vista;

/*
Utilidades básicas para terminal sin formato ANSI.
 */
public class Terminal {
    
    /*
    Limpia la pantalla imprimiendo líneas en blanco.
    */
    public static void limpiarPantalla() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }
    
    /*
    Imprime un título.
    */
    public static void imprimirTitulo(String titulo) {
        System.out.println(titulo);
    }
    
    /*
    Imprime una línea separadora.
    */
    public static void imprimirSeparador(int longitud) {
        for (int i = 0; i < longitud; i++) {
            System.out.print("-");
        }
        System.out.println();
    }
    
    /*
    Imprime mensaje de éxito.
    */
    public static void imprimirExito(String mensaje) {
        System.out.println("[OK] " + mensaje);
    }
    
    /*
    Imprime mensaje de error.
    */
    public static void imprimirError(String mensaje) {
        System.out.println("[ERROR] " + mensaje);
    }
    
    /*
    Imprime mensaje de aviso.
    */
    public static void imprimirAviso(String mensaje) {
        System.out.println("[AVISO] " + mensaje);
    }
    
    /*
    Pausa esperando que el usuario presione Enter.
    */
    public static void pausar(java.util.Scanner scanner) {
        System.out.print("Presione Enter para continuar...");
        scanner.nextLine();
    }
}
