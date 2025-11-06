package icai.dtc.isw.ui;

import icai.dtc.isw.domain.Anuncio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AnuncioDetallePanel extends JPanel {

    private final Anuncio anuncio;
    private final Runnable onBack;

    public AnuncioDetallePanel(Anuncio anuncio, Runnable onBack) {
        this.anuncio = anuncio;
        this.onBack = onBack;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // Header con botón volver
        JPanel header = new UIUtils.GradientBar(new Color(10, 23, 42), new Color(20, 40, 80));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(8, 8, 8, 8));

        JButton btnBack = UIUtils.secondaryButton("← Volver");
        btnBack.addActionListener(e -> { if (onBack != null) onBack.run(); });
        header.add(btnBack, BorderLayout.WEST);

        JLabel ttl = new JLabel("Detalle del anuncio", SwingConstants.CENTER);
        ttl.setForeground(Color.WHITE);
        ttl.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.add(ttl, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // Contenido con tarjeta + mapa
        JPanel content = new JPanel();
        content.setBackground(getBackground());
        content.setLayout(new GridBagLayout());

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,18));
                g2.fillRoundRect(4, 4, getWidth()-8, getHeight()-6, 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(new UIUtils.RoundedBorder(16, new Color(230, 235, 245)));

        GridBagConstraints gbc = UIUtils.baseGbc();
        gbc.insets = new Insets(8, 12, 8, 12);

        // Campos
        JLabel lblCat = new JLabel("Categoría / Trabajo:");
        JLabel valCat = new JLabel(s(anuncio.getCategoria()) +
                (isNotBlank(anuncio.getEspecificacion()) ? " · " + s(anuncio.getEspecificacion()) : ""));

        JLabel lblDesc = new JLabel("Descripción:");
        JLabel valDesc = new JLabel("<html>" + escapeHtml(s(anuncio.getDescripcion())) + "</html>");

        JLabel lblUbi = new JLabel("Ubicación:");
        JLabel valUbi = new JLabel(s(anuncio.getUbicacion()));

        JLabel lblPrecio = new JLabel("Precio:");
        JLabel valPrecio = new JLabel(anuncio.getPrecio() != null ? String.format("%.2f €", anuncio.getPrecio()) : "-");

        // Fila a fila
        gbc.gridy = 0; gbc.gridx = 0; card.add(lblCat, gbc);
        gbc.gridx = 1; card.add(valCat, gbc);

        gbc.gridy = 1; gbc.gridx = 0; card.add(lblDesc, gbc);
        gbc.gridx = 1; card.add(valDesc, gbc);

        gbc.gridy = 2; gbc.gridx = 0; card.add(lblUbi, gbc);
        gbc.gridx = 1; card.add(valUbi, gbc);

        gbc.gridy = 3; gbc.gridx = 0; card.add(lblPrecio, gbc);
        gbc.gridx = 1; card.add(valPrecio, gbc);

        // Mapa
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel mapWrapper = new JPanel(new BorderLayout());
        mapWrapper.setOpaque(false);
        mapWrapper.setPreferredSize(new Dimension(520, 280));
        mapWrapper.setBorder(new EmptyBorder(8, 0, 0, 0));

        CompanyMapPanel map = new CompanyMapPanel();
        mapWrapper.add(map, BorderLayout.CENTER);
        // nombre que saldrá en el popup del pin (usa lo que tengas: empresa, categoría…)
        String popupName = isNotBlank(anuncio.getEspecificacion())
                ? anuncio.getEspecificacion()
                : s(anuncio.getCategoria());
        map.setAddressAsync(s(anuncio.getUbicacion()), popupName);

        card.add(mapWrapper, gbc);

        // Colocar la tarjeta en el centro con márgenes
        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx = 0; outer.gridy = 0;
        outer.insets = new Insets(12, 12, 12, 12);
        content.add(card, outer);

        add(content, BorderLayout.CENTER);
    }

    private static String s(Object o) { return o == null ? "-" : String.valueOf(o); }
    private static boolean isNotBlank(Object o) {
        return o != null && !String.valueOf(o).trim().isEmpty();
    }
    private static String escapeHtml(String in) {
        return in.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
}

