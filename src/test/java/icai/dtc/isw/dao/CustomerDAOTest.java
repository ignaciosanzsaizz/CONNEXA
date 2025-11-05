package icai.dtc.isw.dao;

import icai.dtc.isw.domain.Customer;
import icai.dtc.isw.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerDAOTest {

    private CustomerDAO customerDAO;

    @BeforeEach
    public void setUp() {
        customerDAO = new CustomerDAO();
    }

    @Test
    public void testExistsByEmail() {
        // Este es un test básico que verifica la estructura del método
        // En un entorno real, necesitarías una base de datos de prueba
        String testEmail = "test@example.com";

        // Simplemente verificamos que el método no lance excepciones
        assertDoesNotThrow(() -> customerDAO.existsByEmail(testEmail));
    }

    @Test
    public void testGetClientes() {
        // Test básico para verificar que el método no lance excepciones
        ArrayList<Customer> lista = new ArrayList<>();

        assertDoesNotThrow(() -> customerDAO.getClientes(lista));

        // La lista puede estar vacía o tener elementos dependiendo de la BD
        assertNotNull(lista);
    }

    @Test
    public void testGetClienteWithValidCredentials() {
        // Test básico para verificar que el método no lance excepciones
        String email = "test@example.com";
        String password = "password123";

        assertDoesNotThrow(() -> customerDAO.getCliente(email, password));
    }

    @Test
    public void testInsertCliente() {
        // Test básico para verificar que el método no lance excepciones
        User testUser = new User("999", "testuser", "testpass", "test@example.com");

        assertDoesNotThrow(() -> customerDAO.insertCliente(testUser));
    }

    @Test
    public void testGetByEmail() {
        // Test básico para verificar que el método no lance excepciones
        String testEmail = "test@example.com";

        assertDoesNotThrow(() -> customerDAO.getByEmail(testEmail));
    }
}

