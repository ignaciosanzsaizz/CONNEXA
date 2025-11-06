package icai.dtc.isw.domain;
import java.sql.Timestamp;

public class Chat {
  private Integer id;
  private Integer userId;
  private String empresaNif;
  private String anuncioId;
  private Timestamp createdAt;

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }

  public Integer getUserId() { return userId; }
  public void setUserId(Integer userId) { this.userId = userId; }

  public String getEmpresaNif() { return empresaNif; }
  public void setEmpresaNif(String empresaNif) { this.empresaNif = empresaNif; }

  public String getAnuncioId() { return anuncioId; }
  public void setAnuncioId(String anuncioId) { this.anuncioId = anuncioId; }

  public Timestamp getCreatedAt() { return createdAt; }
  public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
