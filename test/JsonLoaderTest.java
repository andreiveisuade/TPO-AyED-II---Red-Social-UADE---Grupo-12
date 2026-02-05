package test;

import servicio.GestorClientes;
import servicio.ColaSolicitudes;
import persistencia.JsonLoader;
import modelo.Cliente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class JsonLoaderTest {
    
    private GestorClientes gestor;
    private ColaSolicitudes cola;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        gestor = new GestorClientes();
        cola = new ColaSolicitudes();
    }

    @Test
    void testCargarClientesDesdeJson() throws IOException {
        String json = """
            {
              "clientes": [
                {"nombre": "Alice", "scoring": 95, "siguiendo": [], "conexiones": []},
                {"nombre": "Bob", "scoring": 88, "siguiendo": [], "conexiones": []}
              ]
            }
            """;
        
        File archivo = tempDir.resolve("test.json").toFile();
        try (FileWriter writer = new FileWriter(archivo)) {
            writer.write(json);
        }
        
        JsonLoader loader = new JsonLoader();
        loader.cargarDesdeArchivo(archivo.getAbsolutePath(), gestor, cola);
        
        assertEquals(2, gestor.getCantidadClientes());
        assertNotNull(gestor.buscarPorNombre("Alice"));
        assertNotNull(gestor.buscarPorNombre("Bob"));
    }

    @Test
    void testCargarClientesConScoring() throws IOException {
        String json = """
            {
              "clientes": [
                {"nombre": "Alice", "scoring": 95, "siguiendo": [], "conexiones": []},
                {"nombre": "Bob", "scoring": 50, "siguiendo": [], "conexiones": []}
              ]
            }
            """;
        
        File archivo = tempDir.resolve("test.json").toFile();
        try (FileWriter writer = new FileWriter(archivo)) {
            writer.write(json);
        }
        
        JsonLoader loader = new JsonLoader();
        loader.cargarDesdeArchivo(archivo.getAbsolutePath(), gestor, cola);
        
        Cliente alice = gestor.buscarPorNombre("Alice");
        Cliente bob = gestor.buscarPorNombre("Bob");
        
        assertEquals(95, alice.getScoring());
        assertEquals(50, bob.getScoring());
    }

    @Test
    void testCargarSiguiendo() throws IOException {
        String json = """
            {
              "clientes": [
                {"nombre": "Alice", "scoring": 95, "siguiendo": ["Bob"], "conexiones": []},
                {"nombre": "Bob", "scoring": 88, "siguiendo": [], "conexiones": []}
              ]
            }
            """;
        
        File archivo = tempDir.resolve("test.json").toFile();
        try (FileWriter writer = new FileWriter(archivo)) {
            writer.write(json);
        }
        
        JsonLoader loader = new JsonLoader();
        loader.cargarDesdeArchivo(archivo.getAbsolutePath(), gestor, cola);
        
        Cliente alice = gestor.buscarPorNombre("Alice");
        assertTrue(alice.sigueA("Bob"));
    }

    @Test
    void testArchivoNoExiste() {
        assertThrows(IOException.class, () -> {
            JsonLoader loader = new JsonLoader();
            loader.cargarDesdeArchivo("/ruta/inexistente/archivo.json", gestor, cola);
        });
    }

    @Test
    void testJsonVacio() throws IOException {
        String json = """
            {
              "clientes": []
            }
            """;
        
        File archivo = tempDir.resolve("vacio.json").toFile();
        try (FileWriter writer = new FileWriter(archivo)) {
            writer.write(json);
        }
        
        JsonLoader loader = new JsonLoader();
        loader.cargarDesdeArchivo(archivo.getAbsolutePath(), gestor, cola);
        
        assertEquals(0, gestor.getCantidadClientes());
    }
}
