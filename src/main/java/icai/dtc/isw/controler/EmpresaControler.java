package icai.dtc.isw.controler;

import icai.dtc.isw.dao.EmpresaDAO;
import icai.dtc.isw.domain.Empresa;

public class EmpresaControler {
    private final EmpresaDAO dao = new EmpresaDAO();

    public Empresa getByMail(String mail) {
        if (mail == null || mail.isBlank()) return null;
        return dao.getByMail(mail);
    }

    public boolean saveOrUpdate(Empresa e) {
        if (e == null || e.getMail() == null || e.getMail().isBlank()) return false;
        if (e.getEmpresa() == null || e.getEmpresa().isBlank()) return false;
        if (e.getNif() == null || e.getNif().isBlank()) return false;
        if (e.getSector() == null || e.getSector().isBlank()) return false;
        return dao.saveOrUpdate(e);
    }
}
