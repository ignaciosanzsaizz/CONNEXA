package icai.dtc.isw.dao;

import icai.dtc.isw.domain.Chat;
import icai.dtc.isw.domain.MensajeChat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {

    private Connection getConnection() throws Exception {
        ConnectionDAO connDAO = ConnectionDAO.getInstance();
        return connDAO.getConnection();
    }

    /**
     * Obtiene todos los chats de un usuario (como cliente o empresa)
     */
    public List<Chat> getChatsByUser(String userEmail) {
        List<Chat> chats = new ArrayList<>();
        String sql = """
            SELECT c.id, c.cliente_email, c.empresa_email, c.anuncio_id, c.creado_en, c.actualizado_en,
                   u1.username AS cliente_nombre, u2.username AS empresa_nombre, a.descripcion AS anuncio_titulo,
                   (SELECT m.contenido FROM mensajes_chat m WHERE m.chat_id = c.id ORDER BY m.enviado_en DESC LIMIT 1) AS ultimo_mensaje
            FROM chats c
            LEFT JOIN users u1 ON c.cliente_email = u1.mail
            LEFT JOIN users u2 ON c.empresa_email = u2.mail
            LEFT JOIN anuncios a ON c.anuncio_id = a.id
            WHERE c.cliente_email = ? OR c.empresa_email = ?
            ORDER BY c.actualizado_en DESC
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userEmail);
            ps.setString(2, userEmail);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Chat chat = new Chat();
                chat.setId(rs.getInt("id"));
                chat.setClienteEmail(rs.getString("cliente_email"));
                chat.setEmpresaEmail(rs.getString("empresa_email"));
                chat.setAnuncioId(rs.getString("anuncio_id"));
                chat.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
                chat.setActualizadoEn(rs.getTimestamp("actualizado_en").toLocalDateTime());
                chat.setClienteNombre(rs.getString("cliente_nombre"));
                chat.setEmpresaNombre(rs.getString("empresa_nombre"));
                chat.setAnuncioTitulo(rs.getString("anuncio_titulo"));
                chat.setUltimoMensaje(rs.getString("ultimo_mensaje"));
                chats.add(chat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chats;
    }

    /**
     * Obtiene o crea un chat entre un cliente y una empresa para un anuncio
     */
    public Chat getOrCreateChat(String clienteEmail, String empresaEmail, String anuncioId) {
        System.out.println("ChatDAO.getOrCreateChat llamado:");
        System.out.println("  clienteEmail: " + clienteEmail);
        System.out.println("  empresaEmail: " + empresaEmail);
        System.out.println("  anuncioId: " + anuncioId);

        // Primero intentamos obtener el chat existente
        String selectSql = """
            SELECT c.id, c.cliente_email, c.empresa_email, c.anuncio_id, c.creado_en, c.actualizado_en,
                   u1.username AS cliente_nombre, u2.username AS empresa_nombre, a.descripcion AS anuncio_titulo
            FROM chats c
            LEFT JOIN users u1 ON c.cliente_email = u1.mail
            LEFT JOIN users u2 ON c.empresa_email = u2.mail
            LEFT JOIN anuncios a ON c.anuncio_id = a.id
            WHERE c.cliente_email = ? AND c.empresa_email = ? AND c.anuncio_id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, clienteEmail);
            ps.setString(2, empresaEmail);
            ps.setString(3, anuncioId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("  → Chat existente encontrado con ID: " + rs.getInt("id"));
                Chat chat = new Chat();
                chat.setId(rs.getInt("id"));
                chat.setClienteEmail(rs.getString("cliente_email"));
                chat.setEmpresaEmail(rs.getString("empresa_email"));
                chat.setAnuncioId(rs.getString("anuncio_id"));
                chat.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
                chat.setActualizadoEn(rs.getTimestamp("actualizado_en").toLocalDateTime());
                chat.setClienteNombre(rs.getString("cliente_nombre"));
                chat.setEmpresaNombre(rs.getString("empresa_nombre"));
                chat.setAnuncioTitulo(rs.getString("anuncio_titulo"));
                return chat;
            } else {
                System.out.println("  → No se encontró chat existente, creando nuevo...");
            }
        } catch (Exception e) {
            System.err.println("Error al buscar chat existente: " + e.getMessage());
            e.printStackTrace();
        }

        // Si no existe, lo creamos
        String insertSql = "INSERT INTO chats (cliente_email, empresa_email, anuncio_id, creado_en, actualizado_en) VALUES (?, ?, ?, NOW(), NOW())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, clienteEmail);
            ps.setString(2, empresaEmail);
            ps.setString(3, anuncioId);

            System.out.println("  → Ejecutando INSERT...");
            int rowsAffected = ps.executeUpdate();
            System.out.println("  → Filas insertadas: " + rowsAffected);

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int newId = keys.getInt(1);
                System.out.println("  → Nuevo chat creado con ID: " + newId);
                return getChatById(newId);
            } else {
                System.err.println("  → Error: No se pudo obtener el ID generado");
            }
        } catch (Exception e) {
            System.err.println("Error al crear nuevo chat: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene un chat por su ID
     */
    public Chat getChatById(int chatId) {
        String sql = """
            SELECT c.id, c.cliente_email, c.empresa_email, c.anuncio_id, c.creado_en, c.actualizado_en,
                   u1.username AS cliente_nombre, u2.username AS empresa_nombre, a.descripcion AS anuncio_titulo
            FROM chats c
            LEFT JOIN users u1 ON c.cliente_email = u1.mail
            LEFT JOIN users u2 ON c.empresa_email = u2.mail
            LEFT JOIN anuncios a ON c.anuncio_id = a.id
            WHERE c.id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Chat chat = new Chat();
                chat.setId(rs.getInt("id"));
                chat.setClienteEmail(rs.getString("cliente_email"));
                chat.setEmpresaEmail(rs.getString("empresa_email"));
                chat.setAnuncioId(rs.getString("anuncio_id"));
                chat.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
                chat.setActualizadoEn(rs.getTimestamp("actualizado_en").toLocalDateTime());
                chat.setClienteNombre(rs.getString("cliente_nombre"));
                chat.setEmpresaNombre(rs.getString("empresa_nombre"));
                chat.setAnuncioTitulo(rs.getString("anuncio_titulo"));
                return chat;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene todos los mensajes de un chat
     */
    public List<MensajeChat> getMensajesByChat(int chatId) {
        List<MensajeChat> mensajes = new ArrayList<>();
        String sql = "SELECT id, chat_id, remitente_email, contenido, enviado_en, leido FROM mensajes_chat WHERE chat_id = ? ORDER BY enviado_en ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MensajeChat mensaje = new MensajeChat();
                mensaje.setId(rs.getInt("id"));
                mensaje.setChatId(rs.getInt("chat_id"));
                mensaje.setRemitenteEmail(rs.getString("remitente_email"));
                mensaje.setContenido(rs.getString("contenido"));
                mensaje.setEnviadoEn(rs.getTimestamp("enviado_en").toLocalDateTime());
                mensaje.setLeido(rs.getBoolean("leido"));
                mensajes.add(mensaje);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mensajes;
    }

    /**
     * Envía un mensaje en un chat
     */
    public boolean enviarMensaje(int chatId, String remitenteEmail, String contenido) {
        String insertSql = "INSERT INTO mensajes_chat (chat_id, remitente_email, contenido, enviado_en, leido) VALUES (?, ?, ?, NOW(), false)";
        String updateSql = "UPDATE chats SET actualizado_en = NOW() WHERE id = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, chatId);
                ps.setString(2, remitenteEmail);
                ps.setString(3, contenido);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, chatId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Marca los mensajes de un chat como leídos para un usuario específico
     */
    public void marcarMensajesComoLeidos(int chatId, String userEmail) {
        String sql = "UPDATE mensajes_chat SET leido = true WHERE chat_id = ? AND remitente_email != ? AND leido = false";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            ps.setString(2, userEmail);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cuenta mensajes no leídos para un usuario
     */
    public int contarMensajesNoLeidos(String userEmail) {
        String sql = """
            SELECT COUNT(*) FROM mensajes_chat m
            JOIN chats c ON m.chat_id = c.id
            WHERE (c.cliente_email = ? OR c.empresa_email = ?)
            AND m.remitente_email != ?
            AND m.leido = false
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userEmail);
            ps.setString(2, userEmail);
            ps.setString(3, userEmail);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}

