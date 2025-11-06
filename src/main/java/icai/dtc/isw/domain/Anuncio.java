package icai.dtc.isw.domain;

import java.io.Serializable;
import java.sql.Timestamp;

public class Anuncio implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;              // UUID generado
    private String descripcion;
    private Double precio;
    private String categoria;
    private String especificacion;
    private String ubicacion;
    private String nifEmpresa;      // FK a empresa.nif
    private Timestamp creadoEn;
    private Timestamp actualizadoEn;

    public Anuncio() {}

    public Anuncio(String id, String descripcion, Double precio, String categoria,
                   String especificacion, String ubicacion, String nifEmpresa) {
        this.id = id;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.especificacion = especificacion;
        this.ubicacion = ubicacion;
        this.nifEmpresa = nifEmpresa;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getEspecificacion() { return especificacion; }
    public void setEspecificacion(String especificacion) { this.especificacion = especificacion; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getNifEmpresa() { return nifEmpresa; }
    public void setNifEmpresa(String nifEmpresa) { this.nifEmpresa = nifEmpresa; }

    public Timestamp getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Timestamp creadoEn) { this.creadoEn = creadoEn; }

    public Timestamp getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Timestamp actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    @Override
    public String toString() {
        return "Anuncio{" +
                "id='" + id + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", categoria='" + categoria + '\'' +
                ", especificacion='" + especificacion + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", nifEmpresa='" + nifEmpresa + '\'' +
                '}';
    }
}