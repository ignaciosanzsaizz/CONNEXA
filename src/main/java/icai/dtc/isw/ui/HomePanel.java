package icai.dtc.isw.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class HomePanel extends JPanel {

    private final Runnable onLogin;
    private final Runnable onRegister;

    public HomePanel(Runnable onLogin, Runnable onRegister) {
        this.onLogin = onLogin;
        this.onRegister = onRegister;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ---------- Cabecera con gradiente y logo ----------
        JPanel header = new UIUtils.GradientBar(new Color(10, 23, 42), new Color(20, 40, 80));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel tagline = new JLabel("Conecta necesidades con expertos, al instante.");
        tagline.setForeground(new Color(220, 230, 255));
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel brandWrap = new JPanel();
        brandWrap.setOpaque(false);
        brandWrap.setLayout(new BoxLayout(brandWrap, BoxLayout.Y_AXIS));
        brandWrap.add(Box.createVerticalStrut(4));
        brandWrap.add(tagline);

        add(header, BorderLayout.NORTH);

        // ---------- Cuerpo centrado con tarjeta ----------
        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setBackground(getBackground());

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // sombra suave
                g2.setColor(new Color(0,0,0,18));
                g2.fillRoundRect(6, 6, getWidth()-12, getHeight()-10, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(new UIUtils.RoundedBorder(18, new Color(230, 235, 245)));
        card.setPreferredSize(new Dimension(300, 420));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        GridBagConstraints gbc = UIUtils.baseGbc();
        gbc.insets = new Insets(8, 8, 8, 8);

        // Logo grande dentro de la tarjeta
        JLabel bigLogo = new JLabel(scaledLogo("/icons/connexa.png", 120));
        gbc.gridy = 0; gbc.anchor = GridBagConstraints.CENTER;
        card.add(bigLogo, gbc);

        // Título de bienvenida
        JLabel title = new JLabel("Bienvenido a CONNEXA", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(20, 40, 80));
        gbc.gridy = 1; gbc.insets = new Insets(4,8,2,8);
        card.add(title, gbc);

        // Subtítulo / lema
        JLabel subtitle = new JLabel("Servicios cerca de ti, chats privados y resultados al momento.", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
        subtitle.setForeground(new Color(20, 40, 80));
        gbc.gridy = 2; gbc.insets = new Insets(0,8,8,8);
        card.add(subtitle, gbc);

        // Lista de beneficios (mini bullets con emojis)
        JPanel bullets = new JPanel();
        bullets.setOpaque(false);
        bullets.setLayout(new BoxLayout(bullets, BoxLayout.Y_AXIS));
        bullets.add(bullet("🔎 Búsquedas por categoría y trabajo "));
        bullets.add(bullet("📍 Priorizamos cercanía para resultados útiles"));
        bullets.add(bullet("💬 Chats privados con profesionales"));
        bullets.add(bullet("⭐ Guarda tus favoritos para más tarde"));
        gbc.gridy = 3; gbc.insets = new Insets(4,8,8,8);
        card.add(bullets, gbc);

        // Botones CTA
        JPanel ctas = new JPanel(new GridLayout(2, 1, 8, 8));
        ctas.setOpaque(false);

        JButton btnLogin = UIUtils.primaryButton("\uD83D\uDD10 Iniciar sesión");
        // === Cambiar color del botón a azul oscuro del header ===
        Color darkBlue = new Color(10, 23, 42);
        btnLogin.setBackground(darkBlue);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBorder(new UIUtils.RoundedBorder(14, darkBlue));
        // (opcional) si tu L&F ignora background:
        btnLogin.setContentAreaFilled(true);
        btnLogin.setOpaque(true);

        btnLogin.addActionListener(e -> { if (onLogin != null) onLogin.run(); });

        JButton btnRegister = UIUtils.secondaryButton("\uD83D\uDE80 Crear cuenta");
        btnRegister.addActionListener(e -> { if (onRegister != null) onRegister.run(); });

        ctas.add(btnLogin);
        ctas.add(btnRegister);

        gbc.gridy = 4; gbc.insets = new Insets(12,8,0,8);
        card.add(ctas, gbc);

        centerWrap.add(card);
        add(centerWrap, BorderLayout.CENTER);

        // ---------- Pie con pequeño texto legal / versión ----------
        JLabel footer = new JLabel("v1.0 • Hecho en España", SwingConstants.CENTER);
        footer.setForeground(new Color(130, 140, 155));
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setBorder(new EmptyBorder(8, 8, 12, 8));
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel bullet(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(60, 70, 90));
        p.add(l);
        return p;
    }

    private Icon scaledLogo(String path, int targetW) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return new ImageIcon(new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB));
            ImageIcon raw = new ImageIcon(url);
            int w = raw.getIconWidth(), h = raw.getIconHeight();
            if (w <= 0 || h <= 0) return raw;
            float scale = (float) targetW / (float) w;
            int newH = Math.max(1, Math.round(h * scale));
            Image img = raw.getImage().getScaledInstance(targetW, newH, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return new ImageIcon(new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB));
        }
    }
}
