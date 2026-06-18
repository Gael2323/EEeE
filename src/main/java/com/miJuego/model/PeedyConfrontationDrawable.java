package com.miJuego.model;

import com.game2d.model.Drawable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.net.URL;
import com.game2d.model.FallbackShape;

public class PeedyConfrontationDrawable implements Drawable {

    private PeedyConfrontation confrontation;
    private BufferedImage clippySprite;
    private BufferedImage peedyLanding1;
    private BufferedImage peedyLanding2;
    private BufferedImage peedyStand;
    private BufferedImage peedyFly;

    public PeedyConfrontationDrawable(PeedyConfrontation confrontation) {
        this.confrontation = confrontation;
        try {
            clippySprite = ImageIO.read(getClass().getResourceAsStream("/assets/word/clippy_sprite.png"));
            peedyLanding1 = ImageIO.read(getClass().getResourceAsStream("/assets/ingame/enemies/boss_Peedy/peedy_cayendo_picada_superherolanding_0.png"));
            peedyLanding2 = ImageIO.read(getClass().getResourceAsStream("/assets/ingame/enemies/boss_Peedy/peedy_recomponiendose_del_superherolanding_4.png"));
            peedyStand = ImageIO.read(getClass().getResourceAsStream("/assets/ingame/enemies/boss_Peedy/Peedy_Parado0_0.png"));
            peedyFly = ImageIO.read(getClass().getResourceAsStream("/assets/ingame/enemies/boss_Peedy/Peedy_Volando0_0.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error cargando sprites de PeedyConfrontation: " + e.getMessage());
        }
    }

    @Override
    public String getId() { return "peedy-confrontation"; }
    @Override
    public Float getX() { return 0f; }
    @Override
    public Float getY() { return 0f; }
    @Override
    public Float getWidth() { return 32f; }
    @Override
    public Float getHeight() { return 24f; }
    @Override
    public int getLayer() { return 200; }
    @Override
    public Optional<String> getImagePath() { return Optional.empty(); }
    @Override
    public Optional<URL> getImageUrl() { return Optional.empty(); }
    @Override
    public Color getFallbackColor() { return Color.BLACK; }
    @Override
    public FallbackShape getFallbackShape() { return FallbackShape.RECTANGLE; }

    public void draw(Graphics g, int panelW, int panelH) {
        if (!confrontation.isActive()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        
        // Oscurecer ligeramente el fondo para darle foco a la cinemática
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRect(0, 0, panelW, panelH);

        // Variables de la camara
        float camX = CameraContext.getCameraX();
        float camY = CameraContext.getCameraY();
        float scaleX = panelW / CameraContext.VIEWPORT_W;
        float scaleY = panelH / CameraContext.VIEWPORT_H;

        // Posiciones
        float cx = (confrontation.getClippyX() - camX) * scaleX;
        float cy = (confrontation.getClippyY() - camY) * scaleY;
        int cw = Math.max(1, Math.round(2.0f * scaleX));
        int ch = Math.max(1, Math.round(2.0f * scaleY));

        float px = (confrontation.getPeedyX() - camX) * scaleX;
        float py = (confrontation.getPeedyY() - camY) * scaleY;
        int pw = Math.max(1, Math.round(3.0f * scaleX)); // Peedy es un boss grande
        int ph = Math.max(1, Math.round(3.0f * scaleY));

        // DIBUJAR CLIPPY
        if (clippySprite != null) {
            g2.drawImage(clippySprite, (int) cx, (int) cy, cw, ch, null);
        }

        // DIBUJAR PEEDY
        if (confrontation.getEstadoActual() == PeedyConfrontation.Estado.PEEDY_LANDING) {
            float timer = confrontation.getLandingTimer();
            if (timer < 1.0f && peedyLanding1 != null) {
                g2.drawImage(peedyLanding1, (int) px, (int) py, pw, ph, null);
            } else if (timer < 3.0f && peedyLanding2 != null) {
                g2.drawImage(peedyLanding2, (int) px, (int) py, pw, ph, null);
            } else if (peedyStand != null) {
                g2.drawImage(peedyStand, (int) px, (int) py, pw, ph, null);
            }
        } else if (confrontation.getEstadoActual() == PeedyConfrontation.Estado.TALK_PEEDY) {
            if (peedyStand != null) {
                g2.drawImage(peedyStand, (int) px, (int) py, pw, ph, null);
            }
        }

        // GLOBOS DE DIÁLOGO
        if (confrontation.isDialoguePhase()) {
            String[] lines = confrontation.getCurrentLines();
            int idx = confrontation.getDialogueIndex();
            
            boolean isPeedyTalking = (confrontation.getEstadoActual() == PeedyConfrontation.Estado.TALK_PEEDY && idx >= 4 && idx <= 6);
            
            if (isPeedyTalking) {
                drawSpeechBubble(g2, (int) px, (int) py, pw, ph, lines, false);
            } else {
                drawSpeechBubble(g2, (int) cx, (int) cy, cw, ch, lines, true);
            }
        }
    }

    private void drawSpeechBubble(Graphics2D g2, int charX, int charY, int charW, int charH, String[] lines, boolean isClippy) {
        if (lines == null || lines.length == 0) return;

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();

        int maxW = 0;
        for (String line : lines) {
            maxW = Math.max(maxW, fm.stringWidth(line));
        }

        int padding = 12;
        int boxW = maxW + padding * 2;
        int boxH = lines.length * fm.getHeight() + padding * 2;

        int bx = charX + charW + 10;
        int by = charY - boxH / 2;

        // Si se sale de la pantalla, mover
        if (bx + boxW > g2.getClipBounds().width) {
            bx = charX - boxW - 10;
        }

        g2.setColor(new Color(255, 255, 245, 240));
        g2.fillRoundRect(bx, by, boxW, boxH, 12, 12);

        if (isClippy) {
            g2.setColor(new Color(160, 170, 180));
        } else {
            g2.setColor(new Color(200, 50, 50)); // Rojo para Peedy
        }
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(bx, by, boxW, boxH, 12, 12);
        g2.setStroke(new BasicStroke(1.0f));

        g2.setColor(Color.BLACK);
        int ty = by + padding + fm.getAscent();
        for (String line : lines) {
            g2.drawString(line, bx + padding, ty);
            ty += fm.getHeight();
        }
    }
}
