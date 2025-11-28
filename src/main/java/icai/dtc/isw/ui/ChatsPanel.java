package icai.dtc.isw.ui;

import icai.dtc.isw.domain.Chat;
import icai.dtc.isw.domain.MensajeChat;
import icai.dtc.isw.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatsPanel extends JPanel {
    private static final String OFERTA_PREFIX = "[OFERTA]|";
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    private final User currentUser;
    private final ChatApi chatApi = new ChatApi();
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private JPanel listaChatsPanel;
    private JScrollPane scrollListaChats;

    public ChatsPanel(User user) {
        this.currentUser = user;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // CardLayout para cambiar entre lista de chats y ventana de chat individual
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(245, 247, 250));

        add(mainPanel, BorderLayout.CENTER);

        // Crear vista de lista de chats
        crearVistaListaChats();

        // Mostrar lista de chats por defecto
        cardLayout.show(mainPanel, "LISTA");
    }

    private void crearVistaListaChats() {
        JPanel vistaLista = new JPanel(new BorderLayout());
        vistaLista.setBackground(new Color(245, 247, 250));

        // Panel con lista de chats
        listaChatsPanel = new JPanel();
        listaChatsPanel.setLayout(new BoxLayout(listaChatsPanel, BoxLayout.Y_AXIS));
        listaChatsPanel.setBackground(new Color(245, 247, 250));
        listaChatsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollListaChats = new JScrollPane(listaChatsPanel);
        scrollListaChats.setBorder(BorderFactory.createEmptyBorder());
        scrollListaChats.getVerticalScrollBar().setUnitIncrement(16);

        vistaLista.add(scrollListaChats, BorderLayout.CENTER);

        mainPanel.add(vistaLista, "LISTA");

        // Cargar chats
        cargarChats();
    }

    private void cargarChats() {
        listaChatsPanel.removeAll();

        List<Chat> chats = chatApi.getChatsByUser(currentUser.getEmail());

        if (chats == null || chats.isEmpty()) {
            JPanel emptyCard = crearTarjeta();
            JLabel emptyLabel = new JLabel("No tienes conversaciones activas", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            emptyLabel.setForeground(new Color(120, 130, 150));
            emptyCard.add(emptyLabel);
            listaChatsPanel.add(emptyCard);
        } else {
            for (Chat chat : chats) {
                JPanel chatCard = crearTarjetaChat(chat);
                listaChatsPanel.add(chatCard);
                listaChatsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        listaChatsPanel.revalidate();
        listaChatsPanel.repaint();
    }

    private JPanel crearTarjetaChat(Chat chat) {
        JPanel card = crearTarjeta();
        card.setLayout(new BorderLayout(10, 5));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Determinar si el usuario actual es el cliente o la empresa
        boolean esCliente = chat.getClienteEmail().equals(currentUser.getEmail());
        String nombreOtraParte = esCliente ? chat.getEmpresaNombre() : chat.getClienteNombre();

        // Panel izquierdo con avatar
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);

        JLabel avatarLabel = new JLabel(esCliente ? "🏢" : "👤", SwingConstants.CENTER);
        avatarLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));
        avatarLabel.setPreferredSize(new Dimension(50, 50));
        leftPanel.add(avatarLabel, BorderLayout.CENTER);

        // Panel central con información
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel nombreLabel = new JLabel(nombreOtraParte != null ? nombreOtraParte : "Usuario");
        nombreLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nombreLabel.setForeground(new Color(30, 33, 40));

        JLabel anuncioLabel = new JLabel("Anuncio: " + (chat.getAnuncioTitulo() != null ? chat.getAnuncioTitulo() : "N/A"));
        anuncioLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        anuncioLabel.setForeground(new Color(95, 105, 125));

        String resumen = obtenerResumenMensaje(chat.getUltimoMensaje());
        JLabel mensajeLabel = new JLabel(resumen);
        mensajeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        mensajeLabel.setForeground(new Color(70, 80, 100));

        centerPanel.add(nombreLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        centerPanel.add(anuncioLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        centerPanel.add(mensajeLabel);

        card.add(leftPanel, BorderLayout.WEST);
        card.add(centerPanel, BorderLayout.CENTER);

        // Click para abrir el chat
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirChat(chat);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(240, 245, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        return card;
    }

    private void abrirChat(Chat chat) {
        // Crear vista de chat individual
        JPanel vistaChat = crearVistaChat(chat);
        mainPanel.add(vistaChat, "CHAT_" + chat.getId());
        cardLayout.show(mainPanel, "CHAT_" + chat.getId());

        // Marcar mensajes como leídos
        chatApi.marcarMensajesComoLeidos(chat.getId(), currentUser.getEmail());
    }

    private JPanel crearVistaChat(Chat chat) {
        JPanel vistaChat = new JPanel(new BorderLayout());
        vistaChat.setBackground(new Color(245, 247, 250));

        // Header con botón de volver
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(10, 23, 42));
        header.setBorder(new EmptyBorder(12, 12, 12, 12));

        JButton btnVolver = new JButton("← Volver");
        btnVolver.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setBackground(new Color(30, 50, 90));
        btnVolver.setBorderPainted(false);
        btnVolver.setFocusPainted(false);
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> {
            cargarChats(); // Recargar lista
            cardLayout.show(mainPanel, "LISTA");
        });

        boolean esCliente = chat.getClienteEmail().equals(currentUser.getEmail());
        String nombreOtraParte = esCliente ? chat.getEmpresaNombre() : chat.getClienteNombre();

        JLabel headerLabel = new JLabel((esCliente ? "🏢 " : "👤 ") + nombreOtraParte);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        header.add(btnVolver, BorderLayout.WEST);
        header.add(headerLabel, BorderLayout.CENTER);

        // Panel de mensajes con scroll
        JPanel mensajesPanel = new JPanel();
        mensajesPanel.setLayout(new BoxLayout(mensajesPanel, BoxLayout.Y_AXIS));
        mensajesPanel.setBackground(new Color(245, 247, 250));
        mensajesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollMensajes = new JScrollPane(mensajesPanel);
        scrollMensajes.setBorder(BorderFactory.createEmptyBorder());
        scrollMensajes.getVerticalScrollBar().setUnitIncrement(16);

        // Cargar mensajes
        cargarMensajes(chat, mensajesPanel);

        // Panel inferior para escribir mensaje
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextArea txtMensaje = new JTextArea(2, 20);
        txtMensaje.setLineWrap(true);
        txtMensaje.setWrapStyleWord(true);
        txtMensaje.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtMensaje.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 230), 1),
            new EmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane scrollInput = new JScrollPane(txtMensaje);
        scrollInput.setBorder(BorderFactory.createEmptyBorder());

        JButton btnEnviar = new JButton("Enviar");
        btnEnviar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setBackground(new Color(20, 120, 220));
        btnEnviar.setBorderPainted(false);
        btnEnviar.setFocusPainted(false);
        btnEnviar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEnviar.setPreferredSize(new Dimension(110, 40));

        JButton btnOferta = new JButton("Enviar oferta");
        btnOferta.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnOferta.setForeground(Color.WHITE);
        btnOferta.setBackground(new Color(40, 167, 69));
        btnOferta.setBorderPainted(false);
        btnOferta.setFocusPainted(false);
        btnOferta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOferta.setPreferredSize(new Dimension(110, 40));

        btnEnviar.addActionListener(e -> {
            String contenido = txtMensaje.getText().trim();
            if (!contenido.isEmpty()) {
                boolean enviado = chatApi.enviarMensaje(chat.getId(), currentUser.getEmail(), contenido);
                if (enviado) {
                    txtMensaje.setText("");
                    cargarMensajes(chat, mensajesPanel);

                    // Scroll al final
                    SwingUtilities.invokeLater(() -> {
                        JScrollBar vertical = scrollMensajes.getVerticalScrollBar();
                        vertical.setValue(vertical.getMaximum());
                    });
                } else {
                    JOptionPane.showMessageDialog(vistaChat, "Error al enviar mensaje", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnOferta.addActionListener(e -> lanzarOferta(chat, vistaChat, mensajesPanel, scrollMensajes));

        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(btnEnviar);
        buttonsPanel.add(btnOferta);

        inputPanel.add(scrollInput, BorderLayout.CENTER);
        inputPanel.add(buttonsPanel, BorderLayout.EAST);

        vistaChat.add(header, BorderLayout.NORTH);
        vistaChat.add(scrollMensajes, BorderLayout.CENTER);
        vistaChat.add(inputPanel, BorderLayout.SOUTH);

        return vistaChat;
    }

    private void lanzarOferta(Chat chat, JPanel vistaChat, JPanel mensajesPanel, JScrollPane scrollMensajes) {
        JTextField txtPrecio = new JTextField();
        JTextArea txtNota = new JTextArea(3, 20);
        txtNota.setLineWrap(true);
        txtNota.setWrapStyleWord(true);

        JPanel form = new JPanel(new BorderLayout(0, 8));
        form.add(new JLabel("Importe (€)"), BorderLayout.NORTH);
        form.add(txtPrecio, BorderLayout.CENTER);
        JScrollPane notaScroll = new JScrollPane(txtNota);
        notaScroll.setBorder(BorderFactory.createTitledBorder("Notas (opcional)"));
        form.add(notaScroll, BorderLayout.SOUTH);

        int res = JOptionPane.showConfirmDialog(
            vistaChat,
            form,
            "Enviar oferta",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim().replace(",", "."));
            if (precio < 0) throw new NumberFormatException("negative");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vistaChat, "Importe inválido", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nota = txtNota.getText().trim();
        String payload = OFERTA_PREFIX + precio + "|" + nota;

        boolean enviado = chatApi.enviarMensaje(chat.getId(), currentUser.getEmail(), payload);
        if (enviado) {
            cargarMensajes(chat, mensajesPanel);
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollMensajes.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        } else {
            JOptionPane.showMessageDialog(vistaChat, "Error al enviar la oferta", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarMensajes(Chat chat, JPanel mensajesPanel) {
        mensajesPanel.removeAll();

        List<MensajeChat> mensajes = chatApi.getMensajesByChat(chat.getId());

        if (mensajes == null || mensajes.isEmpty()) {
            JLabel emptyLabel = new JLabel("No hay mensajes. ¡Sé el primero en escribir!");
            emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
            emptyLabel.setForeground(new Color(120, 130, 150));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mensajesPanel.add(Box.createVerticalGlue());
            mensajesPanel.add(emptyLabel);
            mensajesPanel.add(Box.createVerticalGlue());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");

            for (MensajeChat mensaje : mensajes) {
                boolean esMio = mensaje.getRemitenteEmail().equals(currentUser.getEmail());

                OfertaData oferta = extraerOferta(mensaje.getContenido());
                if (oferta != null) {
                    mensajesPanel.add(crearPanelMensajeOferta(mensaje, esMio, oferta, sdf));
                    continue;
                }

                JPanel mensajePanel = new JPanel();
                mensajePanel.setLayout(new BoxLayout(mensajePanel, BoxLayout.Y_AXIS));
                mensajePanel.setOpaque(false);
                mensajePanel.setAlignmentX(esMio ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

                JPanel burbujaPanel = new JPanel();
                burbujaPanel.setLayout(new BorderLayout(5, 5));
                burbujaPanel.setBackground(esMio ? new Color(20, 120, 220) : Color.WHITE);
                burbujaPanel.setBorder(BorderFactory.createCompoundBorder(
                    new UIUtils.RoundedBorder(12, esMio ? new Color(20, 120, 220) : new Color(220, 225, 235)),
                    new EmptyBorder(8, 12, 8, 12)
                ));
                burbujaPanel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));

                // Usar JLabel con HTML en lugar de JTextArea para mejor rendering
                String contenidoHtml = "<html><body style='width: 250px'>" +
                                      mensaje.getContenido().replace("\n", "<br>") +
                                      "</body></html>";
                JLabel lblContenido = new JLabel(contenidoHtml);
                lblContenido.setFont(new Font("SansSerif", Font.PLAIN, 13));
                lblContenido.setForeground(esMio ? Color.WHITE : new Color(30, 33, 40));
                lblContenido.setOpaque(false);

                JLabel lblFecha = new JLabel(sdf.format(java.sql.Timestamp.valueOf(mensaje.getEnviadoEn())));
                lblFecha.setFont(new Font("SansSerif", Font.PLAIN, 10));
                lblFecha.setForeground(esMio ? new Color(200, 220, 255) : new Color(120, 130, 150));

                burbujaPanel.add(lblContenido, BorderLayout.CENTER);
                burbujaPanel.add(lblFecha, BorderLayout.SOUTH);

                JPanel wrapperPanel = new JPanel(new FlowLayout(esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
                wrapperPanel.setOpaque(false);
                wrapperPanel.add(burbujaPanel);

                mensajePanel.add(wrapperPanel);
                mensajePanel.add(Box.createRigidArea(new Dimension(0, 8)));

                mensajesPanel.add(mensajePanel);
            }
        }

        mensajesPanel.revalidate();
        mensajesPanel.repaint();

        // Forzar repaint después de un momento para asegurar que se renderiza bien
        SwingUtilities.invokeLater(() -> {
            mensajesPanel.revalidate();
            mensajesPanel.repaint();
        });
    }

    private static class OfertaData {
        Double precio;
        String precioTexto;
        String nota;
    }

    private OfertaData extraerOferta(String contenido) {
        if (contenido == null || !contenido.startsWith(OFERTA_PREFIX)) return null;
        String payload = contenido.substring(OFERTA_PREFIX.length());
        String[] parts = payload.split("\\|", 2);
        OfertaData data = new OfertaData();
        data.precioTexto = parts.length > 0 ? parts[0].trim() : "";

        String normalizado = data.precioTexto.replace("€", "").replace(" ", "").trim();
        if (normalizado.contains(",") && normalizado.contains(".")) {
            normalizado = normalizado.replace(".", "").replace(",", ".");
        } else if (normalizado.contains(",") && !normalizado.contains(".")) {
            normalizado = normalizado.replace(",", ".");
        }

        try {
            data.precio = Double.parseDouble(normalizado);
        } catch (Exception ignored) {}
        data.nota = parts.length > 1 ? parts[1] : null;
        return data;
    }

    private JPanel crearPanelMensajeOferta(MensajeChat mensaje, boolean esMio, OfertaData oferta, SimpleDateFormat sdf) {
        JPanel wrapper = new JPanel(new FlowLayout(esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout(8, 6));
        Color fondo = esMio ? new Color(20, 120, 220) : new Color(245, 248, 255);
        Color borde = esMio ? new Color(20, 120, 220) : new Color(200, 215, 240);
        card.setBackground(fondo);
        card.setBorder(BorderFactory.createCompoundBorder(
            new UIUtils.RoundedBorder(12, borde),
            new EmptyBorder(10, 12, 10, 12)
        ));
        card.setPreferredSize(new Dimension(320, 90));

        JLabel titulo = new JLabel("Oferta enviada");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        titulo.setForeground(esMio ? Color.WHITE : new Color(30, 33, 40));

        String importe = oferta.precio != null
            ? CURRENCY_FORMAT.format(oferta.precio)
            : (oferta.precioTexto != null && !oferta.precioTexto.isBlank() ? oferta.precioTexto : "Oferta");
        JLabel lblPrecio = new JLabel(importe);
        lblPrecio.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblPrecio.setForeground(esMio ? Color.WHITE : new Color(25, 80, 170));

        String nota = (oferta.nota != null && !oferta.nota.isBlank()) ? oferta.nota : "Sin notas adicionales";
        JLabel lblNota = new JLabel("<html><body style='width: 260px'>" + nota.replace("\n", "<br>") + "</body></html>");
        lblNota.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblNota.setForeground(esMio ? new Color(220, 235, 255) : new Color(70, 80, 100));

        JLabel lblFecha = new JLabel(sdf.format(java.sql.Timestamp.valueOf(mensaje.getEnviadoEn())));
        lblFecha.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblFecha.setForeground(esMio ? new Color(200, 220, 255) : new Color(120, 130, 150));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titulo, BorderLayout.WEST);
        top.add(lblFecha, BorderLayout.EAST);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        center.setOpaque(false);
        center.add(lblPrecio);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(lblNota, BorderLayout.CENTER);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        wrapper.add(card);
        wrapper.add(Box.createRigidArea(new Dimension(0, 8)));
        return wrapper;
    }

    private String obtenerResumenMensaje(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Sin mensajes";
        }
        OfertaData oferta = extraerOferta(raw);
        if (oferta != null) {
            String importe = oferta.precio != null
                ? CURRENCY_FORMAT.format(oferta.precio)
                : (oferta.precioTexto != null && !oferta.precioTexto.isBlank() ? oferta.precioTexto : "Oferta");
            String nota = (oferta.nota != null && !oferta.nota.isBlank()) ? " · " + oferta.nota : "";
            return "\uD83D\uDCBC Oferta: " + importe + nota;
        }
        return raw.length() > 50 ? raw.substring(0, 47) + "..." : raw;
    }

    private JPanel crearTarjeta() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new UIUtils.RoundedBorder(12, new Color(230, 235, 245)),
            new EmptyBorder(12, 12, 12, 12)
        ));
        return card;
    }

    public void refrescarChats() {
        cargarChats();
        cardLayout.show(mainPanel, "LISTA");
    }
}
