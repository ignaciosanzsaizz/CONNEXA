package icai.dtc.isw.dao;

import icai.dtc.isw.domain.Contratacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContratacionDAO {

    /**
     * Crea una nueva contratación
     */
    public boolean crear(String nifEmpresa, Integer idUser, String idAnuncio) {
        Connection con = ConnectionDAO.getInstance().getConnection();

        if (existe(nifEmpresa, idUser, idAnuncio)) {
            return true;
        }

        String sql = """
            INSERT INTO contrataciones (nif_empresa, id_user, id_anuncio, es_favorito, estado, fecha_contratacion)
            VALUES (?, ?, ?, false, 'activo', CURRENT_TIMESTAMP)
            """;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, nifEmpresa);
            pst.setInt(2, idUser);
            pst.setString(3, idAnuncio);
            return pst.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica si existe una contratación
     */
    public boolean existe(String nifEmpresa, Integer idUser, String idAnuncio) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "SELECT COUNT(*) FROM contrataciones WHERE nif_empresa = ? AND id_user = ? AND id_anuncio = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, nifEmpresa);
            pst.setInt(2, idUser);
            pst.setString(3, idAnuncio);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Obtiene todas las contrataciones de un usuario con información adicional
     */
    public List<Contratacion> findByUser(Integer idUser) {
        List<Contratacion> lista = new ArrayList<>();
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = """
            SELECT c.*, e.empresa as nombre_empresa, a.descripcion as descripcion_anuncio, a.categoria as categoria_anuncio
            FROM contrataciones c
            LEFT JOIN empresa e ON c.nif_empresa = e.nif
            LEFT JOIN anuncios a ON c.id_anuncio = a.id
            WHERE c.id_user = ?
            ORDER BY c.fecha_contratacion DESC
            """;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idUser);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Contratacion c = new Contratacion();
                    c.setNifEmpresa(rs.getString("nif_empresa"));
                    c.setIdUser(rs.getInt("id_user"));
                    c.setIdAnuncio(rs.getString("id_anuncio"));
                    c.setEsFavorito(rs.getBoolean("es_favorito"));

                    Float calidad = rs.getFloat("calidad");
                    c.setCalidad(rs.wasNull() ? null : calidad);

                    c.setComentarios(rs.getString("comentarios"));
                    c.setFechaContratacion(rs.getTimestamp("fecha_contratacion"));
                    c.setFechaTerminacion(rs.getTimestamp("fecha_terminacion"));
                    c.setEstado(rs.getString("estado"));
                    c.setNombreEmpresa(rs.getString("nombre_empresa"));
                    c.setDescripcionAnuncio(rs.getString("descripcion_anuncio"));
                    c.setCategoriaAnuncio(rs.getString("categoria_anuncio"));

                    lista.add(c);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lista;
    }

    /**
     * Termina un contrato cambiando su estado a "terminado"
     */
    public boolean terminar(String nifEmpresa, Integer idUser, String idAnuncio) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = """
            UPDATE contrataciones
            SET estado = 'terminado', fecha_terminacion = CURRENT_TIMESTAMP
            WHERE nif_empresa = ? AND id_user = ? AND id_anuncio = ? AND estado = 'activo'
            """;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, nifEmpresa);
            pst.setInt(2, idUser);
            pst.setString(3, idAnuncio);
            return pst.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Guarda la valoración y comentario de una contratación terminada
     */
    public boolean valorar(String nifEmpresa, Integer idUser, String idAnuncio, Float calidad, String comentarios) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = """
            UPDATE contrataciones
            SET calidad = ?, comentarios = ?, estado = 'valorado'
            WHERE nif_empresa = ? AND id_user = ? AND id_anuncio = ? AND estado = 'terminado'
            """;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setFloat(1, calidad);
            pst.setString(2, comentarios);
            pst.setString(3, nifEmpresa);
            pst.setInt(4, idUser);
            pst.setString(5, idAnuncio);
            int rows = pst.executeUpdate();

            // El trigger actualizar_calidad_empresa se ejecutará automáticamente
            return rows > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene el estado de una contratación específica
     */
    public String getEstado(String nifEmpresa, Integer idUser, String idAnuncio) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "SELECT estado FROM contrataciones WHERE nif_empresa = ? AND id_user = ? AND id_anuncio = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, nifEmpresa);
            pst.setInt(2, idUser);
            pst.setString(3, idAnuncio);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("estado");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene una contratación específica
     */
    public Contratacion findOne(String nifEmpresa, Integer idUser, String idAnuncio) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = """
            SELECT c.*, e.empresa as nombre_empresa, a.descripcion as descripcion_anuncio, a.categoria as categoria_anuncio
            FROM contrataciones c
            LEFT JOIN empresa e ON c.nif_empresa = e.nif
            LEFT JOIN anuncios a ON c.id_anuncio = a.id
            WHERE c.nif_empresa = ? AND c.id_user = ? AND c.id_anuncio = ?
            """;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, nifEmpresa);
            pst.setInt(2, idUser);
            pst.setString(3, idAnuncio);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Contratacion c = new Contratacion();
                    c.setNifEmpresa(rs.getString("nif_empresa"));
                    c.setIdUser(rs.getInt("id_user"));
                    c.setIdAnuncio(rs.getString("id_anuncio"));
                    c.setEsFavorito(rs.getBoolean("es_favorito"));

                    Float calidad = rs.getFloat("calidad");
                    c.setCalidad(rs.wasNull() ? null : calidad);

                    c.setComentarios(rs.getString("comentarios"));
                    c.setFechaContratacion(rs.getTimestamp("fecha_contratacion"));
                    c.setFechaTerminacion(rs.getTimestamp("fecha_terminacion"));
                    c.setEstado(rs.getString("estado"));
                    c.setNombreEmpresa(rs.getString("nombre_empresa"));
                    c.setDescripcionAnuncio(rs.getString("descripcion_anuncio"));
                    c.setCategoriaAnuncio(rs.getString("categoria_anuncio"));

                    return c;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

