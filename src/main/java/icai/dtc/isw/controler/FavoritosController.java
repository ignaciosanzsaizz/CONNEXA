package icai.dtc.isw.controler;

import icai.dtc.isw.dao.AnuncioDAO;
import icai.dtc.isw.dao.FavoritosDAO;
import icai.dtc.isw.domain.Anuncio;
import java.util.ArrayList;
import java.util.List;

public class FavoritosController {
    private final FavoritosDAO dao = new FavoritosDAO();
    private final AnuncioDAO anuncioDAO = new AnuncioDAO();

    /**
     * Alterna el estado de favorito de un anuncio.
     * @return true si la operación fue exitosa, false en caso contrario.
     */
    public boolean toggleFavorito(String idUsuario, String idAnuncio) {
        return dao.toggleFavorito(idUsuario, idAnuncio);
    }

    /**
     * Comprueba si un anuncio es favorito para un usuario.
     */
    public boolean isFavorito(String idUsuario, String idAnuncio) {
        return dao.isFavorito(idUsuario, idAnuncio);
    }

    /**
     * Obtiene todos los anuncios favoritos de un usuario.
     */
    public List<Anuncio> getFavoritos(String idUsuario) {
        List<String> anuncioIds = dao.getFavoritoIds(idUsuario);
        List<Anuncio> favoritos = new ArrayList<>();
        for (String id : anuncioIds) {
            // Se asume que AnuncioDAO tiene un método findById(String id) que funciona
            Anuncio a = anuncioDAO.findById(id);
            if (a != null) {
                favoritos.add(a);
            }
        }
        return favoritos;
    }
}