package icai.dtc.isw.ui;

import icai.dtc.isw.domain.Empresa;
import icai.dtc.isw.domain.User;
import icai.dtc.isw.domain.Anuncio;
import icai.dtc.isw.domain.Chat;
import icai.dtc.isw.controler.BusquedasControler;
import icai.dtc.isw.controler.ChatControler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private JButton btnPerfil, btnBusquedas, btnFavoritos, btnChats, btnEmpresa;

    private JComboBox<String> cboUbicacion;
    private JComboBox<String> cboCalidad;

    // --- Búsquedas ---
    private final BusquedasControler busquedasCtrl = new BusquedasControler();
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
        panelContenido.add(crearPantalla("⭐ Tus favoritos aparecerán aquí"), "FAVORITOS");
        panelContenido.add(new ChatsPanel(currentUser), "CHATS");

        // Pasamos this al EmpresaPanel para refrescar perfil tras guardar empresa
        panelContenido.add(new EmpresaPanel(currentUser, CATEGORIAS_GENERALES, this), "MI_EMPRESA");

        add(panelContenido, BorderLayout.CENTER);

        // ======== Tab bar inferior con emojis y estado seleccionado ========
        JPanel barraInferior = new JPanel(new GridLayout(1, 5));
        barraInferior.setBorder(new EmptyBorder(8, 8, 8, 8));
        barraInferior.setBackground(Color.WHITE);

        btnPerfil    = navEmojiButton("🧑‍💼", "Perfil");
        btnBusquedas = navEmojiButton("🔎",  "Búsquedas");
        btnFavoritos = navEmojiButton("⭐",  "Favoritos");
        btnChats     = navEmojiButton("💬",  "Chats");
        btnEmpresa   = navEmojiButton("🏢",  "Mi Empresa");

        btnPerfil.addActionListener(e -> {
            setSelectedTab(btnPerfil);
            subLabel.setText("🧑‍💼 Perfil");
            showPerfil();
        });
        btnBusquedas.addActionListener(e -> {
            setSelectedTab(btnBusquedas);
            subLabel.setText("🔎 Búsquedas");
            cardLayout.show(panelContenido, "BUSQUEDAS");
        });
        btnFavoritos.addActionListener(e -> {
            setSelectedTab(btnFavoritos);
            subLabel.setText("⭐ Favoritos");
            cardLayout.show(panelContenido, "FAVORITOS");
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

    /** Abre la pantalla de "Nuevo anuncio" ocupando TODO el área central (pantalla completa de la app). */
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

    // === PERFIL con SCROLL y mapa (si existe empresa con ubicación)
    private JPanel crearPantallaPerfil() {
        // Contenido real
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(245, 247, 250));

        // ---- Tarjeta: Perfil de usuario
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

        GridBagConstraints gbcU = UIUtils.baseGbc();
        gbcU.gridy = 0; gbcU.insets = new Insets(8, 12, 8, 12); cardUser.add(titleUser, gbcU);
        gbcU.gridy = 1; gbcU.insets = new Insets(4, 12, 8, 12); cardUser.add(gridUser, gbcU);
        gbcU.gridy = 2; gbcU.insets = new Insets(12, 12, 8, 12); cardUser.add(btnLogout, gbcU);

        // ---- Tarjeta: Perfil de empresa
        JPanel cardEmp = createCardPanel();
        JLabel titleEmp = titleLabel("Mi Empresa");

        EmpresaApi empApi = new EmpresaApi();
        Empresa emp = empApi.getEmpresa(safeEmail());

        GridBagConstraints gbcE = UIUtils.baseGbc();
        gbcE.gridy = 0; gbcE.insets = new Insets(8, 12, 8, 12);
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

            gbcE.gridy = 1; gbcE.insets = new Insets(4, 12, 8, 12); cardEmp.add(info, gbcE);
            gbcE.gridy = 2; gbcE.insets = new Insets(12, 12, 12, 12); cardEmp.add(irEmpresa, gbcE);
        } else {
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

            gbcE.gridy = 1; gbcE.insets = new Insets(4, 12, 8, 12); cardEmp.add(gridEmp, gbcE);
            gbcE.gridy = 2; gbcE.insets = new Insets(12, 12, 12, 12); cardEmp.add(btnEditar, gbcE);
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
            wrapC2.gridx = 0; wrapC2.gridy = 2;
            wrapC2.fill = GridBagConstraints.BOTH;
            wrapC2.weightx = 1.0;
            wrapC2.weighty = 0.0;

            wrapper.add(mapCard, wrapC2);
            mapCard.setAddressAsync(emp.getUbicacion(), emp.getEmpresa());
        }

        // === Scroll vertical para no cortar contenido ===
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
        JComboBox<String> cboGeneral = UIUtils.styledCombo(CATEGORIAS_GENERALES);
        gbc.weightx = 0; filtrosCard.add(lblGeneral, gbc);
        gbc.gridx = 1; gbc.weightx = 1; filtrosCard.add(cboGeneral, gbc);

        //Trabajo
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblEspecifico = new JLabel("Trabajo");
        lblEspecifico.setForeground(new Color(20, 40, 80));
        lblEspecifico.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JComboBox<String> cboEspecifico = UIUtils.styledCombo(new String[]{});
        cboEspecifico.setEnabled(false);
        filtrosCard.add(lblEspecifico, gbc);
        gbc.gridx = 1; gbc.weightx = 1; filtrosCard.add(cboEspecifico, gbc);

        //Ubicación
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel lblUbicacion = new JLabel("Ubicacion");
        lblUbicacion.setForeground(new Color(20, 40, 80));
        lblUbicacion.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cboUbicacion = UIUtils.styledCombo(new String[]{"500 m", "1 km", "2 km", "5 km", "10 km"});
        cboUbicacion.setSelectedIndex(1); // por defecto "1 km"
        filtrosCard.add(lblUbicacion, gbc);
        gbc.gridx = 1; gbc.weightx = 1; filtrosCard.add(cboUbicacion, gbc);

        //Calidad
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel lblCalidad = new JLabel("Calidad");
        lblCalidad.setForeground(new Color(20, 40, 80));
        lblCalidad.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cboCalidad = UIUtils.styledCombo(new String[]{"⭐", "⭐⭐", "⭐⭐⭐", "⭐⭐⭐⭐", "⭐⭐⭐⭐⭐"});
        cboCalidad.setSelectedIndex(0);
        filtrosCard.add(lblCalidad, gbc);
        gbc.gridx = 1; gbc.weightx = 1; filtrosCard.add(cboCalidad, gbc);

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

        cboUbicacion.addActionListener(e -> recargarResultados(
                (String) cboGeneral.getSelectedItem(),
                cboEspecifico.isEnabled() ? (String) cboEspecifico.getSelectedItem() : null
        ));

        cboCalidad.addActionListener(e -> recargarResultados(
                (String) cboGeneral.getSelectedItem(),
                cboEspecifico.isEnabled() ? (String) cboEspecifico.getSelectedItem() : null
        ));

        if (cboGeneral.getItemCount() > 0) cboGeneral.setSelectedIndex(0);
        // primera carga de resultados con los valores por defecto
        recargarResultados((String) cboGeneral.getSelectedItem(),
                cboEspecifico.isEnabled() ? (String) cboEspecifico.getSelectedItem() : null);

        return contenedor;
    }

    private void recargarResultados(String categoria, String trabajo) {
        // Calidad (⭐..⭐⭐⭐⭐⭐) → índice 0..4  => mínimo 1..5
        int calidadMin = cboCalidad.getSelectedIndex() + 1;

        // Radio en km a partir del combo (500 m, 1 km, 2 km, 5 km, 10 km)
        int radioKm;
        switch (cboUbicacion.getSelectedIndex()) {
            case 0 -> radioKm = 0;  // 500 m ~ 0.5 km (ajusta si tu backend usa decimales)
            case 1 -> radioKm = 1;
            case 2 -> radioKm = 2;
            case 3 -> radioKm = 5;
            default -> radioKm = 10;
        }

        String origen = null;

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

                return busquedasCtrl.buscar(categoria, trabajo, calidadMin, origen, radioKm);
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

    private void mostrarFormularioEmpresa(Empresa emp) {
        JTextField txtNombre = UIUtils.styledTextField(22);
        JTextField txtNif    = UIUtils.styledTextField(22);
        JComboBox<String> cboSector = UIUtils.styledCombo(CATEGORIAS_GENERALES);
        JTextField txtUbicacion = UIUtils.styledTextField(22);

        if (emp != null) {
            txtNombre.setText(emp.getEmpresa());
            txtNif.setText(emp.getNif());
            cboSector.setSelectedItem(emp.getSector());
            txtUbicacion.setText(emp.getUbicacion());
        } else if (cboSector.getItemCount()>0) {
            cboSector.setSelectedIndex(0);
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12,12,12,12));
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = UIUtils.baseGbc();
        gbc.gridy=0; form.add(new JLabel("Nombre de la empresa:"), gbc);
        gbc.gridy=1; form.add(txtNombre, gbc);
        gbc.gridy=2; form.add(new JLabel("NIF/CIF:"), gbc);
        gbc.gridy=3; form.add(txtNif, gbc);
        gbc.gridy=4; form.add(new JLabel("Sector:"), gbc);
        gbc.gridy=5; form.add(cboSector, gbc);
        gbc.gridy=6; form.add(new JLabel("Ubicación:"), gbc);
        gbc.gridy=7; form.add(txtUbicacion, gbc);

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

            EmpresaApi api = new EmpresaApi();
            boolean ok = api.saveEmpresa(safeEmail(), nombre, nif, sector, ubic);
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
        JButton[] all = {btnPerfil, btnBusquedas, btnFavoritos, btnChats, btnEmpresa};
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
        try { return (String) currentUser.getClass().getMethod("getEmail").invoke(currentUser); }
        catch (Exception e) { return null; }
    }

    private static String safe(Object s, String def) {
        return (s != null && !String.valueOf(s).isBlank()) ? String.valueOf(s) : def;
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

    // --------- Tarjeta de resultado con GridBagLayout ----------
    private JPanel crearTarjetaResultado(Anuncio anuncio) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(12, new Color(220, 230, 245)),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Altura MÍNIMA de 130px, ancho suficiente para que NUNCA se monten los botones
        card.setPreferredSize(new Dimension(900, 130));
        card.setMinimumSize(new Dimension(600, 130));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

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
        String cat = anuncio.getCategoria() != null ? anuncio.getCategoria() : "";
        String spec = anuncio.getEspecificacion() != null ? anuncio.getEspecificacion() : "";
        String linea = (spec.isBlank() ? cat : (cat + " · " + spec));

        JLabel lblCategoria = new JLabel(linea);
        lblCategoria.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblCategoria.setForeground(new Color(80, 120, 200));
        lblCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Descripción (título principal)
        String desc = anuncio.getDescripcion() != null ? anuncio.getDescripcion() : "";
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
        String precioStr = (anuncio.getPrecio() != null) ? String.format("%.2f €", anuncio.getPrecio()) : "";
        JLabel lblPrecio = new JLabel(precioStr);
        lblPrecio.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblPrecio.setForeground(new Color(20, 120, 80));
        lblPrecio.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightPanel.add(lblPrecio);
        rightPanel.add(Box.createVerticalStrut(10));

        // Botón Ver detalles
        JButton btnDetalles = UIUtils.primaryButton("Ver detalles");
        btnDetalles.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnDetalles.setPreferredSize(new Dimension(145, 34));
        btnDetalles.setMinimumSize(new Dimension(145, 34));
        btnDetalles.setMaximumSize(new Dimension(145, 34));
        btnDetalles.addActionListener(e -> showDetalleAnuncio(anuncio));
        rightPanel.add(btnDetalles);
        rightPanel.add(Box.createVerticalStrut(8));

        // Botón Chatear (solo si no es el propio anuncio)
        boolean esPropio = anuncio.getEmpresaNif() != null && anuncio.getEmpresaNif().equals(obtenerNifEmpresaActual());

        // DEBUG: Ver por qué no aparece el botón
        System.out.println("DEBUG Anuncio ID: " + anuncio.getId());
        System.out.println("  - NIF Anuncio: " + anuncio.getEmpresaNif());
        System.out.println("  - NIF Usuario actual: " + obtenerNifEmpresaActual());
        System.out.println("  - Es propio: " + esPropio);
        System.out.println("  - Email empresa: " + anuncio.getEmpresaEmail());
        System.out.println("  - Mostrar botón: " + (!esPropio && anuncio.getEmpresaEmail() != null));

        if (!esPropio && anuncio.getEmpresaEmail() != null) {
            JButton btnChat = UIUtils.secondaryButton("💬 Chatear");
            btnChat.setAlignmentX(Component.RIGHT_ALIGNMENT);
            btnChat.setPreferredSize(new Dimension(145, 34));
            btnChat.setMinimumSize(new Dimension(145, 34));
            btnChat.setMaximumSize(new Dimension(145, 34));
            btnChat.setFont(new Font("SansSerif", Font.BOLD, 12));
            btnChat.addActionListener(e -> iniciarChatConAnuncio(anuncio));
            rightPanel.add(btnChat);
            System.out.println("  → ✅ BOTÓN AGREGADO");
        } else {
            System.out.println("  → ❌ BOTÓN NO AGREGADO");
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
            ChatControler chatCtrl = new ChatControler();
            Chat chat = chatCtrl.getOrCreateChat(currentUser.getEmail(), a.getEmpresaEmail(), a.getId());

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
            gbc.gridy=0; card.add(new JLabel("Descripción"), gbc);
            gbc.gridy=1; card.add(txtDescripcion, gbc);

            gbc.gridy=2; card.add(new JLabel("Precio (€)"), gbc);
            gbc.gridy=3; card.add(txtPrecio, gbc);

            gbc.gridy=4; card.add(new JLabel("Categoría"), gbc);
            gbc.gridy=5; card.add(cboCategoria, gbc);

            gbc.gridy=6; card.add(new JLabel("Trabajo"), gbc);
            cboEspecificacion.setEnabled(false);
            gbc.gridy=7; card.add(cboEspecificacion, gbc);

            gbc.gridy=8; card.add(new JLabel("Ubicación"), gbc);
            gbc.gridy=9; card.add(txtUbicacion, gbc);

            gbc.gridy=10; card.add(new JLabel("NIF Empresa"), gbc);
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
            String esp   = cboEspecificacion.isEnabled()? (String) cboEspecificacion.getSelectedItem() : null;
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
}
