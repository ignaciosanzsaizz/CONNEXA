package icai.dtc.isw.domain;
import java.sql.Timestamp;

public class ChatMessage {
  private Integer id;
  private Integer chatId;
  private String senderType; // USER | EMPRESA
  private Integer senderUserId;
  private String senderNif;
  private String body;
  private Timestamp createdAt;
  private Timestamp readAt;

  // getters/setters…
  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public Integer getChatId() { return chatId; }
  public void setChatId(Integer chatId) { this.chatId = chatId; }
  public String getSenderType() { return senderType; }
  public void setSenderType(String senderType) { this.senderType = senderType; }
  public Integer getSenderUserId() { return senderUserId; }
  public void setSenderUserId(Integer senderUserId) { this.senderUserId = senderUserId; }
  public String getSenderNif() { return senderNif; }
  public void setSenderNif(String senderNif) { this.senderNif = senderNif; }
  public String getBody() { return body; }
  public void setBody(String body) { this.body = body; }
  public Timestamp getCreatedAt() { return createdAt; }
  public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
  public Timestamp getReadAt() { return readAt; }
  public void setReadAt(Timestamp readAt) { this.readAt = readAt; }
}
