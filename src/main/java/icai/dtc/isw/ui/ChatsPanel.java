package icai.dtc.isw.ui;

import icai.dtc.isw.controler.ChatControler;
import icai.dtc.isw.domain.Chat;
import icai.dtc.isw.domain.ChatMessage;
import icai.dtc.isw.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class ChatsPanel extends JPanel {
  private final ChatControler ctrl = new ChatControler();
  private Integer currentUserId;      // solo modo usuario
  private Chat chatAbierto;

  private final JPanel msgs = new JPanel(); // contenedor vertical
  private final JTextField input = UIUtils.styledTextField(20);

  public ChatsPanel() {
    setLayout(new BorderLayout());
    setBackground(new Color(245,247,250));

    msgs.setLayout(new BoxLayout(msgs, BoxLayout.Y_AXIS));
    msgs.setBackground(Color.WHITE);

    JScrollPane scrollMsgs = new JScrollPane(msgs);
    scrollMsgs.setBorder(BorderFactory.createEmptyBorder());
    scrollMsgs.getViewport().setBackground(new Color(245,247,250));

    JButton send = UIUtils.primaryButton("Enviar");
    JPanel composer = new JPanel(new BorderLayout(8,0));
    composer.setBorder(new EmptyBorder(8,8,8,8));
    composer.setBackground(new Color(245,247,250));
    composer.add(input, BorderLayout.CENTER);
    composer.add(send, BorderLayout.EAST);

    add(scrollMsgs, BorderLayout.CENTER);
    add(composer, BorderLayout.SOUTH);

    send.addActionListener(e -> {
      String body = input.getText().trim();
      if (body.isEmpty() || chatAbierto == null || currentUserId == null) return;
      try {
        ctrl.sendFromUser(chatAbierto.getId(), currentUserId, body);
        input.setText("");
        loadMessages(chatAbierto.getId(), true);
      } catch (Exception ex) { ex.printStackTrace(); }
    });
  }

  public void setContextAsUser(Object userId) {
    if (userId == null) return;
    if (userId instanceof User) {
      try {
        this.currentUserId = Integer.parseInt(((User) userId).getId());
      } catch (Exception ex) {
        this.currentUserId = null;
      }
    } else if (userId instanceof Number) {
      this.currentUserId = ((Number) userId).intValue();
    } else {
      try { this.currentUserId = Integer.parseInt(String.valueOf(userId)); }
      catch (Exception ex) { this.currentUserId = null; }
    }
  }
  public void openChat(Chat c) {
    this.chatAbierto = c;
    loadMessages(c.getId(), true);
  }

  private void loadMessages(int chatId, boolean scrollToBottom) {
    try {
      List<ChatMessage> l = ctrl.listMessages(chatId, 200, 0);
      msgs.removeAll();
      for (ChatMessage m : l) {
        msgs.add(renderBubble(m));
        msgs.add(Box.createVerticalStrut(6));
      }
      msgs.revalidate(); msgs.repaint();
      if (scrollToBottom) SwingUtilities.invokeLater(() -> {
        JScrollPane sp = (JScrollPane) msgs.getParent().getParent();
        sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
      });
    } catch (Exception ex) { ex.printStackTrace(); }
  }

  private JComponent renderBubble(ChatMessage m) {
    boolean mine = "USER".equals(m.getSenderType());
    JPanel outer = new JPanel(new BorderLayout());
    outer.setOpaque(false);

    // Contenedor vertical: burbuja + timestamp
    JPanel wrap = new JPanel();
    wrap.setOpaque(false);
    wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

    Color brand = new Color(20,82,255);
    Color brandSoft = new Color(232,239,255);
    Color recvBg = new Color(246,248,252);
    Color recvLine = new Color(230,235,245);

    String html = "<html><div style='max-width:520px;'>"+escape(m.getBody())+"</div></html>";
    JLabel bubble = new JLabel(html);
    bubble.setOpaque(true);
    bubble.setBorder(new EmptyBorder(8,12,8,12));
    if (mine) {
      bubble.setBackground(brand);
      bubble.setForeground(Color.WHITE);
      bubble.setBorder(BorderFactory.createCompoundBorder(
          new UIUtils.RoundedBorder(14, brand),
          new EmptyBorder(8,12,8,12)
      ));
    } else {
      bubble.setBackground(recvBg);
      bubble.setForeground(new Color(30,33,40));
      bubble.setBorder(BorderFactory.createCompoundBorder(
          new UIUtils.RoundedBorder(14, recvLine),
          new EmptyBorder(8,12,8,12)
      ));
    }

    JLabel ts = new JLabel(formatTime(m.getCreatedAt()));
    ts.setFont(new Font("SansSerif", Font.PLAIN, 10));
    ts.setForeground(new Color(120,130,150));
    ts.setBorder(new EmptyBorder(2, 6, 0, 6));
    ts.setAlignmentX(mine ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

    bubble.setAlignmentX(mine ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
    wrap.add(bubble);
    wrap.add(ts);

    outer.add(wrap, mine ? BorderLayout.EAST : BorderLayout.WEST);
    return outer;
  }

  private String escape(String s){
    return s==null? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
  }

  private String formatTime(java.sql.Timestamp ts) {
    if (ts == null) return "";
    return new SimpleDateFormat("dd/MM HH:mm").format(ts);
  }
}
