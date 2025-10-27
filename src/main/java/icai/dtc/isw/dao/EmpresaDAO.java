package icai.dtc.isw.dao;

import icai.dtc.isw.domain.Empresa;

import java.sql.*;

public class EmpresaDAO {

    public Empresa getByMail(String mail) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "SELECT mail, empresa, nif, sector FROM empresa WHERE mail = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, mail);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Empresa(
                            rs.getString("mail"),
                            rs.getString("empresa"),
                            rs.getString("nif"),
                            rs.getString("sector")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean exists(String mail) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "SELECT 1 FROM empresa WHERE mail = ? LIMIT 1";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, mail);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insert(Empresa e) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "INSERT INTO empresa (mail, empresa, nif, sector) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, e.getMail());
            pst.setString(2, e.getEmpresa());
            pst.setString(3, e.getNif());
            pst.setString(4, e.getSector());
            return pst.executeUpdate() == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean update(Empresa e) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "UPDATE empresa SET empresa = ?, nif = ?, sector = ? WHERE mail = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, e.getEmpresa());
            pst.setString(2, e.getNif());
            pst.setString(3, e.getSector());
            pst.setString(4, e.getMail());
            return pst.executeUpdate() == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Upsert simple, portable */
    public boolean saveOrUpdate(Empresa e) {
        if (exists(e.getMail())) return update(e);
        return insert(e);
    }
}
