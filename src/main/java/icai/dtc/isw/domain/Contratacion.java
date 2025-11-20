package icai.dtc.isw.domain;

import java.io.Serializable;
import java.sql.Timestamp;

public class Contratacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nifEmpresa;
    private Integer idUser;
    private String idAnuncio;
    private Boolean esFavorito;
    private Float calidad;
    private String comentarios;
    private Timestamp fechaContratacion;
    private Timestamp fechaTerminacion;
    private String estado; // "activo", "terminado", "valorado"

    // Campos adicionales para mostrar información
    private String nombreEmpresa;
    private String descripcionAnuncio;
    private String categoriaAnuncio;

    public Contratacion() {}

    public Contratacion(String nifEmpresa, Integer idUser, String idAnuncio) {
        this.nifEmpresa = nifEmpresa;
        this.idUser = idUser;
        this.idAnuncio = idAnuncio;
        this.esFavorito = false;
        this.estado = "activo";
    }

    // Getters y Setters
    public String getNifEmpresa() {
        return nifEmpresa;
    }

    public void setNifEmpresa(String nifEmpresa) {
        this.nifEmpresa = nifEmpresa;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public String getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(String idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

    public Boolean getEsFavorito() {
        return esFavorito;
    }

    public void setEsFavorito(Boolean esFavorito) {
        this.esFavorito = esFavorito;
    }

    public Float getCalidad() {
        return calidad;
    }

    public void setCalidad(Float calidad) {
        this.calidad = calidad;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public Timestamp getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(Timestamp fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public Timestamp getFechaTerminacion() {
        return fechaTerminacion;
    }

    public void setFechaTerminacion(Timestamp fechaTerminacion) {
        this.fechaTerminacion = fechaTerminacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getDescripcionAnuncio() {
        return descripcionAnuncio;
    }

    public void setDescripcionAnuncio(String descripcionAnuncio) {
        this.descripcionAnuncio = descripcionAnuncio;
    }

    public String getCategoriaAnuncio() {
        return categoriaAnuncio;
    }

    public void setCategoriaAnuncio(String categoriaAnuncio) {
        this.categoriaAnuncio = categoriaAnuncio;
    }

    public boolean isActivo() {
        return "activo".equals(estado);
    }

    public boolean isTerminado() {
        return "terminado".equals(estado);
    }

    public boolean isValorado() {
        return "valorado".equals(estado);
    }
}

