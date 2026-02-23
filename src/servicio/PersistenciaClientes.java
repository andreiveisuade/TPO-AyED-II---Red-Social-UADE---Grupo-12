package servicio;

import modelo.Cliente;
import tda.Diccionario;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Gestiona la persistencia de clientes en archivo JSON.
 *
 * RESPONSABILIDAD: Encapsular toda la lógica de carga y guardado de datos en JSON.
 *
 * SOLID:
 * - SRP: Única responsabilidad es persistencia de clientes
 * - DIP: Depende de abstracciones (Cliente)
 * - OCP: Fácil de extender a otras fuentes de datos
 *
 * GRASP:
 * - Creator: Crea instancias de Cliente desde JSON
 * - Information Expert: Conoce el formato JSON
 */
public class PersistenciaClientes {

    private final String archivoPath;

    /**
     * Constructor que especifica el archivo de persistencia.
     *
     * @param archivoPath Ruta al archivo JSON de clientes
     */
    public PersistenciaClientes(String archivoPath) {
        this.archivoPath = archivoPath;
    }

    /**
     * Carga los clientes desde un archivo JSON.
     * Si falla, retorna un diccionario vacío.
     *
     * Complejidad: O(N) donde N = cantidad de clientes en el archivo
     *
     * @param capacidadDiccionario Capacidad inicial del diccionario
     * @return Diccionario de clientes cargados
     */
    public Diccionario<Integer, Cliente> cargarDesdeArchivo(int capacidadDiccionario) {
        System.out.println("Cargando clientes...");
        Diccionario<Integer, Cliente> clientes = new Diccionario<>(capacidadDiccionario);

        try (FileReader reader = new FileReader(archivoPath)) {
            Gson gson = new Gson();
            ClientesWrapper wrapper = gson.fromJson(reader, ClientesWrapper.class);

            if (wrapper != null && wrapper.clientes != null) {
                for (ClienteDTO dto : wrapper.clientes) {
                    Cliente c = new Cliente(dto.id, dto.nombre, dto.scoring);
                    c.cargarSiguiendo(dto.siguiendo);
                    c.cargarSolicitudes(dto.solicitudes);

                    // Cargar seguidores (puede ser null en versiones antiguas del JSON)
                    if (dto.seguidores != null) {
                        int[] idsSeguidores = new int[dto.seguidores.length];
                        for (int i = 0; i < dto.seguidores.length; i++) {
                            try {
                                idsSeguidores[i] = Integer.parseInt(dto.seguidores[i]);
                            } catch (NumberFormatException e) {
                                idsSeguidores[i] = 0;
                            }
                        }
                        c.cargarSeguidores(idsSeguidores);
                    }

                    // Cargar amistades (Iteración 3 - puede ser null en versiones antiguas)
                    if (dto.amistades != null) {
                        c.cargarAmistades(dto.amistades);
                    }

                    clientes.insertar(c.getId(), c);
                }
            }
        } catch (IOException e) {
            System.err.println("Error cargando datos (iniciando vacío): " + e.getMessage());
        }

        return clientes;
    }

    /**
     * Guarda el estado actual de clientes en el archivo JSON.
     *
     * Complejidad: O(N * M) donde N = cantidad de clientes, M = cantidad de seguidores promedio
     *
     * @param clientes Diccionario de clientes a guardar
     */
    public void guardarCambios(Diccionario<Integer, Cliente> clientes) {
        System.out.println("Guardando datos en " + archivoPath + "...");
        try (FileWriter writer = new FileWriter(archivoPath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            ClientesWrapper wrapper = new ClientesWrapper();
            Object[] objs = clientes.obtenerValores();
            wrapper.clientes = new ClienteDTO[objs.length];

            for (int i = 0; i < objs.length; i++) {
                Cliente c = (Cliente) objs[i];
                ClienteDTO dto = new ClienteDTO();
                dto.id = c.getId();
                dto.nombre = c.getNombre();
                dto.scoring = c.getScoring();
                dto.siguiendo = c.getSiguiendo();
                dto.solicitudes = c.getSolicitudesRecibidasSerialized();
                dto.seguidores = c.getSeguidoresSerialized();
                dto.amistades = c.getAmistadesSerialized();  // Iteración 3
                wrapper.clientes[i] = dto;
            }

            gson.toJson(wrapper, writer);
            System.out.println("Datos guardados exitosamente.");
        } catch (IOException e) {
            System.err.println("Error guardando datos: " + e.getMessage());
        }
    }

    /**
     * DTO (Data Transfer Object) para transferencia de datos con JSON.
     * Representa la estructura de un cliente en el archivo JSON.
     */
    public static class ClienteDTO {
        public int id;
        public String nombre;
        public int scoring;
        public int[] siguiendo;
        public String[] solicitudes;
        public String[] seguidores;
        public int[] amistades;  // Iteración 3: Relaciones generales
    }

    /**
     * Wrapper interno para estructura JSON de clientes.
     */
    public static class ClientesWrapper {
        public ClienteDTO[] clientes;
    }
}
