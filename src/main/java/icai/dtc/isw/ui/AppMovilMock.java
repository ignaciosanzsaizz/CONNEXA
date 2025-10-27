package icai.dtc.isw.ui;

import icai.dtc.isw.domain.User;
import icai.dtc.isw.domain.Empresa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.image.BufferedImage;

public class AppMovilMock extends JFrame {

    /** Sectores reutilizados desde las categorías generales de Búsquedas */
    public static final String[] CATEGORIAS_GENERALES = new String[] {
            "Hogar y reparaciones",
            "Salud, belleza y cuidados",
            "Educación y cultura",
            "Eventos y ocio",
            "Negocio y administración",
            "Logística y movilidad",
            "Tecnología y digital"
    };

    /** Trabajos específicos por categoría (para Búsquedas) */
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

    private final JLabel tituloLabel;
    private final CardLayout cardLayout;
    private final JPanel panelContenido;
    private final User currentUser;

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

        // Barra superior
        JPanel barraSuperior = new UIUtils.GradientBar(new Color(10, 23, 42), new Color(20, 40, 80));
        barraSuperior.setLayout(new BorderLayout());
        barraSuperior.setBorder(new EmptyBorder(12, 12, 12, 12));
        tituloLabel = new JLabel("Perfil", SwingConstants.CENTER);
        tituloLabel.setForeground(Color.WHITE);
        tituloLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        barraSuperior.add(tituloLabel, BorderLayout.CENTER);
        add(barraSuperior, BorderLayout.NORTH);

        // Contenido con CardLayout
        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setBackground(new Color(245, 247, 250));

        // Pestañas / pantallas
        panelContenido.add(crearPantallaPerfil(), "PERFIL");
        panelContenido.add(crearPantallaBusquedas(), "BUSQUEDAS");
        panelContenido.add(crearPantalla("Favoritos"), "FAVORITOS");
        panelContenido.add(new ChatsPanel(), "CHATS");
        panelContenido.add(new EmpresaPanel(currentUser, CATEGORIAS_GENERALES), "MI_EMPRESA");

        add(panelContenido, BorderLayout.CENTER);

        // Barra inferior de navegación (5 iconos)
        JPanel barraInferior = new JPanel(new GridLayout(1, 5));
        barraInferior.setBorder(new EmptyBorder(8, 8, 8, 8));
        barraInferior.setBackground(Color.WHITE);

        JButton btnPerfil    = navIconButton("Perfil",     "/icons/MiPerfil.png");
        JButton btnBusquedas = navIconButton("Búsquedas",  "/icons/Busquedas.png");
        JButton btnFavoritos = navIconButton("Favoritos",  "/icons/Favoritos.png");
        JButton btnChats     = navIconButton("Chats",      "/icons/Chats.png");
        JButton btnEmpresa   = navIconButton("Mi Empresa", "/icons/MiEmpresa.png");

        btnPerfil.addActionListener(e -> { tituloLabel.setText("Perfil");        cardLayout.show(panelContenido, "PERFIL"); });
        btnBusquedas.addActionListener(e -> { tituloLabel.setText("Búsquedas");  cardLayout.show(panelContenido, "BUSQUEDAS"); });
        btnFavoritos.addActionListener(e -> { tituloLabel.setText("Favoritos");  cardLayout.show(panelContenido, "FAVORITOS"); });
        btnChats.addActionListener(e -> { tituloLabel.setText("Chats");          cardLayout.show(panelContenido, "CHATS"); });
        btnEmpresa.addActionListener(e -> { tituloLabel.setText("Mi Empresa");   cardLayout.show(panelContenido, "MI_EMPRESA"); });

        barraInferior.add(btnPerfil);
        barraInferior.add(btnBusquedas);
        barraInferior.add(btnFavoritos);
        barraInferior.add(btnChats);
        barraInferior.add(btnEmpresa);

