package icai.dtc.isw.ui;

import icai.dtc.isw.domain.Anuncio;
import icai.dtc.isw.domain.Empresa;
import icai.dtc.isw.domain.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Base64;
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

        // Panel para la foto de perfil
        JLabel lblFotoPreview = new JLabel();
        lblFotoPreview.setPreferredSize(new Dimension(120, 120));
        lblFotoPreview.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 225), 2));
        lblFotoPreview.setHorizontalAlignment(SwingConstants.CENTER);

        // Imagen por defecto
        ImageIcon defaultIcon = createDefaultCompanyIcon(120);
        lblFotoPreview.setIcon(defaultIcon);

        final String[] selectedImageBase64 = {null}; // Para almacenar la imagen seleccionada

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
                        // Convertir a base64
                        selectedImageBase64[0] = imageToBase64(img);
                        // Mostrar preview
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
        form.setBorder(new EmptyBorder(8,8,8,8));
        form.setBackground(Color.WHITE);

        GridBagConstraints gbc = UIUtils.baseGbc();
        gbc.gridy=0; form.add(new JLabel("Foto de perfil:"), gbc);
        gbc.gridy=1; form.add(fotoPanel, gbc);
        gbc.gridy=2; form.add(new JLabel("Nombre de la empresa:"), gbc);
        gbc.gridy=3; form.add(txtNombre, gbc);
        gbc.gridy=4; form.add(new JLabel("NIF/CIF:"), gbc);
        gbc.gridy=5; form.add(txtNif, gbc);
        gbc.gridy=6; form.add(new JLabel("Sector:"), gbc);
        gbc.gridy=7; form.add(cboSector, gbc);
        gbc.gridy=8; form.add(new JLabel("Ubicación:"), gbc);
        gbc.gridy=9; form.add(txtUbicacion, gbc);

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

            // Si no se seleccionó foto, usar la imagen por defecto
            String fotoPerfil = selectedImageBase64[0];
            if (fotoPerfil == null) {
                fotoPerfil = iconToBase64(defaultIcon);
            }

            boolean ok = api.saveEmpresa(safeEmail(), nombre, nif, sector, ubicacion, fotoPerfil);
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
}
