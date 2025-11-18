package icai.dtc.isw.ui;

import icai.dtc.isw.client.Client;
import icai.dtc.isw.domain.Anuncio;

import java.util.HashMap;
import java.util.List;

/**
 * API de favoritos que encapsula el acceso al servidor vía sockets.
 */
public class FavoritosApi {

    /**
     * Alterna el estado de un anuncio en favoritos.
     * @return true si tras la operación queda como favorito, false si queda desmarcado y null si falló.
     */
    public Boolean toggleFavorito(String idUsuario, String idAnuncio) {
        Client c = new Client();
        HashMap<String, Object> session = new HashMap<>();
        session.put("idUsuario", idUsuario);
        session.put("idAnuncio", idAnuncio);
        session = c.sentMessage("/favoritos/toggle", session);
        Object ok = session.get("ok");
        if (!Boolean.TRUE.equals(ok)) return null;
        Object flag = session.get("isFavorito");
        return Boolean.TRUE.equals(flag);
    }

    /**
     * Consulta si un anuncio está marcado como favorito para un usuario.
     */
    public boolean isFavorito(String idUsuario, String idAnuncio) {
        Client c = new Client();
        HashMap<String, Object> session = new HashMap<>();
        session.put("idUsuario", idUsuario);
        session.put("idAnuncio", idAnuncio);
        session = c.sentMessage("/favoritos/is", session);
        Object flag = session.get("isFavorito");
        return Boolean.TRUE.equals(flag);
    }

    /**
     * Devuelve los anuncios favoritos del usuario.
     */
    @SuppressWarnings("unchecked")
    public List<Anuncio> getFavoritos(String idUsuario) {
        Client c = new Client();
        HashMap<String, Object> session = new HashMap<>();
        session.put("idUsuario", idUsuario);
        session = c.sentMessage("/favoritos/list", session);
        Object value = session.get("anuncios");
        return (value instanceof List) ? (List<Anuncio>) value : null;
    }
}
