package icai.dtc.isw.dao;

import icai.dtc.isw.domain.Anuncio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnuncioDAO {

    /**
     * Inserta un nuevo anuncio en la base de datos.
     * Las columnas creado_en y actualizado_en se rellenan automáticamente por el trigger/default de la BD.
     */
    public boolean insert(Anuncio anuncio) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = """
            INSERT INTO anuncios (id, descripcion, precio, categoria, especificacion, ubicacion, nif_empresa)
            VALUES (?, ?, ?, ?, ?, ?, ?)
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
        String sql = "SELECT id, descripcion, precio, categoria, especificacion, ubicacion, nif_empresa, creado_en, actualizado_en FROM anuncios WHERE id = ?";

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
        String sql = "SELECT id, descripcion, precio, categoria, especificacion, ubicacion, nif_empresa, creado_en, actualizado_en FROM anuncios WHERE nif_empresa = ? ORDER BY creado_en DESC";

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
        String sql = "SELECT id, descripcion, precio, categoria, especificacion, ubicacion, nif_empresa, creado_en, actualizado_en FROM anuncios ORDER BY creado_en DESC";

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
        return a;
    }
}

