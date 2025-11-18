package icai.dtc.isw.ui;

import icai.dtc.isw.client.Client;
import icai.dtc.isw.domain.Chat;
import icai.dtc.isw.domain.MensajeChat;

import java.util.HashMap;
import java.util.List;

/**
 * API para gestionar chats y mensajes desde la UI sin acceder directamente a la BD.
 */
public class ChatApi {

    @SuppressWarnings("unchecked")
    public List<Chat> getChatsByUser(String email) {
        Client c = new Client();
        HashMap<String, Object> session = new HashMap<>();
        session.put("email", email);
        session = c.sentMessage("/chat/list", session);
        Object chats = session.get("chats");
        return (chats instanceof List) ? (List<Chat>) chats : null;
    }

    public Chat getOrCreateChat(String clienteEmail, String empresaEmail, String anuncioId) {
        Client c = new Client();
        HashMap<String, Object> session = new HashMap<>();
        session.put("clienteEmail", clienteEmail);
        session.put("empresaEmail", empresaEmail);
        session.put("anuncioId", anuncioId);
        session = c.sentMessage("/chat/getOrCreate", session);
        Object chat = session.get("chat");
        return (chat instanceof Chat) ? (Chat) chat : null;
    }

    @SuppressWarnings("unchecked")
    public List<MensajeChat> getMensajesByChat(int chatId) {
        Client c = new Client();
        HashMap<String, Object> session = new HashMap<>();
        session.put("chatId", chatId);
        session = c.sentMessage("/chat/messages", session);
        Object mensajes = session.get("mensajes");
        return (mensajes instanceof List) ? (List<MensajeChat>) mensajes : null;
    }

    public boolean enviarMensaje(int chatId, String remitenteEmail, String contenido) {
        Client c = new Client();
        HashMap<String, Object> session = new HashMap<>();
        session.put("chatId", chatId);
        session.put("remitenteEmail", remitenteEmail);
        session.put("contenido", contenido);
        session = c.sentMessage("/chat/send", session);
        Object ok = session.get("ok");
        return Boolean.TRUE.equals(ok);
    }

    public void marcarMensajesComoLeidos(int chatId, String userEmail) {
        Client c = new Client();
        HashMap<String, Object> session = new HashMap<>();
        session.put("chatId", chatId);
        session.put("userEmail", userEmail);
        c.sentMessage("/chat/read", session);
    }
}

