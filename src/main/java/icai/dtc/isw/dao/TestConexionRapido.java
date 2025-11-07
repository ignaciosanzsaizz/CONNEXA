package icai.dtc.isw.dao;

import java.sql.*;

public class TestConexionRapido {
    public static void main(String[] args) {
        System.out.println("Test de conexión y estructura...");
        try {
            ConnectionDAO dao = ConnectionDAO.getInstance();
            Connection conn = dao.getConnection();
            System.out.println("✓ Conexión OK");

            // Verificar tabla chats
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM chats");
            if (rs.next()) {
                System.out.println("✓ Tabla chats existe - Registros: " + rs.getInt(1));
            }

            // Verificar tabla mensajes_chat
            rs = st.executeQuery("SELECT COUNT(*) FROM mensajes_chat");
            if (rs.next()) {
                System.out.println("✓ Tabla mensajes_chat existe - Registros: " + rs.getInt(1));
            }

            // Verificar anuncios con empresa_email
            rs = st.executeQuery("SELECT COUNT(*) FROM anuncios a LEFT JOIN empresa e ON a.nif_empresa = e.nif WHERE e.mail IS NOT NULL");
            if (rs.next()) {
                System.out.println("✓ Anuncios con empresa email: " + rs.getInt(1));
            }

            // Verificar anuncios sin empresa_email
            rs = st.executeQuery("SELECT COUNT(*) FROM anuncios a LEFT JOIN empresa e ON a.nif_empresa = e.nif WHERE e.mail IS NULL");
            if (rs.next()) {
                int sinEmail = rs.getInt(1);
                if (sinEmail > 0) {
                    System.out.println("⚠ Anuncios SIN empresa email: " + sinEmail);
                    System.out.println("  → Estos anuncios no tendrán botón de chat");
                }
            }

            conn.close();
            System.out.println("\n✓ Todo OK");

        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

