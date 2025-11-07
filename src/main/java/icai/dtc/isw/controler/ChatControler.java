package icai.dtc.isw.controler;

import icai.dtc.isw.dao.ChatDAO;
import icai.dtc.isw.domain.Chat;
import icai.dtc.isw.domain.MensajeChat;

import java.util.List;

public class ChatControler {
    private final ChatDAO chatDAO = new ChatDAO();

    /**
     * Obtiene todos los chats de un usuario
     */
    public List<Chat> getChatsByUser(String userEmail) {
        return chatDAO.getChatsByUser(userEmail);
    }

    /**
     * Obtiene o crea un chat entre un cliente y una empresa para un anuncio
     */
    public Chat getOrCreateChat(String clienteEmail, String empresaEmail, String anuncioId) {
        return chatDAO.getOrCreateChat(clienteEmail, empresaEmail, anuncioId);
    }

    /**
     * Obtiene un chat por su ID
     */
    public Chat getChatById(int chatId) {
        return chatDAO.getChatById(chatId);
    }

    /**
     * Obtiene todos los mensajes de un chat
     */
    public List<MensajeChat> getMensajesByChat(int chatId) {
        return chatDAO.getMensajesByChat(chatId);
    }

    /**
     * Envía un mensaje en un chat
     */
    public boolean enviarMensaje(int chatId, String remitenteEmail, String contenido) {
        if (contenido == null || contenido.trim().isEmpty()) {
            return false;
        }
        return chatDAO.enviarMensaje(chatId, remitenteEmail, contenido.trim());
    }

    /**
     * Marca los mensajes de un chat como leídos
     */
    public void marcarMensajesComoLeidos(int chatId, String userEmail) {
        chatDAO.marcarMensajesComoLeidos(chatId, userEmail);
    }

    /**
     * Cuenta mensajes no leídos para un usuario
     */
    public int contarMensajesNoLeidos(String userEmail) {
        return chatDAO.contarMensajesNoLeidos(userEmail);
    }
}

