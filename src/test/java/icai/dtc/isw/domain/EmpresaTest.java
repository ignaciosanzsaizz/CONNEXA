package icai.dtc.isw.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaTest {

    @Test
    void constructor_y_getters() {
        Empresa e = new Empresa("mail@x.com", "MiEmpresa", "B12345678", "Servicios", "Madrid");
        assertEquals("mail@x.com", e.getMail());
        assertEquals("MiEmpresa", e.getEmpresa());
        assertEquals("B12345678", e.getNif());
        assertEquals("Servicios", e.getSector());
        assertEquals("Madrid", e.getUbicacion());
    }

    @Test
    void setters_funcionan() {
        Empresa e = new Empresa();
        e.setMail("m@x.com");
        e.setEmpresa("Otra");
        e.setNif("C11111111");
        e.setSector("IT");
        e.setUbicacion("Valencia");

        assertAll(
                () -> assertEquals("m@x.com", e.getMail()),
                () -> assertEquals("Otra", e.getEmpresa()),
                () -> assertEquals("C11111111", e.getNif()),
                () -> assertEquals("IT", e.getSector()),
                () -> assertEquals("Valencia", e.getUbicacion())
        );
    }
}
