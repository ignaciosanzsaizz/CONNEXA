package icai.dtc.isw.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ChatTest {

    @Test
    void constructorVacio_y_settersBasicos() {
        Chat c = new Chat();
        c.setId(101);
        c.setClienteEmail("cliente@x.com");
        c.setEmpresaEmail("empresa@x.com");
        c.setAnuncioId("AN-9");
        LocalDateTime creado = LocalDateTime.now().minusHours(1);
        LocalDateTime actualizado = LocalDateTime.now();
        c.setCreadoEn(creado);
        c.setActualizadoEn(actualizado);

        assertAll(
                () -> assertEquals(101, c.getId()),
                () -> assertEquals("cliente@x.com", c.getClienteEmail()),
                () -> assertEquals("empresa@x.com", c.getEmpresaEmail()),
                () -> assertEquals("AN-9", c.getAnuncioId()),
                () -> assertEquals(creado, c.getCreadoEn()),
                () -> assertEquals(actualizado, c.getActualizadoEn())
        );
    }

    @Test
    void constructorCompleto_asignaCamposPrincipales() {
        LocalDateTime creado = LocalDateTime.of(2025, 1, 10, 10, 0);
        LocalDateTime actualizado = LocalDateTime.of(2025, 1, 10, 12, 0);

        Chat c = new Chat(5, "cli@x.com", "emp@x.com", "AN-1", creado, actualizado);

        assertEquals(5, c.getId());
        assertEquals("cli@x.com", c.getClienteEmail());
        assertEquals("emp@x.com", c.getEmpresaEmail());
        assertEquals("AN-1", c.getAnuncioId());
        assertEquals(creado, c.getCreadoEn());
        assertEquals(actualizado, c.getActualizadoEn());
    }

    @Test
    void camposAdicionales_paraMostrarInfo_seAsignanBien() {
        Chat c = new Chat();
        c.setClienteNombre("Cliente Uno");
        c.setEmpresaNombre("Empresa SA");
        c.setAnuncioTitulo("Limpieza Express");
        c.setUltimoMensaje("¿Cuándo puedes pasar?");

        assertAll(
                () -> assertEquals("Cliente Uno", c.getClienteNombre()),
                () -> assertEquals("Empresa SA", c.getEmpresaNombre()),
                () -> assertEquals("Limpieza Express", c.getAnuncioTitulo()),
                () -> assertEquals("¿Cuándo puedes pasar?", c.getUltimoMensaje())
        );
    }

    @Test
    void permiteNulls_sinLanzarExcepcion() {
        Chat c = new Chat();
        c.setClienteEmail(null);
        c.setEmpresaEmail(null);
        c.setAnuncioId(null);
        c.setCreadoEn(null);
        c.setActualizadoEn(null);

        assertAll(
                () -> assertNull(c.getClienteEmail()),
                () -> assertNull(c.getEmpresaEmail()),
                () -> assertNull(c.getAnuncioId()),
                () -> assertNull(c.getCreadoEn()),
                () -> assertNull(c.getActualizadoEn())
        );
    }
}
