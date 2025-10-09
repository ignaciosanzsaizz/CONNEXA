package icai.dtc.isw.ui;

import icai.dtc.isw.client.Client;
import icai.dtc.isw.domain.Customer;
import icai.dtc.isw.domain.User;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class JVentana extends JFrame {
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

    private int id;

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private static final String PANTALLA_HOME = "HOME";
    private static final String PANTALLA_LOGIN = "LOGIN";
    private static final String PANTALLA_REGISTER = "REGISTER";
    private static final String PANTALLA_REGISTER_OK = "REGISTER_OK";

    private JTextField txtLoginEmail;
    private JPasswordField txtLoginPass;
    private JLabel lblLoginError;

    private JTextField txtRegEmail;
    private JTextField txtRegUser;
    private JPasswordField txtRegPass;
    private JLabel lblRegError;

    public JVentana() {
        super("INGENIERÍA DEL SOFTWARE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel pnlNorte = new JPanel(new BorderLayout());
        JLabel lblTitulo = new JLabel("Prueba COMUNICACIÓN", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Courier", Font.BOLD, 20));
        pnlNorte.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlNorte.add(lblTitulo, BorderLayout.CENTER);
        add(pnlNorte, BorderLayout.NORTH);

        root.add(buildHomePanel(), PANTALLA_HOME);
        root.add(buildLoginPanel(), PANTALLA_LOGIN);
        root.add(buildRegisterPanel(), PANTALLA_REGISTER);
        root.add(buildRegisterOkPanel(), PANTALLA_REGISTER_OK);
        add(root, BorderLayout.CENTER);

        setSize(520, 420);
        setResizable(false);
        setLocationRelativeTo(null);

        // Arranque en Home
        cards.show(root, PANTALLA_HOME);
    }

    private JPanel buildHomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lbl = new JLabel("Bienvenido", SwingConstants.CENTER);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));

        JButton btnLogin = new JButton("Iniciar sesión");
        btnLogin.addActionListener(e -> {
            clearLoginErrors();
            cards.show(root, PANTALLA_LOGIN);
        });

        JButton btnRegister = new JButton("Registrarse");
        btnRegister.addActionListener(e -> {
            clearRegisterErrors();
            cards.show(root, PANTALLA_REGISTER);
        });

        gbc.gridy = 0;
        panel.add(lbl, gbc);
        gbc.gridy = 1;
        panel.add(btnLogin, gbc);
        gbc.gridy = 2;
        panel.add(btnRegister, gbc);

        return panel;
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        JLabel title = new JLabel("Iniciar sesión", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JLabel lblEmail = new JLabel("Correo:");
        txtLoginEmail = new JTextField(24);

        JLabel lblPass = new JLabel("Contraseña:");
        txtLoginPass = new JPasswordField(24);

        lblLoginError = new JLabel(" ");
        lblLoginError.setForeground(new Color(180, 0, 0));

        JButton btnAcceder = new JButton("Acceder");
        btnAcceder.addActionListener(e -> doLogin());

        JButton btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> cards.show(root, PANTALLA_HOME));

        gbc.gridy = 0; panel.add(title, gbc);
        gbc.gridy = 1; panel.add(lblEmail, gbc);
        gbc.gridy = 2; panel.add(txtLoginEmail, gbc);
        gbc.gridy = 3; panel.add(lblPass, gbc);
        gbc.gridy = 4; panel.add(txtLoginPass, gbc);
        gbc.gridy = 5; panel.add(lblLoginError, gbc);

        JPanel botones = new JPanel(new GridLayout(1, 2, 8, 8));
        botones.add(btnVolver);
        botones.add(btnAcceder);
        gbc.gridy = 6; panel.add(botones, gbc);

        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        JLabel title = new JLabel("Registro de usuario", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JLabel lblEmail = new JLabel("Correo:");
        txtRegEmail = new JTextField(24);

        JLabel lblUser = new JLabel("Nombre de usuario:");
        txtRegUser = new JTextField(24);

        JLabel lblPass = new JLabel("Contraseña:");
        txtRegPass = new JPasswordField(24);

        lblRegError = new JLabel(" ");
        lblRegError.setForeground(new Color(180, 0, 0));

        JButton btnRegistrar = new JButton("Registrarse");
        btnRegistrar.addActionListener(e -> doRegister());

        JButton btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> cards.show(root, PANTALLA_HOME));

        gbc.gridy = 0; panel.add(title, gbc);
        gbc.gridy = 1; panel.add(lblEmail, gbc);
        gbc.gridy = 2; panel.add(txtRegEmail, gbc);
        gbc.gridy = 3; panel.add(lblUser, gbc);
        gbc.gridy = 4; panel.add(txtRegUser, gbc);
        gbc.gridy = 5; panel.add(lblPass, gbc);
        gbc.gridy = 6; panel.add(txtRegPass, gbc);
        gbc.gridy = 7; panel.add(lblRegError, gbc);

        JPanel botones = new JPanel(new GridLayout(1, 2, 8, 8));
        botones.add(btnVolver);
        botones.add(btnRegistrar);
        gbc.gridy = 8; panel.add(botones, gbc);

        return panel;
    }

    private JPanel buildRegisterOkPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;

        JLabel lbl = new JLabel("Se ha registrado correctamente.", SwingConstants.CENTER);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 16f));

        JButton btnOK = new JButton("OK");
        btnOK.addActionListener(e -> {
            clearLoginErrors();
            cards.show(root, PANTALLA_HOME);
        });

        gbc.gridy = 0; panel.add(lbl, gbc);
        gbc.gridy = 1; panel.add(btnOK, gbc);

        return panel;
    }

    private void doRegister() {
        clearRegisterErrors();

        String email = txtRegEmail.getText().trim();
        String user  = txtRegUser.getText().trim();
        String pass  = new String(txtRegPass.getPassword());

        if (email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            lblRegError.setText("Rellene todos los campos.");
            return;
        }
        if (!isEmail(email)) {
            lblRegError.setText("Correo no válido.");
            return;
        }

        try {
            String err = registerUser(email, user, pass);
            if (err == null) {
                cards.show(root, PANTALLA_REGISTER_OK);
            } else {
                if ("EMAIL_EXISTS".equalsIgnoreCase(err)) {
                    lblRegError.setText("El correo ya existe en la base de datos.");
                } else {
                    lblRegError.setText(err);
                }
            }
        } catch (Exception ex) {
            lblRegError.setText("Error de comunicación con el servidor.");
        }
    }

    private void doLogin() {
        clearLoginErrors();

        String email = txtLoginEmail.getText().trim();
        String pass  = new String(txtLoginPass.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            lblLoginError.setText("Rellene correo y contraseña.");
            return;
        }
        if (!isEmail(email)) {
            lblLoginError.setText("Correo no válido.");
            return;
        }

        try {
            User c = loginUser(email, pass);
            if (c != null) {
                SwingUtilities.invokeLater(() -> {
                    new AppMovilMock().setVisible(true);
                });
                dispose();
            } else {
                lblLoginError.setText("Credenciales incorrectas.");
            }
        } catch (Exception ex) {
            lblLoginError.setText("Error de comunicación con el servidor.");
        }
    }

    private void clearRegisterErrors() {
        if (lblRegError != null) lblRegError.setText(" ");
    }
    private void clearLoginErrors() {
        if (lblLoginError != null) lblLoginError.setText(" ");
    }

    private boolean isEmail(String value) {
        // Validación simple
        return value.contains("@") && value.contains(".") && !value.contains(" ");
    }
    private String registerUser(String email, String username, String password) {
        Client cliente = new Client();
        HashMap<String, Object> session = new HashMap<>();
        String context = "/registerUser";

        session.put("email", email);
        session.put("username", username);
        session.put("password", password);

        session = cliente.sentMessage(context, session);


        Object ok = session.get("ok");
        if (Boolean.TRUE.equals(ok)) return null;
        Object error = session.get("error");
        if (error instanceof String) return (String) error;
        Object c = session.get("Customer");
        if (c instanceof Customer) return null;

        return "No se pudo completar el registro.";
    }

    private User loginUser(String email, String password) {
        Client cliente = new Client();
        HashMap<String, Object> session = new HashMap<>();
        String context = "/loginUser";

        session.put("username", email);
        session.put("password", password);

        session = cliente.sentMessage(context, session);

        User c = (User)session.get("user");

        return c;
    }

    public String recuperarInformacion() {
        Client cliente=new Client();
        HashMap<String,Object> session=new HashMap<>();
        String context="/getCustomer";
        session.put("id",id);
        session=cliente.sentMessage(context,session);
        Customer cu=(Customer)session.get("Customer");
        String nombre;
        if (cu==null) {
            nombre="Error - No encontrado en la base de datos";
        }else {
            nombre=cu.getName();
        }
        return nombre;
    }

    static class AppMovilMock extends JFrame {
        private final JLabel tituloLabel;
        private final CardLayout cardLayout;
        private final JPanel panelContenido;

        public AppMovilMock() {
            super("App Móvil (Mock)");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            setSize(360, 640);
            setMinimumSize(new Dimension(320, 560));
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            tituloLabel = new JLabel("Perfil", SwingConstants.CENTER);
            tituloLabel.setFont(tituloLabel.getFont().deriveFont(Font.BOLD, 20f));
            JPanel barraSuperior = new JPanel(new BorderLayout());
            barraSuperior.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            barraSuperior.add(tituloLabel, BorderLayout.CENTER);
            add(barraSuperior, BorderLayout.NORTH);

            cardLayout = new CardLayout();
            panelContenido = new JPanel(cardLayout);

            panelContenido.add(crearPantalla("Pantalla de Perfil"), "PERFIL");
            panelContenido.add(crearPantalla("Pantalla de Búsquedas"), "BUSQUEDAS");
            panelContenido.add(crearPantalla("Pantalla de Favoritos"), "FAVORITOS");

            add(panelContenido, BorderLayout.CENTER);

            JPanel barraInferior = new JPanel(new GridLayout(1, 3));
            barraInferior.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            JButton btnPerfil = new JButton("Perfil");
            JButton btnBusquedas = new JButton("Búsquedas");
            JButton btnFavoritos = new JButton("Favoritos");

            btnPerfil.addActionListener(new CambiarPantalla("PERFIL", "Perfil"));
            btnBusquedas.addActionListener(new CambiarPantalla("BUSQUEDAS", "Búsquedas"));
            btnFavoritos.addActionListener(new CambiarPantalla("FAVORITOS", "Favoritos"));

            barraInferior.add(btnPerfil);
            barraInferior.add(btnBusquedas);
            barraInferior.add(btnFavoritos);

            add(barraInferior, BorderLayout.SOUTH);
        }

        private JPanel crearPantalla(String textoCentro) {
            JPanel panel = new JPanel(new GridBagLayout());
            JLabel lbl = new JLabel(textoCentro, SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 16f));
            panel.add(lbl);
            return panel;
        }

        private class CambiarPantalla implements java.awt.event.ActionListener {
            private final String card;
            private final String titulo;

            private CambiarPantalla(String card, String titulo) {
                this.card = card;
                this.titulo = titulo;
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                tituloLabel.setText(titulo);
                cardLayout.show(panelContenido, card);
            }
        }
    }
}
