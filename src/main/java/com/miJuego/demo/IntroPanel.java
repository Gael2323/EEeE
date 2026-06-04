package com.miJuego.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de la introducción — secuencia de páginas tipo cómic.
 *
 * <p>Muestra una imagen/texto por página. El jugador avanza con
 * click izquierdo, ESPACIO o ENTER. Puede volver atrás con click
 * derecho o BACKSPACE. Puede saltear toda la intro con ESC.</p>
 *
 * <p>Las páginas se agregan con {@link #addPage(IntroPage)} antes
 * de mostrar el panel.</p>
 */
public class IntroPanel extends JPanel implements MouseListener, KeyListener {

    // ─── Colores ─────────────────────────────────────────────────────────
    private static final Color BG_COLOR     = new Color(0, 0, 0);
    private static final Color TEXT_COLOR   = new Color(220, 220, 220);
    private static final Color HINT_COLOR   = new Color(130, 130, 130);
    private static final Color PAGE_COLOR   = new Color(80, 80, 80);

    // ─── Páginas ─────────────────────────────────────────────────────────
    private final List<IntroPage> pages = new ArrayList<>();
    private int currentPage = 0;

    // Callback cuando termina la intro
    private Runnable onFinished;

    // ─── Modelo de una página ────────────────────────────────────────────
    public static class IntroPage {
        String title;           // Texto grande (puede ser null)
        String subtitle;        // Texto chico debajo (puede ser null)
        String[] bodyLines;     // Líneas de texto (puede ser null)
        BufferedImage image;    // Imagen de fondo o ilustración (puede ser null)

        public IntroPage() {}

        public IntroPage title(String t)       { this.title = t; return this; }
        public IntroPage subtitle(String s)    { this.subtitle = s; return this; }
        public IntroPage body(String... lines) { this.bodyLines = lines; return this; }
        public IntroPage image(BufferedImage i){ this.image = i; return this; }

        /** Carga imagen desde resources. */
        public IntroPage imageFromResource(String path) {
            try {
                InputStream is = IntroPage.class.getResourceAsStream(path);
                if (is != null) this.image = ImageIO.read(is);
            } catch (IOException ignored) {}
            return this;
        }
    }

    public IntroPanel() {
        setPreferredSize(new Dimension(640, 480));
        setBackground(BG_COLOR);
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);
    }

    public void addPage(IntroPage page) {
        pages.add(page);
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    // ─── Pintado ─────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        // Fondo negro
        g2.setColor(BG_COLOR);
        g2.fillRect(0, 0, w, h);

        if (pages.isEmpty()) {
            g2.dispose();
            return;
        }

        IntroPage page = pages.get(currentPage);

        // Imagen de fondo (si tiene)
        if (page.image != null) {
            // Centrar y escalar manteniendo aspecto
            int imgW = page.image.getWidth();
            int imgH = page.image.getHeight();
            double scale = Math.min((double) w / imgW, (double) h / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int drawX = (w - drawW) / 2;
            int drawY = (h - drawH) / 2;
            g2.drawImage(page.image, drawX, drawY, drawW, drawH, null);

            // Overlay semi-transparente para que el texto sea legible
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRect(0, 0, w, h);
        }

        int y = h / 3;

        // Título
        if (page.title != null) {
            g2.setColor(TEXT_COLOR);
            g2.setFont(new Font("Tahoma", Font.BOLD, 32));
            FontMetrics fm = g2.getFontMetrics();
            int textX = (w - fm.stringWidth(page.title)) / 2;
            g2.drawString(page.title, textX, y);
            y += 50;
        }

        // Subtítulo
        if (page.subtitle != null) {
            g2.setColor(HINT_COLOR);
            g2.setFont(new Font("Tahoma", Font.ITALIC, 16));
            FontMetrics fm = g2.getFontMetrics();
            int textX = (w - fm.stringWidth(page.subtitle)) / 2;
            g2.drawString(page.subtitle, textX, y);
            y += 40;
        }

        // Cuerpo
        if (page.bodyLines != null) {
            g2.setColor(TEXT_COLOR);
            g2.setFont(new Font("Tahoma", Font.PLAIN, 15));
            FontMetrics fm = g2.getFontMetrics();
            for (String line : page.bodyLines) {
                int textX = (w - fm.stringWidth(line)) / 2;
                g2.drawString(line, textX, y);
                y += 24;
            }
        }

        // ── Indicador de página y hint ───────────────────────────────────
        g2.setColor(PAGE_COLOR);
        g2.setFont(new Font("Tahoma", Font.PLAIN, 12));
        String pageIndicator = (currentPage + 1) + " / " + pages.size();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(pageIndicator, (w - fm.stringWidth(pageIndicator)) / 2, h - 20);

        g2.setColor(HINT_COLOR);
        g2.setFont(new Font("Tahoma", Font.PLAIN, 13));
        String hint = "Click o ESPACIO para continuar  ·  ESC para saltar";
        fm = g2.getFontMetrics();
        g2.drawString(hint, (w - fm.stringWidth(hint)) / 2, h - 40);

        g2.dispose();
    }

    // ─── Navegación ──────────────────────────────────────────────────────

    private void nextPage() {
        if (currentPage < pages.size() - 1) {
            currentPage++;
            repaint();
        } else {
            finish();
        }
    }

    private void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            repaint();
        }
    }

    private void finish() {
        if (onFinished != null) onFinished.run();
    }

    // ─── Eventos ─────────────────────────────────────────────────────────

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            nextPage();
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            prevPage();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE, KeyEvent.VK_ENTER, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN -> nextPage();
            case KeyEvent.VK_BACK_SPACE, KeyEvent.VK_LEFT, KeyEvent.VK_UP -> prevPage();
            case KeyEvent.VK_ESCAPE -> finish();
        }
    }

    // ── Unused ────────────────────────────────────────────────────────────
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
