package icai.dtc.isw.ui;

/**
 * Emula la aplicación móvil de CONNEXA en forma de ventana Swing,
 * mostrando las pestañas de perfil, búsquedas, favoritos, chats y
 * empresa, y orquestando todas las llamadas al servidor.
 */

import icai.dtc.isw.domain.Empresa;
import icai.dtc.isw.domain.User;
import icai.dtc.isw.domain.Anuncio;
import icai.dtc.isw.domain.Chat;
import icai.dtc.isw.domain.Contratacion;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

/**
 * AppMovilMock
 * - Pestaña "Búsquedas" migrada a tarjetas estilo "Mi Empresa"
 * - Filtros fijos arriba (sin scroll) y scroll SOLO en resultados
 * - Sin óvalos en categoría/especificación (solo texto coloreado)
 */
public class AppMovilMock extends JFrame {

    // === Datos / constantes existentes ===
    public static final String[] CATEGORIAS_GENERALES = new String[] {
            "Hogar y reparaciones",
            "Salud, belleza y cuidados",
            "Educación y cultura",
            "Eventos y ocio",
            "Negocio y administración",
            "Logística y movilidad",
            "Tecnología y digital"
    };
    private static final Map<String, String[]> ESPECIFICAS = new LinkedHashMap<>();
    static {
        ESPECIFICAS.put("Hogar y reparaciones", new String[] {
                "Electricidad", "Fontanería", "Cerrajería", "Pintura",
                "Carpintería", "Albañilería", "Climatización", "Limpieza", "Jardinería"
        });
        ESPECIFICAS.put("Salud, belleza y cuidados", new String[] {
                "Peluquería", "Estética", "Masajistas", "Fisioterapeutas", "Cuidadores", "Entrenamiento personal"
        });
        ESPECIFICAS.put("Educación y cultura", new String[] {
                "Enseñanza particular", "Academias", "Música", "Traductores e intérpretes"
        });
        ESPECIFICAS.put("Eventos y ocio", new String[] {
                "Organización de eventos", "Catering y repostería", "Fotógrafos y vídeo", "Animación y sonido"
        });
        ESPECIFICAS.put("Negocio y administración", new String[] {
                "Marketing local", "Recursos humanos", "Asesoría y gestoría", "Legal básica", "Formación para comercios"
        });
        ESPECIFICAS.put("Logística y movilidad", new String[] {
                "Repartidores", "Mudanzas y portes", "Mensajería urgente"
        });
        ESPECIFICAS.put("Tecnología y digital", new String[] {
                "Informáticos", "Soporte a comercios", "Diseño web y e-commerce"
        });
    }

    // === Estado de UI ===
    private final JLabel tituloLabel;
    private final JLabel subLabel;
    private final CardLayout cardLayout;
    private final JPanel panelContenido;
    private final User currentUser;
    // Guardamos referencia a la tarjeta de PERFIL para poder reconstruirla
    private JPanel perfilPanel;
    // Botones de la tab bar para marcar seleccionado
    private JButton btnPerfil, btnBusquedas, btnFavoritos, btnChats, btnEmpresa, btnContrataciones;
    private JComboBox<String> cboGeneral;
    private JComboBox<String> cboEspecifico;
    private JComboBox<String> cboUbicacion;
    private JComboBox<String> cboCalidad;
    private JLabel lblUbicacionActual;

    // Almacenamos el panel de favoritos para una recarga más sencilla.
    private JPanel favoritosWrapper;
    private JPanel contratacionesWrapper;

    private String userLocation;
    private boolean locationPrompted;

    // --- APIs remotas ---
    private final AnuncioApi anuncioApi = new AnuncioApi();
    private final FavoritosApi favoritosApi = new FavoritosApi();
    private final ChatApi chatApi = new ChatApi();
    private final ContratacionApi contratacionApi = new ContratacionApi();

    private JPanel contenedorLista; // contenedor vertical con tarjetas

    public AppMovilMock(User user) {
        super("CONNEXA APP");
        Image icon = new ImageIcon(getClass().getResource("/icons/connexa_mini.png")).getImage();
        setIconImage(icon);

        this.currentUser = user;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(360, 640);
        setMinimumSize(new Dimension(320, 560));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        // ======== Barra superior (gradiente + título + subtítulo) ========
        JPanel barraSuperior = new UIUtils.GradientBar(new Color(10, 23, 42), new Color(20, 40, 80));
        barraSuperior.setLayout(new BorderLayout());
        barraSuperior.setBorder(new EmptyBorder(12, 12, 8, 12));

        tituloLabel = new JLabel("CONNEXA", SwingConstants.LEFT);
        tituloLabel.setForeground(Color.WHITE);
        tituloLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        subLabel = new JLabel("🧑‍💼 Perfil", SwingConstants.LEFT);
        subLabel.setForeground(new Color(220, 230, 255));
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLabel.setBorder(new EmptyBorder(2, 0, 4, 0));
        JPanel titWrap = new JPanel();
        titWrap.setOpaque(false);
        titWrap.setLayout(new BoxLayout(titWrap, BoxLayout.Y_AXIS));
        titWrap.add(tituloLabel);
        titWrap.add(subLabel);

        barraSuperior.add(titWrap, BorderLayout.WEST);
        add(barraSuperior, BorderLayout.NORTH);
        // ======== Contenido con CardLayout ========
        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setBackground(new Color(245, 247, 250));

        // Construimos y guardamos PERFIL (scrolleable)
        perfilPanel = crearPantallaPerfil();
        panelContenido.add(perfilPanel, "PERFIL");
        panelContenido.add(crearPantallaBusquedas(), "BUSQUEDAS");
        // MODIFICADO: Llamar a la nueva pantalla de favoritos y guardar referencia
        favoritosWrapper = crearPantallaFavoritos();
        panelContenido.add(favoritosWrapper, "FAVORITOS");
        contratacionesWrapper = new ContratacionesPanel(currentUser);
        panelContenido.add(contratacionesWrapper, "CONTRATACIONES");
        panelContenido.add(new ChatsPanel(currentUser), "CHATS");
        // Pasamos this al EmpresaPanel para refrescar perfil tras guardar empresa
        panelContenido.add(new EmpresaPanel(currentUser, CATEGORIAS_GENERALES, this), "MI_EMPRESA");
        add(panelContenido, BorderLayout.CENTER);

        // ======== Tab bar inferior con emojis y estado seleccionado ========
        JPanel barraInferior = new JPanel(new GridLayout(1, 6));
        barraInferior.setBorder(new EmptyBorder(8, 8, 8, 8));
        barraInferior.setBackground(Color.WHITE);

        btnPerfil         = navEmojiButton("🧑‍💼", "Perfil");
        btnBusquedas      = navEmojiButton("🔎",  "Búsquedas");
        btnFavoritos      = navEmojiButton("⭐",  "Favoritos");
        btnContrataciones = navEmojiButton("📋",  "Contrataciones");
        btnChats          = navEmojiButton("💬",  "Chats");
        btnEmpresa        = navEmojiButton("🏢",  "Mi Empresa");

        btnPerfil.addActionListener(e -> {
            setSelectedTab(btnPerfil);
            subLabel.setText("🧑‍💼 Perfil");
            showPerfil();
        });
        btnBusquedas.addActionListener(e -> {
            setSelectedTab(btnBusquedas);
            subLabel.setText("🔎 Búsquedas");
            solicitarUbicacionInicial();
            cardLayout.show(panelContenido, "BUSQUEDAS");
            recargarResultadosConValoresActuales();
        });

        // CORRECCIÓN FINAL: Listener para recargar favoritos al hacer clic en la pestaña
        btnFavoritos.addActionListener(e -> {
            setSelectedTab(btnFavoritos);
            subLabel.setText("⭐ Favoritos");
            cardLayout.show(panelContenido, "FAVORITOS");
            JPanel favsContainer = getFavoritosContainer();
            if (favsContainer != null) {
                recargarFavoritos(favsContainer);
            }
        });

        btnContrataciones.addActionListener(e -> {
            setSelectedTab(btnContrataciones);
            subLabel.setText("📋 Contrataciones");
            cardLayout.show(panelContenido, "CONTRATACIONES");
            if (contratacionesWrapper instanceof ContratacionesPanel) {
                ((ContratacionesPanel) contratacionesWrapper).cargarContrataciones();
            }
        });

        btnChats.addActionListener(e -> {
            setSelectedTab(btnChats);
            subLabel.setText("💬 Chats");
            cardLayout.show(panelContenido, "CHATS");
        });
        btnEmpresa.addActionListener(e -> {
            setSelectedTab(btnEmpresa);
            subLabel.setText("🏢 Mi Empresa");
            cardLayout.show(panelContenido, "MI_EMPRESA");
        });
        barraInferior.add(btnPerfil);
        barraInferior.add(btnBusquedas);
        barraInferior.add(btnFavoritos);
        barraInferior.add(btnContrataciones);
        barraInferior.add(btnChats);
        barraInferior.add(btnEmpresa);

        add(barraInferior, BorderLayout.SOUTH);

        // Arrancamos con Perfil seleccionado
        setSelectedTab(btnPerfil);
    }

    /* ========= API pública para refrescar, navegar y AÑADIR ANUNCIO ========= */

