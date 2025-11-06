package icai.dtc.isw.ui;

import icai.dtc.isw.domain.Empresa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class CrearAnuncioPanel extends JDialog {

    private final AnuncioApi api = new AnuncioApi();
    private final Empresa empresa;
    private final AppMovilMock host;

    // Categorías permitidas
    private static final String[] CATEGORIAS = {
        "Hogar y reparaciones",
        "Salud, belleza y cuidados",
        "Educación y cultura",
        "Eventos y ocio",
        "Negocio y administración",
        "Logística y movilidad",
        "Tecnología y digital"
    };

    // Mapeo de categorías a especificaciones
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

    // Componentes del formulario
    private JTextArea txtDescripcion;
    private JTextField txtPrecio;
    private JComboBox<String> cboCategoria;
    private JComboBox<String> cboEspecificacion;
    private JTextField txtUbicacion;
    private JTextField txtNifEmpresa;

    public CrearAnuncioPanel(Frame parent, Empresa empresa, AppMovilMock host) {
        super(parent, "Crear Anuncio", true);
        this.empresa = empresa;
        this.host = host;

        initComponents();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));

        // Panel principal con el formulario
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Título
        JLabel titleLabel = new JLabel("Crear nuevo anuncio", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(new Color(20, 40, 80));
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = UIUtils.baseGbc();

        // Descripción (JTextArea)
        gbc.gridy = 0;
        formPanel.add(createLabel("Descripción:"), gbc);

        txtDescripcion = new JTextArea(4, 22);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(12, new Color(220, 226, 235)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        txtDescripcion.setBackground(new Color(250, 251, 253));
        JScrollPane scrollDesc = new JScrollPane(txtDescripcion);
        scrollDesc.setBorder(new UIUtils.RoundedBorder(12, new Color(220, 226, 235)));
        gbc.gridy = 1;
        formPanel.add(scrollDesc, gbc);

        // Precio
        gbc.gridy = 2;
        formPanel.add(createLabel("Precio (€):"), gbc);
        txtPrecio = UIUtils.styledTextField(22);
        gbc.gridy = 3;
        formPanel.add(txtPrecio, gbc);

        // Categoría
        gbc.gridy = 4;
        formPanel.add(createLabel("Categoría:"), gbc);
        cboCategoria = UIUtils.styledCombo(CATEGORIAS);
        gbc.gridy = 5;
        formPanel.add(cboCategoria, gbc);

        // Especificación (JComboBox que depende de categoría)
        gbc.gridy = 6;
        formPanel.add(createLabel("Especificación:"), gbc);
        cboEspecificacion = UIUtils.styledCombo(new String[]{});
        cboEspecificacion.setEnabled(false);
        gbc.gridy = 7;
        formPanel.add(cboEspecificacion, gbc);

        // Listener para actualizar especificaciones cuando cambia la categoría
        cboCategoria.addActionListener(e -> {
            String categoriaSeleccionada = (String) cboCategoria.getSelectedItem();
            cboEspecificacion.removeAllItems();

            if (categoriaSeleccionada != null && ESPECIFICAS.containsKey(categoriaSeleccionada)) {
                for (String especificacion : ESPECIFICAS.get(categoriaSeleccionada)) {
                    cboEspecificacion.addItem(especificacion);
                }
                cboEspecificacion.setEnabled(true);
                if (cboEspecificacion.getItemCount() > 0) {
                    cboEspecificacion.setSelectedIndex(0);
                }
            } else {
                cboEspecificacion.setEnabled(false);
            }
        });

        // Inicializar con la primera categoría
        if (cboCategoria.getItemCount() > 0) {
            cboCategoria.setSelectedIndex(0);
        }

        // Ubicación
        gbc.gridy = 8;
        formPanel.add(createLabel("Ubicación:"), gbc);
        txtUbicacion = UIUtils.styledTextField(22);
        gbc.gridy = 9;
        formPanel.add(txtUbicacion, gbc);

        // NIF Empresa (read-only)
        gbc.gridy = 10;
        formPanel.add(createLabel("NIF Empresa:"), gbc);
        txtNifEmpresa = UIUtils.styledTextField(22);
        txtNifEmpresa.setText(empresa != null ? empresa.getNif() : "");
        txtNifEmpresa.setEditable(false);
        txtNifEmpresa.setBackground(new Color(240, 242, 245));
        gbc.gridy = 11;
        formPanel.add(txtNifEmpresa, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnCrear = UIUtils.primaryButton("Crear anuncio");
        btnCrear.addActionListener(e -> crearAnuncio());

        JButton btnCancelar = UIUtils.secondaryButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        buttonPanel.add(btnCrear);
        buttonPanel.add(btnCancelar);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        pack();
        setMinimumSize(new Dimension(500, 650));
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(60, 70, 90));
        return label;
    }

    private void crearAnuncio() {
        // Obtener valores
        String descripcion = txtDescripcion.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String categoria = (String) cboCategoria.getSelectedItem();
        String especificacion = (String) cboEspecificacion.getSelectedItem();
        String ubicacion = txtUbicacion.getText().trim();
        String nifEmpresa = txtNifEmpresa.getText().trim();

        // Validar campos obligatorios
        if (descripcion.isEmpty()) {
            mostrarError("La descripción es obligatoria.");
            return;
        }

        if (precioStr.isEmpty()) {
            mostrarError("El precio es obligatorio.");
            return;
        }

        // Validar que el precio sea numérico
        Double precio;
        try {
            precio = Double.parseDouble(precioStr);
            if (precio <= 0) {
                mostrarError("El precio debe ser mayor que cero.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarError("El precio debe ser un número válido.");
            return;
        }

        if (categoria == null || categoria.isEmpty()) {
            mostrarError("Debes seleccionar una categoría.");
            return;
        }

        if (especificacion == null || especificacion.isEmpty()) {
            mostrarError("Debes seleccionar una especificación.");
            return;
        }

        if (ubicacion.isEmpty()) {
            mostrarError("La ubicación es obligatoria.");
            return;
        }

        if (nifEmpresa.isEmpty()) {
            mostrarError("El NIF de la empresa no está disponible.");
            return;
        }

        // Intentar crear el anuncio
        try {
            boolean exito = api.createAnuncio(descripcion, precio, categoria,
                                              especificacion, ubicacion, nifEmpresa);

            if (exito) {
                JOptionPane.showMessageDialog(
                    this,
                    "Anuncio creado correctamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Cerrar el diálogo
                dispose();

                // Volver al panel de inicio o actualizar la vista
                if (host != null) {
                    // Puedes navegar a donde sea necesario, por ejemplo:
                    // host.showInicio();
                }
            } else {
                mostrarError("No se pudo crear el anuncio. Inténtalo de nuevo.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError("Error al crear el anuncio: " + ex.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Error de validación",
            JOptionPane.WARNING_MESSAGE
        );
    }
}

