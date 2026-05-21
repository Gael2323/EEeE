package com.game2d.view;

import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Color;
import javax.swing.JLabel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MessageToastOverlayTest {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void successMessageAddsToastPanel() throws Exception {
        runOnEdt(() -> {
            JPanel host = new JPanel(null);
            host.setSize(400, 300);
            MessageToastOverlay overlay = new MessageToastOverlay(host);
            overlay.successMessage("Listo");
            assertTrue(host.getComponentCount() > 0);
            JLabel label = (JLabel) host.getComponent(0);
            assertFalse(label.isOpaque());
            assertEquals(new Color(0, 140, 0), label.getForeground());
        });
    }

    @Test
    void errorMessageAddsToastPanel() throws Exception {
        runOnEdt(() -> {
            JPanel host = new JPanel(null);
            host.setSize(400, 300);
            MessageToastOverlay overlay = new MessageToastOverlay(host);
            overlay.errorMessage("Falló");
            assertTrue(host.getComponentCount() > 0);
            JLabel label = (JLabel) host.getComponent(0);
            assertEquals(new Color(200, 0, 0), label.getForeground());
        });
    }

    @Test
    void gameViewDeclaresMessageMethods() throws NoSuchMethodException {
        assertNotNull(GameView.class.getMethod("successMessage", String.class));
        assertNotNull(GameView.class.getMethod("errorMessage", String.class));
    }

    private static void runOnEdt(Runnable action) throws Exception {
        SwingUtilities.invokeAndWait(action);
    }
}
