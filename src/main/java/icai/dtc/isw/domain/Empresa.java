package icai.dtc.isw.domain;

import java.io.Serializable;

public class Empresa implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private String mail;       // pk/fk a users.mail
    private String empresa;
    private String nif;
    private String sector;
    private String ubicacion;
    private String fotoPerfil; // base64 de la imagen de perfil
    private Float calidad; // promedio de valoraciones (1-5)
    public Empresa() {}

    public Empresa(String mail, String empresa, String nif, String sector, String ubicacion) {
        this.mail = mail;
        this.empresa = empresa;
        this.nif = nif;
        this.sector = sector;
        this.ubicacion = ubicacion;
    }

    public Empresa(String mail, String empresa, String nif, String sector, String ubicacion, String fotoPerfil) {
        this.mail = mail;
        this.empresa = empresa;
        this.nif = nif;
        this.sector = sector;
        this.ubicacion = ubicacion;
        this.fotoPerfil = fotoPerfil;
    }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public Float getCalidad() { return calidad; }
    public void setCalidad(Float calidad) { this.calidad = calidad; }

    @Override
    public Empresa clone() {
        try {
            return (Empresa) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clonación no soportada");
        }
    }

    public Empresa deepClone() {
        Empresa cloned = new Empresa(this.mail, this.empresa, this.nif, this.sector, this.ubicacion, this.fotoPerfil);
        cloned.setCalidad(this.calidad);
        return cloned;
    }
}
