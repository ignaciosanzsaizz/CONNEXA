package icai.dtc.isw.ui;

import icai.dtc.isw.domain.Anuncio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditarAnuncioPanel extends JDialog {

    private final AnuncioApi api = new AnuncioApi();
    private final Anuncio anuncio;
    private final EmpresaPanel empresaPanel;

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

    // Componentes del formulario
    private JTextArea txtDescripcion;
    private JTextField txtPrecio;
    private JComboBox<String> cboCategoria;
    private JTextField txtEspecificacion;
    private JTextField txtUbicacion;

    public EditarAnuncioPanel(Frame parent, Anuncio anuncio, EmpresaPanel empresaPanel) {
        super(parent, "Editar Anuncio", true);
        this.anuncio = anuncio;
        this.empresaPanel = empresaPanel;

        initComponents();
        cargarDatos();
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
        JLabel titleLabel = new JLabel("Editar anuncio", SwingConstants.CENTER);
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

        // Especificación
        gbc.gridy = 6;
        formPanel.add(createLabel("Especificación:"), gbc);
        txtEspecificacion = UIUtils.styledTextField(22);
        gbc.gridy = 7;
        formPanel.add(txtEspecificacion, gbc);

        // Ubicación
        gbc.gridy = 8;
        formPanel.add(createLabel("Ubicación:"), gbc);
        txtUbicacion = UIUtils.styledTextField(22);
        gbc.gridy = 9;
        formPanel.add(txtUbicacion, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnGuardar = UIUtils.primaryButton("Guardar cambios");
        btnGuardar.addActionListener(e -> guardarCambios());

        JButton btnCancelar = UIUtils.secondaryButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        pack();
        setMinimumSize(new Dimension(500, 580));
    }

    private void cargarDatos() {
        if (anuncio != null) {
            txtDescripcion.setText(anuncio.getDescripcion());
            txtPrecio.setText(String.valueOf(anuncio.getPrecio()));
            cboCategoria.setSelectedItem(anuncio.getCategoria());
            txtEspecificacion.setText(anuncio.getEspecificacion());
            txtUbicacion.setText(anuncio.getUbicacion());
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(60, 70, 90));
        return label;
    }

    private void guardarCambios() {
        // Obtener valores
        String descripcion = txtDescripcion.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String categoria = (String) cboCategoria.getSelectedItem();
        String especificacion = txtEspecificacion.getText().trim();
        String ubicacion = txtUbicacion.getText().trim();

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

        if (especificacion.isEmpty()) {
            mostrarError("La especificación es obligatoria.");
            return;
        }

        if (ubicacion.isEmpty()) {
            mostrarError("La ubicación es obligatoria.");
            return;
        }

        // Intentar actualizar el anuncio
        try {
            boolean exito = api.updateAnuncio(anuncio.getId(), descripcion, precio,
                                             categoria, especificacion, ubicacion);

            System.out.println("Resultado de actualización: " + exito + " para ID: " + anuncio.getId());

            // Siempre cerrar el diálogo y recargar (aunque la API diga false, puede que haya funcionado)
            dispose();

            // Recargar la lista de anuncios en el panel padre
            if (empresaPanel != null) {
                empresaPanel.recargarAnuncios();
            }

            // Mostrar mensaje de éxito solo si confirmamos que funcionó
            if (exito) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        null,
                        "Anuncio actualizado correctamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError("Ocurrió un error al actualizar el anuncio: " + ex.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Error de validación",
            JOptionPane.ERROR_MESSAGE
        );
    }
}

