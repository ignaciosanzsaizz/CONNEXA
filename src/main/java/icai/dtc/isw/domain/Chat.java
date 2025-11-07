package icai.dtc.isw.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Chat implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String clienteEmail;
    private String empresaEmail;
    private String anuncioId;  // VARCHAR para coincidir con anuncios.id
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    // Campos adicionales para mostrar info
    private String clienteNombre;
    private String empresaNombre;
    private String anuncioTitulo;
    private String ultimoMensaje;

    public Chat() {}

    public Chat(Integer id, String clienteEmail, String empresaEmail, String anuncioId,
                LocalDateTime creadoEn, LocalDateTime actualizadoEn) {
        this.id = id;
        this.clienteEmail = clienteEmail;
        this.empresaEmail = empresaEmail;
        this.anuncioId = anuncioId;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }

    public String getEmpresaEmail() { return empresaEmail; }
    public void setEmpresaEmail(String empresaEmail) { this.empresaEmail = empresaEmail; }

    public String getAnuncioId() { return anuncioId; }
    public void setAnuncioId(String anuncioId) { this.anuncioId = anuncioId; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getEmpresaNombre() { return empresaNombre; }
    public void setEmpresaNombre(String empresaNombre) { this.empresaNombre = empresaNombre; }

    public String getAnuncioTitulo() { return anuncioTitulo; }
    public void setAnuncioTitulo(String anuncioTitulo) { this.anuncioTitulo = anuncioTitulo; }

    public String getUltimoMensaje() { return ultimoMensaje; }
    public void setUltimoMensaje(String ultimoMensaje) { this.ultimoMensaje = ultimoMensaje; }
}

