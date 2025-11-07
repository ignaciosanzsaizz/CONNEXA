package icai.dtc.isw.dao;

import icai.dtc.isw.domain.Anuncio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnuncioDAO {

    /**
     * Inserta un nuevo anuncio en la base de datos.
     * Los campos creado_en y actualizado_en se establecen a NOW().
     */
    public boolean insert(Anuncio anuncio) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = """
            INSERT INTO anuncios (id, descripcion, precio, categoria, especificacion, ubicacion, nif_empresa, creado_en, actualizado_en)
            VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            """;
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, anuncio.getId());
            pst.setString(2, anuncio.getDescripcion());
            pst.setDouble(3, anuncio.getPrecio());
            pst.setString(4, anuncio.getCategoria());
            pst.setString(5, anuncio.getEspecificacion());
            pst.setString(6, anuncio.getUbicacion());
            pst.setString(7, anuncio.getNifEmpresa());

            int rows = pst.executeUpdate();
            return rows > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene un anuncio por su ID.
     */
    public Anuncio findById(String id) {
        if (id == null || id.isBlank()) return null;
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "SELECT a.id, a.descripcion, a.precio, a.categoria, a.especificacion, a.ubicacion, a.nif_empresa, a.creado_en, a.actualizado_en, e.mail AS empresa_email " +
                     "FROM anuncios a LEFT JOIN empresa e ON a.nif_empresa = e.nif " +
                     "WHERE a.id = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAnuncio(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene todos los anuncios de una empresa (por NIF).
     */
    public List<Anuncio> findByNifEmpresa(String nifEmpresa) {
        List<Anuncio> anuncios = new ArrayList<>();
        if (nifEmpresa == null || nifEmpresa.isBlank()) return anuncios;

        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "SELECT a.id, a.descripcion, a.precio, a.categoria, a.especificacion, a.ubicacion, a.nif_empresa, a.creado_en, a.actualizado_en, e.mail AS empresa_email " +
                     "FROM anuncios a LEFT JOIN empresa e ON a.nif_empresa = e.nif " +
                     "WHERE a.nif_empresa = ? ORDER BY a.creado_en DESC";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, nifEmpresa);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    anuncios.add(mapResultSetToAnuncio(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return anuncios;
    }

    /**
     * Obtiene todos los anuncios.
     */
    public List<Anuncio> findAll() {
        List<Anuncio> anuncios = new ArrayList<>();
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "SELECT a.id, a.descripcion, a.precio, a.categoria, a.especificacion, a.ubicacion, a.nif_empresa, a.creado_en, a.actualizado_en, e.mail AS empresa_email " +
                     "FROM anuncios a LEFT JOIN empresa e ON a.nif_empresa = e.nif " +
                     "ORDER BY a.creado_en DESC";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                anuncios.add(mapResultSetToAnuncio(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return anuncios;
    }

    /**
     * Actualiza un anuncio existente.
     * El campo actualizado_en se establece a NOW().
     */
    public boolean update(Anuncio anuncio) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = """
            UPDATE anuncios 
            SET descripcion = ?, precio = ?, categoria = ?, especificacion = ?, 
                ubicacion = ?, actualizado_en = NOW()
            WHERE id = ?
            """;
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, anuncio.getDescripcion());
            pst.setDouble(2, anuncio.getPrecio());
            pst.setString(3, anuncio.getCategoria());
            pst.setString(4, anuncio.getEspecificacion());
            pst.setString(5, anuncio.getUbicacion());
            pst.setString(6, anuncio.getId());

            int rows = pst.executeUpdate();
            System.out.println("AnuncioDAO.update: Filas actualizadas = " + rows + " para ID = " + anuncio.getId());
            return rows > 0;
        } catch (SQLException ex) {
            System.err.println("AnuncioDAO.update: Error SQL al actualizar anuncio ID = " + anuncio.getId());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un anuncio por su ID.
     */
    public boolean delete(String id) {
        if (id == null || id.isBlank()) {
            System.out.println("AnuncioDAO.delete: ID es null o vacío");
            return false;
        }
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "DELETE FROM anuncios WHERE id = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);
            int rows = pst.executeUpdate();
            System.out.println("AnuncioDAO.delete: Filas eliminadas = " + rows + " para ID = " + id);
            return rows > 0;
        } catch (SQLException ex) {
            System.err.println("AnuncioDAO.delete: Error SQL al eliminar anuncio ID = " + id);
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Mapea un ResultSet a un objeto Anuncio.
     */
    private Anuncio mapResultSetToAnuncio(ResultSet rs) throws SQLException {
        Anuncio a = new Anuncio();
        a.setId(rs.getString("id"));
        a.setDescripcion(rs.getString("descripcion"));
        a.setPrecio(rs.getDouble("precio"));
        a.setCategoria(rs.getString("categoria"));
        a.setEspecificacion(rs.getString("especificacion"));
        a.setUbicacion(rs.getString("ubicacion"));
        a.setNifEmpresa(rs.getString("nif_empresa"));
        a.setCreadoEn(rs.getTimestamp("creado_en"));
        a.setActualizadoEn(rs.getTimestamp("actualizado_en"));

        // Intentar obtener empresa_email si existe en el ResultSet
        try {
            String empresaEmail = rs.getString("empresa_email");
            a.setEmpresaEmail(empresaEmail);
        } catch (SQLException e) {
            // Si no existe la columna, no hacemos nada
        }

        return a;
    }


    /**
     * Busca anuncios por categoría y especificación.
     * Si un parámetro es null, no se filtra por ese campo.
     */
    public List<Anuncio> search(String categoria, String esp) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "SELECT a.id, a.descripcion, a.precio, a.categoria, a.especificacion, a.ubicacion, a.nif_empresa, a.creado_en, a.actualizado_en, e.mail AS empresa_email " +
                     "FROM anuncios a " +
                     "LEFT JOIN empresa e ON a.nif_empresa = e.nif " +
                     "WHERE (? IS NULL OR a.categoria = ?) " +
                     "  AND (? IS NULL OR a.especificacion = ?) " +
                     "ORDER BY a.creado_en DESC";
        List<Anuncio> out = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, categoria); ps.setString(2, categoria);
            ps.setString(3, esp);       ps.setString(4, esp);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Anuncio a = new Anuncio();
                    a.setId(rs.getString("id"));
                    a.setDescripcion(rs.getString("descripcion"));
                    a.setPrecio(rs.getDouble("precio"));
                    a.setCategoria(rs.getString("categoria"));
                    a.setEspecificacion(rs.getString("especificacion"));
                    a.setUbicacion(rs.getString("ubicacion"));
                    a.setNifEmpresa(rs.getString("nif_empresa"));
                    a.setCreadoEn(rs.getTimestamp("creado_en"));
                    a.setActualizadoEn(rs.getTimestamp("actualizado_en"));
                    a.setEmpresaEmail(rs.getString("empresa_email"));
                    out.add(a);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error en búsqueda de anuncios: " + ex.getMessage());
            ex.printStackTrace();
        }
        return out;
    }
}

