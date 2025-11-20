package icai.dtc.isw.dao;

import icai.dtc.isw.domain.Pago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PagosDAO {

    /** Versión de bajo nivel con conexión existente */
    public void upsertPago(Connection con, Pago pago) throws SQLException {
        String sql = "INSERT INTO pagos (id, nombre, numerotarjeta, fechacaducidad, cvv) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "nombre = EXCLUDED.nombre, " +
                "numerotarjeta = EXCLUDED.numerotarjeta, " +
                "fechacaducidad = EXCLUDED.fechacaducidad, " +
                "cvv = EXCLUDED.cvv";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pago.getId());
            ps.setString(2, pago.getNombre());
            ps.setString(3, pago.getNumeroTarjeta());
            ps.setString(4, pago.getFechaCaducidad());
            ps.setString(5, pago.getCvv());
            ps.executeUpdate();
        }
    }

    /** Obtener pago usando conexión existente */
    public Pago getPagoByUserId(Connection con, Integer userId) throws SQLException {
        String sql = "SELECT id, nombre, numerotarjeta, fechacaducidad, cvv " +
                "FROM pagos WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Pago p = new Pago();
                    p.setId(rs.getInt("id"));
                    p.setNombre(rs.getString("nombre"));
                    p.setNumeroTarjeta(rs.getString("numerotarjeta"));
                    p.setFechaCaducidad(rs.getString("fechacaducidad"));
                    p.setCvv(rs.getString("cvv"));
                    return p;
                }
            }
        }
        return null;
    }


    /** upsert usando ConnectionDAO */
    public boolean upsert(Pago pago) {
        if (pago == null || pago.getId() == null) return false;

        Connection con = ConnectionDAO.getInstance().getConnection();
        try {
            upsertPago(con, pago);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** findByUserId usando ConnectionDAO */
    public Pago findByUserId(Integer userId) {
        if (userId == null) return null;

        Connection con = ConnectionDAO.getInstance().getConnection();
        try {
            return getPagoByUserId(con, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Borrar método de pago por id de usuario */
    public boolean deleteByUserId(Integer userId) {
        if (userId == null) return false;

        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "DELETE FROM pagos WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
