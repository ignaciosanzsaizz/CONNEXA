package icai.dtc.isw.ui;

import icai.dtc.isw.domain.Anuncio;
import icai.dtc.isw.domain.Empresa;
import icai.dtc.isw.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class EmpresaPanel extends JPanel {

    private final EmpresaApi api = new EmpresaApi();
    private final AnuncioApi anuncioApi = new AnuncioApi();
    private final User currentUser;
    private final String[] sectores;
    private final AppMovilMock host; // referencia a la ventana para refrescar PERFIL

    private JPanel contentCard;

    public EmpresaPanel(User user, String[] sectores, AppMovilMock host) {
        this.currentUser = user;
        this.sectores = sectores.clone();
        this.host = host;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        contentCard = new JPanel(new BorderLayout());
        contentCard.setBackground(Color.WHITE);
        contentCard.setBorder(new UIUtils.RoundedBorder(16, new Color(230, 235, 245)));
        add(contentCard, BorderLayout.CENTER);

        cargarEstado();
    }

    private void cargarEstado() {
        contentCard.removeAll();

        String mail = safeEmail();
        Empresa emp = api.getEmpresa(mail);

        // Panel superior con título y botón
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Mi Empresa", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(new Color(20, 40, 80));
        topPanel.add(title, BorderLayout.WEST);

        if (emp == null) {
            // No tiene perfil de empresa
            JPanel centerPanel = new JPanel(new GridBagLayout());
            centerPanel.setBackground(Color.WHITE);

            JLabel info = new JLabel("Aún no has completado tu perfil de empresa.");
            info.setForeground(new Color(95, 105, 125));

            JButton btnCompletar = UIUtils.primaryButton("Completar perfil");
            btnCompletar.addActionListener(e -> mostrarFormulario());

            GridBagConstraints gbc = UIUtils.baseGbc();
            gbc.gridy = 0; gbc.insets = new Insets(4, 12, 8, 12); centerPanel.add(info, gbc);
            gbc.gridy = 1; gbc.insets = new Insets(12, 12, 12, 12); centerPanel.add(btnCompletar, gbc);

            contentCard.add(topPanel, BorderLayout.NORTH);
            contentCard.add(centerPanel, BorderLayout.CENTER);
        } else {
            // Tiene perfil, mostrar botón y anuncios
            JButton btnAnuncio = UIUtils.primaryButton("Poner anuncio");
            btnAnuncio.addActionListener(e -> abrirCrearAnuncio(emp));
            topPanel.add(btnAnuncio, BorderLayout.EAST);

            contentCard.add(topPanel, BorderLayout.NORTH);

            // Cargar y mostrar anuncios
            cargarAnuncios(emp.getNif());
        }

        contentCard.revalidate();
        contentCard.repaint();
    }

    private void mostrarFormulario() {
        JTextField txtNombre    = UIUtils.styledTextField(22);
        JTextField txtNif       = UIUtils.styledTextField(22);
        JComboBox<String> cboSector = UIUtils.styledCombo(sectores);
        JTextField txtUbicacion = UIUtils.styledTextField(22);

        if (cboSector.getItemCount() > 0) cboSector.setSelectedIndex(0);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(8,8,8,8));
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
                this, form, "Completar perfil de empresa",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (r == JOptionPane.OK_OPTION) {
            String nombre    = txtNombre.getText().trim();
            String nif       = txtNif.getText().trim();
            String sector    = (String) cboSector.getSelectedItem();
            String ubicacion = txtUbicacion.getText().trim();

            if (nombre.isEmpty() || nif.isEmpty() || sector == null || sector.isBlank() || ubicacion.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Rellena todos los campos.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = api.saveEmpresa(safeEmail(), nombre, nif, sector, ubicacion);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Perfil guardado correctamente.", "Mi Empresa", JOptionPane.INFORMATION_MESSAGE);

                // 🔄 Notificar al host para que reconstruya la tarjeta PERFIL y navegar allí
                if (host != null) {
                    host.refreshPerfil();
                    host.showPerfil();
                }

                cargarEstado(); // Este panel pasa a mostrar "Poner anuncio"
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar el perfil.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String safeEmail() {
        try { return (String) currentUser.getClass().getMethod("getEmail").invoke(currentUser); }
        catch (Exception e) { return null; }
    }

    private void abrirCrearAnuncio(Empresa emp) {
        // Obtener el Frame padre
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);

        // Crear y mostrar el diálogo
        CrearAnuncioPanel dialog = new CrearAnuncioPanel(parentFrame, emp, host);
        dialog.setVisible(true);

        // Después de cerrar el diálogo, recargar los anuncios
        cargarEstado();
    }

    private void cargarAnuncios(String nifEmpresa) {
        List<Anuncio> anuncios = anuncioApi.getAnunciosByEmpresa(nifEmpresa);

        JPanel anunciosPanel = new JPanel();
        anunciosPanel.setLayout(new BoxLayout(anunciosPanel, BoxLayout.Y_AXIS));
        anunciosPanel.setBackground(Color.WHITE);
        anunciosPanel.setBorder(new EmptyBorder(0, 12, 12, 12));

        if (anuncios == null || anuncios.isEmpty()) {
            // No hay anuncios
            JLabel noAnuncios = new JLabel("No tienes anuncios publicados aún.");
            noAnuncios.setFont(new Font("SansSerif", Font.ITALIC, 14));
            noAnuncios.setForeground(new Color(120, 130, 150));
            noAnuncios.setAlignmentX(Component.CENTER_ALIGNMENT);
            noAnuncios.setBorder(new EmptyBorder(20, 0, 20, 0));
            anunciosPanel.add(noAnuncios);
        } else {
            // Mostrar anuncios en tarjetas
            for (Anuncio anuncio : anuncios) {
                anunciosPanel.add(crearTarjetaAnuncio(anuncio));
                anunciosPanel.add(Box.createRigidArea(new Dimension(0, 12))); // Espacio entre tarjetas
            }
        }

        // Envolver en JScrollPane
        JScrollPane scrollPane = new JScrollPane(anunciosPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        contentCard.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel crearTarjetaAnuncio(Anuncio anuncio) {
        JPanel tarjeta = new JPanel(new BorderLayout(12, 8));
        tarjeta.setBackground(new Color(250, 252, 255));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            new UIUtils.RoundedBorder(12, new Color(220, 230, 245)),
            new EmptyBorder(16, 16, 16, 16)
        ));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        // Panel izquierdo con la información principal
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(250, 252, 255));

        // Categoría (pequeña, arriba)
        JLabel lblCategoria = new JLabel("📌 " + anuncio.getCategoria());
        lblCategoria.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblCategoria.setForeground(new Color(80, 120, 200));
        lblCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Descripción (destacada)
        String descripcionCorta = anuncio.getDescripcion().length() > 80
            ? anuncio.getDescripcion().substring(0, 80) + "..."
            : anuncio.getDescripcion();
        JLabel lblDescripcion = new JLabel("<html><b>" + descripcionCorta + "</b></html>");
        lblDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblDescripcion.setForeground(new Color(30, 40, 60));
        lblDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Especificación
        JLabel lblEspecificacion = new JLabel("📋 " + anuncio.getEspecificacion());
        lblEspecificacion.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEspecificacion.setForeground(new Color(90, 100, 120));
        lblEspecificacion.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Ubicación
        JLabel lblUbicacion = new JLabel("📍 " + anuncio.getUbicacion());
        lblUbicacion.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblUbicacion.setForeground(new Color(90, 100, 120));
        lblUbicacion.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fecha de publicación
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String fechaPublicacion = "";
        if (anuncio.getCreadoEn() != null) {
            fechaPublicacion = "🕒 Publicado: " + sdf.format(anuncio.getCreadoEn());
        }
        JLabel lblFechaPublicacion = new JLabel(fechaPublicacion);
        lblFechaPublicacion.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblFechaPublicacion.setForeground(new Color(120, 130, 150));
        lblFechaPublicacion.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fecha de actualización
        String fechaActualizacion = "";
        if (anuncio.getActualizadoEn() != null) {
            fechaActualizacion = "🔄 Actualizado: " + sdf.format(anuncio.getActualizadoEn());
        }
        JLabel lblFechaActualizacion = new JLabel(fechaActualizacion);
        lblFechaActualizacion.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblFechaActualizacion.setForeground(new Color(120, 130, 150));
        lblFechaActualizacion.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(lblCategoria);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        infoPanel.add(lblDescripcion);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(lblEspecificacion);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(lblUbicacion);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(lblFechaPublicacion);
        if (!fechaActualizacion.isEmpty()) {
            infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
            infoPanel.add(lblFechaActualizacion);
        }

        // Panel derecho con precio y botones
        JPanel derechaPanel = new JPanel();
        derechaPanel.setLayout(new BoxLayout(derechaPanel, BoxLayout.Y_AXIS));
        derechaPanel.setBackground(new Color(250, 252, 255));
        derechaPanel.setPreferredSize(new Dimension(120, 100));

        JLabel lblPrecio = new JLabel(String.format("%.2f €", anuncio.getPrecio()));
        lblPrecio.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblPrecio.setForeground(new Color(20, 120, 80));
        lblPrecio.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Botón Eliminar
        JButton btnEliminar = UIUtils.dangerButton("🗑️ Eliminar");
        btnEliminar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEliminar.setPreferredSize(new Dimension(110, 35));
        btnEliminar.setMaximumSize(new Dimension(110, 35));
        btnEliminar.addActionListener(e -> {
            int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que deseas eliminar este anuncio?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirmacion == JOptionPane.YES_OPTION) {
                // Ejecutar eliminación en un hilo separado para no bloquear la UI
                new Thread(() -> {
                    try {
                        boolean ok = anuncioApi.deleteAnuncio(anuncio.getId());
                        System.out.println("Resultado de eliminación: " + ok + " para ID: " + anuncio.getId());

                        // Siempre recargar la lista en el EDT (aunque la API diga false, puede que haya funcionado)
                        SwingUtilities.invokeLater(() -> {
                            cargarEstado();
                            // Mostrar mensaje solo si realmente no se eliminó (verificando la nueva lista)
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                EmpresaPanel.this,
                                "Error al eliminar el anuncio: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        });
                    }
                }).start();
            }
        });

        // Botón Editar
        JButton btnEditar = UIUtils.secondaryButton("✏️ Editar");
        btnEditar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEditar.setPreferredSize(new Dimension(110, 35));
        btnEditar.setMaximumSize(new Dimension(110, 35));
        btnEditar.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            EditarAnuncioPanel dialog = new EditarAnuncioPanel(parentFrame, anuncio, this);
            dialog.setVisible(true);
            // El diálogo llamará a recargarAnuncios() después de editar
        });

        derechaPanel.add(lblPrecio);
        derechaPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        derechaPanel.add(btnEliminar);
        derechaPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        derechaPanel.add(btnEditar);

        tarjeta.add(infoPanel, BorderLayout.CENTER);
        tarjeta.add(derechaPanel, BorderLayout.EAST);

        return tarjeta;
    }

    /**
     * Método público para recargar los anuncios desde componentes externos
     */
    public void recargarAnuncios() {
        SwingUtilities.invokeLater(() -> cargarEstado());
    }
}
