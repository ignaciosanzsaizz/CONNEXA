package icai.dtc.isw.dao;

import icai.dtc.isw.domain.Empresa;

import java.sql.*;

public class EmpresaDAO {

    public Empresa findByMail(String mail) {
        if (mail == null || mail.isBlank()) return null;
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "SELECT mail, empresa, nif, sector, ubicacion, foto_perfil FROM empresa WHERE mail = ? LIMIT 1";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, mail);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Empresa e = new Empresa();
                    e.setMail(rs.getString("mail"));
                    e.setEmpresa(rs.getString("empresa"));
                    e.setNif(rs.getString("nif"));
                    e.setSector(rs.getString("sector"));
                    e.setUbicacion(rs.getString("ubicacion"));
                    e.setFotoPerfil(rs.getString("foto_perfil"));
                    return e;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Empresa findByNif(String nif) {
        if (nif == null || nif.isBlank()) return null;
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = "SELECT mail, empresa, nif, sector, ubicacion, foto_perfil FROM empresa WHERE nif = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, nif);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Empresa e = new Empresa();
                    e.setMail(rs.getString("mail"));
                    e.setEmpresa(rs.getString("empresa"));
                    e.setNif(rs.getString("nif"));
                    e.setSector(rs.getString("sector"));
                    e.setUbicacion(rs.getString("ubicacion"));
                    e.setFotoPerfil(rs.getString("foto_perfil"));
                    return e;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /** Inserta o actualiza (upsert) colisionando por NIF (PK) */
    public boolean upsert(Empresa emp) {
        Connection con = ConnectionDAO.getInstance().getConnection();
        String sql = """
            INSERT INTO empresa (nif, mail, empresa, sector, ubicacion, foto_perfil, calidad, num_trabajos, verificado)
            VALUES (?, ?, ?, ?, ?, ?, NULL, 0, false)
            ON CONFLICT (nif) DO UPDATE
            SET mail        = EXCLUDED.mail,
                empresa     = EXCLUDED.empresa,
                sector      = EXCLUDED.sector,
                ubicacion   = EXCLUDED.ubicacion,
                foto_perfil = EXCLUDED.foto_perfil
            """;
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, emp.getNif());
            pst.setString(2, emp.getMail());
            pst.setString(3, emp.getEmpresa());
            pst.setString(4, emp.getSector());
            pst.setString(5, emp.getUbicacion());
            pst.setString(6, emp.getFotoPerfil());
            // INSERT o UPDATE devuelve >=1 en Postgres
            return pst.executeUpdate() >= 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}