package com.game2d.view;

import com.game2d.model.Action;
import com.game2d.model.FrameSnapshot;
import com.game2d.model.GameStatus;
import com.game2d.model.Menu;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Dimension;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class NavBarPanel extends JPanel {

    private final HudCard scoreCard = new HudCard(
        HudCard.IconType.STAR, "PUNTAJE",
        new Color(235, 243, 255), new Color(195, 218, 250),
        new Color(130, 165, 220), new Color(20, 60, 140)
    );
    private final HudCard goldCard = new HudCard(
        HudCard.IconType.COIN, "MONEDAS",
        new Color(255, 251, 230), new Color(255, 236, 185),
        new Color(225, 185, 80), new Color(130, 80, 0)
    );
    private final HudCard livesCard = new HudCard(
        HudCard.IconType.HEART, "VIDAS",
        new Color(255, 238, 238), new Color(255, 205, 205),
        new Color(235, 125, 125), new Color(180, 20, 20)
    );

    private final JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final List<JButton> buttons = new ArrayList<>();
    private final boolean menuUiEnabled = ViewSettings.MENU_UI_ENABLED;

    private ViewListener listener;
    private String lastStatusSignature = "";
    private String lastMenuSignature = "";

    NavBarPanel() {
        setLayout(new BorderLayout(0, 2));
        setOpaque(false);

        // Border estilo XP
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(140, 160, 210)));

        // ── 1. ZONA NORTE: Barra de Menú ──────────────────────────────────────
        JPanel menuStatsPanel = new JPanel(new BorderLayout());
        menuStatsPanel.setOpaque(false);
        menuStatsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 200, 230)));

        // Menú simulado de Word 2003
        JLabel menuLabel = new JLabel("  Archivo  Edición  Ver  Insertar  Formato  Herramientas  Tabla  Ventana  ?");
        menuLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
        menuLabel.setForeground(new Color(60, 80, 110));
        menuStatsPanel.add(menuLabel, BorderLayout.WEST);

        add(menuStatsPanel, BorderLayout.NORTH);

        // ── 2. ZONA CENTRAL: Tarjetas del HUD ──────────────────────────────────
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 4));
        centerPanel.setOpaque(false);
        centerPanel.add(goldCard);
        centerPanel.add(livesCard);
        centerPanel.add(scoreCard);

        add(centerPanel, BorderLayout.CENTER);

        // ── 3. ZONA SUR: Atajos de Teclado (Tarjeta Amarilla) ─────────────────
        JLabel hint = new JLabel(" [1-8] Seleccionar  ·  Click colocar  ·  [U] Mejorar  ·  [S] Vender  ·  [Enter] Oleada  ·  [P] Pausa ");
        hint.setFont(new Font("Tahoma", Font.BOLD, 10));
        hint.setForeground(new Color(60, 60, 45));
        hint.setBackground(new Color(255, 255, 220)); // Amarillo post-it suave
        hint.setOpaque(true);
        hint.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 200, 150), 1),
            BorderFactory.createEmptyBorder(1, 10, 1, 10)
        ));

        JPanel hintWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 1));
        hintWrapper.setOpaque(false);
        hintWrapper.add(hint);

        add(hintWrapper, BorderLayout.SOUTH);

        actionsPanel.setOpaque(false);
        if (menuUiEnabled) {
            add(actionsPanel, BorderLayout.CENTER);
        } else {
            actionsPanel.setVisible(false);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // Gradiente que imita la barra de herramientas celeste clásica de Office 2003 / WinXP Luna Blue
        GradientPaint gp = new GradientPaint(
            0, 0, new Color(227, 239, 255),
            0, getHeight(), new Color(175, 203, 245)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Borde superior blanco brillante
        g2.setColor(new Color(255, 255, 255, 180));
        g2.drawLine(0, 0, getWidth(), 0);

        // Textura del "tirador" de barra de herramientas a la izquierda
        g2.setColor(new Color(120, 140, 180));
        for (int i = 6; i < getHeight() - 6; i += 4) {
            g2.fillRect(5, i, 2, 2);
            g2.fillRect(9, i, 2, 2);
        }
        g2.setColor(new Color(255, 255, 255, 200));
        for (int i = 7; i < getHeight() - 5; i += 4) {
            g2.fillRect(6, i, 1, 1);
            g2.fillRect(10, i, 1, 1);
        }

        super.paintComponent(g);
    }

    void setViewListener(ViewListener listener) {
        this.listener = listener;
    }

    void update(FrameSnapshot frame) {
        updateStatus(frame.getStatus());
        if (menuUiEnabled && frame.getMenu() != null) {
            updateMenu(frame.getMenu());
        }
    }

    private void updateStatus(GameStatus status) {
        String signature = status.getScore() + "|" + status.getGold() + "|" + status.getLives();
        if (signature.equals(lastStatusSignature)) {
            return;
        }
        lastStatusSignature = signature;

        scoreCard.setValue(String.valueOf(status.getScore()));
        goldCard.setValue(formatStat(status.getGold()));
        livesCard.setValue(formatStat(status.getLives()));
    }

    private static String formatStat(int value) {
        return value < 0 ? "—" : String.valueOf(value);
    }

    private void updateMenu(Menu menu) {
        String signature = menuSignature(menu);
        if (signature.equals(lastMenuSignature)) {
            updateEnabledStates(menu);
            return;
        }
        lastMenuSignature = signature;
        actionsPanel.removeAll();
        buttons.clear();
        for (Action action : menu.getActions()) {
            JButton button = new JButton(action.getLabel());
            button.setFont(button.getFont().deriveFont(Font.PLAIN, 15f));
            button.setEnabled(action.isEnabled());
            String actionId = action.getId();
            button.addActionListener(e -> fireAction(actionId));
            buttons.add(button);
            actionsPanel.add(button);
        }
        actionsPanel.revalidate();
        actionsPanel.repaint();
        revalidate();
        repaint();
    }

    private void fireAction(String actionId) {
        if (listener != null) {
            listener.onActionInvoked(actionId);
        }
    }

    private void updateEnabledStates(Menu menu) {
        List<? extends Action> actions = menu.getActions();
        for (int i = 0; i < buttons.size() && i < actions.size(); i++) {
            buttons.get(i).setEnabled(actions.get(i).isEnabled());
        }
    }

    private String menuSignature(Menu menu) {
        return menu.getTitle() + "|" + menu.getActions().stream()
                .map(a -> a.getId() + ":" + a.getLabel())
                .collect(Collectors.joining(","));
    }

    // ─── TARJETAS INDIVIDUALES DEL HUD ─────────────────────────────────────────
    private static class HudCard extends JPanel {
        enum IconType { COIN, HEART, STAR }
        
        private final IconType iconType;
        private final String title;
        private String value = "0";
        private final Color startColor;
        private final Color endColor;
        private final Color borderColor;
        private final Color textColor;

        HudCard(IconType iconType, String title, Color startColor, Color endColor, Color borderColor, Color textColor) {
            this.iconType = iconType;
            this.title = title;
            this.startColor = startColor;
            this.endColor = endColor;
            this.borderColor = borderColor;
            this.textColor = textColor;
            
            setPreferredSize(new Dimension(160, 42));
            setOpaque(false);
        }

        void setValue(String val) {
            this.value = val;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Fondo con gradiente y bordes redondeados
            GradientPaint gp = new GradientPaint(0, 0, startColor, 0, h, endColor);
            g2.setPaint(gp);
            g2.fillRoundRect(2, 2, w - 5, h - 5, 8, 8);

            // Borde fino
            g2.setColor(borderColor);
            g2.setStroke(new java.awt.BasicStroke(1.5f));
            g2.drawRoundRect(2, 2, w - 5, h - 5, 8, 8);

            // Dibujar el icono programático
            int iconX = 14;
            int iconY = (h - 22) / 2;
            int iconSize = 22;
            drawIcon(g2, iconX, iconY, iconSize);

            // Dibujar el texto
            g2.setColor(textColor);
            
            // Título (pequeño arriba)
            g2.setFont(new Font("Tahoma", Font.BOLD, 9));
            g2.drawString(title, 45, 14);

            // Valor (grande abajo)
            g2.setFont(new Font("Tahoma", Font.BOLD, 15));
            g2.drawString(value, 45, 30);

            g2.dispose();
        }

        private void drawIcon(Graphics2D g2, int x, int y, int size) {
            switch (iconType) {
                case COIN -> {
                    // Sombra de la moneda
                    g2.setColor(new Color(150, 100, 0, 60));
                    g2.fillOval(x + 1, y + 1, size, size);
                    // Moneda de oro
                    GradientPaint coinGrad = new GradientPaint(
                        x, y, new Color(255, 223, 0),
                        x + size, y + size, new Color(204, 153, 0)
                    );
                    g2.setPaint(coinGrad);
                    g2.fillOval(x, y, size, size);
                    // Borde interior fino
                    g2.setColor(new Color(255, 238, 120));
                    g2.setStroke(new java.awt.BasicStroke(1f));
                    g2.drawOval(x + 2, y + 2, size - 4, size - 4);
                    // Borde exterior
                    g2.setColor(new Color(180, 120, 0));
                    g2.drawOval(x, y, size, size);
                    // Signo de moneda
                    g2.setColor(new Color(140, 80, 0));
                    g2.setFont(new Font("Tahoma", Font.BOLD, 12));
                    java.awt.FontMetrics fm = g2.getFontMetrics();
                    String txt = "$";
                    g2.drawString(txt, x + (size - fm.stringWidth(txt)) / 2, y + (size + fm.getAscent() - fm.getDescent()) / 2 - 1);
                }
                case HEART -> {
                    // Formar corazón con Path2D
                    Path2D.Double path = new Path2D.Double();
                    path.moveTo(x + size / 2.0, y + size / 4.0);
                    path.curveTo(x + size / 2.0, y, x, y, x, y + size / 2.0);
                    path.curveTo(x, y + size * 0.73, x + size / 2.0, y + size * 0.95, x + size / 2.0, y + size * 0.95);
                    path.curveTo(x + size / 2.0, y + size * 0.95, x + size, y + size * 0.73, x + size, y + size / 2.0);
                    path.curveTo(x, y, x + size / 2.0, y, x + size / 2.0, y + size / 4.0); // Nota: corregido punto control final para cerrar
                    path.closePath();
                    
                    // Sombra desplazada
                    g2.translate(1, 1);
                    g2.setColor(new Color(100, 0, 0, 50));
                    g2.fill(path);
                    g2.translate(-1, -1);
                    
                    GradientPaint heartGrad = new GradientPaint(
                        x, y, new Color(255, 75, 75),
                        x + size, y + size, new Color(185, 10, 10)
                    );
                    g2.setPaint(heartGrad);
                    g2.fill(path);
                    
                    g2.setColor(new Color(145, 0, 0));
                    g2.setStroke(new java.awt.BasicStroke(1.2f));
                    g2.draw(path);
                    
                    // Brillo blanco
                    g2.setColor(new Color(255, 255, 255, 180));
                    g2.fillOval(x + 3, y + 3, 5, 4);
                }
                case STAR -> {
                    int midX = x + size/2;
                    int midY = y + size/2;
                    
                    Path2D.Double star = new Path2D.Double();
                    int numPoints = 5;
                    double outerRadius = size / 2.0;
                    double innerRadius = size / 4.0;
                    for (int i = 0; i < 2 * numPoints; i++) {
                        double r = (i % 2 == 0) ? outerRadius : innerRadius;
                        double angle = Math.PI * i / numPoints - Math.PI / 2;
                        double px = midX + Math.cos(angle) * r;
                        double py = midY + Math.sin(angle) * r;
                        if (i == 0) {
                            star.moveTo(px, py);
                        } else {
                            star.lineTo(px, py);
                        }
                    }
                    star.closePath();
                    
                    // Sombra desplazada
                    g2.translate(1, 1);
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fill(star);
                    g2.translate(-1, -1);
                    
                    GradientPaint starGrad = new GradientPaint(
                        x, y, new Color(255, 235, 100),
                        x + size, y + size, new Color(255, 180, 0)
                    );
                    g2.setPaint(starGrad);
                    g2.fill(star);
                    
                    g2.setColor(new Color(210, 140, 0));
                    g2.setStroke(new java.awt.BasicStroke(1f));
                    g2.draw(star);
                }
            }
        }
    }
}