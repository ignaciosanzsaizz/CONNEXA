package icai.dtc.isw.domain;

import java.io.Serializable;

public class Pago implements Serializable {
    private static final long serialVersionUID = 1L;

    // Coincide con users.id (INTEGER)
    private Integer id;
    private String nombre;
    private String numeroTarjeta;
    private String fechaCaducidad;
    private String cvv;

    public Pago() {}

    public Pago(Integer id, String nombre, String numeroTarjeta,
                String fechaCaducidad, String cvv) {
        this.id = id;
        this.nombre = nombre;
        this.numeroTarjeta = numeroTarjeta;
        this.fechaCaducidad = fechaCaducidad;
        this.cvv = cvv;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(String fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}
