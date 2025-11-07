package icai.dtc.isw.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class AnuncioTest {

    private Anuncio a;

    @BeforeEach
    void setUp() {
        a = new Anuncio();
        a.setId("uuid-123");
        a.setDescripcion("Limpieza del hogar");
        a.setPrecio(49.99);
        a.setCategoria("Hogar");
        a.setEspecificacion("2 horas, material incluido");
        a.setUbicacion("Madrid");
        a.setNifEmpresa("A12345678");
        a.setEmpresaEmail("contacto@empresa.com");
    }

    @Test
    void gettersSetters_basicos() {
        assertEquals("uuid-123", a.getId());
        assertEquals("Limpieza del hogar", a.getDescripcion());
        assertEquals(49.99, a.getPrecio());
        assertEquals("Hogar", a.getCategoria());
        assertEquals("2 horas, material incluido", a.getEspecificacion());
        assertEquals("Madrid", a.getUbicacion());
        assertEquals("A12345678", a.getNifEmpresa());
        assertEquals("contacto@empresa.com", a.getEmpresaEmail());
    }

    @Test
    void alias_getEmpresaNif_coincideConNifEmpresa() {
        assertEquals(a.getNifEmpresa(), a.getEmpresaNif());
    }

    @Test
    void timestamps_puedenAsignarse() {
        Timestamp ahora = new Timestamp(System.currentTimeMillis());
        a.setCreadoEn(ahora);
        a.setActualizadoEn(ahora);
        assertEquals(ahora, a.getCreadoEn());
        assertEquals(ahora, a.getActualizadoEn());
    }

    @Test
    void toString_contieneCamposClave() {
        String s = a.toString();
        assertNotNull(s);
        assertTrue(s.contains("Anuncio{"));
        assertTrue(s.contains("id='uuid-123'"));
        assertTrue(s.contains("descripcion='Limpieza del hogar'"));
        assertTrue(s.contains("precio=49.99"));
        assertTrue(s.contains("categoria='Hogar'"));
        assertTrue(s.contains("nifEmpresa='A12345678'"));
        assertFalse(s.contains("contacto@empresa.com"));
    }

    @Test
    void precio_debeSerPositivo() {
        assertTrue(a.getPrecio() > 0);
    }
}
