package test;

import servicio.GestorClientes;
import modelo.Cliente;

public class TestPersistenciaSolicitudes {
    
    private static final String TEST_DB = "data/clientes_TEST.json";

    public static void main(String[] args) {
        System.out.println("=== TEST PERSISTENCIA DE SOLICITUDES ===\n");
        
        // 1. Inicializar con BD de prueba
        System.out.println("-> Paso 1: Inicializando gestor y creando datos...");
        GestorClientes gestor = new GestorClientes(TEST_DB);
        
        // Limpiar archivo si ya existía con datos (simple overwrite)
        // En este caso, asumimos que el gestor cargó lo que había.
        // Agregamos usuarios frescos para el test.
        int idA = gestor.agregarCliente("User A", 50);
        int idB = gestor.agregarCliente("User B", 60); // B debe persistir
        
        // Guardar estado inicial (usuarios creados)
        gestor.guardarCambios(); 
        
        System.out.println("User A ID: " + idA);
        System.out.println("User B ID: " + idB);
        
        // 2. Enviar solicitud A -> B
        System.out.println("\n-> Paso 2: User A envía solicitud a User B");
        // Esto debería guardar automáticamente gracias a nuestra modificación
        boolean enviado = gestor.enviarSolicitud(idA, idB);
        
        if (enviado) {
            System.out.println("Solicitud enviada exitosamente.");
        } else {
            System.err.println("Fallo al enviar solicitud.");
            return;
        }
        
        // 3. Verificar en memoria (antes de reiniciar)
        Cliente b = gestor.buscarPorId(idB);
        System.out.println("Solicitudes en B (memoria): " + b.getCantidadSolicitudesPendientes());
        
        // 4. Reiniciar (Simular apagado y encendido de la app)
        System.out.println("\n-> Paso 3: Reiniciando GestorClientes (Recarga desde JSON)...");
        gestor = null; // Eliminar referencia
        GestorClientes gestorReiniciado = new GestorClientes(TEST_DB);
        
        // 5. Verificar persistencia
        System.out.println("\n-> Paso 4: Verificando datos recargados...");
        Cliente bRecargado = gestorReiniciado.buscarPorId(idB);
        
        if (bRecargado == null) {
            System.err.println("[FALLO] User B no fue encontrado tras reinicio.");
        } else {
            int pendientes = bRecargado.getCantidadSolicitudesPendientes();
            System.out.println("Solicitudes en B (recargado): " + pendientes);
            
            if (pendientes == 1) {
                System.out.println("[EXITO] La solicitud persistió correctamente.");
            } else {
                System.err.println("[FALLO] Se esperaban 1 solicitud, se encontraron: " + pendientes);
            }
        }
    }
}
