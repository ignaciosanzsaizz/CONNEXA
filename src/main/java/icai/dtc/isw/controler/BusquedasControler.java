package icai.dtc.isw.controler;

import icai.dtc.isw.dao.AnuncioDAO;
import icai.dtc.isw.domain.Anuncio;
import icai.dtc.isw.ui.Geocoding;

import java.util.List;
import java.util.stream.Collectors;

public class BusquedasControler {
    private final AnuncioDAO anuncioDAO = new AnuncioDAO();

    public List<Anuncio> buscar(String categoria, String trabajo,
                                Integer calidadMin, String origen, Double radioKm) {

        List<Anuncio> base = anuncioDAO.search(
            categoria == null || categoria.isBlank() ? null : categoria,
            trabajo   == null || trabajo.isBlank()   ? null : trabajo);

        // TODO: si existe ratingsDAO, filtrar por calidadMin aquí

        if (origen != null && !origen.isBlank() && radioKm != null && radioKm > 0) {
            try {
                Geocoding.LatLon origenLatLon = Geocoding.geocodeCached(origen);
                if (origenLatLon != null) {
                    double radio = radioKm;
                    base = base.stream()
                            .filter(anuncio -> {
                                try {
                                    double distance = Geocoding.distanceKm(origenLatLon,
                                            Geocoding.geocodeCached(anuncio.getUbicacion()));
                                    return !Double.isNaN(distance) && distance <= radio;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return base;
    }
}
