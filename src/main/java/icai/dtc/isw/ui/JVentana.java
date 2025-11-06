package icai.dtc.isw.ui;

import icai.dtc.isw.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class JVentana extends JFrame {

    public static final String PANTALLA_HOME        = "HOME";
    public static final String PANTALLA_LOGIN       = "LOGIN";
    public static final String PANTALLA_REGISTER    = "REGISTER";
    public static final String PANTALLA_REGISTER_OK = "REGISTER_OK";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final AuthApi auth = new AuthApi();

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new JVentana().setVisible(true));
    }

    public JVentana() {
        super("CONNEXA APP");

        // Icono (si no existe, no falla)
        try {
            Image icon = new ImageIcon(getClass().getResource("/icons/connexa_mini.png")).getImage();
            setIconImage(icon);
        } catch (Exception ignored) {}

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Barra superior con gradiente y título
        JPanel pnlNorte = new UIUtils.GradientBar(new Color(10, 23, 42), new Color(20, 40, 80));
        pnlNorte.setLayout(new BorderLayout());
        pnlNorte.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lblTitulo = new JLabel("CONNEXA", SwingConstants.CENTER);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        pnlNorte.add(lblTitulo, BorderLayout.CENTER);

        add(pnlNorte, BorderLayout.NORTH);

        // Contenedor principal (CardLayout)
        root.setBackground(new Color(245, 247, 250));
        add(root, BorderLayout.CENTER);

        // ---- Pantallas ----

        // HOME
        root.add(new HomePanel(
                this::irLogin,
                this::irRegister
        ), PANTALLA_HOME);

        // LOGIN  -> usa tu LoginPanel(BiConsumer<String,String>, Runnable, Runnable)
        root.add(new LoginPanel(
                // onLogin (email, pass)
                (email, pass) -> {
                    try {
                        User u = auth.loginUser(email, pass);
                        if (u != null) {
                            SwingUtilities.invokeLater(() -> new AppMovilMock(u).setVisible(true));
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(this, "Credenciales incorrectas.", "Login", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error de comunicación con el servidor.", "Login", JOptionPane.ERROR_MESSAGE);
                    }
                },
                // onBack
                this::irHome,
                // onGoRegister
                this::irRegister
        ), PANTALLA_LOGIN);

        // REGISTER -> usa tu RegisterPanel(Consumer<String[]>, Runnable)
        root.add(new RegisterPanel(
                // onRegister -> values[0]=email, values[1]=user, values[2]=pass
                (values) -> {
                    String email = values[0], user = values[1], pass = values[2];
                    try {
                        String err = auth.registerUser(email, user, pass);
                        if (err == null) {
                            JOptionPane.showMessageDialog(this, "Cuenta creada correctamente. Inicia sesión.", "Registro correcto", JOptionPane.INFORMATION_MESSAGE);
                            cards.show(root, PANTALLA_LOGIN);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "EMAIL_EXISTS".equalsIgnoreCase(err) ? "El correo ya existe en la base de datos." : err,
                                    "Registro", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error de comunicación con el servidor.", "Registro", JOptionPane.ERROR_MESSAGE);
                    }
                },
                // onBack
                this::irHome
        ), PANTALLA_REGISTER);

        // REGISTER_OK (si lo usas en tu flujo)
        root.add(new RegisterOkPanel(() -> cards.show(root, PANTALLA_HOME)), PANTALLA_REGISTER_OK);

        
        // Tamaño y arranque
        setSize(360, 640);
        setResizable(false);
        setLocationRelativeTo(null);
        cards.show(root, PANTALLA_HOME);
    }

    /* ------------ Navegación rápida ------------ */
    private void irHome()     { cards.show(root, PANTALLA_HOME); }
    private void irLogin()    { cards.show(root, PANTALLA_LOGIN); }
    private void irRegister() { cards.show(root, PANTALLA_REGISTER); }
}