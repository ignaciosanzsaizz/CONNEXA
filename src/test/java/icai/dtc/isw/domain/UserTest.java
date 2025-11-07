package icai.dtc.isw.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void ctorSinId_debeDejarIdVacio() {
        User u = new User("pepe", "1234", "pepe@example.com");
        assertEquals("", u.getId());
        assertEquals("pepe", u.getUsername());
        assertEquals("1234", u.getPassword());
        assertEquals("pepe@example.com", u.getEmail());
    }

    @Test
    void ctorConId_asignaTodosLosCampos() {
        User u = new User("ID-001", "ana", "abcd", "ana@example.com");
        assertEquals("ID-001", u.getId());
        assertEquals("ana", u.getUsername());
        assertEquals("abcd", u.getPassword());
        assertEquals("ana@example.com", u.getEmail());
    }

    @Test
    void permiteNulls_siSeProporcionan() {
        User u = new User(null, null, null, null);
        assertNull(u.getId());
        assertNull(u.getUsername());
        assertNull(u.getPassword());
        assertNull(u.getEmail());
    }

    @Test
    void inmutabilidad_camposFinal_noHaySetters() {
        User u = new User("ID-X", "x", "y", "z");
        assertEquals("ID-X", u.getId());
    }
}
