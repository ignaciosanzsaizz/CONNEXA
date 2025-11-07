package icai.dtc.isw.dao;

import icai.dtc.isw.controler.ChatControler;
import icai.dtc.isw.domain.Chat;

import java.util.List;

/**
 * Clase de prueba para verificar que el sistema de chat funciona correctamente
 */
public class TestChat {

    public static void main(String[] args) {
        System.out.println("=== TEST DEL SISTEMA DE CHAT ===\n");

        ChatControler chatCtrl = new ChatControler();

        // Test 1: Verificar conexión
        System.out.println("Test 1: Verificando conexión a la base de datos...");
        try {
            ConnectionDAO conn = ConnectionDAO.getInstance();
            conn.getConnection();
            System.out.println("✓ Conexión exitosa\n");
        } catch (Exception e) {
            System.err.println("✗ Error de conexión: " + e.getMessage());
            return;
        }

        // Test 2: Obtener chats de un usuario de prueba
        System.out.println("Test 2: Obteniendo chats...");
        try {
            // Usar un email de prueba - ajusta esto según tus datos
            List<Chat> chats = chatCtrl.getChatsByUser("test@example.com");
            System.out.println("✓ Chats encontrados: " + chats.size());

            if (chats.isEmpty()) {
                System.out.println("  → No hay chats para este usuario aún");
            } else {
                for (Chat chat : chats) {
                    System.out.println("  → Chat #" + chat.getId() +
                                     " entre " + chat.getClienteEmail() +
                                     " y " + chat.getEmpresaEmail());
                }
            }
            System.out.println();
        } catch (Exception e) {
            System.err.println("✗ Error obteniendo chats: " + e.getMessage());
            e.printStackTrace();
        }

        // Test 3: Verificar estructura de tablas
        System.out.println("Test 3: Verificando estructura de tablas...");
        try {
            ConnectionDAO connDAO = ConnectionDAO.getInstance();
            var conn = connDAO.getConnection();
            var meta = conn.getMetaData();

            // Verificar tabla chats
            var rs = meta.getTables(null, null, "chats", null);
            if (rs.next()) {
                System.out.println("✓ Tabla 'chats' existe");
            } else {
                System.out.println("✗ Tabla 'chats' no encontrada");
            }

            // Verificar tabla mensajes_chat
            rs = meta.getTables(null, null, "mensajes_chat", null);
            if (rs.next()) {
                System.out.println("✓ Tabla 'mensajes_chat' existe");
            } else {
                System.out.println("✗ Tabla 'mensajes_chat' no encontrada");
            }

            conn.close();
            System.out.println();
        } catch (Exception e) {
            System.err.println("✗ Error verificando tablas: " + e.getMessage());
        }

        System.out.println("\n=== FIN DE LAS PRUEBAS ===");
        System.out.println("\nSistema de chat listo para usar!");
        System.out.println("Para probarlo:");
        System.out.println("1. Inicia sesión en la aplicación");
        System.out.println("2. Ve a la pestaña 'Búsquedas'");
        System.out.println("3. Haz doble clic en un anuncio");
        System.out.println("4. Presiona 'Contactar' para iniciar un chat");
    }
}

