package icai.dtc.isw.domain;

import java.io.Serializable;

public class Empresa implements Serializable {
    private static final long serialVersionUID = 1L;

    private String mail;
    private String empresa;
    private String nif;
    private String sector;

    public Empresa() {}

    public Empresa(String mail, String empresa, String nif, String sector) {
        this.mail = mail;
        this.empresa = empresa;
        this.nif = nif;
        this.sector = sector;
    }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
}
