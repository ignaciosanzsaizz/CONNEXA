package icai.dtc.isw.controler;

import icai.dtc.isw.dao.ContratacionDAO;
import icai.dtc.isw.domain.Contratacion;

import java.util.List;

public class ContratacionControler {
    private final ContratacionDAO dao = new ContratacionDAO();

    /**
     * Crea una nueva contratación
     */
    public boolean crearContratacion(String nifEmpresa, Integer idUser, String idAnuncio) {
        if (nifEmpresa == null || nifEmpresa.isBlank() || idUser == null || idAnuncio == null || idAnuncio.isBlank()) {
            return false;
        }
        return dao.crear(nifEmpresa, idUser, idAnuncio);
    }

    /**
     * Verifica si existe una contratación
     */
    public boolean existeContratacion(String nifEmpresa, Integer idUser, String idAnuncio) {
        if (nifEmpresa == null || nifEmpresa.isBlank() || idUser == null || idAnuncio == null || idAnuncio.isBlank()) {
            return false;
        }
        return dao.existe(nifEmpresa, idUser, idAnuncio);
    }

    /**
     * Obtiene todas las contrataciones de un usuario
     */
    public List<Contratacion> getContratacionesByUser(Integer idUser) {
        if (idUser == null) {
            return List.of();
        }
        return dao.findByUser(idUser);
    }

    /**
     * Termina un contrato
     */
    public boolean terminarContrato(String nifEmpresa, Integer idUser, String idAnuncio) {
        if (nifEmpresa == null || nifEmpresa.isBlank() || idUser == null || idAnuncio == null || idAnuncio.isBlank()) {
            return false;
        }
        return dao.terminar(nifEmpresa, idUser, idAnuncio);
    }

    /**
     * Valora una contratación terminada
     */
    public boolean valorarContratacion(String nifEmpresa, Integer idUser, String idAnuncio, Float calidad, String comentarios) {
        if (nifEmpresa == null || nifEmpresa.isBlank() || idUser == null || idAnuncio == null || idAnuncio.isBlank()) {
            return false;
        }
        if (calidad == null || calidad < 1 || calidad > 5) {
            return false;
        }
        return dao.valorar(nifEmpresa, idUser, idAnuncio, calidad, comentarios);
    }

    /**
     * Obtiene el estado de una contratación
     */
    public String getEstado(String nifEmpresa, Integer idUser, String idAnuncio) {
        if (nifEmpresa == null || nifEmpresa.isBlank() || idUser == null || idAnuncio == null || idAnuncio.isBlank()) {
            return null;
        }
        return dao.getEstado(nifEmpresa, idUser, idAnuncio);
    }

    /**
     * Obtiene una contratación específica
     */
    public Contratacion getContratacion(String nifEmpresa, Integer idUser, String idAnuncio) {
        if (nifEmpresa == null || nifEmpresa.isBlank() || idUser == null || idAnuncio == null || idAnuncio.isBlank()) {
            return null;
        }
        return dao.findOne(nifEmpresa, idUser, idAnuncio);
    }
}

