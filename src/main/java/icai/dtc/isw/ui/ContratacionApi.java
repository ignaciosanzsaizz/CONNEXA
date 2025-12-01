package icai.dtc.isw.ui;

import icai.dtc.isw.client.Client;
import icai.dtc.isw.domain.Contratacion;

import java.util.HashMap;
import java.util.List;

public class ContratacionApi {

    /**
     * Crea una nueva contratación
     */
    public boolean crearContratacion(String nifEmpresa, Integer idUser, String idAnuncio) {
        Client c = new Client();
        HashMap<String, Object> s = new HashMap<>();
        s.put("nifEmpresa", nifEmpresa);
        s.put("idUser", idUser);
        s.put("idAnuncio", idAnuncio);

        s = c.sentMessage("/contratacion/crear", s);

        return s != null && Boolean.TRUE.equals(s.get("ok"));
    }

    /**
     * Verifica si existe una contratación
     */
    public boolean existeContratacion(String nifEmpresa, Integer idUser, String idAnuncio) {
        Client c = new Client();
        HashMap<String, Object> s = new HashMap<>();
        s.put("nifEmpresa", nifEmpresa);
        s.put("idUser", idUser);
        s.put("idAnuncio", idAnuncio);
        s = c.sentMessage("/contratacion/existe", s);
        return s != null && Boolean.TRUE.equals(s.get("existe"));
    }

    /**
     * Obtiene todas las contrataciones de un usuario
     */
    @SuppressWarnings("unchecked")
    public List<Contratacion> getContrataciones(Integer idUser) {
        Client c = new Client();
        HashMap<String, Object> s = new HashMap<>();
        s.put("idUser", idUser);

        s = c.sentMessage("/contratacion/list", s);

        if (s == null) {
            return List.of();
        }

        Object o = s.get("contrataciones");
        if (o instanceof List<?>) {
            return (List<Contratacion>) o;
        }

        return List.of();
    }

    /**
     * Termina un contrato
     */
    public boolean terminarContrato(String nifEmpresa, Integer idUser, String idAnuncio) {
        Client c = new Client();
        HashMap<String, Object> s = new HashMap<>();
        s.put("nifEmpresa", nifEmpresa);
        s.put("idUser", idUser);
        s.put("idAnuncio", idAnuncio);
        s = c.sentMessage("/contratacion/terminar", s);
        return s != null && Boolean.TRUE.equals(s.get("ok"));
    }

    /**
     * Valora una contratación
     */
    public boolean valorarContratacion(String nifEmpresa, Integer idUser, String idAnuncio, Float calidad, String comentarios) {
        Client c = new Client();
        HashMap<String, Object> s = new HashMap<>();
        s.put("nifEmpresa", nifEmpresa);
        s.put("idUser", idUser);
        s.put("idAnuncio", idAnuncio);
        s.put("calidad", calidad);
        s.put("comentarios", comentarios);
        s = c.sentMessage("/contratacion/valorar", s);
        return s != null && Boolean.TRUE.equals(s.get("ok"));
    }

    /**
     * Obtiene las valoraciones de una empresa
     */
    @SuppressWarnings("unchecked")
    public List<Contratacion> getValoraciones(String nifEmpresa) {
        Client c = new Client();
        HashMap<String, Object> s = new HashMap<>();
        s.put("nifEmpresa", nifEmpresa);
        s = c.sentMessage("/contratacion/valoraciones", s);

        if (s == null) {
            return List.of();
        }

        Object o = s.get("valoraciones");
        if (o instanceof List<?>) {
            return (List<Contratacion>) o;
        }
        return List.of();
    }

    /**
     * Obtiene el estado de una contratación
     */
    public String getEstado(String nifEmpresa, Integer idUser, String idAnuncio) {
        Client c = new Client();
        HashMap<String, Object> s = new HashMap<>();
        s.put("nifEmpresa", nifEmpresa);
        s.put("idUser", idUser);
        s.put("idAnuncio", idAnuncio);
        s = c.sentMessage("/contratacion/estado", s);
        return (String) s.get("estado");
    }
}

