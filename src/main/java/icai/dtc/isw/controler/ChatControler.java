package icai.dtc.isw.controler;

import icai.dtc.isw.dao.ChatDAO;
import icai.dtc.isw.domain.Chat;
import icai.dtc.isw.domain.ChatMessage;
import java.util.List;

public class ChatControler {
  private final ChatDAO dao = new ChatDAO();

  public Chat openOrCreate(int userId, String empresaNif, String anuncioId) throws Exception {
    return dao.getOrCreate(userId, empresaNif, anuncioId);
  }
  public List<Chat> listForUser(int userId) throws Exception { return dao.listForUser(userId); }
  public List<ChatMessage> listMessages(int chatId, int limit, int offset) throws Exception {
    return dao.listMessages(chatId, limit, offset);
  }
  public ChatMessage lastMessage(int chatId) throws Exception {
    return dao.lastMessage(chatId);
  }
  public ChatMessage sendFromUser(int chatId, int userId, String body) throws Exception {
    return dao.sendFromUser(chatId, userId, body);
  }
}
