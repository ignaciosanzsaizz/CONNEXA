package icai.dtc.isw.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MensajeChat implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer chatId;
    private String remitenteEmail;
    private String contenido;
    private LocalDateTime enviadoEn;
    private boolean leido;

    public MensajeChat() {}

    public MensajeChat(Integer id, Integer chatId, String remitenteEmail, String contenido,
                       LocalDateTime enviadoEn, boolean leido) {
        this.id = id;
        this.chatId = chatId;
        this.remitenteEmail = remitenteEmail;
        this.contenido = contenido;
        this.enviadoEn = enviadoEn;
        this.leido = leido;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getChatId() { return chatId; }
    public void setChatId(Integer chatId) { this.chatId = chatId; }

    public String getRemitenteEmail() { return remitenteEmail; }
    public void setRemitenteEmail(String remitenteEmail) { this.remitenteEmail = remitenteEmail; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getEnviadoEn() { return enviadoEn; }
    public void setEnviadoEn(LocalDateTime enviadoEn) { this.enviadoEn = enviadoEn; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }
}

