package icai.dtc.isw.dao;

import icai.dtc.isw.domain.Anuncio;

import java.sql.*;
import java.util.List;

/**
 * Test simple para ver qué devuelve el método search
 */
public class TestSearchSimple {

    public static void main(String[] args) {
        System.out.println("=== TEST DE BÚSQUEDA SIMPLE ===\n");

        try {
            // 1. Test directo con SQL
            System.out.println("1. CONSULTA SQL DIRECTA:");
            ConnectionDAO connDAO = ConnectionDAO.getInstance();
            Connection conn = connDAO.getConnection();

            String sql = "SELECT a.id, a.descripcion, a.categoria, a.especificacion, a.nif_empresa, " +
                        "a.creado_en, a.actualizado_en, e.mail AS empresa_email " +
                        "FROM anuncios a " +
                        "LEFT JOIN empresa e ON a.nif_empresa = e.nif " +
                        "ORDER BY a.creado_en DESC " +
                        "LIMIT 5";

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            int count = 0;
            System.out.println("\nPrimeros 5 anuncios con LEFT JOIN:");
            while (rs.next()) {
                count++;
                System.out.println("\n  Anuncio " + count + ":");
                System.out.println("    ID: " + rs.getString("id"));
                System.out.println("    Descripción: " + rs.getString("descripcion"));
                System.out.println("    Categoría: " + rs.getString("categoria"));
                System.out.println("    Especificación: " + rs.getString("especificacion"));
                System.out.println("    NIF Empresa: " + rs.getString("nif_empresa"));
                System.out.println("    Email Empresa: " + rs.getString("empresa_email"));
            }

            System.out.println("\n  Total encontrados: " + count);

            // 2. Test con el método search del DAO
            System.out.println("\n2. USANDO AnuncioDAO.search():");
            AnuncioDAO dao = new AnuncioDAO();

            List<Anuncio> resultados = dao.search(null, null);
            System.out.println("  Resultados: " + resultados.size());

            for (int i = 0; i < Math.min(5, resultados.size()); i++) {
                Anuncio a = resultados.get(i);
                System.out.println("\n  Anuncio " + (i+1) + ":");
                System.out.println("    ID: " + a.getId());
                System.out.println("    Descripción: " + a.getDescripcion());
                System.out.println("    Categoría: " + a.getCategoria());
                System.out.println("    Especificación: " + a.getEspecificacion());
                System.out.println("    NIF Empresa: " + a.getNifEmpresa());
                System.out.println("    Email Empresa: " + a.getEmpresaEmail());
            }

            // 3. Buscar específicamente por una categoría
            System.out.println("\n3. BUSCAR POR CATEGORÍA 'Hogar y reparaciones':");
            List<Anuncio> porCategoria = dao.search("Hogar y reparaciones", null);
            System.out.println("  Resultados: " + porCategoria.size());

            for (Anuncio a : porCategoria) {
                System.out.println("    - " + a.getId() + ": " + a.getDescripcion() +
                                 " (Email: " + a.getEmpresaEmail() + ")");
            }

            // 4. Ver estructura de la tabla empresa
            System.out.println("\n4. TABLA EMPRESA:");
            sql = "SELECT nif, empresa, mail FROM empresa ORDER BY empresa LIMIT 10";
            rs = st.executeQuery(sql);

            int empCount = 0;
            while (rs.next()) {
                empCount++;
                System.out.println("  " + empCount + ". NIF: " + rs.getString("nif") +
                                 ", Nombre: " + rs.getString("empresa") +
                                 ", Mail: " + rs.getString("mail"));
            }

            if (empCount == 0) {
                System.out.println("  ⚠️ NO HAY EMPRESAS - Por eso los anuncios no tienen email");
            }

            conn.close();

            System.out.println("\n=== FIN DEL TEST ===");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

