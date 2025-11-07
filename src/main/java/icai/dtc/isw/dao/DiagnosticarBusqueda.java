package icai.dtc.isw.dao;

import icai.dtc.isw.domain.Anuncio;

import java.sql.*;
import java.util.List;

/**
 * Script para diagnosticar problemas con búsquedas de anuncios
 */
public class DiagnosticarBusqueda {

    public static void main(String[] args) {
        System.out.println("=== DIAGNÓSTICO DE BÚSQUEDA ===\n");

        try {
            ConnectionDAO connDAO = ConnectionDAO.getInstance();
            Connection conn = connDAO.getConnection();

            System.out.println("✓ Conexión exitosa\n");

            // 1. Ver TODOS los anuncios de la BD
            System.out.println("1. TODOS LOS ANUNCIOS EN LA BASE DE DATOS:");
            String sql = "SELECT a.id, a.descripcion, a.categoria, a.especificacion, a.nif_empresa, " +
                        "e.mail AS empresa_email, e.empresa AS empresa_nombre " +
                        "FROM anuncios a " +
                        "LEFT JOIN empresa e ON a.nif_empresa = e.nif " +
                        "ORDER BY a.creado_en DESC";

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            int total = 0;
            int conEmpresa = 0;
            int sinEmpresa = 0;

            while (rs.next()) {
                total++;
                String id = rs.getString("id");
                String desc = rs.getString("descripcion");
                String cat = rs.getString("categoria");
                String esp = rs.getString("especificacion");
                String nif = rs.getString("nif_empresa");
                String email = rs.getString("empresa_email");
                String nombreEmp = rs.getString("empresa_nombre");

                System.out.println("\n  Anuncio #" + total + ":");
                System.out.println("    ID: " + id);
                System.out.println("    Descripción: " + desc);
                System.out.println("    Categoría: " + cat);
                System.out.println("    Especificación: " + esp);
                System.out.println("    NIF Empresa: " + nif);

                if (email != null && !email.isEmpty()) {
                    conEmpresa++;
                    System.out.println("    ✓ Email Empresa: " + email + " (" + nombreEmp + ")");
                } else {
                    sinEmpresa++;
                    System.out.println("    ✗ SIN EMAIL - No aparecerá en búsquedas con JOIN");
                }
            }

            System.out.println("\n  RESUMEN:");
            System.out.println("    Total anuncios: " + total);
            System.out.println("    Con empresa: " + conEmpresa);
            System.out.println("    Sin empresa: " + sinEmpresa);

            // 2. Probar el método search del DAO
            System.out.println("\n2. PRUEBA DEL MÉTODO search() DEL DAO:");
            AnuncioDAO dao = new AnuncioDAO();

            // Buscar sin filtros
            List<Anuncio> todos = dao.search(null, null);
            System.out.println("  search(null, null) devuelve: " + todos.size() + " anuncios");
            for (Anuncio a : todos) {
                System.out.println("    - " + a.getId() + ": " + a.getDescripcion() +
                                 " (empresa_email: " + a.getEmpresaEmail() + ")");
            }

            // 3. Ver empresas
            System.out.println("\n3. EMPRESAS EN LA BASE DE DATOS:");
            sql = "SELECT nif, empresa, mail FROM empresa";
            rs = st.executeQuery(sql);

            int countEmpresas = 0;
            while (rs.next()) {
                countEmpresas++;
                System.out.println("  " + countEmpresas + ". NIF: " + rs.getString("nif") +
                                 ", Nombre: " + rs.getString("empresa") +
                                 ", Email: " + rs.getString("mail"));
            }

            if (countEmpresas == 0) {
                System.out.println("  ⚠ NO HAY EMPRESAS - Los anuncios no tendrán email");
            }

            // 4. Verificar user1@gmail.com
            System.out.println("\n4. VERIFICANDO user1@gmail.com:");
            sql = "SELECT mail, username FROM users WHERE mail = 'user1@gmail.com'";
            rs = st.executeQuery(sql);
            if (rs.next()) {
                System.out.println("  ✓ Usuario existe: " + rs.getString("username"));
            } else {
                System.out.println("  ✗ Usuario NO existe");
            }

            // 5. Ver empresa de user1@gmail.com
            sql = "SELECT nif, empresa, mail FROM empresa WHERE mail = 'user1@gmail.com'";
            rs = st.executeQuery(sql);
            if (rs.next()) {
                String nif = rs.getString("nif");
                System.out.println("  ✓ Empresa existe:");
                System.out.println("    NIF: " + nif);
                System.out.println("    Nombre: " + rs.getString("empresa"));

                // Ver anuncios de esta empresa
                sql = "SELECT COUNT(*) FROM anuncios WHERE nif_empresa = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, nif);
                ResultSet rs2 = ps.executeQuery();
                if (rs2.next()) {
                    int count = rs2.getInt(1);
                    System.out.println("    Anuncios: " + count);

                    if (count > 0) {
                        sql = "SELECT id, descripcion, categoria, especificacion FROM anuncios WHERE nif_empresa = ?";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, nif);
                        rs2 = ps.executeQuery();
                        System.out.println("    Listado:");
                        while (rs2.next()) {
                            System.out.println("      - " + rs2.getString("id") + ": " +
                                             rs2.getString("descripcion") +
                                             " [" + rs2.getString("categoria") + " / " +
                                             rs2.getString("especificacion") + "]");
                        }
                    }
                }
            } else {
                System.out.println("  ✗ NO tiene empresa creada");
                System.out.println("    → Los anuncios creados no tendrán email de empresa");
            }

            conn.close();

            System.out.println("\n=== FIN DEL DIAGNÓSTICO ===");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

