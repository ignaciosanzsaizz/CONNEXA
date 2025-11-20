package icai.dtc.isw.server;

/**
 * Servidor TCP basado en sockets que actúa como backend ligero:
 * recibe peticiones serializadas (Message), invoca los controladores
 * del dominio y responde con los datos para la UI.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import icai.dtc.isw.configuration.PropertiesISW;
import icai.dtc.isw.controler.BusquedasControler;
import icai.dtc.isw.controler.ChatControler;
import icai.dtc.isw.controler.FavoritosController;
import icai.dtc.isw.controler.UserControler;
import icai.dtc.isw.domain.User;
import icai.dtc.isw.message.Message;
import icai.dtc.isw.controler.PagosControler;
import icai.dtc.isw.domain.Pago;

public class SocketServer extends Thread {
    public static int port = Integer.parseInt(PropertiesISW.getInstance().getProperty("port"));

    protected Socket socket;

    private SocketServer(Socket socket) {
        this.socket = socket;
        System.out.println("New client connected from " + socket.getInetAddress().getHostAddress());
        start();
    }

    @Override
    public void run() {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();

            ObjectInputStream objectInputStream = new ObjectInputStream(in);
            Message mensajeIn = (Message) objectInputStream.readObject();

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            Message mensajeOut = new Message();

            HashMap<String, Object> session = mensajeIn.getSession();

            switch (mensajeIn.getContext()) {

                case "/loginUser": {
                    String username = (String) session.get("username");
                    String password = (String) session.get("password");

                    UserControler uc = new UserControler();
                    User u = uc.login(username, password);

                    mensajeOut.setContext("/loginResponse");
                    if (u != null) {
                        session.put("user", u);
                    } else {
                        session.put("error", "Usuario o contraseña incorrectos");
                    }
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    break;
                }

                case "/registerUser": {
                    String username = (String) session.get("username");
                    String password = (String) session.get("password");
                    String email    = (String) session.get("email");

                    icai.dtc.isw.controler.UserControler uc = new icai.dtc.isw.controler.UserControler();
                    mensajeOut.setContext("/registerResponse");
                    try {
                        User u = uc.register(username, password, email);
                        session.put("ok", true);
                        session.put("user", u);
                    } catch (IllegalArgumentException ex) {
                        session.put("ok", false);
                        session.put("error", ex.getMessage());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        session.put("ok", false);
                        session.put("error", "REGISTER_FAILED");
                    }
                    objectOutputStream.flush();
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    break;
                }

                // ===== EMPRESA =====

                // Consulta por MAIL (para el perfil del usuario logueado)
                case "/empresa/get": {
                    String mail = (String) session.get("mail");
                    icai.dtc.isw.controler.EmpresaControler ec = new icai.dtc.isw.controler.EmpresaControler();
                    icai.dtc.isw.domain.Empresa emp = ec.getEmpresa(mail);

                    mensajeOut.setContext("/empresaGetResponse");
                    session.put("empresa", emp);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                // (Opcional) Consulta directa por NIF
                case "/empresa/getByNif": {
                    String nif = (String) session.get("nif");
                    icai.dtc.isw.controler.EmpresaControler ec = new icai.dtc.isw.controler.EmpresaControler();
                    icai.dtc.isw.domain.Empresa emp = ec.getEmpresaByNif(nif);

                    mensajeOut.setContext("/empresaGetByNifResponse");
                    session.put("empresa", emp);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                // Guardar/actualizar por NIF (PK)
                case "/empresa/save": {
                    String mail       = (String) session.get("mail");
                    String empresa    = (String) session.get("empresa");
                    String nif        = (String) session.get("nif");       // <-- clave primaria
                    String sector     = (String) session.get("sector");
                    String ubicacion  = (String) session.get("ubicacion");
                    String fotoPerfil = (String) session.get("fotoPerfil");

                    icai.dtc.isw.controler.EmpresaControler ec = new icai.dtc.isw.controler.EmpresaControler();
                    boolean ok = ec.save(mail, empresa, nif, sector, ubicacion, fotoPerfil);

                    mensajeOut.setContext("/empresaSaveResponse");
                    session.put("ok", ok);
                    if (!ok) session.put("error", "EMPRESA_SAVE_FAILED");
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                // ===== ANUNCIO =====

                // Crear anuncio
                case "/anuncio/create": {
                    String descripcion    = (String) session.get("descripcion");
                    Double precio         = (Double) session.get("precio");
                    String categoria      = (String) session.get("categoria");
                    String especificacion = (String) session.get("especificacion");
                    String ubicacion      = (String) session.get("ubicacion");
                    String nifEmpresa     = (String) session.get("nif_empresa");

                    icai.dtc.isw.controler.AnuncioControler ac = new icai.dtc.isw.controler.AnuncioControler();
                    boolean ok = ac.createAnuncio(descripcion, precio, categoria,
                                                   especificacion, ubicacion, nifEmpresa);

                    mensajeOut.setContext("/anuncioCreateResponse");
                    session.put("ok", ok);
                    if (!ok) session.put("error", "ANUNCIO_CREATE_FAILED");
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                // Obtener un anuncio por ID
                case "/anuncio/get": {
                    String id = (String) session.get("id");
                    icai.dtc.isw.controler.AnuncioControler ac = new icai.dtc.isw.controler.AnuncioControler();
                    icai.dtc.isw.domain.Anuncio anuncio = ac.getAnuncio(id);

                    mensajeOut.setContext("/anuncioGetResponse");
                    session.put("anuncio", anuncio);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                // Listar anuncios por empresa
                case "/anuncio/list": {
                    String nifEmpresa = (String) session.get("nif_empresa");
                    icai.dtc.isw.controler.AnuncioControler ac = new icai.dtc.isw.controler.AnuncioControler();
                    java.util.List<icai.dtc.isw.domain.Anuncio> anuncios = ac.getAnunciosByEmpresa(nifEmpresa);

                    mensajeOut.setContext("/anuncioListResponse");
                    session.put("anuncios", anuncios);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                // Actualizar anuncio
                case "/anuncio/update": {
                    String id             = (String) session.get("id");
                    String descripcion    = (String) session.get("descripcion");
                    Double precio         = (Double) session.get("precio");
                    String categoria      = (String) session.get("categoria");
                    String especificacion = (String) session.get("especificacion");
                    String ubicacion      = (String) session.get("ubicacion");

                    icai.dtc.isw.controler.AnuncioControler ac = new icai.dtc.isw.controler.AnuncioControler();
                    boolean ok = ac.updateAnuncio(id, descripcion, precio, categoria, especificacion, ubicacion);

                    mensajeOut.setContext("/anuncioUpdateResponse");
                    session.put("ok", ok);
                    if (!ok) session.put("error", "ANUNCIO_UPDATE_FAILED");
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                // Eliminar anuncio por ID
                case "/anuncio/delete": {
                    String id = (String) session.get("id");
                    icai.dtc.isw.controler.AnuncioControler ac = new icai.dtc.isw.controler.AnuncioControler();
                    boolean ok = ac.deleteAnuncio(id);

                    mensajeOut.setContext("/anuncioDeleteResponse");
                    session.put("ok", ok);
                    if (!ok) session.put("error", "ANUNCIO_DELETE_FAILED");
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/anuncio/search": {
                    String categoria = (String) session.get("categoria");
                    String trabajo = (String) session.get("trabajo");
                    Integer calidadMin = (Integer) session.get("calidadMin");
                    String origen = (String) session.get("origen");
                    Number radioNumber = (Number) session.get("radioKm");
                    Double radioKm = radioNumber != null ? radioNumber.doubleValue() : null;

                    BusquedasControler bc = new BusquedasControler();
                    java.util.List<icai.dtc.isw.domain.Anuncio> anuncios =
                            bc.buscar(categoria, trabajo, calidadMin, origen, radioKm);

                    mensajeOut.setContext("/anuncioSearchResponse");
                    session.put("anuncios", anuncios);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/favoritos/toggle": {
                    String idUsuario = (String) session.get("idUsuario");
                    String idAnuncio = (String) session.get("idAnuncio");
                    FavoritosController fc = new FavoritosController();
                    boolean ok = fc.toggleFavorito(idUsuario, idAnuncio);
                    boolean isFavorito = fc.isFavorito(idUsuario, idAnuncio);

                    mensajeOut.setContext("/favoritosToggleResponse");
                    session.put("ok", ok);
                    session.put("isFavorito", isFavorito);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/favoritos/is": {
                    String idUsuario = (String) session.get("idUsuario");
                    String idAnuncio = (String) session.get("idAnuncio");
                    FavoritosController fc = new FavoritosController();
                    boolean isFavorito = fc.isFavorito(idUsuario, idAnuncio);

                    mensajeOut.setContext("/favoritosIsResponse");
                    session.put("isFavorito", isFavorito);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/favoritos/list": {
                    String idUsuario = (String) session.get("idUsuario");
                    FavoritosController fc = new FavoritosController();
                    java.util.List<icai.dtc.isw.domain.Anuncio> favoritos = fc.getFavoritos(idUsuario);

                    mensajeOut.setContext("/favoritosListResponse");
                    session.put("anuncios", favoritos);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/chat/list": {
                    String email = (String) session.get("email");
                    ChatControler chatControler = new ChatControler();
                    java.util.List<icai.dtc.isw.domain.Chat> chats = chatControler.getChatsByUser(email);

                    mensajeOut.setContext("/chatListResponse");
                    session.put("chats", chats);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/chat/getOrCreate": {
                    String clienteEmail = (String) session.get("clienteEmail");
                    String empresaEmail = (String) session.get("empresaEmail");
                    String anuncioId = (String) session.get("anuncioId");
                    ChatControler chatControler = new ChatControler();
                    icai.dtc.isw.domain.Chat chat = chatControler.getOrCreateChat(clienteEmail, empresaEmail, anuncioId);

                    mensajeOut.setContext("/chatGetOrCreateResponse");
                    session.put("chat", chat);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/chat/messages": {
                    Number chatIdNumber = (Number) session.get("chatId");
                    int chatId = chatIdNumber != null ? chatIdNumber.intValue() : 0;
                    ChatControler chatControler = new ChatControler();
                    java.util.List<icai.dtc.isw.domain.MensajeChat> mensajes = chatControler.getMensajesByChat(chatId);

                    mensajeOut.setContext("/chatMessagesResponse");
                    session.put("mensajes", mensajes);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/chat/send": {
                    Number chatIdNumber = (Number) session.get("chatId");
                    int chatId = chatIdNumber != null ? chatIdNumber.intValue() : 0;
                    String remitenteEmail = (String) session.get("remitenteEmail");
                    String contenido = (String) session.get("contenido");
                    ChatControler chatControler = new ChatControler();
                    boolean ok = chatControler.enviarMensaje(chatId, remitenteEmail, contenido);

                    mensajeOut.setContext("/chatSendResponse");
                    session.put("ok", ok);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/chat/read": {
                    Number chatIdNumber = (Number) session.get("chatId");
                    int chatId = chatIdNumber != null ? chatIdNumber.intValue() : 0;
                    String userEmail = (String) session.get("userEmail");
                    ChatControler chatControler = new ChatControler();
                    chatControler.marcarMensajesComoLeidos(chatId, userEmail);

                    mensajeOut.setContext("/chatReadResponse");
                    session.put("ok", true);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                // ===== CONTRATACIONES =====

                case "/contratacion/crear": {
                    String nifEmpresa = (String) session.get("nifEmpresa");
                    Number idUserNumber = (Number) session.get("idUser");
                    Integer idUser = idUserNumber != null ? idUserNumber.intValue() : null;
                    String idAnuncio = (String) session.get("idAnuncio");

                    icai.dtc.isw.controler.ContratacionControler cc = new icai.dtc.isw.controler.ContratacionControler();
                    boolean ok = cc.crearContratacion(nifEmpresa, idUser, idAnuncio);

                    mensajeOut.setContext("/contratacionCrearResponse");
                    session.put("ok", ok);
                    if (!ok) session.put("error", "CONTRATACION_CREATE_FAILED");
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/contratacion/existe": {
                    String nifEmpresa = (String) session.get("nifEmpresa");
                    Number idUserNumber = (Number) session.get("idUser");
                    Integer idUser = idUserNumber != null ? idUserNumber.intValue() : null;
                    String idAnuncio = (String) session.get("idAnuncio");

                    icai.dtc.isw.controler.ContratacionControler cc = new icai.dtc.isw.controler.ContratacionControler();
                    boolean existe = cc.existeContratacion(nifEmpresa, idUser, idAnuncio);

                    mensajeOut.setContext("/contratacionExisteResponse");
                    session.put("existe", existe);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/contratacion/list": {
                    Number idUserNumber = (Number) session.get("idUser");
                    Integer idUser = idUserNumber != null ? idUserNumber.intValue() : null;

                    icai.dtc.isw.controler.ContratacionControler cc = new icai.dtc.isw.controler.ContratacionControler();
                    java.util.List<icai.dtc.isw.domain.Contratacion> contrataciones = cc.getContratacionesByUser(idUser);


                    mensajeOut.setContext("/contratacionListResponse");
                    session.put("contrataciones", contrataciones);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/contratacion/terminar": {
                    String nifEmpresa = (String) session.get("nifEmpresa");
                    Number idUserNumber = (Number) session.get("idUser");
                    Integer idUser = idUserNumber != null ? idUserNumber.intValue() : null;
                    String idAnuncio = (String) session.get("idAnuncio");

                    icai.dtc.isw.controler.ContratacionControler cc = new icai.dtc.isw.controler.ContratacionControler();
                    boolean ok = cc.terminarContrato(nifEmpresa, idUser, idAnuncio);

                    mensajeOut.setContext("/contratacionTerminarResponse");
                    session.put("ok", ok);
                    if (!ok) session.put("error", "CONTRATACION_TERMINAR_FAILED");
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/contratacion/valorar": {
                    String nifEmpresa = (String) session.get("nifEmpresa");
                    Number idUserNumber = (Number) session.get("idUser");
                    Integer idUser = idUserNumber != null ? idUserNumber.intValue() : null;
                    String idAnuncio = (String) session.get("idAnuncio");
                    Number calidadNumber = (Number) session.get("calidad");
                    Float calidad = calidadNumber != null ? calidadNumber.floatValue() : null;
                    String comentarios = (String) session.get("comentarios");

                    icai.dtc.isw.controler.ContratacionControler cc = new icai.dtc.isw.controler.ContratacionControler();
                    boolean ok = cc.valorarContratacion(nifEmpresa, idUser, idAnuncio, calidad, comentarios);

                    mensajeOut.setContext("/contratacionValorarResponse");
                    session.put("ok", ok);
                    if (!ok) session.put("error", "CONTRATACION_VALORAR_FAILED");
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/contratacion/estado": {
                    String nifEmpresa = (String) session.get("nifEmpresa");
                    Number idUserNumber = (Number) session.get("idUser");
                    Integer idUser = idUserNumber != null ? idUserNumber.intValue() : null;
                    String idAnuncio = (String) session.get("idAnuncio");

                    icai.dtc.isw.controler.ContratacionControler cc = new icai.dtc.isw.controler.ContratacionControler();
                    String estado = cc.getEstado(nifEmpresa, idUser, idAnuncio);

                    mensajeOut.setContext("/contratacionEstadoResponse");
                    session.put("estado", estado);
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }
                case "/pago/save": {
                    // El cliente manda un objeto Pago ya relleno en session["pago"]
                    Pago pago = (Pago) session.get("pago");

                    PagosControler pc = new PagosControler();
                    boolean ok = pc.save(pago);

                    mensajeOut.setContext("/pagoSaveResponse");
                    session.put("ok", ok);
                    if (!ok) {
                        session.put("error", "PAGO_SAVE_FAILED");
                    }
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/pago/get": {
                    // El cliente manda el id del usuario en session["userId"] como String
                    String userIdStr = (String) session.get("userId");
                    Integer userId = null;
                    try {
                        userId = Integer.parseInt(userIdStr);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    PagosControler pc = new PagosControler();
                    Pago pago = (userId != null) ? pc.getPagoByUserId(userId) : null;

                    mensajeOut.setContext("/pagoGetResponse");
                    session.put("pago", pago);       // puede ser null si no hay
                    session.put("ok", pago != null); // true si existe, false si no
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }

                case "/pago/delete": {
                    // El cliente manda el id del usuario en session["userId"] como String
                    String userIdStr = (String) session.get("userId");
                    Integer userId = null;
                    try {
                        userId = Integer.parseInt(userIdStr);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    PagosControler pc = new PagosControler();
                    boolean ok = (userId != null) && pc.deleteByUserId(userId);

                    mensajeOut.setContext("/pagoDeleteResponse");
                    session.put("ok", ok);
                    if (!ok) {
                        session.put("error", "PAGO_DELETE_FAILED");
                    }
                    mensajeOut.setSession(session);
                    objectOutputStream.writeObject(mensajeOut);
                    objectOutputStream.flush();
                    break;
                }
                default:
                    System.out.println("\nParámetro no encontrado: " + mensajeIn.getContext());
                    break;
            }

        } catch (IOException ex) {
            System.out.println("Unable to get streams from client");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try { if (in != null) in.close(); } catch (IOException ignored) {}
            try { if (out != null) out.close(); } catch (IOException ignored) {}
            try { if (socket != null) socket.close(); } catch (IOException ex) { ex.printStackTrace(); }
        }
    }

    public static void main(String[] args) {
        System.out.println("SocketServer Example - Listening port " + port);
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            while (true) {
                new SocketServer(server.accept());
            }
        } catch (IOException ex) {
            System.out.println("Unable to start server.");
        } finally {
            try { if (server != null) server.close(); } catch (IOException ex) { ex.printStackTrace(); }
        }
    }
}
