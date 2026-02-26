import vista.Menu;
import java.util.Scanner;

/*
Punto de entrada de la aplicación.
Muestra la selección de base de datos y luego inicia el menú principal.
 */
public class Main {

    private static final String DB_PRODUCCION = "data/clientes_1M.json";
    private static final String DB_DEMO       = "data/clientes_iteracion_3.json";

    public static void main(String[] args) {
        String dbPath = seleccionarBaseDeDatos();
        if (dbPath == null) return;

        Menu menu = new Menu(dbPath);
        menu.iniciar();
    }

    /*
    Pantalla inicial: permite elegir con qué base de datos arrancar.
    Retorna el path seleccionado, o null si el usuario elige salir.
    */
    private static String seleccionarBaseDeDatos() {
        Scanner scanner = new Scanner(System.in);

        // Limpiar pantalla
        for (int i = 0; i < 50; i++) System.out.println();

        System.out.println("+------------------------------------------------+");
        System.out.println("|        RED SOCIAL - UADE - TPO AyED II         |");
        System.out.println("+------------------------------------------------+");
        System.out.println();
        System.out.println("  Seleccionar base de datos:");
        System.out.println();
        System.out.println("  1. Produccion (1M clientes)");
        System.out.println("     " + DB_PRODUCCION);
        System.out.println();
        System.out.println("  2. Demo Iteracion 3 (10 clientes)");
        System.out.println("     " + DB_DEMO);
        System.out.println("     Ideal para visualizar matrices de adyacencia");
        System.out.println();
        System.out.println("  0. Salir");
        System.out.println("--------------------------------------------------");

        while (true) {
            System.out.print("> Opcion: ");
            try {
                String linea = scanner.nextLine().trim();
                int opcion = Integer.parseInt(linea);

                switch (opcion) {
                    case 1: return DB_PRODUCCION;
                    case 2: return DB_DEMO;
                    case 0: return null;
                    default:
                        System.out.println("  Opcion no valida. Ingrese 1, 2 o 0.");
                }
            } catch (NumberFormatException e) {
                System.out.println("  Ingrese un numero.");
            }
        }
    }
}
