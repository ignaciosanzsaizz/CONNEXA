package icai.dtc.isw.dao;

import icai.dtc.isw.domain.Anuncio;

import java.sql.*;
import java.util.List;

/**
 * Script para verificar la estructura de la base de datos y datos de prueba
 */
public class VerificarBaseDatos {

    public static void main(String[] args) {
        System.out.println("=== VERIFICACIÓN DE BASE DE DATOS ===\n");

        try {
            ConnectionDAO connDAO = ConnectionDAO.getInstance();
            Connection conn = connDAO.getConnection();

            System.out.println("✓ Conexión exitosa a la base de datos\n");

            // 1. Verificar tabla chats
            System.out.println("1. Verificando tabla 'chats'...");
            verificarTabla(conn, "chats");

            // 2. Verificar tabla mensajes_chat
            System.out.println("\n2. Verificando tabla 'mensajes_chat'...");
            verificarTabla(conn, "mensajes_chat");

            // 3. Verificar anuncios con email de empresa
            System.out.println("\n3. Verificando anuncios...");
            AnuncioDAO anuncioDAO = new AnuncioDAO();
            List<Anuncio> anuncios = anuncioDAO.findAll();
            System.out.println("   Total anuncios: " + anuncios.size());

            int conEmail = 0;
            int sinEmail = 0;
            for (Anuncio a : anuncios) {
                if (a.getEmpresaEmail() != null && !a.getEmpresaEmail().isEmpty()) {
                    conEmail++;
                    System.out.println("   ✓ Anuncio ID " + a.getId() + " - Empresa: " + a.getEmpresaEmail());
                } else {
                    sinEmail++;
                    System.out.println("   ✗ Anuncio ID " + a.getId() + " - SIN EMAIL (NIF: " + a.getNifEmpresa() + ")");
                }
            }
            System.out.println("   → Con email: " + conEmail);
            System.out.println("   → Sin email: " + sinEmail);

            // 4. Verificar empresas
            System.out.println("\n4. Verificando empresas...");
            String sql = "SELECT nif, mail FROM empresa";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            int countEmpresas = 0;
            while (rs.next()) {
                countEmpresas++;
                System.out.println("   Empresa NIF: " + rs.getString("nif") + " - Email: " + rs.getString("mail"));
            }
            System.out.println("   Total empresas: " + countEmpresas);

            // 5. Verificar usuarios
            System.out.println("\n5. Verificando usuarios...");
            sql = "SELECT mail, username FROM users";
            rs = st.executeQuery(sql);
            int countUsers = 0;
            while (rs.next()) {
                countUsers++;
                System.out.println("   Usuario: " + rs.getString("username") + " - Email: " + rs.getString("mail"));
            }
            System.out.println("   Total usuarios: " + countUsers);

            conn.close();

            System.out.println("\n=== VERIFICACIÓN COMPLETA ===");

            if (sinEmail > 0) {
                System.out.println("\n⚠ ADVERTENCIA: Hay " + sinEmail + " anuncios sin email de empresa.");
                System.out.println("   Estos anuncios no podrán recibir mensajes de chat.");
                System.out.println("   Verifica que las empresas estén correctamente vinculadas en la BD.");
            }

        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void verificarTabla(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, tableName, null);

            if (tables.next()) {
                System.out.println("   ✓ Tabla '" + tableName + "' existe");

                // Listar columnas
                ResultSet columns = meta.getColumns(null, null, tableName, null);
                System.out.println("   Columnas:");
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    String nullable = columns.getString("IS_NULLABLE");
                    System.out.println("     - " + columnName + " (" + columnType + ")" +
                                     (nullable.equals("NO") ? " NOT NULL" : ""));
                }

                // Contar registros
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tableName);
                if (rs.next()) {
                    System.out.println("   Registros: " + rs.getInt(1));
                }
            } else {
                System.out.println("   ✗ Tabla '" + tableName + "' NO existe");
            }
        } catch (SQLException e) {
            System.err.println("   ✗ Error verificando tabla: " + e.getMessage());
        }
    }
}

