package icai.dtc.isw.ui;

import icai.dtc.isw.domain.Pago;
import icai.dtc.isw.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel dentro de "Mi perfil" para mostrar / añadir / editar / borrar
 * el método de pago del usuario.
 */
public class MetodoPagoPanel extends JPanel {

    private final User currentUser;
    private final PagoApi pagoApi = new PagoApi();

    private Pago pagoActual;   // método de pago guardado (puede ser null)

    public MetodoPagoPanel(User currentUser) {
        this.currentUser = currentUser;
        setOpaque(false);
        setLayout(new BorderLayout());
        cargarPagoInicial();
    }

    /** Carga el método de pago desde el servidor y pinta la UI */
    private void cargarPagoInicial() {
        try {
            pagoActual = pagoApi.getMetodoPago(currentUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        renderUI();
    }

    /** Reconstruye el contenido del panel según haya o no pago guardado */
    private void renderUI() {
        removeAll();

        // Contenedor vertical centrado
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(8, 8, 8, 8));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Título
        JLabel titulo = new JLabel("Método de pago");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        titulo.setForeground(new Color(20, 40, 80));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(titulo);
        content.add(Box.createVerticalStrut(6));

        if (pagoActual == null) {
            //  No hay método de pago guardado
            JLabel info = new JLabel("No tienes ningún método de pago guardado.");
            info.setForeground(new Color(95, 105, 125));
            info.setFont(new Font("SansSerif", Font.PLAIN, 12));
            info.setAlignmentX(Component.CENTER_ALIGNMENT);
            info.setHorizontalAlignment(SwingConstants.CENTER);

            content.add(info);
            content.add(Box.createVerticalStrut(8));

            JButton btnAdd = UIUtils.primaryButton("Añadir método de pago");
            btnAdd.setPreferredSize(new Dimension(200, 32));
            btnAdd.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnAdd.addActionListener(e -> mostrarDialogoPago(null));

            content.add(btnAdd);

        } else {
            //  Mostrar información de la tarjeta, centrada
            JPanel l1 = labelLinea("Titular: ", safe(pagoActual.getNombre(), "-"));
            JPanel l2 = labelLinea("Número: ", safe(pagoActual.getNumeroTarjeta(), "-"));
            JPanel l3 = labelLinea("Caducidad: ", safe(pagoActual.getFechaCaducidad(), "-"));
            JPanel l4 = labelLinea("CVV: ", "***"); // CVV siempre oculto

            content.add(l1);
            content.add(Box.createVerticalStrut(2));
            content.add(l2);
            content.add(Box.createVerticalStrut(2));
            content.add(l3);
            content.add(Box.createVerticalStrut(2));
            content.add(l4);
            content.add(Box.createVerticalStrut(8));

            // Botones editar / borrar centrados
            JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
            botones.setOpaque(false);
            botones.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton btnEditar = UIUtils.secondaryButton("Editar");
            btnEditar.setPreferredSize(new Dimension(90, 30));
            btnEditar.addActionListener(e -> mostrarDialogoPago(pagoActual));

            JButton btnBorrar = UIUtils.dangerButton("Borrar");
            btnBorrar.setPreferredSize(new Dimension(90, 30));
            btnBorrar.addActionListener(e -> borrarMetodoPago());

            botones.add(btnEditar);
            botones.add(btnBorrar);

            content.add(botones);
        }

        add(content, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel labelLinea(String k, String v) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lk = new JLabel(k);
        lk.setForeground(new Color(95, 105, 125));
        lk.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel lv = new JLabel(v);
        lv.setForeground(new Color(30, 33, 40));
        lv.setFont(new Font("SansSerif", Font.PLAIN, 12));

        p.add(lk);
        p.add(lv);
        return p;
    }

    private String safe(String s, String def) {
        return (s != null && !s.isBlank()) ? s : def;
    }

    /** Diálogo para añadir o editar método de pago */
    private void mostrarDialogoPago(Pago pagoExistente) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        String tituloVentana = (pagoExistente == null) ?
                "Añadir método de pago" :
                "Editar método de pago";

        JDialog dialog = new JDialog(parent, tituloVentana, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        //  ROOT
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        //  HEADER
        JLabel titulo = new JLabel(tituloVentana, SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(new Color(20, 40, 80));
        titulo.setBorder(new EmptyBorder(0, 0, 12, 0));
        root.add(titulo, BorderLayout.NORTH);

        //  CARD CENTRAL
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(new UIUtils.RoundedBorder(16, new Color(230, 235, 245)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 16, 2, 16);

        JTextField titularField = UIUtils.styledTextField(22);
        JTextField numeroField  = UIUtils.styledTextField(22);
        JTextField fechaField   = UIUtils.styledTextField(10);
        JTextField cvvField     = UIUtils.styledTextField(6);

        if (pagoExistente != null) {
            titularField.setText(safe(pagoExistente.getNombre(), ""));
            numeroField.setText(safe(pagoExistente.getNumeroTarjeta(), ""));
            fechaField.setText(safe(pagoExistente.getFechaCaducidad(), ""));
            cvvField.setText(safe(pagoExistente.getCvv(), ""));
        }

        int row = 0;
        gbc.gridy = row++; card.add(labelCampo("Titular de la tarjeta:"), gbc);
        gbc.gridy = row++; card.add(titularField, gbc);

        gbc.gridy = row++; card.add(labelCampo("Número de tarjeta:"), gbc);
        gbc.gridy = row++; card.add(numeroField, gbc);

        gbc.gridy = row++; card.add(labelCampo("Fecha de caducidad (MM/YY):"), gbc);
        gbc.gridy = row++; card.add(fechaField, gbc);

        gbc.gridy = row++; card.add(labelCampo("CVV:"), gbc);
        gbc.gridy = row++; card.add(cvvField, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(10, 16, 12, 16);
        card.add(Box.createVerticalStrut(8), gbc);

        root.add(card, BorderLayout.CENTER);

        //  FOOTER
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton cancelarBtn = UIUtils.secondaryButton("Cancelar");
        JButton guardarBtn  = UIUtils.primaryButton("Guardar");

        cancelarBtn.addActionListener(ev -> dialog.dispose());

        guardarBtn.addActionListener(ev -> {
            String titular = titularField.getText().trim();
            String numero  = numeroField.getText().trim();
            String fecha   = fechaField.getText().trim();
            String cvv     = cvvField.getText().trim();

            // Validación de campos vacíos
            if (titular.isEmpty() || numero.isEmpty() || fecha.isEmpty() || cvv.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Todos los campos son obligatorios.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            //  Validación número de tarjeta: 16 dígitos
            String numeroSinEspacios = numero.replaceAll("\\s+", "");
            if (!numeroSinEspacios.matches("\\d{16}")) {
                JOptionPane.showMessageDialog(dialog,
                        "El número de tarjeta debe contener exactamente 16 dígitos.",
                        "Número de tarjeta no válido",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            //  Validación fecha: formato MM/YY con mes 01-12
            if (!fecha.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                JOptionPane.showMessageDialog(dialog,
                        "La fecha de caducidad debe tener el formato MM/YY y un mes válido (01-12).",
                        "Fecha de caducidad no válida",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            //  Validación CVV: 3 dígitos
            if (!cvv.matches("\\d{3}")) {
                JOptionPane.showMessageDialog(dialog,
                        "El CVV debe contener exactamente 3 dígitos.",
                        "CVV no válido",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String userIdStr = currentUser.getId();
            Integer userId;
            try {
                userId = Integer.parseInt(userIdStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "El identificador del usuario no es numérico: " + userIdStr,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Guardamos el número sin espacios en BD
            Pago nuevo = new Pago(userId, titular, numeroSinEspacios, fecha, cvv);

            boolean ok = pagoApi.guardarMetodoPago(currentUser, nuevo);
            if (ok) {
                this.pagoActual = nuevo;
                renderUI();
                JOptionPane.showMessageDialog(dialog,
                        "Método de pago guardado correctamente.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "No se ha podido guardar el método de pago.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(cancelarBtn);
        footer.add(guardarBtn);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /** Borrar el método de pago actual */
    private void borrarMetodoPago() {
        int r = JOptionPane.showConfirmDialog(
                this,
                "¿Seguro que quieres eliminar tu método de pago?",
                "Confirmar borrado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (r != JOptionPane.YES_OPTION) return;

        boolean ok = pagoApi.borrarMetodoPago(currentUser);
        if (ok) {
            pagoActual = null;
            renderUI();
            JOptionPane.showMessageDialog(this,
                    "Método de pago eliminado.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se ha podido eliminar el método de pago.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel labelCampo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(20, 40, 80));
        return l;
    }
}