    /** Reconstruye la tarjeta PERFIL con los datos actuales (empresa incluida) */
    public void refreshPerfil() {
        panelContenido.remove(perfilPanel);
        perfilPanel = crearPantallaPerfil();
        panelContenido.add(perfilPanel, "PERFIL");
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    /** Muestra la pestaña PERFIL y ajusta el título/subtítulo */
    public void showPerfil() {
        cardLayout.show(panelContenido, "PERFIL");
    }

    /** Abre la pantalla de "Nuevo anuncio" ocupando TODO el área central (pantalla completa de la app).
     */
    public void showNuevoAnuncio() {
        JPanel nuevo = new NuevoAnuncioPanel();
        panelContenido.add(nuevo, "NUEVO_ANUNCIO");
        cardLayout.show(panelContenido, "NUEVO_ANUNCIO");
        subLabel.setText("➕ Nuevo anuncio");
        setSelectedTab(null); // ninguna pestaña de la barra inferior marcada
    }

    /* ---------------- Pantallas ---------------- */

    private JPanel crearPantalla(String textoCentro) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(245, 247, 250));

        JPanel card = createCardPanel();
        JLabel lbl = new JLabel(textoCentro, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbl.setForeground(new Color(30, 33, 40));

        card.add(lbl);
        wrapper.add(card);
        return wrapper;
    }

    // --- NUEVO: Pantalla y lógica de Favoritos ---

    // Método auxiliar para obtener el contenedor interno de la lista de favoritos
    private JPanel getFavoritosContainer() {
        if (favoritosWrapper == null || favoritosWrapper.getComponentCount() == 0) return null;

        Component first = favoritosWrapper.getComponent(0);
        if (first instanceof JScrollPane) {
            return (JPanel) ((JScrollPane) first).getViewport().getView();
        }
        return null;
    }


