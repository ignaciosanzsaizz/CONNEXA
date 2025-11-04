package icai.dtc.isw.ui;

import icai.dtc.isw.client.Client;
import icai.dtc.isw.domain.Anuncio;

import java.util.HashMap;
import java.util.List;

public class AnuncioApi {

    /**
     * Crea un nuevo anuncio en el servidor.
     */
    public boolean createAnuncio(String descripcion, Double precio, String categoria,
                                  String especificacion, String ubicacion, String nifEmpresa) {
        Client c = new Client();
        HashMap<String, Object> session = new HashMap<>();
        session.put("descripcion", descripcion);
        session.put("precio", precio);
        session.put("categoria", categoria);
        session.put("especificacion", especificacion);
        session.put("ubicacion", ubicacion);
        session.put("nif_empresa", nifEmpresa);

        session = c.sentMessage("/anuncio/create", session);
        return Boolean.TRUE.equals(session.get("ok"));
    }

    /**
     * Obtiene un anuncio por su ID.
     */
    public Anuncio getAnuncio(String id) {
        Client c = new Client();
        HashMap<String, Object> session = new HashMap<>();
        session.put("id", id);

        session = c.sentMessage("/anuncio/get", session);
        Object o = session.get("anuncio");
        return (o instanceof Anuncio) ? (Anuncio) o : null;
    }

    /**
     * Obtiene todos los anuncios de una empresa.
     */
    @SuppressWarnings("unchecked")
    public List<Anuncio> getAnunciosByEmpresa(String nifEmpresa) {
        Client c = new Client();
        HashMap<String, Object> session = new HashMap<>();
        session.put("nif_empresa", nifEmpresa);

        session = c.sentMessage("/anuncio/list", session);
        Object o = session.get("anuncios");
        return (o instanceof List) ? (List<Anuncio>) o : null;
    }

    /**
     * Actualiza un anuncio existente.
     */
    public boolean updateAnuncio(String id, String descripcion, Double precio, String categoria,
                                  String especificacion, String ubicacion) {
        Client c = new Client();
        HashMap<String, Object> session = new HashMap<>();
        session.put("id", id);
        session.put("descripcion", descripcion);
        session.put("precio", precio);
        session.put("categoria", categoria);
        session.put("especificacion", especificacion);
        session.put("ubicacion", ubicacion);

        session = c.sentMessage("/anuncio/update", session);
        return Boolean.TRUE.equals(session.get("ok"));
    }

    /**
     * Elimina un anuncio por su ID.
     */
    public boolean deleteAnuncio(String id) {
        Client c = new Client();
        HashMap<String, Object> session = new HashMap<>();
        session.put("id", id);

        session = c.sentMessage("/anuncio/delete", session);
        return Boolean.TRUE.equals(session.get("ok"));
    }
}

