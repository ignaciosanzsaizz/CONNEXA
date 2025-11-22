package icai.dtc.isw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la gestión de la tabla 'favoritos'.
 * AJUSTE FINAL: id_user se convierte de String (Java) a Integer (BD).
 * id_anuncio se maneja como String (compatible con character(1) de BD).
 */
public class FavoritosDAO {

    /**
     * Alterna el estado de un anuncio como favorito para un usuario (INSERT / DELETE).
     */
    public boolean toggleFavorito(String idUsuarioStr, String idAnuncio) {
        if (isFavorito(idUsuarioStr, idAnuncio)) {
            return deleteFavorito(idUsuarioStr, idAnuncio);
        } else {
            return insertFavorito(idUsuarioStr, idAnuncio);
        }
    }

    private boolean insertFavorito(String idUsuarioStr, String idAnuncio) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "INSERT INTO favoritos (id_user, id_anuncio, es_favorito) VALUES (?, ?, TRUE)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            // Conversión OBLIGATORIA: String a Integer para la columna id_user de PgAdmin
            int idUsuario = Integer.parseInt(idUsuarioStr);

            pst.setInt(1, idUsuario);
            pst.setString(2, idAnuncio); // Se usa setString para la columna character(1)
            return pst.executeUpdate() > 0;
        } catch (NumberFormatException nfe) {
            System.err.println("Error de formato: El ID de usuario no es un número: " + idUsuarioStr);
            nfe.printStackTrace();
            return false;
        } catch (SQLException ex) {
            // 23505 es código PostgreSQL para "unique_violation" (ya existe)
            if ("23505".equals(ex.getSQLState())) {
                return true;
            }
            System.err.println("Error insertando favorito: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    private boolean deleteFavorito(String idUsuarioStr, String idAnuncio) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "DELETE FROM favoritos WHERE id_user = ? AND id_anuncio = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            // Conversión OBLIGATORIA: String a Integer para la columna id_user de PgAdmin
            int idUsuario = Integer.parseInt(idUsuarioStr);

            pst.setInt(1, idUsuario);
            pst.setString(2, idAnuncio); // Se usa setString para la columna character(1)
            return pst.executeUpdate() > 0;
        } catch (NumberFormatException nfe) {
            System.err.println("Error de formato: El ID de usuario no es un número: " + idUsuarioStr);
            nfe.printStackTrace();
            return false;
        } catch (SQLException ex) {
            System.err.println("Error eliminando favorito: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Comprueba si un anuncio es favorito para un usuario.
     */
    public boolean isFavorito(String idUsuarioStr, String idAnuncio) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "SELECT 1 FROM favoritos WHERE id_user = ? AND id_anuncio = ? LIMIT 1";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            // Conversión OBLIGATORIA: String a Integer para la columna id_user de PgAdmin
            int idUsuario = Integer.parseInt(idUsuarioStr);

            pst.setInt(1, idUsuario);
            pst.setString(2, idAnuncio);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (NumberFormatException nfe) {
            System.err.println("Error de formato: El ID de usuario no es un número: " + idUsuarioStr);
            nfe.printStackTrace();
            return false;
        } catch (SQLException ex) {
            System.err.println("Error comprobando favorito: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene la lista de IDs de anuncios favoritos para un usuario.
     */
    public List<String> getFavoritoIds(String idUsuarioStr) {
        List<String> favoritoIds = new ArrayList<>();
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "SELECT id_anuncio FROM favoritos WHERE id_user = ? AND es_favorito = TRUE";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            // Conversión OBLIGATORIA: String a Integer para la columna id_user de PgAdmin
            int idUsuario = Integer.parseInt(idUsuarioStr);

            pst.setInt(1, idUsuario);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    // Nota: Aquí recuperamos el valor como String, que es su tipo en Java.
                    favoritoIds.add(rs.getString("id_anuncio"));
                }
            }
        } catch (NumberFormatException nfe) {
            System.err.println("Error de formato: El ID de usuario no es un número: " + idUsuarioStr);
            nfe.printStackTrace();
        } catch (SQLException ex) {
            System.err.println("Error obteniendo IDs de favoritos: " + ex.getMessage());
            ex.printStackTrace();
        }
        return favoritoIds;
    }
}