        add(barraInferior, BorderLayout.SOUTH);
    }

    /* ---------------- Pantallas ---------------- */

    private JPanel crearPantalla(String textoCentro) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(245, 247, 250));

        JPanel card = new JPanel();
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(new UIUtils.RoundedBorder(16, new Color(230, 235, 245)));
        card.setLayout(new GridBagLayout());

        JLabel lbl = new JLabel(textoCentro, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lbl.setForeground(new Color(30, 33, 40));

        card.add(lbl);
        wrapper.add(card);
        return wrapper;
    }

    private JPanel crearPantallaPerfil() {
        // Wrapper con dos tarjetas apiladas: datos de usuario y datos de empresa
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(245, 247, 250));

        // ---- Tarjeta: Perfil de usuario
        JPanel cardUser = new JPanel(new GridBagLayout());
        cardUser.setOpaque(true);
        cardUser.setBackground(Color.WHITE);
        cardUser.setBorder(new UIUtils.RoundedBorder(16, new Color(230, 235, 245)));

        JLabel titleUser = new JLabel("Tu perfil", SwingConstants.CENTER);
        titleUser.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleUser.setForeground(new Color(20, 40, 80));

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

        // ---- Tarjeta: Perfil de empresa SOLO AQUÍ (si existe) con botón Editar
        JPanel cardEmp = new JPanel(new GridBagLayout());
        cardEmp.setOpaque(true);
        cardEmp.setBackground(Color.WHITE);
        cardEmp.setBorder(new UIUtils.RoundedBorder(16, new Color(230, 235, 245)));

        JLabel titleEmp = new JLabel("Mi Empresa", SwingConstants.CENTER);
        titleEmp.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleEmp.setForeground(new Color(20, 40, 80));

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
                tituloLabel.setText("Mi Empresa");
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

            addRow(gridEmp, g2, "Empresa", emp.getEmpresa());
            addRow(gridEmp, g2, "NIF/CIF", emp.getNif());
            addRow(gridEmp, g2, "Sector", emp.getSector());
            addRow(gridEmp, g2, "Mail", emp.getMail());

            JButton btnEditar = UIUtils.secondaryButton("Editar perfil");
            btnEditar.addActionListener(e -> mostrarFormularioEmpresa(emp));

            gbcE.gridy = 1; gbcE.insets = new Insets(4, 12, 8, 12); cardEmp.add(gridEmp, gbcE);
            gbcE.gridy = 2; gbcE.insets = new Insets(12, 12, 12, 12); cardEmp.add(btnEditar, gbcE);
        }

        // ---- Añadir ambas tarjetas al wrapper
        GridBagConstraints wrapC = new GridBagConstraints();
        wrapC.insets = new Insets(10, 10, 5, 10);
        wrapC.gridx = 0; wrapC.gridy = 0; wrapC.fill = GridBagConstraints.HORIZONTAL; wrapC.weightx = 1;
        wrapper.add(cardUser, wrapC);
        wrapC.gridy = 1; wrapC.insets = new Insets(5, 10, 10, 10);
        wrapper.add(cardEmp, wrapC);

        return wrapper;
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

    private void mostrarFormularioEmpresa(Empresa emp) {
        // Diálogo de edición reutilizado en Perfil (igual que en EmpresaPanel)
        JTextField txtNombre = UIUtils.styledTextField(22);
        JTextField txtNif    = UIUtils.styledTextField(22);
        JComboBox<String> cboSector = UIUtils.styledCombo(CATEGORIAS_GENERALES);

        if (emp != null) {
            txtNombre.setText(emp.getEmpresa());
            txtNif.setText(emp.getNif());
            cboSector.setSelectedItem(emp.getSector());
        } else {
            if (cboSector.getItemCount() > 0) cboSector.setSelectedIndex(0);
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(8,8,8,8));
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = UIUtils.baseGbc();
        gbc.gridy = 0; form.add(new JLabel("Nombre de la empresa:"), gbc);
        gbc.gridy = 1; form.add(txtNombre, gbc);
        gbc.gridy = 2; form.add(new JLabel("NIF/CIF:"), gbc);
        gbc.gridy = 3; form.add(txtNif, gbc);
        gbc.gridy = 4; form.add(new JLabel("Sector:"), gbc);
        gbc.gridy = 5; form.add(cboSector, gbc);

        int r = JOptionPane.showConfirmDialog(
                this,
                form,
                "Editar perfil de empresa",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (r == JOptionPane.OK_OPTION) {
            String nombre = txtNombre.getText().trim();
            String nif    = txtNif.getText().trim();
            String sector = (String) cboSector.getSelectedItem();

            if (nombre.isEmpty() || nif.isEmpty() || sector == null || sector.isBlank()) {
                JOptionPane.showMessageDialog(this, "Rellena todos los campos.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            EmpresaApi api = new EmpresaApi();
            boolean ok = api.saveEmpresa(safeEmail(), nombre, nif, sector);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Perfil de empresa guardado.", "Mi Empresa", JOptionPane.INFORMATION_MESSAGE);
                // refrescar la pantalla Perfil para mostrar los nuevos datos
                panelContenido.remove(0);
                panelContenido.add(crearPantallaPerfil(), "PERFIL");
                cardLayout.show(panelContenido, "PERFIL");
                tituloLabel.setText("Perfil");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar el perfil.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel crearPantallaBusquedas() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(new Color(245, 247, 250));
        contenedor.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel filtrosCard = new JPanel(new GridBagLayout());
        filtrosCard.setBackground(Color.WHITE);
        filtrosCard.setBorder(new UIUtils.RoundedBorder(16, new Color(230, 235, 245)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel lblGeneral = new JLabel("Categoría");
        lblGeneral.setForeground(new Color(20, 40, 80));
        JComboBox<String> cboGeneral = UIUtils.styledCombo(CATEGORIAS_GENERALES);
        gbc.weightx = 0; filtrosCard.add(lblGeneral, gbc);
        gbc.gridx = 1; gbc.weightx = 1; filtrosCard.add(cboGeneral, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblEspecifico = new JLabel("Trabajo");
        lblEspecifico.setForeground(new Color(20, 40, 80));
        JComboBox<String> cboEspecifico = UIUtils.styledCombo(new String[]{});
        cboEspecifico.setEnabled(false);
        filtrosCard.add(lblEspecifico, gbc);
        gbc.gridx = 1; gbc.weightx = 1; filtrosCard.add(cboEspecifico, gbc);

        JPanel resultadosCard = new JPanel(new BorderLayout());
        resultadosCard.setBackground(Color.WHITE);
        resultadosCard.setBorder(new UIUtils.RoundedBorder(16, new Color(230, 235, 245)));
        resultadosCard.setPreferredSize(new Dimension(320, 420));

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> listaResultados = new JList<>(model);
        listaResultados.setCellRenderer(new UIUtils.CleanListCellRenderer());
        JScrollPane scroll = new JScrollPane(listaResultados);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        resultadosCard.add(scroll, BorderLayout.CENTER);

        contenedor.add(filtrosCard, BorderLayout.NORTH);
        contenedor.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        contenedor.add(resultadosCard, BorderLayout.SOUTH);

        cboGeneral.addActionListener(e -> {
            String general = (String) cboGeneral.getSelectedItem();
            model.clear();
            cboEspecifico.removeAllItems();
            if (general != null && ESPECIFICAS.containsKey(general)) {
                for (String s : ESPECIFICAS.get(general)) cboEspecifico.addItem(s);
                cboEspecifico.setEnabled(true);
                if (cboEspecifico.getItemCount() > 0) cboEspecifico.setSelectedIndex(0);
            } else {
                cboEspecifico.setEnabled(false);
            }
        });

        cboEspecifico.addActionListener(e -> {
            if (!cboEspecifico.isEnabled()) return;
            model.clear();
        });

        if (cboGeneral.getItemCount() > 0) cboGeneral.setSelectedIndex(0);

        return contenedor;
    }

    /* ---------------- Utilidades ---------------- */

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

    /** Crea un botón de navegación solo con icono (sin texto) */
    private JButton navIconButton(String tooltip, String resourcePath) {
        JButton b = new JButton();
        b.setUI(new BasicButtonUI());
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setBorder(new UIUtils.RoundedBorder(12, new Color(220, 226, 235)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setToolTipText(tooltip);

        ImageIcon icon = loadIcon(resourcePath, 75);
        b.setIcon(icon);

        b.setPreferredSize(new Dimension(72, 52));
        return b;
    }

    /** Carga un icono desde /resources con altura targetH y escala proporcional */
    private ImageIcon loadIcon(String path, int targetH) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            // Fallback: botón sin icono
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
}
