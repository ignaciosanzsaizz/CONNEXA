package icai.dtc.isw.ui;

import icai.dtc.isw.client.Client;
import icai.dtc.isw.domain.Pago;
import icai.dtc.isw.domain.User;

import java.util.HashMap;

public class PagoApi {

    /** Guardar o actualizar método de pago */
    public boolean guardarMetodoPago(User user, Pago pago) {
        Client client = new Client();

        HashMap<String, Object> session = new HashMap<>();
        session.put("user", user);
        session.put("pago", pago);

        session = client.sentMessage("/pago/save", session);

        Object okObj = session.get("ok");
        return (okObj instanceof Boolean) && (Boolean) okObj;
    }

    /** Obtener método de pago del usuario (puede devolver null si no hay) */
    public Pago getMetodoPago(User user) {
        Client client = new Client();

        HashMap<String, Object> session = new HashMap<>();
        session.put("userId", user.getId()); // String

        session = client.sentMessage("/pago/get", session);

        Object pagoObj = session.get("pago");
        if (pagoObj instanceof Pago) {
            return (Pago) pagoObj;
        }
        return null;
    }

    /** Borrar método de pago del usuario */
    public boolean borrarMetodoPago(User user) {
        Client client = new Client();

        HashMap<String, Object> session = new HashMap<>();
        session.put("userId", user.getId());

        session = client.sentMessage("/pago/delete", session);

        Object okObj = session.get("ok");
        return (okObj instanceof Boolean) && (Boolean) okObj;
    }
}
