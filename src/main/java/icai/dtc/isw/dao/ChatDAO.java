package icai.dtc.isw.dao;

import icai.dtc.isw.domain.Chat;
import icai.dtc.isw.domain.ChatMessage;
import java.sql.*;
import java.util.*;

public class ChatDAO {

  public Chat getOrCreate(int userId, String empresaNif, String anuncioId) throws SQLException {
    String sel = "SELECT * FROM chat WHERE user_id=? AND empresa_nif=? AND anuncio_id=?";
    Connection con = ConnectionDAO.getInstance().getConnection();
    try (PreparedStatement ps = con.prepareStatement(sel)) {
      ps.setInt(1, userId);
      ps.setString(2, empresaNif);
      ps.setString(3, anuncioId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) return mapChat(rs);
    }
    String ins = "INSERT INTO chat(user_id, empresa_nif, anuncio_id) VALUES(?,?,?) RETURNING *";
    try (PreparedStatement ps = con.prepareStatement(ins)) {
      ps.setInt(1, userId);
      ps.setString(2, empresaNif);
      ps.setString(3, anuncioId);
      ResultSet rs = ps.executeQuery();
      rs.next(); return mapChat(rs);
    }
  }

  public List<Chat> listForUser(int userId) throws SQLException {
    String q = "SELECT * FROM chat WHERE user_id=? ORDER BY created_at DESC";
    Connection con = ConnectionDAO.getInstance().getConnection();
    try (PreparedStatement ps = con.prepareStatement(q)) {
      ps.setInt(1, userId);
      ResultSet rs = ps.executeQuery();
      List<Chat> out = new ArrayList<>();
      while (rs.next()) out.add(mapChat(rs));
      return out;
    }
  }

  public List<ChatMessage> listMessages(int chatId, int limit, int offset) throws SQLException {
    String q = "SELECT * FROM chat_message WHERE chat_id=? ORDER BY created_at ASC LIMIT ? OFFSET ?";
    Connection con = ConnectionDAO.getInstance().getConnection();
    try (PreparedStatement ps = con.prepareStatement(q)) {
      ps.setInt(1, chatId); ps.setInt(2, limit); ps.setInt(3, offset);
      ResultSet rs = ps.executeQuery();
      List<ChatMessage> out = new ArrayList<>();
      while (rs.next()) out.add(mapMsg(rs));
      return out;
    }
  }

  public ChatMessage lastMessage(int chatId) throws SQLException {
    String q = "SELECT * FROM chat_message WHERE chat_id=? ORDER BY created_at DESC LIMIT 1";
    Connection con = ConnectionDAO.getInstance().getConnection();
    try (PreparedStatement ps = con.prepareStatement(q)) {
      ps.setInt(1, chatId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) return mapMsg(rs);
      return null;
    }
  }

  public ChatMessage sendFromUser(int chatId, int userId, String body) throws SQLException {
    String ins = "INSERT INTO chat_message(chat_id, sender_type, sender_user_id, body) " +
                 "VALUES(?, 'USER', ?, ?) RETURNING *";
    Connection con = ConnectionDAO.getInstance().getConnection();
    try (PreparedStatement ps = con.prepareStatement(ins)) {
      ps.setInt(1, chatId); ps.setInt(2, userId); ps.setString(3, body);
      ResultSet rs = ps.executeQuery(); rs.next(); return mapMsg(rs);
    }
  }

  // utilidades
  private Chat mapChat(ResultSet rs) throws SQLException {
    Chat c = new Chat();
    c.setId(rs.getInt("id"));
    c.setUserId(rs.getInt("user_id"));
    c.setEmpresaNif(rs.getString("empresa_nif"));
    c.setAnuncioId(rs.getString("anuncio_id"));
    c.setCreatedAt(rs.getTimestamp("created_at"));
    return c;
  }

  private ChatMessage mapMsg(ResultSet rs) throws SQLException {
    ChatMessage m = new ChatMessage();
    m.setId(rs.getInt("id"));
    m.setChatId(rs.getInt("chat_id"));
    m.setSenderType(rs.getString("sender_type"));
    m.setSenderUserId((Integer) rs.getObject("sender_user_id"));
    m.setSenderNif(rs.getString("sender_nif"));
    m.setBody(rs.getString("body"));
    m.setCreatedAt(rs.getTimestamp("created_at"));
    m.setReadAt(rs.getTimestamp("read_at"));
    return m;
  }
}
