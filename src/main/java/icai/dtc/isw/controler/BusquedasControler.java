package icai.dtc.isw.controler;

import icai.dtc.isw.dao.AnuncioDAO;
import icai.dtc.isw.domain.Anuncio;
import java.util.*;
import java.util.stream.Collectors;

public class BusquedasControler {
    private final AnuncioDAO anuncioDAO = new AnuncioDAO();

    public List<Anuncio> buscar(String categoria, String trabajo,
                                Integer calidadMin, String origen, Integer radioKm) {

        List<Anuncio> base = anuncioDAO.search(
            categoria == null || categoria.isBlank() ? null : categoria,
            trabajo   == null || trabajo.isBlank()   ? null : trabajo);

        // TODO: si existe ratingsDAO, filtrar por calidadMin aquí

        // TODO: si tenemos geocodificación → filtrar por radioKm respecto a 'origen'

        return base;
    }
}
