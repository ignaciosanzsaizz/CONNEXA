package icai.dtc.isw.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.BiConsumer;

public class LoginPanel extends JPanel {

    private final JTextField txtEmail;
    private final JPasswordField txtPass;
    private final JLabel lblError;

    /**
     * @param onLogin       callback con (email, pass)
     * @param onBack        callback al pulsar "Volver"
     * @param onGoRegister  callback para saltar a "Registrarse"
     */
    public LoginPanel(BiConsumer<String,String> onLogin, Runnable onBack, Runnable onGoRegister) {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250));

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(4, 4, getWidth()-8, getHeight()-6, 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(new UIUtils.RoundedBorder(16, new Color(230, 235, 245)));
        card.setPreferredSize(new Dimension(300, 480));
        card.setBorder(new EmptyBorder(12,12,12,12));

        GridBagConstraints gbc = UIUtils.baseGbc();

        // Logo
        JLabel logo = new JLabel(scaledLogo("/icons/connexa.png", 120));
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(logo, gbc);

        // Título
        JLabel title = new JLabel("Iniciar sesión", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(20, 40, 80));
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 8, 12, 8);
        card.add(title, gbc);

        // Email
        JLabel lblEmail = new JLabel("Correo");
        lblEmail.setForeground(new Color(20, 40, 80));
        gbc.gridy = 2; gbc.insets = new Insets(2, 8, 2, 8);
        card.add(lblEmail, gbc);

        txtEmail = UIUtils.styledTextField(24);
        gbc.gridy = 3; gbc.insets = new Insets(2, 8, 8, 8);
        card.add(txtEmail, gbc);

        // Contraseña
        JLabel lblPass = new JLabel("Contraseña");
        lblPass.setForeground(new Color(20, 40, 80));
        gbc.gridy = 4; gbc.insets = new Insets(2, 8, 2, 8);
        card.add(lblPass, gbc);

        txtPass = UIUtils.styledPassField(24);
        gbc.gridy = 5; gbc.insets = new Insets(2, 8, 2, 8);
        card.add(txtPass, gbc);

        JCheckBox chk = new JCheckBox("Mostrar contraseña");
        chk.setOpaque(false);
        chk.addActionListener(e -> txtPass.setEchoChar(chk.isSelected() ? '\0' : (char) UIManager.get("PasswordField.echoChar")));
        gbc.gridy = 6; gbc.insets = new Insets(0,8,8,8);
        card.add(chk, gbc);

        lblError = new JLabel(" ");
        lblError.setForeground(new Color(200, 30, 30));
        gbc.gridy = 7; gbc.insets = new Insets(4, 8, 8, 8);
        card.add(lblError, gbc);

        // Botones
        JPanel botones = new JPanel(new GridLayout(1, 2, 10, 10));
        botones.setOpaque(false);
        JButton btnVolver = UIUtils.secondaryButton("Volver");
        JButton btnAcceder = UIUtils.primaryButton("Acceder");

        // === Color azul oscuro como la cabecera ===
        Color darkBlue = new Color(10, 23, 42);
        btnAcceder.setBackground(darkBlue);
        btnAcceder.setForeground(Color.WHITE);
        btnAcceder.setBorder(new UIUtils.RoundedBorder(14, darkBlue));
        btnAcceder.setContentAreaFilled(true);
        btnAcceder.setOpaque(true);

        botones.add(btnVolver);
        botones.add(btnAcceder);

        gbc.gridy = 8; gbc.insets = new Insets(10, 8, 6, 8);
        card.add(botones, gbc);

        // CTA registro
        JButton linkRegister = new JButton("¿No tienes cuenta? Crear una");
        linkRegister.setBorderPainted(false);
        linkRegister.setContentAreaFilled(false);
        linkRegister.setForeground(new Color(20, 82, 255));
        linkRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridy = 9; gbc.insets = new Insets(4, 8, 4, 8);
        card.add(linkRegister, gbc);

        // Acciones
        btnVolver.addActionListener(e -> {
            clearError();
            if (onBack != null) onBack.run();
        });
        btnAcceder.addActionListener(e -> {
            clearError();
            String email = txtEmail.getText().trim();
            String pass  = new String(txtPass.getPassword());
            if (email.isEmpty() || pass.isEmpty()) {
                showError("Rellene correo y contraseña.");
                return;
            }
            if (!isEmail(email)) {
                showError("Correo no válido.");
                return;
            }
            if (onLogin != null) onLogin.accept(email, pass);
        });
        linkRegister.addActionListener(e -> {
            clearError();
            if (onGoRegister != null) onGoRegister.run();
        });

        add(card, new GridBagConstraints());
    }

    private void showError(String msg) { lblError.setText(msg); }
    public  void clearError() { lblError.setText(" "); }

    private boolean isEmail(String value) {
        return value != null && value.contains("@") && value.contains(".") && !value.contains(" ");
    }

    private Icon scaledLogo(String path, int targetW) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) return new ImageIcon();
        ImageIcon raw = new ImageIcon(url);
        int w = raw.getIconWidth();
        int h = raw.getIconHeight();
        if (w <= 0 || h <= 0) return raw;
        float scale = (float) targetW / (float) w;
        int newH = Math.max(1, Math.round(h * scale));
        Image img = raw.getImage().getScaledInstance(targetW, newH, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}
