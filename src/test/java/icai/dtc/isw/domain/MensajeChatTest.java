package icai.dtc.isw.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MensajeChatTest {

    @Test
    void constructorVacio_y_settersFuncionan() {
        MensajeChat m = new MensajeChat();

        m.setId(10);
        m.setChatId(77);
        m.setRemitenteEmail("user@example.com");
        m.setContenido("Hola!");
        LocalDateTime ahora = LocalDateTime.now();
        m.setEnviadoEn(ahora);
        m.setLeido(false);

        assertAll(
                () -> assertEquals(10, m.getId()),
                () -> assertEquals(77, m.getChatId()),
                () -> assertEquals("user@example.com", m.getRemitenteEmail()),
                () -> assertEquals("Hola!", m.getContenido()),
                () -> assertEquals(ahora, m.getEnviadoEn()),
                () -> assertFalse(m.isLeido())
        );
    }

    @Test
    void constructorCompleto_asignaCampos() {
        LocalDateTime t = LocalDateTime.of(2025, 1, 1, 12, 30);
        MensajeChat m = new MensajeChat(1, 2, "empresa@x.com", "Mensaje", t, true);

        assertEquals(1, m.getId());
        assertEquals(2, m.getChatId());
        assertEquals("empresa@x.com", m.getRemitenteEmail());
        assertEquals("Mensaje", m.getContenido());
        assertEquals(t, m.getEnviadoEn());
        assertTrue(m.isLeido());
    }

    @Test
    void toggleLeido_funciona() {
        MensajeChat m = new MensajeChat();
        m.setLeido(false);
        assertFalse(m.isLeido());
        m.setLeido(true);
        assertTrue(m.isLeido());
    }
}
