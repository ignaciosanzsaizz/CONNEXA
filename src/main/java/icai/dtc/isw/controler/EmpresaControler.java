package icai.dtc.isw.controler;

import icai.dtc.isw.dao.EmpresaDAO;
import icai.dtc.isw.domain.Empresa;

public class EmpresaControler {
    private final EmpresaDAO dao = new EmpresaDAO();

    /** Para el perfil del usuario seguimos recuperando por mail (UI actual). */
    public Empresa getEmpresa(String mail) {
        return dao.findByMail(mail);
    }

    /** Guardar/actualizar colisionando por NIF (PK real). */
    public boolean save(String mail, String empresa, String nif, String sector, String ubicacion) {
        if (mail == null || mail.isBlank()
                || empresa == null || empresa.isBlank()
                || nif == null || nif.isBlank()
                || sector == null || sector.isBlank()
                || ubicacion == null || ubicacion.isBlank()) {
            return false;
        }
        Empresa e = new Empresa(mail, empresa, nif, sector, ubicacion);
        return dao.upsert(e);
    }

    /** (Opcional) Getter explícito por NIF, por si lo necesitas en otras pantallas. */
    public Empresa getEmpresaByNif(String nif) {
        return dao.findByNif(nif);
    }
}