    private JPanel crearPantallaFavoritos() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(245, 247, 250));
        wrapper.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Contenedor dinámico de la lista
        JPanel contenedorFavs = new JPanel();
        contenedorFavs.setLayout(new BoxLayout(contenedorFavs, BoxLayout.Y_AXIS));
        contenedorFavs.setBackground(new Color(245, 247, 250));

        JScrollPane scroll = new JScrollPane(contenedorFavs);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    private void recargarFavoritos(JPanel contenedorFavs) {
        contenedorFavs.removeAll();

        // Obtener el ID del usuario actual
        String idUsuario = safeUserId();
        if (idUsuario == null) {
            JLabel err = new JLabel("Error: ID de usuario no disponible.", SwingConstants.CENTER);
            err.setForeground(new Color(170,60,60));
            err.setBorder(new EmptyBorder(12,16,12,16));
            contenedorFavs.add(err);
            contenedorFavs.revalidate();
            contenedorFavs.repaint();
            return;
        }

        JLabel loading = new JLabel("⏳ Cargando favoritos...", SwingConstants.CENTER);
        loading.setFont(new Font("SansSerif", Font.ITALIC, 14));
        loading.setForeground(new Color(100, 120, 150));
        contenedorFavs.add(loading);
        contenedorFavs.revalidate();
        contenedorFavs.repaint();

        SwingWorker<java.util.List<Anuncio>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<Anuncio> doInBackground() {
                return favoritosApi.getFavoritos(idUsuario);
            }

            @Override
            protected void done() {
                try {
                    var lista = get();
                    contenedorFavs.removeAll();

                    if (lista == null || lista.isEmpty()) {
                        JLabel empty = new JLabel("No tienes anuncios en favoritos.", SwingConstants.CENTER);
                        empty.setForeground(new Color(120,130,150));
                        empty.setBorder(new EmptyBorder(12,16,12,16));
                        contenedorFavs.add(empty);
                    } else {
                        for (Anuncio a : lista) {
                            JPanel tarjeta = crearTarjetaResultado(a);
                            // La tarjeta ya tiene el listener de doble click
                            tarjeta.addMouseListener(new MouseAdapter() {
                                @Override public void mouseClicked(MouseEvent e) {
                                    if (e.getClickCount() == 2) {
                                        // Aquí no se sabe qué componente es el padre para el modal,
                                        // se asume this (el JFrame AppMovilMock)
                                        mostrarDetalleAnuncio(a, AppMovilMock.this);
                                    }
                                }
                            });
                            contenedorFavs.add(tarjeta);
                            contenedorFavs.add(Box.createRigidArea(new Dimension(0, 10)));
                        }
                    }
                    contenedorFavs.revalidate();
                    contenedorFavs.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    contenedorFavs.removeAll();
                    JLabel err = new JLabel("Error al cargar favoritos: " + ex.getMessage(), SwingConstants.CENTER);
                    err.setForeground(new Color(170,60,60));
                    err.setBorder(new EmptyBorder(12,16,12,16));
                    contenedorFavs.add(err);
                    contenedorFavs.revalidate();
                    contenedorFavs.repaint();
                }
            }
        };
        worker.execute();
    }

    // Método auxiliar para obtener el ID de usuario
    private String safeUserId() {
        try { return (String) currentUser.getClass().getMethod("getId").invoke(currentUser); }
        catch (Exception e) { return null; }
    }

    // Lógica para guardar/eliminar favorito y actualizar el botón.
    private void toggleFavorito(Anuncio anuncio, JButton btnFav) {
        String idUsuario = safeUserId();
        if (idUsuario == null || anuncio.getId() == null) {
            JOptionPane.showMessageDialog(this, "Error: No se pudo identificar el usuario o el anuncio.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return favoritosApi.toggleFavorito(idUsuario, anuncio.getId());
            }

            @Override
            protected void done() {
                try {
                    Boolean newState = get();
                    if (newState != null) {
                        boolean isFav = newState;
                        // Actualizar el botón
                        JButton newButton = createStarButton(isFav);
                        btnFav.setText(newButton.getText());
                        btnFav.setBackground(newButton.getBackground());
                        btnFav.setBorder(newButton.getBorder());
                        btnFav.setForeground(newButton.getForeground());
                        btnFav.setToolTipText(isFav ? "★ Quitar de favoritos" : "☆ Agregar a favoritos");

                        // Volver a añadir el listener de toggle
                        for (java.awt.event.ActionListener listener : btnFav.getActionListeners()) {
                            btnFav.removeActionListener(listener);
                        }
                        btnFav.addActionListener(e -> toggleFavorito(anuncio, btnFav));

                        // Recargar la vista de Favoritos si estamos en ella
                        if (favoritosWrapper.isShowing()) {
                            // Si está visible, forzamos la recarga del contenido.
                            JPanel favsContainer = getFavoritosContainer();
                            if (favsContainer != null) {
                                recargarFavoritos(favsContainer);
                            }
                        }

                    } else {
                        JOptionPane.showMessageDialog(AppMovilMock.this, "Error al guardar/eliminar favorito.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(AppMovilMock.this, "Error de comunicación: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // Método auxiliar para saber si un componente se está mostrando actualmente
    private boolean isDisplaying(Component c) {
        // Mejorar la comprobación de si es el panel visible en CardLayout
        return c.isVisible() && c.getParent() != null && c.getParent().isVisible();
    }

    // Crea un botón con forma de estrella usando gráficos en lugar de caracteres
    private JButton createStarButton(boolean isFavorito) {
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dibujar estrella en el centro
                int w = getWidth();
                int h = getHeight();
                int size = Math.min(w, h) - 12;
                int cx = w / 2;
                int cy = h / 2;

                // Puntos de la estrella
                int[] xPoints = new int[10];
                int[] yPoints = new int[10];
                double angle = -Math.PI / 2; // Empezar desde arriba
                for (int i = 0; i < 10; i++) {
                    double r = (i % 2 == 0) ? size / 2.0 : size / 4.0;
                    xPoints[i] = cx + (int) (r * Math.cos(angle));
                    yPoints[i] = cy + (int) (r * Math.sin(angle));
                    angle += Math.PI / 5;
                }

                // Dibujar estrella rellena o solo contorno
                Color starColor = getForeground();
                if (isFavorito || getModel().isRollover()) {
                    g2d.setColor(starColor);
                    g2d.fillPolygon(xPoints, yPoints, 10);
                } else {
                    g2d.setColor(starColor);
                    g2d.setStroke(new BasicStroke(2.0f));
                    g2d.drawPolygon(xPoints, yPoints, 10);
                }

                g2d.dispose();
            }
        };

        b.setUI(new BasicButtonUI());
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(42, 42));
        b.setMinimumSize(new Dimension(42, 42));
        b.setMaximumSize(new Dimension(42, 42));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Colores
        Color borderColor = isFavorito ? new Color(255, 165, 0) : new Color(200, 200, 200);
        Color bgColor = isFavorito ? new Color(255, 248, 220) : new Color(250, 250, 250);
        Color starColor = isFavorito ? new Color(255, 140, 0) : new Color(150, 150, 150);

        b.setBackground(bgColor);
        b.setBorder(BorderFactory.createCompoundBorder(
            new UIUtils.RoundedBorder(12, borderColor),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setForeground(starColor);

        // Efecto hover
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (isFavorito) {
                    b.setBackground(new Color(255, 235, 180));
                    b.setBorder(BorderFactory.createCompoundBorder(
                        new UIUtils.RoundedBorder(12, new Color(255, 140, 0)),
                        BorderFactory.createEmptyBorder(2, 2, 2, 2)
                    ));
                } else {
                    b.setBackground(new Color(255, 248, 220));
                    b.setForeground(new Color(255, 140, 0));
                    b.setBorder(BorderFactory.createCompoundBorder(
                        new UIUtils.RoundedBorder(12, new Color(255, 165, 0)),
                        BorderFactory.createEmptyBorder(2, 2, 2, 2)
                    ));
                }
                b.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setBackground(bgColor);
                b.setForeground(starColor);
                b.setBorder(BorderFactory.createCompoundBorder(
                    new UIUtils.RoundedBorder(12, borderColor),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
                ));
                b.repaint();
            }
        });

        return b;
    }

    // PERFIL con SCROLL y mapa (si existe empresa con ubicación)
    private JPanel crearPantallaPerfil() {
        // Contenido real
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(245, 247, 250));

        //  Tarjeta: Perfil de usuario
        JPanel cardUser = createCardPanel();
        JLabel titleUser = titleLabel("Tu perfil");

        JPanel gridUser = new JPanel(new GridBagLayout());
        gridUser.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 8, 4, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0;
        Map<String, String> data = buildUserDataMap(currentUser);
        for (Map.Entry<String,String> e : data.entrySet()) {
            JLabel k = new JLabel(e.getKey() + ":");
            k.setForeground(new Color(95, 105, 125));
            JLabel v = new JLabel(e.getValue());
            v.setForeground(new Color(30, 33, 40));
            g.gridx = 0; gridUser.add(k, g);
            g.gridx = 1; gridUser.add(v, g);
            g.gridy++;
        }

        JButton btnLogout = UIUtils.dangerButton("Cerrar sesión");
        btnLogout.addActionListener(ev -> {
            dispose();
            SwingUtilities.invokeLater(() -> new JVentana().setVisible(true));
        });

        // NUEVO: panel método de pago
        MetodoPagoPanel metodoPagoPanel = new MetodoPagoPanel(currentUser);

        GridBagConstraints gbcU = UIUtils.baseGbc();
        // Título
        gbcU.gridy = 0;
        gbcU.insets = new Insets(8, 12, 8, 12);
        cardUser.add(titleUser, gbcU);
        // Datos de usuario
        gbcU.gridy = 1;
        gbcU.insets = new Insets(4, 12, 8, 12);
        cardUser.add(gridUser, gbcU);
        // Botón logout
        gbcU.gridy = 2;
        gbcU.insets = new Insets(12, 12, 8, 12);
        cardUser.add(btnLogout, gbcU);
        // Método de pago (debajo del logout)
        gbcU.gridy = 3;
        gbcU.insets = new Insets(8, 12, 12, 12);
        cardUser.add(metodoPagoPanel, gbcU);

        //  Tarjeta: Perfil de empresa
        JPanel cardEmp = createCardPanel();
        JLabel titleEmp = titleLabel("Mi Empresa");

        EmpresaApi empApi = new EmpresaApi();
        Empresa emp = empApi.getEmpresa(safeEmail());

        GridBagConstraints gbcE = UIUtils.baseGbc();
        gbcE.gridy = 0;
        gbcE.insets = new Insets(8, 12, 8, 12);
        cardEmp.add(titleEmp, gbcE);

        if (emp == null) {
            JLabel info = new JLabel("Completa tu perfil de empresa en la pestaña 'Mi Empresa'.");
            info.setForeground(new Color(95, 105, 125));

            JButton irEmpresa = UIUtils.secondaryButton("Completar ahora");
            irEmpresa.addActionListener(e -> {
                setSelectedTab(btnEmpresa);
                subLabel.setText("🏢 Mi Empresa");
                cardLayout.show(panelContenido, "MI_EMPRESA");
            });
            gbcE.gridy = 1;
            gbcE.insets = new Insets(4, 12, 8, 12);
            cardEmp.add(info, gbcE);
            gbcE.gridy = 2;
            gbcE.insets = new Insets(12, 12, 12, 12);
            cardEmp.add(irEmpresa, gbcE);
        } else {
            // Panel para foto de perfil
            JPanel fotoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
            fotoPanel.setOpaque(false);

            JLabel lblFoto = new JLabel();
            lblFoto.setPreferredSize(new Dimension(100, 100));
            lblFoto.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 225), 2));
            lblFoto.setHorizontalAlignment(SwingConstants.CENTER);

            // Cargar foto de perfil o usar icono por defecto
            if (emp.getFotoPerfil() != null && !emp.getFotoPerfil().isEmpty()) {
                try {
                    ImageIcon fotoIcon = base64ToImageIcon(emp.getFotoPerfil(), 100);
                    lblFoto.setIcon(fotoIcon);
                } catch (Exception ex) {
                    lblFoto.setIcon(createDefaultCompanyIcon(100));
                }
            } else {
                lblFoto.setIcon(createDefaultCompanyIcon(100));
            }

            fotoPanel.add(lblFoto);

            JPanel gridEmp = new JPanel(new GridBagLayout());
            gridEmp.setOpaque(false);
            GridBagConstraints g2 = new GridBagConstraints();
            g2.insets = new Insets(4,8,4,8);
            g2.fill = GridBagConstraints.HORIZONTAL;
            g2.gridx = 0; g2.gridy = 0;
            addRow(gridEmp, g2, "Empresa",   emp.getEmpresa());
            addRow(gridEmp, g2, "NIF/CIF",   emp.getNif());
            addRow(gridEmp, g2, "Sector",    emp.getSector());
            addRow(gridEmp, g2, "Ubicación", emp.getUbicacion());
            addRow(gridEmp, g2, "Mail",      emp.getMail());

            JButton btnEditar = UIUtils.secondaryButton("Editar perfil");
            btnEditar.addActionListener(e -> mostrarFormularioEmpresa(emp));

            gbcE.gridy = 1;
            gbcE.insets = new Insets(4, 12, 8, 12);
            cardEmp.add(fotoPanel, gbcE);
            gbcE.gridy = 2;
            gbcE.insets = new Insets(4, 12, 8, 12);
            cardEmp.add(gridEmp, gbcE);
            gbcE.gridy = 3;
            gbcE.insets = new Insets(12, 12, 12, 12);
            cardEmp.add(btnEditar, gbcE);
        }

        // Añadir tarjetas al wrapper
        GridBagConstraints wrapC = new GridBagConstraints();
        wrapC.insets = new Insets(10, 10, 5, 10);
        wrapC.gridx = 0; wrapC.gridy = 0;
        wrapC.fill = GridBagConstraints.HORIZONTAL;
        wrapC.weightx = 1;
        wrapper.add(cardUser, wrapC);

        wrapC.gridy = 1;
        wrapC.insets = new Insets(5, 10, 10, 10);
        wrapper.add(cardEmp, wrapC);

        // Mapa debajo de la tarjeta empresa (si hay ubicación)
        if (emp != null && emp.getUbicacion() != null && !emp.getUbicacion().isBlank()) {
            CompanyMapPanel mapCard = new CompanyMapPanel();
            mapCard.setPreferredSize(new Dimension(320, 240));

            GridBagConstraints wrapC2 = new GridBagConstraints();
            wrapC2.insets = new Insets(5, 10, 10, 10);
            wrapC2.gridx = 0;
            wrapC2.gridy = 2;
            wrapC2.fill = GridBagConstraints.BOTH;
            wrapC2.weightx = 1.0;
            wrapC2.weighty = 0.0;

            wrapper.add(mapCard, wrapC2);
            mapCard.setAddressAsync(emp.getUbicacion(), emp.getEmpresa());
        }

        //  Scroll vertical para no cortar contenido
        JScrollPane scroll = new JScrollPane(
                wrapper,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Panel raíz que se devuelve
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));
        root.add(scroll, BorderLayout.CENTER);

        return root;
    }


    private void addRow(JPanel grid, GridBagConstraints g, String k, String v) {
        JLabel lk = new JLabel(k + ":");
        lk.setForeground(new Color(95,105,125));
        JLabel lv = new JLabel(v != null ? v : "-");
        lv.setForeground(new Color(30,33,40));
        g.gridx = 0; grid.add(lk, g);
        g.gridx = 1; grid.add(lv, g);
        g.gridy++;
    }

    private JPanel crearPantallaBusquedas() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(new Color(245, 247, 250));
        contenedor.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel filtrosCard = createCardPanel();
        //Tarjeta de Filtros
        GridBagConstraints gbc = new GridBagConstraints();
        filtrosCard.setLayout(new GridBagLayout());
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        //Categoria
        JLabel lblGeneral = new JLabel("Categoría");
        lblGeneral.setForeground(new Color(20, 40, 80));
        lblGeneral.setFont(new Font("SansSerif", Font.PLAIN, 12)); // texto más pequeño
        cboGeneral = UIUtils.styledCombo(CATEGORIAS_GENERALES);
        gbc.weightx = 0; filtrosCard.add(lblGeneral, gbc);
        gbc.gridx = 1; gbc.weightx = 1; filtrosCard.add(cboGeneral, gbc);
        //Trabajo
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblEspecifico = new JLabel("Trabajo");
        lblEspecifico.setForeground(new Color(20, 40, 80));
        lblEspecifico.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cboEspecifico = UIUtils.styledCombo(new String[]{});
        cboEspecifico.setEnabled(false);
        filtrosCard.add(lblEspecifico, gbc);
        gbc.gridx = 1; gbc.weightx = 1; filtrosCard.add(cboEspecifico, gbc);
        //Ubicación
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel lblUbicacion = new JLabel("Ubicacion");
        lblUbicacion.setForeground(new Color(20, 40, 80));
        lblUbicacion.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cboUbicacion = UIUtils.styledCombo(new String[]{"500 m", "1 km", "2 km", "5 km", "10 km"});
        cboUbicacion.setSelectedIndex(1);
        // por defecto "1 km"
        filtrosCard.add(lblUbicacion, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        filtrosCard.add(cboUbicacion, gbc);

        //Calidad
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel lblCalidad = new JLabel("Calidad");
        lblCalidad.setForeground(new Color(20, 40, 80));
        lblCalidad.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cboCalidad = UIUtils.styledCombo(new String[]{"⭐", "⭐⭐", "⭐⭐⭐", "⭐⭐⭐⭐", "⭐⭐⭐⭐⭐"});
        cboCalidad.setSelectedIndex(0);
        filtrosCard.add(lblCalidad, gbc);
        gbc.gridx = 1; gbc.weightx = 1; filtrosCard.add(cboCalidad, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel ubicacionPanel = new JPanel(new BorderLayout(8, 0));
        ubicacionPanel.setOpaque(false);
        lblUbicacionActual = new JLabel("Ubicaci\u00f3n no establecida");
        lblUbicacionActual.setForeground(new Color(90, 100, 120));
        JButton btnCambiarUbicacion = UIUtils.secondaryButton("📍 Establecer ubicaci\u00f3n");
        btnCambiarUbicacion.addActionListener(e -> solicitarUbicacionManual());
        ubicacionPanel.add(lblUbicacionActual, BorderLayout.CENTER);
        ubicacionPanel.add(btnCambiarUbicacion, BorderLayout.EAST);
        filtrosCard.add(ubicacionPanel, gbc);
        gbc.gridwidth = 1;

        // ====== Resultados (contenedor vertical con tarjetas) ======
        JPanel resultadosCard = createCardPanel();
        resultadosCard.setLayout(new BorderLayout());
        resultadosCard.setPreferredSize(new Dimension(320, 420));

        contenedorLista = new JPanel();
        contenedorLista.setLayout(new BoxLayout(contenedorLista, BoxLayout.Y_AXIS));
        contenedorLista.setBackground(new Color(245, 247, 250));
        JScrollPane scroll = new JScrollPane(contenedorLista);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        resultadosCard.add(scroll, BorderLayout.CENTER);

        // Composición final: filtros fijos arriba, resultados con scroll en el centro
        contenedor.add(filtrosCard, BorderLayout.NORTH);
        contenedor.add(resultadosCard, BorderLayout.CENTER);

        // === Listeners filtros ===
        cboGeneral.addActionListener(e -> {
            String general = (String) cboGeneral.getSelectedItem();
            contenedorLista.removeAll();
            cboEspecifico.removeAllItems();
            if (general != null && ESPECIFICAS.containsKey(general)) {
                for (String s : ESPECIFICAS.get(general)) cboEspecifico.addItem(s);

                cboEspecifico.setEnabled(true);
                if (cboEspecifico.getItemCount() > 0) cboEspecifico.setSelectedIndex(0);
            } else {
                cboEspecifico.setEnabled(false);
            }
            // tras cambiar la categoría, recargamos resultados

            recargarResultados((String) cboGeneral.getSelectedItem(),
                    cboEspecifico.isEnabled() ? (String) cboEspecifico.getSelectedItem() : null);
        });
        cboEspecifico.addActionListener(e -> {
            if (!cboEspecifico.isEnabled()) return;
            contenedorLista.removeAll();
            recargarResultados((String) cboGeneral.getSelectedItem(),
                    (String) cboEspecifico.getSelectedItem());
        });
        cboUbicacion.addActionListener(e -> {
            if (!cboUbicacion.isEnabled()) return;
            recargarResultados((String) cboGeneral.getSelectedItem(),
                    cboEspecifico.isEnabled() ? (String) cboEspecifico.getSelectedItem() : null);
        });
        cboCalidad.addActionListener(e -> recargarResultados(
                (String) cboGeneral.getSelectedItem(),
                cboEspecifico.isEnabled() ? (String) cboEspecifico.getSelectedItem() : null
        ));
        if (cboGeneral.getItemCount() > 0) cboGeneral.setSelectedIndex(0);
        // primera carga de resultados con los valores por defecto
        recargarResultados((String) cboGeneral.getSelectedItem(),
                cboEspecifico.isEnabled() ? (String) cboEspecifico.getSelectedItem() : null);
        actualizarEstadoUbicacionUI();
        return contenedor;
    }

    private void recargarResultados(String categoria, String trabajo) {
        // Calidad (⭐..⭐⭐⭐⭐⭐) → índice 0..4  => mínimo 1..5
        int calidadMin = cboCalidad.getSelectedIndex() + 1;
        // Radio en km a partir del combo (500 m, 1 km, 2 km, 5 km, 10 km)
        Double radioKm = null;
        if (cboUbicacion.isEnabled()) {
            radioKm = switch (cboUbicacion.getSelectedIndex()) {
                case 0 -> 0.5;
                case 1 -> 1d;
                case 2 -> 2d;
                case 3 -> 5d;
                default -> 10d;
            };
        }

        String origen = (cboUbicacion.isEnabled() && userLocation != null && !userLocation.isBlank())
                ? userLocation
                : null;
        final Double radioKmFinal = radioKm;
        final String origenFinal = origen;

        contenedorLista.removeAll();
        // Mostrar mensaje de carga
        JLabel loading = new JLabel("⏳ Cargando resultados...", SwingConstants.CENTER);
        loading.setFont(new Font("SansSerif", Font.ITALIC, 14));
        loading.setForeground(new Color(100, 120, 150));
        contenedorLista.add(loading);
        contenedorLista.revalidate();
        contenedorLista.repaint();
        // Ejecutar búsqueda en background
        SwingWorker<java.util.List<Anuncio>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<Anuncio> doInBackground() {
                System.out.println("=== BÚSQUEDA ===");
                System.out.println("Categoría: " + categoria);
                System.out.println("Trabajo: " + trabajo);

                return anuncioApi.searchAnuncios(categoria, trabajo, calidadMin, origenFinal, radioKmFinal);
            }

            @Override
            protected void done() {
                try {
                    var lista = get();
                    contenedorLista.removeAll();

                    System.out.println("Resultados encontrados: " + (lista != null ? lista.size() : 0));
                    if (lista == null || lista.isEmpty()) {
                        JLabel empty = new JLabel("Sin resultados para los filtros actuales");
                        empty.setForeground(new Color(120,130,150));
                        empty.setBorder(new EmptyBorder(12,16,12,16));
                        contenedorLista.add(empty);
                    } else {
                        for (Anuncio a : lista) {
                            JPanel tarjeta = crearTarjetaResultado(a);
                            tarjeta.addMouseListener(new MouseAdapter() {
                                @Override public void mouseClicked(MouseEvent e) {
                                    if (e.getClickCount() == 2) {

                                        mostrarDetalleAnuncio(a, tarjeta);
                                    }
                                }

                            });
                            contenedorLista.add(tarjeta);
                            contenedorLista.add(Box.createRigidArea(new Dimension(0, 10)));
                        }
                    }
                    contenedorLista.revalidate();
                    contenedorLista.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    contenedorLista.removeAll();
                    JLabel err = new JLabel("Error al cargar resultados: " + ex.getMessage());
                    err.setForeground(new Color(170,60,60));
                    err.setBorder(new EmptyBorder(12,16,12,16));
                    contenedorLista.add(err);
                    contenedorLista.revalidate();
                    contenedorLista.repaint();
                }
            }
        };
        worker.execute();
    }

    private void solicitarUbicacionInicial() {
        if ((userLocation == null || userLocation.isBlank()) && !locationPrompted) {
            locationPrompted = true;
            mostrarDialogoUbicacion("Para usar el filtro por distancia introduce tu direcci\u00f3n (opcional).");
        }
        actualizarEstadoUbicacionUI();
    }

    private void solicitarUbicacionManual() {
        locationPrompted = true;
        boolean submitted = mostrarDialogoUbicacion("Introduce tu direcci\u00f3n para priorizar los resultados cercanos.");
        actualizarEstadoUbicacionUI();
        if (submitted) {
            recargarResultadosConValoresActuales();
        }
    }

    private boolean mostrarDialogoUbicacion(String mensaje) {
        String valor = (String) JOptionPane.showInputDialog(
                this,
                mensaje + "\n(Puedes dejarlo vac\u00edo para mantener la funcionalidad desactivada).",
                "Tu ubicaci\u00f3n",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                userLocation != null ? userLocation : ""
        );
        if (valor == null) return false;
        valor = valor.trim();
        userLocation = valor.isEmpty() ? null : valor;
        return true;
    }

    private void actualizarEstadoUbicacionUI() {
        boolean disponible = userLocation != null && !userLocation.isBlank();
        if (lblUbicacionActual != null) {
        lblUbicacionActual.setText(disponible ? userLocation : "Ubicaci\u00f3n no establecida");
        }
        if (cboUbicacion != null) {
            cboUbicacion.setEnabled(disponible);
        }
    }

    private void recargarResultadosConValoresActuales() {
        if (cboGeneral == null) return;
        recargarResultados((String) cboGeneral.getSelectedItem(),
                (cboEspecifico != null && cboEspecifico.isEnabled()) ? (String) cboEspecifico.getSelectedItem() : null);
    }

    private void mostrarFormularioEmpresa(Empresa emp) {
        JTextField txtNombre = UIUtils.styledTextField(22);
        JTextField txtNif    = UIUtils.styledTextField(22);
        JComboBox<String> cboSector = UIUtils.styledCombo(CATEGORIAS_GENERALES);
        JTextField txtUbicacion = UIUtils.styledTextField(22);

        // Panel para la foto de perfil
        JLabel lblFotoPreview = new JLabel();
        lblFotoPreview.setPreferredSize(new Dimension(120, 120));
        lblFotoPreview.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 225), 2));
        lblFotoPreview.setHorizontalAlignment(SwingConstants.CENTER);

        final String[] selectedImageBase64 = {null};

        if (emp != null) {
            txtNombre.setText(emp.getEmpresa());
            txtNif.setText(emp.getNif());
            cboSector.setSelectedItem(emp.getSector());
            txtUbicacion.setText(emp.getUbicacion());

            // Cargar foto existente o icono por defecto
            if (emp.getFotoPerfil() != null && !emp.getFotoPerfil().isEmpty()) {
                selectedImageBase64[0] = emp.getFotoPerfil();
                lblFotoPreview.setIcon(base64ToImageIcon(emp.getFotoPerfil(), 120));
            } else {
                lblFotoPreview.setIcon(createDefaultCompanyIcon(120));
            }
        } else {
            if (cboSector.getItemCount()>0) {
                cboSector.setSelectedIndex(0);
            }
            lblFotoPreview.setIcon(createDefaultCompanyIcon(120));
        }

        JButton btnSeleccionarFoto = UIUtils.secondaryButton("Seleccionar foto");
        btnSeleccionarFoto.setPreferredSize(new Dimension(160, 35));
        btnSeleccionarFoto.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Seleccionar foto de perfil");
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Imágenes (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png");
            fileChooser.setFileFilter(filter);

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    BufferedImage img = ImageIO.read(selectedFile);
                    if (img != null) {
                        selectedImageBase64[0] = imageToBase64(img);
                        ImageIcon preview = new ImageIcon(scaleImage(img, 120, 120));
                        lblFotoPreview.setIcon(preview);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error al cargar la imagen: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel fotoPanel = new JPanel(new BorderLayout(8, 8));
        fotoPanel.setBackground(Color.WHITE);
        JPanel fotoPreviewPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fotoPreviewPanel.setBackground(Color.WHITE);
        fotoPreviewPanel.add(lblFotoPreview);
        fotoPanel.add(fotoPreviewPanel, BorderLayout.CENTER);
        fotoPanel.add(btnSeleccionarFoto, BorderLayout.SOUTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12,12,12,12));
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = UIUtils.baseGbc();
        gbc.gridy=0; form.add(new JLabel("Foto de perfil:"), gbc);
        gbc.gridy=1; form.add(fotoPanel, gbc);
        gbc.gridy=2; form.add(new JLabel("Nombre de la empresa:"), gbc);
        gbc.gridy=3; form.add(txtNombre, gbc);
        gbc.gridy=4; form.add(new JLabel("NIF/CIF:"), gbc);
        gbc.gridy=5;
        form.add(txtNif, gbc);
        gbc.gridy=6; form.add(new JLabel("Sector:"), gbc);
        gbc.gridy=7; form.add(cboSector, gbc);
        gbc.gridy=8; form.add(new JLabel("Ubicación:"), gbc);
        gbc.gridy=9; form.add(txtUbicacion, gbc);
        int r = JOptionPane.showConfirmDialog(
                this, form, "Editar perfil de empresa",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (r == JOptionPane.OK_OPTION) {
            String nombre = txtNombre.getText().trim();
            String nif    = txtNif.getText().trim();
            String sector = (String) cboSector.getSelectedItem();
            String ubic   = txtUbicacion.getText().trim();
            if (nombre.isEmpty() || nif.isEmpty() || sector==null || sector.isBlank() || ubic.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Rellena todos los campos.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Si no se seleccionó nueva foto, mantener la existente o usar defecto
            String fotoPerfil = selectedImageBase64[0];
            if (fotoPerfil == null) {
                if (emp != null && emp.getFotoPerfil() != null) {
                    fotoPerfil = emp.getFotoPerfil();
                } else {
                    fotoPerfil = iconToBase64(createDefaultCompanyIcon(120));
                }
            }

            EmpresaApi api = new EmpresaApi();
            boolean ok = api.saveEmpresa(safeEmail(), nombre, nif, sector, ubic, fotoPerfil);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Empresa guardada", "Mi Empresa", JOptionPane.INFORMATION_MESSAGE);
                refreshPerfil();
                setSelectedTab(btnPerfil);
                subLabel.setText("🧑‍💼 Perfil");
                showPerfil();
            } else {
                JOptionPane.showMessageDialog(this, "Error guardando", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /* ---------------- Utilidades de estilo ---------------- */

    private JPanel createCardPanel() {
        JPanel card = new JPanel(new GridBagLayout()) {
            // sombra suave
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,18));
                g2.fillRoundRect(4, 4, getWidth()-8, getHeight()-6, 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(new UIUtils.RoundedBorder(16, new Color(230, 235, 245)));
        return card;
    }

    private JLabel titleLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 16)); // más compacto
        l.setForeground(new Color(20, 40, 80));
        return l;
    }

    private JButton navEmojiButton(String emoji, String tooltip) {
        JButton b = new JButton(emoji);
        b.setUI(new BasicButtonUI());
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setBorder(new UIUtils.RoundedBorder(14, new Color(220, 226, 235)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setToolTipText(tooltip);

        b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        b.setPreferredSize(new Dimension(72, 52)); // Mantener tamaño acordado
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        // efecto hover
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (b.isEnabled()) b.setBackground(new Color(245, 248, 255));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (b.isEnabled()) b.setBackground(Color.WHITE);

            }
        });
        return b;
    }

    private void setSelectedTab(JButton selected) {
        JButton[] all = {btnPerfil, btnBusquedas, btnFavoritos, btnContrataciones, btnChats, btnEmpresa};
        for (JButton b : all) {
            if (b == null) continue;
            if (selected == null) {
                b.setBackground(Color.WHITE);
                b.setBorder(new UIUtils.RoundedBorder(14, new Color(220, 226, 235)));
                continue;
            }
            if (b == selected) {
                b.setBackground(new Color(232, 239, 255));
                b.setBorder(new UIUtils.RoundedBorder(14, new Color(120, 160, 255)));
            } else {
                b.setBackground(Color.WHITE);
                b.setBorder(new UIUtils.RoundedBorder(14, new Color(220, 226, 235)));
            }
        }
    }

    private Map<String,String> buildUserDataMap(User u) {
        LinkedHashMap<String,String> m = new LinkedHashMap<>();
        putIfPresent(m, "ID", call(u, "getId"));
        putIfPresent(m, "Usuario", call(u, "getUsername"));
        putIfPresent(m, "Email", call(u, "getEmail"));
        return m.isEmpty() ? new LinkedHashMap<>(Map.of("Usuario", String.valueOf(u))) : m;
    }

    private void putIfPresent(Map<String,String> map, String key, String val) {
        if (val != null && !val.trim().isEmpty() && !"null".equalsIgnoreCase(val.trim())) map.put(key, val);
    }

    private String call(Object obj, String method) {
        try {
            Method m = obj.getClass().getMethod(method);
            Object v = m.invoke(obj);
            return v == null ? null : String.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }

    private String safeEmail() {
        try { return (String) currentUser.getClass().getMethod("getEmail").invoke(currentUser);
        }
        catch (Exception e) { return null;
        }
    }

    private static String safe(Object s, String def) {
        return (s != null && !String.valueOf(s).isBlank()) ?
                String.valueOf(s) : def;
    }

    // En caso de que alguien aún llame a esto, devolvemos un 1x1 vacío
    private ImageIcon loadIcon(String path, int targetH) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            return new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        }
        ImageIcon raw = new ImageIcon(url);
        int w = raw.getIconWidth();
        int h = raw.getIconHeight();
        if (h <= 0) return raw;
        float scale = (float) targetH / (float) h;
        int newW = Math.max(1, Math.round(w * scale));
        Image img = raw.getImage().getScaledInstance(newW, targetH, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    // --------- Tarjeta de resultado con GridBagLayout (MODIFICADO para incluir el botón FAV) ----------
    private JPanel crearTarjetaResultado(Anuncio anuncio) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(12, new Color(220, 230, 245)),
                new EmptyBorder(12, 16, 12, 16)
        ));
        // Altura de 320px para que todos los botones sean visibles
        card.setPreferredSize(new Dimension(900, 320));
        card.setMinimumSize(new Dimension(600, 320));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        // ========== PANEL IZQUIERDO: Información del anuncio ==========
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = 0;
        left.anchor = GridBagConstraints.NORTHWEST;
        left.insets = new Insets(4, 4, 4, 16);
        left.weightx = 1.0;
        left.weighty = 1.0;
        left.fill = GridBagConstraints.BOTH;

        JPanel dataPanel = new JPanel();
        dataPanel.setOpaque(false);
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
        // Categoría · Especificación
        String cat = anuncio.getCategoria() != null ?
                anuncio.getCategoria() : "";
        String spec = anuncio.getEspecificacion() != null ? anuncio.getEspecificacion() : "";
        String linea = (spec.isBlank() ? cat : (cat + " · " + spec));

        JLabel lblCategoria = new JLabel(linea);
        lblCategoria.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblCategoria.setForeground(new Color(80, 120, 200));
        lblCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Descripción (título principal)
        String desc = anuncio.getDescripcion() != null ?
                anuncio.getDescripcion() : "";
        String descCorta = desc.length() > 70 ? desc.substring(0, 70) + "..." : desc;
        JLabel lblTitulo = new JLabel("<html><b>" + descCorta + "</b></html>");
        lblTitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblTitulo.setForeground(new Color(30, 40, 60));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Ubicación
        String ubi = anuncio.getUbicacion() != null ? anuncio.getUbicacion() : "";
        JLabel lblUbicacion = new JLabel("📍 " + ubi);
        lblUbicacion.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblUbicacion.setForeground(new Color(90, 100, 120));
        lblUbicacion.setAlignmentX(Component.LEFT_ALIGNMENT);

        dataPanel.add(lblCategoria);
        dataPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        dataPanel.add(lblTitulo);
        dataPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        dataPanel.add(lblUbicacion);
        dataPanel.add(Box.createVerticalGlue());

        card.add(dataPanel, left);
        // ========== PANEL DERECHO: Precio + Botones verticales ==========
        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = 0;
        right.anchor = GridBagConstraints.NORTHEAST;
        right.insets = new Insets(4, 8, 4, 4);
        right.fill = GridBagConstraints.NONE;
        right.weightx = 0;
        right.weighty = 0;

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        // Precio grande y alineado a la derecha
        String precioStr = (anuncio.getPrecio() != null) ?
                String.format("%.2f €", anuncio.getPrecio()) : "";
        JLabel lblPrecio = new JLabel(precioStr);
        lblPrecio.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblPrecio.setForeground(new Color(20, 120, 80));
        lblPrecio.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightPanel.add(lblPrecio);
        rightPanel.add(Box.createVerticalStrut(10));

        // === Panel de botones Horizontales (FAV + DETALLES) ===
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnWrapper.setOpaque(false);
        btnWrapper.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnWrapper.setMaximumSize(new Dimension(180, 45));

        // --- Botón de Favoritos (Estrella) ---
        String idUsuario = safeUserId();
        boolean isFav = false;
        if (idUsuario != null && anuncio.getId() != null) {
            try {
                isFav = favoritosApi.isFavorito(idUsuario, anuncio.getId());
            } catch (Exception ignored) {}
        }

        JButton btnFav = createStarButton(isFav);
        btnFav.setToolTipText(isFav ? "⭐ Quitar de favoritos" : "⭐ Agregar a favoritos");

        // Listener del botón de favoritos
        btnFav.addActionListener(e -> toggleFavorito(anuncio, btnFav));

        // Botón Ver detalles
        JButton btnDetalles = UIUtils.primaryButton("Ver detalles");
        btnDetalles.setPreferredSize(new Dimension(115, 38));
        btnDetalles.setMinimumSize(new Dimension(115, 38));
        btnDetalles.setMaximumSize(new Dimension(115, 38));
        btnDetalles.addActionListener(e -> showDetalleAnuncio(anuncio));

        // Añadir los botones al wrapper en orden: ESTRELLA primero, DETALLES después
        btnWrapper.add(btnFav);
        btnWrapper.add(btnDetalles);

        rightPanel.add(btnWrapper);
        rightPanel.add(Box.createVerticalStrut(8));


        // Botón Chatear (solo si no es el propio anuncio)
        boolean esPropio = anuncio.getEmpresaNif() != null && anuncio.getEmpresaNif().equals(obtenerNifEmpresaActual());
        if (!esPropio && anuncio.getEmpresaEmail() != null) {
            // Botón Ver valoraciones
            JButton btnValoraciones = UIUtils.secondaryButton("⭐ Ver valoraciones");
            btnValoraciones.setAlignmentX(Component.RIGHT_ALIGNMENT);
            btnValoraciones.setPreferredSize(new Dimension(145, 34));
            btnValoraciones.setMinimumSize(new Dimension(145, 34));
            btnValoraciones.setMaximumSize(new Dimension(145, 34));
            btnValoraciones.setFont(new Font("SansSerif", Font.BOLD, 12));
            btnValoraciones.addActionListener(e -> mostrarValoraciones(anuncio.getEmpresaNif(), anuncio.getNombreEmpresa()));
            rightPanel.add(btnValoraciones);
            rightPanel.add(Box.createVerticalStrut(6));

            JButton btnOferta = crearBotonOferta(anuncio);
            btnOferta.setAlignmentX(Component.RIGHT_ALIGNMENT);
            rightPanel.add(btnOferta);
        }

        card.add(rightPanel, right);
        // Cursor de mano para toda la tarjeta
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return card;
    }

    // Muestra el panel de detalle del anuncio sustituyendo la pantalla de búsquedas
    private void showDetalleAnuncio(Anuncio a) {
        JPanel detalle = new AnuncioDetallePanel(a, () -> {
            // Volver a la lista de búsquedas
            cardLayout.show(panelContenido, "BUSQUEDAS");
            subLabel.setText("🔎 Búsquedas");
            setSelectedTab(btnBusquedas);

        });
        panelContenido.add(detalle, "DETALLE_ANUNCIO");
        cardLayout.show(panelContenido, "DETALLE_ANUNCIO");
        subLabel.setText("📄 Detalle");
        setSelectedTab(null);
    }

    private void mostrarDetalleAnuncio(Anuncio a, Component parent) {
        String precio = "";
        try {
            if (a.getPrecio() != null) {
                double p = ((Number)a.getPrecio()).doubleValue();
                if (p > 0) precio = String.format("Precio: %.2f €\n", p);
            }
        } catch (Throwable ignored) {}

        // Panel personalizado con detalles
        JPanel detallePanel = new JPanel();
        detallePanel.setLayout(new BoxLayout(detallePanel, BoxLayout.Y_AXIS));
        detallePanel.setBackground(Color.WHITE);
        detallePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextArea txtDetalle = new JTextArea(
                "Descripción: " + safe(a.getDescripcion(), "") + "\n\n" +
                        "Categoría: " + safe(a.getCategoria(), "") + "\n" +
                        "Trabajo: " + safe(a.getEspecificacion(), "") + "\n" +
                        precio +

                        "Ubicación: " + safe(a.getUbicacion(), "")
        );
        txtDetalle.setEditable(false);
        txtDetalle.setOpaque(false);
        txtDetalle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtDetalle.setForeground(new Color(30, 33, 40));

        detallePanel.add(txtDetalle);
        // Botón para contactar (solo si no es el propio anuncio del usuario)
        boolean esPropio = a.getEmpresaNif() != null && a.getEmpresaNif().equals(obtenerNifEmpresaActual());
        Object[] options;
        if (esPropio) {
            options = new Object[]{"Cerrar"};
        } else {
            options = new Object[]{"Contactar", "Cerrar"};
        }

        int opcion = JOptionPane.showOptionDialog(
                SwingUtilities.getWindowAncestor(parent),
                detallePanel,
                "Detalle del anuncio",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,

                null,
                options,
                options[0]
        );
        // Si eligió "Contactar" (índice 0 cuando no es propio)
        if (!esPropio && opcion == 0) {
            iniciarChatConAnuncio(a);
        }
    }

    private String obtenerNifEmpresaActual() {
        EmpresaApi empApi = new EmpresaApi();
        Empresa emp = empApi.getEmpresa(safeEmail());
        return emp != null ? emp.getNif() : null;
    }

    private void iniciarChatConAnuncio(Anuncio a) {
        // Validaciones
        if (a == null) {
            JOptionPane.showMessageDialog(this, "Anuncio no válido", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (a.getEmpresaEmail() == null || a.getEmpresaEmail().trim().isEmpty()) {
            System.err.println("Error: empresaEmail es null o vacío para anuncio ID: " + a.getId());
            System.err.println("Anuncio NIF: " + a.getNifEmpresa());
            JOptionPane.showMessageDialog(this,
                    "No se puede contactar con esta empresa.\nLa empresa no tiene email configurado.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (a.getId() == null || a.getId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Anuncio sin ID válido", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("Iniciando chat:");
        System.out.println("  Cliente: " + currentUser.getEmail());
        System.out.println("  Empresa: " + a.getEmpresaEmail());
        System.out.println("  Anuncio ID: " + a.getId());
        try {
            // Crear o obtener chat existente
            Chat chat = chatApi.getOrCreateChat(currentUser.getEmail(), a.getEmpresaEmail(), a.getId());

            if (chat != null) {
                System.out.println("Chat creado/obtenido con ID: " + chat.getId());
                // Cambiar a la pestaña de chats y abrir el chat específico
                setSelectedTab(btnChats);
                subLabel.setText("💬 Chats");

                // Obtener el ChatsPanel y abrirlo
                Component[] components = panelContenido.getComponents();
                for (Component comp : components) {
                    if (comp instanceof ChatsPanel) {
                        ChatsPanel chatsPanel = (ChatsPanel) comp;
                        cardLayout.show(panelContenido, "CHATS");
                        // Refrescar y abrir el chat específico
                        SwingUtilities.invokeLater(() -> chatsPanel.refrescarChats());
                        break;
                    }
                }
            } else {
                System.err.println("Error: chatCtrl.getOrCreateChat devolvió null");
                JOptionPane.showMessageDialog(this,
                        "Error al crear el chat.\nPor favor, verifica que la empresa existe en la base de datos.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("Excepción al crear chat: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al crear el chat: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ====================== PANTALLA COMPLETA: NUEVO ANUNCIO ====================== */

    /** Panel interno de "Nuevo Anuncio" a pantalla completa dentro del CardLayout */
    private class NuevoAnuncioPanel extends JPanel {
        private final JTextField txtDescripcion = UIUtils.styledTextField(26);
        private final JTextField txtPrecio = UIUtils.styledTextField(12);
        private final JComboBox<String> cboCategoria = UIUtils.styledCombo(CATEGORIAS_GENERALES);
        private final JComboBox<String> cboEspecificacion = UIUtils.styledCombo(new String[]{});
        private final JTextField txtUbicacion = UIUtils.styledTextField(22);
        private final JTextField txtNifEmpresa = UIUtils.styledTextField(18);
        NuevoAnuncioPanel() {
            super(new BorderLayout());
            setBackground(new Color(245,247,250));
            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(12,12,12,12));

            JButton btnBack = UIUtils.secondaryButton("← Volver");
            btnBack.addActionListener(e -> {
                cardLayout.show(panelContenido, "BUSQUEDAS");
                subLabel.setText("🔎 Búsquedas");
                setSelectedTab(btnBusquedas);
            });
            JLabel titulo = titleLabel("Nuevo anuncio");
            titulo.setHorizontalAlignment(SwingConstants.CENTER);

            header.add(btnBack, BorderLayout.WEST);
            header.add(titulo, BorderLayout.CENTER);
            // Formulario
            JPanel card = createCardPanel();
            card.setLayout(new GridBagLayout());
            card.setOpaque(true);
            card.setBackground(Color.WHITE);
            GridBagConstraints gbc = UIUtils.baseGbc();

            // Prefill NIF/ubicación desde empresa si existe
            EmpresaApi empApi = new EmpresaApi();
            Empresa emp = empApi.getEmpresa(safeEmail());
            if (emp != null) {
                txtNifEmpresa.setText(safe(emp.getNif(), ""));
                txtUbicacion.setText(safe(emp.getUbicacion(), ""));
            }

            // Campos
            gbc.gridy=0;
            card.add(new JLabel("Descripción"), gbc);
            gbc.gridy=1; card.add(txtDescripcion, gbc);

            gbc.gridy=2; card.add(new JLabel("Precio (€)"), gbc);
            gbc.gridy=3; card.add(txtPrecio, gbc);

            gbc.gridy=4; card.add(new JLabel("Categoría"), gbc);
            gbc.gridy=5;
            card.add(cboCategoria, gbc);

            gbc.gridy=6; card.add(new JLabel("Trabajo"), gbc);
            cboEspecificacion.setEnabled(false);
            gbc.gridy=7; card.add(cboEspecificacion, gbc);

            gbc.gridy=8; card.add(new JLabel("Ubicación"), gbc);
            gbc.gridy=9; card.add(txtUbicacion, gbc);

            gbc.gridy=10;
            card.add(new JLabel("NIF Empresa"), gbc);
            gbc.gridy=11; card.add(txtNifEmpresa, gbc);

            // Footer acciones
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.setOpaque(false);
            JButton btnCancelar = UIUtils.secondaryButton("Cancelar");
            JButton btnGuardar  = UIUtils.primaryButton("Guardar anuncio");
            btnCancelar.addActionListener(e -> {
                cardLayout.show(panelContenido, "BUSQUEDAS");
                subLabel.setText("🔎 Búsquedas");
                setSelectedTab(btnBusquedas);
            });
            btnGuardar.addActionListener(e -> guardarAnuncio());

            footer.add(btnCancelar);
            footer.add(btnGuardar);

            // listeners categoría → específica
            cboCategoria.addActionListener(e -> {
                String general = (String) cboCategoria.getSelectedItem();
                cboEspecificacion.removeAllItems();
                if (general != null && ESPECIFICAS.containsKey(general)) {

                    for (String s : ESPECIFICAS.get(general)) cboEspecificacion.addItem(s);
                    cboEspecificacion.setEnabled(true);
                    if (cboEspecificacion.getItemCount() > 0) cboEspecificacion.setSelectedIndex(0);
                } else {
                    cboEspecificacion.setEnabled(false);

                }
            });
            if (cboCategoria.getItemCount() > 0) cboCategoria.setSelectedIndex(0);

            // Ensamblado pantalla completa
            JPanel center = new JPanel(new BorderLayout());
            center.setOpaque(false);
            center.setBorder(new EmptyBorder(12,12,12,12));
            center.add(card, BorderLayout.CENTER);

            add(header, BorderLayout.NORTH);
            add(center, BorderLayout.CENTER);
            add(footer, BorderLayout.SOUTH);
        }

        private void guardarAnuncio() {
            String desc  = txtDescripcion.getText().trim();
            String precioStr = txtPrecio.getText().trim();
            String cat   = (String) cboCategoria.getSelectedItem();
            String esp   = cboEspecificacion.isEnabled()?
                    (String) cboEspecificacion.getSelectedItem() : null;
            String ubic  = txtUbicacion.getText().trim();
            String nif   = txtNifEmpresa.getText().trim();
            if (desc.isEmpty() || precioStr.isEmpty() || cat==null || cat.isBlank() || ubic.isEmpty() || nif.isEmpty()) {
                JOptionPane.showMessageDialog(AppMovilMock.this, "Rellena los campos obligatorios.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Double precio;
            try { precio = Double.parseDouble(precioStr.replace(",", ".")); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(AppMovilMock.this, "Precio inválido.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            AnuncioApi api = new AnuncioApi();
            boolean ok = api.createAnuncio(desc, precio, cat, esp, ubic, nif);
            if (ok) {
                JOptionPane.showMessageDialog(AppMovilMock.this, "Anuncio creado", "Nuevo anuncio", JOptionPane.INFORMATION_MESSAGE);
                // Volver a búsquedas y refrescar resultados con la categoría seleccionada
                cardLayout.show(panelContenido, "BUSQUEDAS");
                subLabel.setText("🔎 Búsquedas");
                setSelectedTab(btnBusquedas);
                // Opcional: recargar resultados usando cat/esp actuales
                recargarResultados(cat, esp);
            } else {
                JOptionPane.showMessageDialog(AppMovilMock.this, "Error creando el anuncio", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Convierte una imagen BufferedImage a String Base64
     */
    private String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convierte un ImageIcon a String Base64
     */
    private String iconToBase64(ImageIcon icon) {
        try {
            BufferedImage bi = new BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D g2d = bi.createGraphics();
            icon.paintIcon(null, g2d, 0, 0);
            g2d.dispose();
            return imageToBase64(bi);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convierte String Base64 a ImageIcon
     */
    private ImageIcon base64ToImageIcon(String base64, int size) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img != null) {
                return new ImageIcon(scaleImage(img, size, size));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createDefaultCompanyIcon(size);
    }

    /**
     * Escala una imagen manteniendo aspecto
     */
    private Image scaleImage(BufferedImage original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();

        double scale = Math.min((double)maxWidth / width, (double)maxHeight / height);
        int newWidth = (int)(width * scale);
        int newHeight = (int)(height * scale);

        return original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }

    /**
     * Crea un icono por defecto para empresas
     */
    private ImageIcon createDefaultCompanyIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo degradado
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(80, 120, 200),
            size, size, new Color(50, 90, 160)
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, size, size, size/5, size/5);

        // Icono de empresa (edificio simplificado)
        g2d.setColor(Color.WHITE);
        int buildingWidth = size * 3 / 5;
        int buildingHeight = size * 4 / 5;
        int buildingX = (size - buildingWidth) / 2;
        int buildingY = (size - buildingHeight) / 2 + size / 10;

        g2d.fillRect(buildingX, buildingY, buildingWidth, buildingHeight);

        // Ventanas
        g2d.setColor(new Color(80, 120, 200));
        int windowSize = buildingWidth / 5;
        int spacing = windowSize / 2;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                int wx = buildingX + spacing + col * (windowSize + spacing);
                int wy = buildingY + spacing + row * (windowSize + spacing);
                g2d.fillRect(wx, wy, windowSize, windowSize);
            }
        }

        g2d.dispose();
        return new ImageIcon(img);
    }

    /**
     * Muestra las valoraciones y calificación promedio de una empresa
     */
    private void mostrarValoraciones(String nifEmpresa, String nombreEmpresa) {
        SwingWorker<List<Contratacion>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Contratacion> doInBackground() {
                return contratacionApi.getValoraciones(nifEmpresa);
            }

            @Override
            protected void done() {
                try {
                    List<Contratacion> valoraciones = get();

                    // Calcular promedio de calidad
                    double promedioCalidad = 0.0;
                    int totalValoraciones = 0;

                    if (valoraciones != null && !valoraciones.isEmpty()) {
                        for (Contratacion c : valoraciones) {
                            if (c.getCalidad() != null) {
                                promedioCalidad += c.getCalidad();
                                totalValoraciones++;
                            }
                        }
                        if (totalValoraciones > 0) {
                            promedioCalidad /= totalValoraciones;
                        }
                    }

                    // Crear panel de valoraciones
                    JPanel panelValoraciones = new JPanel();
                    panelValoraciones.setLayout(new BoxLayout(panelValoraciones, BoxLayout.Y_AXIS));
                    panelValoraciones.setBackground(Color.WHITE);
                    panelValoraciones.setBorder(new EmptyBorder(15, 15, 15, 15));

                    // Título con nombre de empresa
                    JLabel lblTitulo = new JLabel("Valoraciones de " + (nombreEmpresa != null ? nombreEmpresa : "la empresa"));
                    lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
                    lblTitulo.setForeground(new Color(20, 40, 80));
                    lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
                    panelValoraciones.add(lblTitulo);
                    panelValoraciones.add(Box.createVerticalStrut(10));

                    // Mostrar promedio
                    if (totalValoraciones > 0) {
                        String estrellas = "⭐".repeat(Math.max(1, (int) Math.round(promedioCalidad)));
                        JLabel lblPromedio = new JLabel(String.format("Calificación promedio: %s (%.1f/5.0) - %d valoración%s",
                            estrellas, promedioCalidad, totalValoraciones, totalValoraciones != 1 ? "es" : ""));
                        lblPromedio.setFont(new Font("SansSerif", Font.BOLD, 14));
                        lblPromedio.setForeground(new Color(220, 140, 0));
                        lblPromedio.setAlignmentX(Component.LEFT_ALIGNMENT);
                        panelValoraciones.add(lblPromedio);
                    } else {
                        JLabel lblSinVal = new JLabel("Esta empresa aún no tiene valoraciones");
                        lblSinVal.setFont(new Font("SansSerif", Font.ITALIC, 13));
                        lblSinVal.setForeground(new Color(120, 130, 150));
                        lblSinVal.setAlignmentX(Component.LEFT_ALIGNMENT);
                        panelValoraciones.add(lblSinVal);
                    }

                    panelValoraciones.add(Box.createVerticalStrut(15));

                    // Mostrar comentarios individuales
                    if (valoraciones != null && !valoraciones.isEmpty()) {
                        JLabel lblComentarios = new JLabel("Comentarios de clientes:");
                        lblComentarios.setFont(new Font("SansSerif", Font.BOLD, 13));
                        lblComentarios.setForeground(new Color(40, 50, 70));
                        lblComentarios.setAlignmentX(Component.LEFT_ALIGNMENT);
                        panelValoraciones.add(lblComentarios);
                        panelValoraciones.add(Box.createVerticalStrut(10));

                        for (Contratacion c : valoraciones) {
                            if (c.getCalidad() != null || (c.getComentarios() != null && !c.getComentarios().isBlank())) {
                                JPanel tarjetaComentario = new JPanel();
                                tarjetaComentario.setLayout(new BoxLayout(tarjetaComentario, BoxLayout.Y_AXIS));
                                tarjetaComentario.setBackground(new Color(248, 250, 252));
                                tarjetaComentario.setBorder(BorderFactory.createCompoundBorder(
                                    new UIUtils.RoundedBorder(8, new Color(220, 230, 245)),
                                    new EmptyBorder(10, 12, 10, 12)
                                ));
                                tarjetaComentario.setAlignmentX(Component.LEFT_ALIGNMENT);
                                tarjetaComentario.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));

                                // Estrellas
                                if (c.getCalidad() != null) {
                                    int estrellas = Math.max(1, Math.min(5, Math.round(c.getCalidad())));
                                    JLabel lblEstrellas = new JLabel("⭐".repeat(estrellas) + " (" + c.getCalidad() + "/5.0)");
                                    lblEstrellas.setFont(new Font("SansSerif", Font.BOLD, 12));
                                    lblEstrellas.setForeground(new Color(220, 140, 0));
                                    lblEstrellas.setAlignmentX(Component.LEFT_ALIGNMENT);
                                    tarjetaComentario.add(lblEstrellas);
                                    tarjetaComentario.add(Box.createVerticalStrut(5));
                                }

                                // Comentario
                                if (c.getComentarios() != null && !c.getComentarios().isBlank()) {
                                    JTextArea txtComentario = new JTextArea(c.getComentarios());
                                    txtComentario.setEditable(false);
                                    txtComentario.setLineWrap(true);
                                    txtComentario.setWrapStyleWord(true);
                                    txtComentario.setFont(new Font("SansSerif", Font.PLAIN, 12));
                                    txtComentario.setForeground(new Color(50, 60, 80));
                                    txtComentario.setBackground(new Color(248, 250, 252));
                                    txtComentario.setBorder(null);
                                    txtComentario.setAlignmentX(Component.LEFT_ALIGNMENT);
                                    tarjetaComentario.add(txtComentario);
                                }

                                panelValoraciones.add(tarjetaComentario);
                                panelValoraciones.add(Box.createVerticalStrut(8));
                            }
                        }
                    }

                    // Scroll para el panel
                    JScrollPane scroll = new JScrollPane(panelValoraciones);
                    scroll.setBorder(BorderFactory.createEmptyBorder());
                    scroll.setPreferredSize(new Dimension(550, 400));
                    scroll.getVerticalScrollBar().setUnitIncrement(16);

                    // Mostrar diálogo
                    JOptionPane.showMessageDialog(
                        AppMovilMock.this,
                        scroll,
                        "Valoraciones de " + (nombreEmpresa != null ? nombreEmpresa : "la empresa"),
                        JOptionPane.PLAIN_MESSAGE
                    );

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                        AppMovilMock.this,
                        "Error al cargar las valoraciones: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }

    /**
     * Crea el botón "Lanzar oferta" dinámicamente según el estado de la contratación
     */
    private JButton crearBotonOferta(Anuncio anuncio) {
        Integer idUser = obtenerIdUserInt();
        String nifEmpresa = anuncio.getEmpresaNif();
        String idAnuncio = anuncio.getId();

        JButton btnOferta = new JButton();
        btnOferta.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnOferta.setFocusPainted(false);
        btnOferta.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnOferta.setPreferredSize(new Dimension(145, 34));
        btnOferta.setMinimumSize(new Dimension(145, 34));
        btnOferta.setMaximumSize(new Dimension(145, 34));
        btnOferta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Verificar el estado de la contratación en segundo plano
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                if (idUser == null || nifEmpresa == null || idAnuncio == null) return null;
                return contratacionApi.getEstado(nifEmpresa, idUser, idAnuncio);
            }

            @Override
            protected void done() {
                try {
                    String estado = get();

                    if (estado == null) {
                        // No hay contratación, mostrar botón "Lanzar oferta"
                        btnOferta.setText("🚀 Lanzar oferta");
                        btnOferta.setBackground(new Color(40, 167, 69));
                        btnOferta.setForeground(Color.WHITE);
                        btnOferta.setBorder(new UIUtils.RoundedBorder(14, new Color(40, 167, 69)));
                        btnOferta.setEnabled(true);
                        btnOferta.addActionListener(e -> lanzarOferta(anuncio, btnOferta));
                    } else {
                        // Ya existe contratación, mostrar estado
                        switch (estado) {
                            case "activo":
                                btnOferta.setText("✅ Contratado");
                                btnOferta.setBackground(new Color(220, 255, 220));
                                btnOferta.setForeground(new Color(0, 120, 0));
                                btnOferta.setBorder(new UIUtils.RoundedBorder(14, new Color(0, 180, 0)));
                                btnOferta.setEnabled(false);
                                break;
                            case "terminado":
                                btnOferta.setText("⏳ Pendiente valorar");
                                btnOferta.setBackground(new Color(255, 245, 200));
                                btnOferta.setForeground(new Color(180, 120, 0));
                                btnOferta.setBorder(new UIUtils.RoundedBorder(14, new Color(220, 160, 0)));
                                btnOferta.setEnabled(false);
                                break;
                            case "valorado":
                                // Ya valorado - permitir contratar de nuevo
                                btnOferta.setText("🔄 Contratar de nuevo");
                                btnOferta.setBackground(new Color(40, 167, 69));
                                btnOferta.setForeground(Color.WHITE);
                                btnOferta.setBorder(new UIUtils.RoundedBorder(14, new Color(40, 167, 69)));
                                btnOferta.setEnabled(true);
                                btnOferta.addActionListener(e -> lanzarOferta(anuncio, btnOferta));
                                break;
                            default:
                                btnOferta.setText("🚀 Lanzar oferta");
                                btnOferta.setBackground(new Color(40, 167, 69));
                                btnOferta.setForeground(Color.WHITE);
                                btnOferta.setBorder(new UIUtils.RoundedBorder(14, new Color(40, 167, 69)));
                                btnOferta.setEnabled(true);
                                btnOferta.addActionListener(e -> lanzarOferta(anuncio, btnOferta));
                        }
                    }
                } catch (Exception ex) {
                    // En caso de error, mostrar botón por defecto
                    btnOferta.setText("🚀 Lanzar oferta");
                    btnOferta.setBackground(new Color(40, 167, 69));
                    btnOferta.setForeground(Color.WHITE);
                    btnOferta.setBorder(new UIUtils.RoundedBorder(14, new Color(40, 167, 69)));
                    btnOferta.setEnabled(true);
                    btnOferta.addActionListener(e -> lanzarOferta(anuncio, btnOferta));
                }
            }
        };
        worker.execute();

        // Mientras tanto, mostrar botón en estado de carga
        btnOferta.setText("⏳ Cargando...");
        btnOferta.setBackground(new Color(240, 240, 240));
        btnOferta.setForeground(new Color(100, 100, 100));
        btnOferta.setBorder(new UIUtils.RoundedBorder(14, new Color(200, 200, 200)));
        btnOferta.setEnabled(false);

        return btnOferta;
    }

    private void lanzarOferta(Anuncio anuncio, JButton btnOferta) {
        Integer idUser = obtenerIdUserInt();
        String nifEmpresa = anuncio.getEmpresaNif();
        String idAnuncio = anuncio.getId();

        if (idUser == null || nifEmpresa == null || idAnuncio == null) {
            JOptionPane.showMessageDialog(this,
                "Error: No se pudo obtener la información necesaria.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Deseas contratar este servicio?\n\n" +
            "Empresa: " + (anuncio.getEmpresaNif() != null ? anuncio.getEmpresaNif() : "N/A") + "\n" +
            "Servicio: " + anuncio.getDescripcion() + "\n" +
            "Categoría: " + anuncio.getCategoria(),
            "Confirmar contratación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return contratacionApi.crearContratacion(nifEmpresa, idUser, idAnuncio);
                }

                @Override
                protected void done() {
                    try {
                        Boolean ok = get();

                        if (Boolean.TRUE.equals(ok)) {
                            JOptionPane.showMessageDialog(
                                AppMovilMock.this,
                                "¡Contratación realizada con éxito!\n" +
                                "Puedes ver tus contrataciones en la pestaña 📋 Contrataciones.",
                                "Contratación exitosa",
                                JOptionPane.INFORMATION_MESSAGE
                            );

                            btnOferta.setText("✅ Contratado");
                            btnOferta.setBackground(new Color(220, 255, 220));
                            btnOferta.setForeground(new Color(0, 120, 0));
                            btnOferta.setBorder(new UIUtils.RoundedBorder(14, new Color(0, 180, 0)));
                            btnOferta.setEnabled(false);
                        } else {
                            JOptionPane.showMessageDialog(
                                AppMovilMock.this,
                                "No se pudo realizar la contratación.\nPuede que ya exista una contratación activa.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                            AppMovilMock.this,
                            "Error al procesar la contratación: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            worker.execute();
        }
    }

    /**
     * Obtiene el ID del usuario como Integer
     */
    private Integer obtenerIdUserInt() {
        try {
            Object id = currentUser.getClass().getMethod("getId").invoke(currentUser);
            if (id instanceof String) {
                String idStr = (String) id;
                if (idStr == null || idStr.isEmpty() || idStr.isBlank()) {
                    // Si no hay ID, usamos el email como identificador único
                    // Generamos un hash del email para tener un ID numérico
                    String email = safeEmail();
                    if (email != null) {
                        return Math.abs(email.hashCode());
                    }
                    return null;
                }
                return Integer.parseInt(idStr);
            } else if (id instanceof Integer) {
                return (Integer) id;
            } else if (id instanceof Number) {
                return ((Number) id).intValue();
            }
        } catch (NumberFormatException e) {
            // Si falla el parseo, intentamos usar el hash del email
            String email = safeEmail();
            if (email != null) {
                return Math.abs(email.hashCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
