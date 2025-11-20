package icai.dtc.isw.ui;

import icai.dtc.isw.domain.Contratacion;
import icai.dtc.isw.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class ContratacionesPanel extends JPanel {

    private final ContratacionApi api = new ContratacionApi();
    private final User currentUser;
    private JPanel contenedorLista;

    public ContratacionesPanel(User currentUser) {
        this.currentUser = currentUser;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Título
        JLabel titulo = new JLabel("Mis Contrataciones", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(new Color(20, 40, 80));
        titulo.setBorder(new EmptyBorder(8, 0, 12, 0));

        // Contenedor de la lista
        contenedorLista = new JPanel();
        contenedorLista.setLayout(new BoxLayout(contenedorLista, BoxLayout.Y_AXIS));
        contenedorLista.setBackground(new Color(245, 247, 250));

        JScrollPane scroll = new JScrollPane(contenedorLista);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(titulo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        cargarContrataciones();
    }

    public void cargarContrataciones() {
        contenedorLista.removeAll();

        Integer idUser = obtenerIdUser();
        if (idUser == null) {
            JLabel error = new JLabel("Error: No se pudo obtener el ID del usuario", SwingConstants.CENTER);
            error.setForeground(new Color(170, 60, 60));
            contenedorLista.add(error);
            contenedorLista.revalidate();
            contenedorLista.repaint();
            return;
        }

        JLabel loading = new JLabel("⏳ Cargando contrataciones...", SwingConstants.CENTER);
        loading.setFont(new Font("SansSerif", Font.ITALIC, 14));
        loading.setForeground(new Color(100, 120, 150));
        contenedorLista.add(loading);
        contenedorLista.revalidate();
        contenedorLista.repaint();

        SwingWorker<List<Contratacion>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Contratacion> doInBackground() {
                return api.getContrataciones(idUser);
            }

            @Override
            protected void done() {
                try {
                    List<Contratacion> lista = get();
                    contenedorLista.removeAll();

                    if (lista == null || lista.isEmpty()) {
                        JLabel empty = new JLabel("No tienes contrataciones activas", SwingConstants.CENTER);
                        empty.setForeground(new Color(120, 130, 150));
                        empty.setBorder(new EmptyBorder(12, 16, 12, 16));
                        contenedorLista.add(empty);
                    } else {
                        for (Contratacion c : lista) {
                            JPanel tarjeta = crearTarjetaContratacion(c);
                            contenedorLista.add(tarjeta);
                            contenedorLista.add(Box.createRigidArea(new Dimension(0, 12)));
                        }
                    }
                    contenedorLista.revalidate();
                    contenedorLista.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    contenedorLista.removeAll();
                    JLabel err = new JLabel("Error al cargar contrataciones: " + ex.getMessage(), SwingConstants.CENTER);
                    err.setForeground(new Color(170, 60, 60));
                    err.setBorder(new EmptyBorder(12, 16, 12, 16));
                    contenedorLista.add(err);
                    contenedorLista.revalidate();
                    contenedorLista.repaint();
                }
            }
        };
        worker.execute();
    }

    private JPanel crearTarjetaContratacion(Contratacion c) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new UIUtils.RoundedBorder(12, new Color(220, 230, 245)),
            new EmptyBorder(16, 16, 16, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Panel izquierdo: Información
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel lblEmpresa = new JLabel("🏢 " + c.getNombreEmpresa());
        lblEmpresa.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblEmpresa.setForeground(new Color(20, 40, 80));
        lblEmpresa.setAlignmentX(Component.LEFT_ALIGNMENT);

        String descripcion = c.getDescripcionAnuncio();
        if (descripcion != null && descripcion.length() > 60) {
            descripcion = descripcion.substring(0, 60) + "...";
        }
        JLabel lblDescripcion = new JLabel(descripcion);
        lblDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblDescripcion.setForeground(new Color(60, 70, 90));
        lblDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCategoria = new JLabel("📌 " + c.getCategoriaAnuncio());
        lblCategoria.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblCategoria.setForeground(new Color(90, 100, 120));
        lblCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String fecha = c.getFechaContratacion() != null ?
            "📅 Contratado: " + sdf.format(c.getFechaContratacion()) : "";
        JLabel lblFecha = new JLabel(fecha);
        lblFecha.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblFecha.setForeground(new Color(120, 130, 150));
        lblFecha.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(lblEmpresa);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(lblDescripcion);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(lblCategoria);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(lblFecha);

        // Panel derecho: Estado y acciones
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(150, 100));

        // Etiqueta de estado
        JLabel lblEstado = crearEtiquetaEstado(c.getEstado());
        lblEstado.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(lblEstado);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Mostrar valoración si existe
        if (c.isValorado() && c.getCalidad() != null) {
            String estrellas = "⭐".repeat(Math.round(c.getCalidad()));
            JLabel lblCalidad = new JLabel(estrellas);
            lblCalidad.setFont(new Font("SansSerif", Font.PLAIN, 16));
            lblCalidad.setAlignmentX(Component.CENTER_ALIGNMENT);
            rightPanel.add(lblCalidad);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        // Botones según el estado
        if (c.isActivo()) {
            JButton btnTerminar = UIUtils.dangerButton("Terminar contrato");
            btnTerminar.setPreferredSize(new Dimension(140, 35));
            btnTerminar.setMinimumSize(new Dimension(140, 35));
            btnTerminar.setMaximumSize(new Dimension(140, 35));
            btnTerminar.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnTerminar.addActionListener(e -> terminarContrato(c));
            rightPanel.add(btnTerminar);
        } else if (c.isTerminado()) {
            JButton btnValorar = UIUtils.primaryButton("✨ Valorar");
            btnValorar.setPreferredSize(new Dimension(140, 35));
            btnValorar.setMinimumSize(new Dimension(140, 35));
            btnValorar.setMaximumSize(new Dimension(140, 35));
            btnValorar.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnValorar.addActionListener(e -> mostrarDialogoValoracion(c));
            rightPanel.add(btnValorar);
        }

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }

    private JLabel crearEtiquetaEstado(String estado) {
        JLabel lbl = new JLabel();
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setBorder(new EmptyBorder(4, 12, 4, 12));
        lbl.setOpaque(true);

        switch (estado) {
            case "activo":
                lbl.setText("🟢 ACTIVO");
                lbl.setBackground(new Color(220, 255, 220));
                lbl.setForeground(new Color(0, 120, 0));
                break;
            case "terminado":
                lbl.setText("🟡 TERMINADO");
                lbl.setBackground(new Color(255, 245, 200));
                lbl.setForeground(new Color(180, 120, 0));
                break;
            case "valorado":
                lbl.setText("⭐ VALORADO");
                lbl.setBackground(new Color(220, 235, 255));
                lbl.setForeground(new Color(40, 80, 180));
                break;
            default:
                lbl.setText(estado.toUpperCase());
                lbl.setBackground(new Color(240, 240, 240));
                lbl.setForeground(new Color(100, 100, 100));
        }

        return lbl;
    }

    private void terminarContrato(Contratacion c) {
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Estás seguro de que deseas terminar este contrato?\n" +
            "Empresa: " + c.getNombreEmpresa() + "\n" +
            "Después podrás valorar el servicio.",
            "Confirmar terminación de contrato",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return api.terminarContrato(c.getNifEmpresa(), c.getIdUser(), c.getIdAnuncio());
                }

                @Override
                protected void done() {
                    try {
                        Boolean ok = get();
                        if (Boolean.TRUE.equals(ok)) {
                            JOptionPane.showMessageDialog(
                                ContratacionesPanel.this,
                                "Contrato terminado. Ahora puedes valorar el servicio.",
                                "Contrato terminado",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            cargarContrataciones();
                        } else {
                            JOptionPane.showMessageDialog(
                                ContratacionesPanel.this,
                                "No se pudo terminar el contrato. Inténtalo de nuevo.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(
                            ContratacionesPanel.this,
                            "Error: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            worker.execute();
        }
    }

    private void mostrarDialogoValoracion(Contratacion c) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = UIUtils.baseGbc();

        // Título
        JLabel titulo = new JLabel("Valora tu experiencia con " + c.getNombreEmpresa());
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        titulo.setForeground(new Color(20, 40, 80));
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titulo, gbc);

        // Estrellas
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Calidad (estrellas):"), gbc);

        JPanel estrellasPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        estrellasPanel.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        JRadioButton[] estrellas = new JRadioButton[5];

        for (int i = 0; i < 5; i++) {
            final int valor = i + 1;
            estrellas[i] = new JRadioButton("⭐".repeat(valor));
            estrellas[i].setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            estrellas[i].setOpaque(false);
            group.add(estrellas[i]);
            estrellasPanel.add(estrellas[i]);
        }
        estrellas[4].setSelected(true); // 5 estrellas por defecto

        gbc.gridx = 1;
        panel.add(estrellasPanel, gbc);

        // Comentario
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Comentario (opcional):"), gbc);

        JTextArea txtComentario = new JTextArea(4, 25);
        txtComentario.setLineWrap(true);
        txtComentario.setWrapStyleWord(true);
        txtComentario.setBorder(BorderFactory.createCompoundBorder(
            new UIUtils.RoundedBorder(8, new Color(220, 226, 235)),
            new EmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane scrollComentario = new JScrollPane(txtComentario);
        scrollComentario.setBorder(null);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(scrollComentario, gbc);

        int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Valorar servicio",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            // Obtener la calificación seleccionada
            float calidad = 5.0f;
            for (int i = 0; i < 5; i++) {
                if (estrellas[i].isSelected()) {
                    calidad = i + 1;
                    break;
                }
            }

            String comentario = txtComentario.getText().trim();
            if (comentario.isEmpty()) {
                comentario = null;
            }

            final float calidadFinal = calidad;
            final String comentarioFinal = comentario;

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return api.valorarContratacion(
                        c.getNifEmpresa(),
                        c.getIdUser(),
                        c.getIdAnuncio(),
                        calidadFinal,
                        comentarioFinal
                    );
                }

                @Override
                protected void done() {
                    try {
                        Boolean ok = get();
                        if (Boolean.TRUE.equals(ok)) {
                            JOptionPane.showMessageDialog(
                                ContratacionesPanel.this,
                                "¡Gracias por tu valoración!\nLa calidad de la empresa se ha actualizado.",
                                "Valoración guardada",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            cargarContrataciones();
                        } else {
                            JOptionPane.showMessageDialog(
                                ContratacionesPanel.this,
                                "No se pudo guardar la valoración. Inténtalo de nuevo.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(
                            ContratacionesPanel.this,
                            "Error: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            worker.execute();
        }
    }

    private Integer obtenerIdUser() {
        try {
            Object id = currentUser.getClass().getMethod("getId").invoke(currentUser);
            if (id instanceof String) {
                String idStr = (String) id;
                if (idStr == null || idStr.isEmpty() || idStr.isBlank()) {
                    // Si no hay ID, usamos el hash del email
                    try {
                        String email = (String) currentUser.getClass().getMethod("getEmail").invoke(currentUser);
                        if (email != null) {
                            return Math.abs(email.hashCode());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
            try {
                String email = (String) currentUser.getClass().getMethod("getEmail").invoke(currentUser);
                if (email != null) {
                    return Math.abs(email.hashCode());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

