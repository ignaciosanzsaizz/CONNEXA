package icai.dtc.isw.ui;

import icai.dtc.isw.controler.ChatControler;
import icai.dtc.isw.domain.Chat;
import icai.dtc.isw.domain.ChatMessage;
import icai.dtc.isw.domain.User;
import icai.dtc.isw.dao.EmpresaDAO;
import icai.dtc.isw.dao.AnuncioDAO;
import icai.dtc.isw.domain.Anuncio;
import icai.dtc.isw.domain.Empresa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * ChatsListPanel: lista de chats al estilo WhatsApp.
 * - No usa pestañas. Solo lista con preview y fecha.
 * - Al hacer click notifica mediante callback onOpenChat.
 */
public class ChatsListPanel extends JPanel {
    private final ChatControler ctrl = new ChatControler();
    private Integer currentUserId;

    private final DefaultListModel<Chat> model = new DefaultListModel<>();
    private final JList<Chat> list = new JList<>(model);
    private final Map<Integer, ChatMessage> lastMsgByChat = new HashMap<>();
    private final Map<String, String> empresaNombreByNif = new HashMap<>();
    private final Map<String, String> categoriaByAnuncio = new HashMap<>();
    private final JButton btnRefresh = UIUtils.secondaryButton("Actualizar");

    private final EmpresaDAO empresaDAO = new EmpresaDAO();
    private final AnuncioDAO anuncioDAO = new AnuncioDAO();

    private Consumer<Chat> onOpenChat;

    public ChatsListPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(8, 8, 8, 8));
        JLabel title = new JLabel("Chats");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.add(title, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new ChatItemRenderer());
        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createEmptyBorder());
        add(sp, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> refreshList());
        list.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    Chat c = list.getSelectedValue();
                    if (c != null && c.getId() != null && onOpenChat != null) onOpenChat.accept(c);
                }
            }
        });
    }

    public void setOnOpenChat(Consumer<Chat> onOpenChat) { this.onOpenChat = onOpenChat; }

    public void setContextAsUser(Object user) {
        if (user == null) return;
        if (user instanceof User) {
            try { this.currentUserId = Integer.parseInt(((User) user).getId()); } catch (Exception ignored) { this.currentUserId = null; }
        } else if (user instanceof Number) {
            this.currentUserId = ((Number) user).intValue();
        } else {
            try { this.currentUserId = Integer.parseInt(String.valueOf(user)); } catch (Exception ignored) { this.currentUserId = null; }
        }
        refreshList();
    }

    public void refreshList() {
        model.clear();
        lastMsgByChat.clear();
        if (currentUserId == null) {
            model.addElement(fakeInfoChat("Inicia sesión", "No hay usuario activo", null));
            return;
        }
        try {
            List<Chat> chats = ctrl.listForUser(currentUserId);
            for (Chat c : chats) model.addElement(c);
            // Pre-carga de últimos mensajes, nombre empresa y categoría
            for (Chat c : chats) {
                try { lastMsgByChat.put(c.getId(), ctrl.lastMessage(c.getId())); } catch (Exception ignored) {}
                String nif = c.getEmpresaNif();
                String an = c.getAnuncioId();
                if (nif != null && !empresaNombreByNif.containsKey(nif)) {
                    Empresa e = empresaDAO.findByNif(nif);
                    if (e != null && e.getEmpresa() != null) empresaNombreByNif.put(nif, e.getEmpresa());
                }
                if (an != null && !categoriaByAnuncio.containsKey(an)) {
                    Anuncio a = anuncioDAO.findById(an);
                    if (a != null && a.getCategoria() != null) categoriaByAnuncio.put(an, a.getCategoria());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            model.addElement(fakeInfoChat("Error", "No se pudo cargar el historial", null));
        }
    }

    private Chat fakeInfoChat(String nif, String anuncio, Timestamp createdAt) {
        Chat c = new Chat();
        c.setId(null);
        c.setEmpresaNif(nif);
        c.setAnuncioId(anuncio);
        c.setCreatedAt(createdAt);
        return c;
    }

    private static String ellipsize(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }
    private static String formatTs(Timestamp ts) {
        if (ts == null) return "";
        return new SimpleDateFormat("dd/MM HH:mm").format(ts);
    }

    private class ChatItemRenderer extends JPanel implements ListCellRenderer<Chat> {
        private final JLabel lblTop = new JLabel();
        private final JLabel lblBottom = new JLabel();
        private final JPanel wrap = new JPanel();

        ChatItemRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
            wrap.setOpaque(false);
            wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
            lblTop.setFont(new Font("SansSerif", Font.BOLD, 12));
            lblBottom.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblBottom.setForeground(new Color(90, 95, 105));
            wrap.add(lblTop);
            wrap.add(Box.createVerticalStrut(2));
            wrap.add(lblBottom);
            add(wrap, BorderLayout.CENTER);
            setBorder(new EmptyBorder(10, 12, 10, 12));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Chat> list, Chat value, int index, boolean isSelected, boolean cellHasFocus) {
            boolean isInfo = value.getId() == null;
            String empresa = value.getEmpresaNif() != null ? empresaNombreByNif.getOrDefault(value.getEmpresaNif(), value.getEmpresaNif()) : "";
            String categoria = value.getAnuncioId() != null ? categoriaByAnuncio.get(value.getAnuncioId()) : null;
            String top = empresa + (categoria != null ? "  •  " + categoria : (value.getAnuncioId()!=null? "  •  " + value.getAnuncioId() : ""));
            lblTop.setText(top.isBlank() ? (isInfo ? value.getAnuncioId() : "Chat") : top);

            String bottom;
            if (!isInfo) {
                ChatMessage lm = lastMsgByChat.get(value.getId());
                String when = formatTs(lm != null ? lm.getCreatedAt() : value.getCreatedAt());
                String body = lm != null ? lm.getBody() : "(sin mensajes)";
                bottom = when + "  ·  " + ellipsize(body, 48);
            } else {
                bottom = value.getAnuncioId() != null ? value.getAnuncioId() : "";
            }
            lblBottom.setText(bottom);

            if (isSelected) {
                setBackground(new Color(232, 239, 255));
                lblTop.setForeground(new Color(20, 40, 80));
            } else {
                setBackground(Color.WHITE);
                lblTop.setForeground(new Color(30, 33, 40));
            }
            return this;
        }
    }
}
