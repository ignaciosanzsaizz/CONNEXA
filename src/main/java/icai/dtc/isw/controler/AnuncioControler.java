package icai.dtc.isw.controler;

import icai.dtc.isw.dao.AnuncioDAO;
import icai.dtc.isw.domain.Anuncio;

import java.util.List;
import java.util.UUID;

public class AnuncioControler {
    private final AnuncioDAO dao = new AnuncioDAO();

    /**
     * Crea un nuevo anuncio con validaciones.
     * Genera automáticamente un UUID si no se proporciona.
     */
    public boolean createAnuncio(String descripcion, Double precio, String categoria,
                                  String especificacion, String ubicacion, String nifEmpresa) {
        // Validaciones
        if (descripcion == null || descripcion.isBlank()) return false;
        if (precio == null || precio <= 0) return false;
        if (categoria == null || categoria.isBlank()) return false;
        if (especificacion == null || especificacion.isBlank()) return false;
        if (ubicacion == null || ubicacion.isBlank()) return false;
        if (nifEmpresa == null || nifEmpresa.isBlank()) return false;

        // Generar UUID para el anuncio
        String id = UUID.randomUUID().toString();

        Anuncio anuncio = new Anuncio(id, descripcion, precio, categoria,
                                      especificacion, ubicacion, nifEmpresa);

        return dao.insert(anuncio);
    }

    /**
     * Obtiene un anuncio por su ID.
     */
    public Anuncio getAnuncio(String id) {
        return dao.findById(id);
    }

    /**
     * Obtiene todos los anuncios de una empresa.
     */
    public List<Anuncio> getAnunciosByEmpresa(String nifEmpresa) {
        return dao.findByNifEmpresa(nifEmpresa);
    }

    /**
     * Obtiene todos los anuncios.
     */
    public List<Anuncio> getAllAnuncios() {
        return dao.findAll();
    }
}

