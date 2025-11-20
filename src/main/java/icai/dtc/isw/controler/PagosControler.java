package icai.dtc.isw.controler;

import icai.dtc.isw.dao.PagosDAO;
import icai.dtc.isw.domain.Pago;

/**
 * Controlador de dominio para pagos en el servidor.
 */
public class PagosControler {

    private final PagosDAO dao = new PagosDAO();

    /** Guardar o actualizar método de pago */
    public boolean save(Pago pago) {
        if (pago == null) return false;
        if (pago.getId() == null) return false;
        if (pago.getNombre() == null || pago.getNombre().isBlank()) return false;
        if (pago.getNumeroTarjeta() == null || pago.getNumeroTarjeta().isBlank()) return false;
        if (pago.getFechaCaducidad() == null || pago.getFechaCaducidad().isBlank()) return false;
        if (pago.getCvv() == null || pago.getCvv().isBlank()) return false;

        return dao.upsert(pago);
    }

    /** Recuperar método de pago por id de usuario (users.id) */
    public Pago getPagoByUserId(Integer userId) {
        if (userId == null) return null;
        return dao.findByUserId(userId);
    }

    /** Borrar método de pago por id de usuario */
    public boolean deleteByUserId(Integer userId) {
        if (userId == null) return false;
        return dao.deleteByUserId(userId);
    }
}
