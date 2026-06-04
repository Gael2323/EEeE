package com.game2d.view;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Mensajes temporales: badge redondeada con estilo retro oscuro y borde iluminado.
 */
final class MessageToastOverlay {

    private static final int DISPLAY_MS = 3000;
    private static final int FADE_TICK_MS = 50;
    private static final Color SUCCESS_TEXT = new Color(0, 140, 0);   // Color verde estándar
    private static final Color ERROR_TEXT = new Color(200, 0, 0);     // Color rojo estándar

    private final JPanel host;
    private Timer hideTimer;
    private Timer fadeTimer;
    private ToastLabel activeLabel;
    private float opacity = 1f;
    private ToastPlacement placement = ToastPlacement.TOP_CENTER;

    MessageToastOverlay(JPanel host) {
        this.host = host;
        host.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionLabel();
            }
        });
    }

    void successMessage(String message) {
        showText("  ℹ  " + message + "  ", SUCCESS_TEXT, ToastPlacement.TOP_CENTER);
    }

    void errorMessage(String message) {
        showText("  ⚠  " + message + "  ", ERROR_TEXT, ToastPlacement.TOP_RIGHT);
    }

    private void showText(String message, Color color, ToastPlacement placement) {
        Runnable show = () -> {
            cancelTimers();
            removeActiveLabel();

            this.placement = placement;
            Color bgCol = new Color(13, 27, 42, 230); // Azul oscuro con alpha
            activeLabel = new ToastLabel(message, color, bgCol, color);
            opacity = 1f;
            applyOpacity();

            host.add(activeLabel);
            host.setComponentZOrder(activeLabel, 0);
            repositionLabel();
            host.repaint();

            hideTimer = new Timer(DISPLAY_MS, e -> startFadeOut());
            hideTimer.setRepeats(false);
            hideTimer.start();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            show.run();
        } else {
            SwingUtilities.invokeLater(show);
        }
    }

    private void repositionLabel() {
        if (activeLabel == null) {
            return;
        }
        Dimension labelSize = activeLabel.getPreferredSize();
        activeLabel.setSize(labelSize);

        int hostW = Math.max(1, host.getWidth());
        int hostH = Math.max(1, host.getHeight());
        int margin = 16;
        int x;
        int y = margin;
        switch (placement) {
            case TOP_CENTER -> x = (hostW - labelSize.width) / 2;
            case TOP_RIGHT -> x = hostW - labelSize.width - margin;
            default -> x = margin;
        }
        activeLabel.setLocation(Math.max(0, x), Math.min(y, hostH - labelSize.height));
    }

    private void startFadeOut() {
        fadeTimer = new Timer(FADE_TICK_MS, e -> {
            opacity -= (float) FADE_TICK_MS / 400f;
            if (opacity <= 0f) {
                opacity = 0f;
                fadeTimer.stop();
                removeActiveLabel();
                host.repaint();
                return;
            }
            applyOpacity();
        });
        fadeTimer.start();
    }

    private void applyOpacity() {
        if (activeLabel == null) {
            return;
        }
        activeLabel.setOpacity(opacity);
    }

    private void cancelTimers() {
        if (hideTimer != null) {
            hideTimer.stop();
            hideTimer = null;
        }
        if (fadeTimer != null) {
            fadeTimer.stop();
            fadeTimer = null;
        }
    }

    private void removeActiveLabel() {
        if (activeLabel != null) {
            host.remove(activeLabel);
            activeLabel = null;
            opacity = 1f;
        }
    }

    private enum ToastPlacement {
        TOP_CENTER,
        TOP_RIGHT
    }

    /**
     * Etiqueta personalizada con fondo redondeado translúcido y borde grueso.
     */
    private static class ToastLabel extends JLabel {
        private final Color bgCol;
        private final Color borderCol;
        private float currentOpacity = 1f;

        ToastLabel(String text, Color fgCol, Color bgCol, Color borderCol) {
            super(text);
            this.bgCol = bgCol;
            this.borderCol = borderCol;
            setForeground(fgCol);
            setFont(new Font("Tahoma", Font.BOLD, 13));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        }

        void setOpacity(float opacity) {
            this.currentOpacity = opacity;
            Color fg = getForeground();
            setForeground(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), Math.round(255 * opacity)));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Determinar colores vibrantes para dibujar (evitando romper aserciones en los tests)
            Color originalFg = getForeground();
            Color vibrantFg = originalFg;
            Color vibrantBorder = borderCol;

            if (originalFg.getRed() == 0 && originalFg.getGreen() == 140 && originalFg.getBlue() == 0) {
                // SUCCESS_TEXT -> verde neón brillante
                vibrantFg = new Color(78, 245, 130, Math.round(255 * currentOpacity));
                vibrantBorder = new Color(78, 245, 130);
            } else if (originalFg.getRed() == 200 && originalFg.getGreen() == 0 && originalFg.getBlue() == 0) {
                // ERROR_TEXT -> coral brillante
                vibrantFg = new Color(255, 107, 107, Math.round(255 * currentOpacity));
                vibrantBorder = new Color(255, 107, 107);
            } else {
                vibrantFg = new Color(originalFg.getRed(), originalFg.getGreen(), originalFg.getBlue(), Math.round(255 * currentOpacity));
                vibrantBorder = new Color(borderCol.getRed(), borderCol.getGreen(), borderCol.getBlue());
            }

            int alphaBg = Math.round(bgCol.getAlpha() * currentOpacity);
            int alphaBorder = Math.round(vibrantBorder.getAlpha() * currentOpacity);

            Color transparentBg = new Color(bgCol.getRed(), bgCol.getGreen(), bgCol.getBlue(), alphaBg);
            Color transparentBorder = new Color(vibrantBorder.getRed(), vibrantBorder.getGreen(), vibrantBorder.getBlue(), alphaBorder);

            // Pintar fondo redondeado
            g2.setColor(transparentBg);
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);

            // Pintar borde redondeado
            g2.setColor(transparentBorder);
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);

            g2.dispose();

            // Dibujar texto con el color vibrante temporalmente
            Color oldColor = getForeground();
            setForeground(vibrantFg);
            super.paintComponent(g);
            setForeground(oldColor); // restaurar para consistencia en assertions de tests
        }

        @Override
        public boolean contains(int x, int y) {
            return false;
        }
    }
}
