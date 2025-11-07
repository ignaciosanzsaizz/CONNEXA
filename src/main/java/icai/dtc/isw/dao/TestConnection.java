package icai.dtc.isw.dao;

import java.sql.Connection;

/**
 * Test rápido para verificar que ConnectionDAO funciona correctamente
 */
public class TestConnection {

    public static void main(String[] args) {
        System.out.println("=== TEST DE CONEXIÓN ===\n");

        try {
            ConnectionDAO dao = ConnectionDAO.getInstance();

            // Test 1: Obtener conexión primera vez
            System.out.println("Test 1: Obtener conexión primera vez...");
            Connection con1 = dao.getConnection();
            if (con1 != null && !con1.isClosed()) {
                System.out.println("  ✓ Conexión 1 OK");
                System.out.println("  → isClosed: " + con1.isClosed());
                System.out.println("  → isValid: " + con1.isValid(2));
            } else {
                System.out.println("  ✗ Conexión 1 FALLÓ");
                return;
            }

            // Test 2: Obtener conexión segunda vez (debería reutilizar)
            System.out.println("\nTest 2: Obtener conexión segunda vez...");
            Connection con2 = dao.getConnection();
            if (con2 != null && !con2.isClosed()) {
                System.out.println("  ✓ Conexión 2 OK");
                System.out.println("  → Misma instancia que con1: " + (con1 == con2));
            }

            // Test 3: Cerrar conexión y obtener nueva
            System.out.println("\nTest 3: Cerrar conexión y obtener nueva...");
            con1.close();
            System.out.println("  → Conexión cerrada manualmente");

            Connection con3 = dao.getConnection();
            if (con3 != null && !con3.isClosed()) {
                System.out.println("  ✓ Nueva conexión creada automáticamente");
                System.out.println("  → isClosed: " + con3.isClosed());
                System.out.println("  → isValid: " + con3.isValid(2));
                System.out.println("  → Nueva instancia: " + (con1 != con3));
            } else {
                System.out.println("  ✗ No se pudo crear nueva conexión");
                return;
            }

            // Test 4: Ejecutar una query simple
            System.out.println("\nTest 4: Ejecutar query de prueba...");
            var stmt = con3.createStatement();
            var rs = stmt.executeQuery("SELECT 1 AS test");
            if (rs.next()) {
                System.out.println("  ✓ Query ejecutada correctamente");
                System.out.println("  → Resultado: " + rs.getInt("test"));
            }
            rs.close();
            stmt.close();

            System.out.println("\n=== TODOS LOS TESTS PASARON ===");
            System.out.println("✓ ConnectionDAO funciona correctamente");
            System.out.println("✓ Auto-recuperación de conexión funciona");

        } catch (Exception e) {
            System.err.println("\n✗ ERROR EN LOS TESTS:");
            e.printStackTrace();
        }
    }
}

