package com.game2d.view;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Mensajes temporales: solo texto en negrita (verde o rojo), sin fondo. No bloquean el juego.
 */
final class MessageToastOverlay {

    private static final int DISPLAY_MS = 3000;
    private static final int FADE_TICK_MS = 50;
    private static final Color SUCCESS_TEXT = new Color(0, 140, 0);
    private static final Color ERROR_TEXT = new Color(200, 0, 0);

    private final JPanel host;
    private Timer hideTimer;
    private Timer fadeTimer;
    private JLabel activeLabel;
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
        showText(message, SUCCESS_TEXT, ToastPlacement.TOP_CENTER);
    }

    void errorMessage(String message) {
        showText(message, ERROR_TEXT, ToastPlacement.TOP_RIGHT);
    }

    private void showText(String message, Color color, ToastPlacement placement) {
        Runnable show = () -> {
            cancelTimers();
            removeActiveLabel();

            this.placement = placement;
            activeLabel = new JLabel(message);
            activeLabel.setFont(activeLabel.getFont().deriveFont(Font.BOLD, 16f));
            activeLabel.setForeground(color);
            activeLabel.setOpaque(false);
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
        Color base = activeLabel.getForeground();
        activeLabel.setForeground(new Color(
                base.getRed(), base.getGreen(), base.getBlue(),
                Math.round(255 * opacity)));
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
}
