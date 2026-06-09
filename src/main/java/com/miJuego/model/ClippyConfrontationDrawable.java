package com.miJuego.model;

import com.game2d.model.Drawable;
import com.game2d.model.FallbackShape;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Representa visualmente la confrontación cinematográfica.
 * En la fase inicial (in-game), dibuja globos clásicos de Office Assistant.
 * En la fase cinemática (FNF), dibuja letterbox, retratos gigantes, y efectos especiales de glitch.
 */
public class ClippyConfrontationDrawable implements Drawable {

    private static final Map<String, BufferedImage> clippyExpressions = new HashMap<>();
    private static BufferedImage defaultClippy;

    static {
        defaultClippy = loadClippyImage("clippy_sprite.png");
        loadClippyExpression("neutro", "clippy_neutro.png");
        loadClippyExpression("feliz", "clippy_feliz.png");
        loadClippyExpression("preocupado", "clippy_preocupado.png");
        loadClippyExpression("pensando", "clippy_pensando.png");
        loadClippyExpression("sorprendido", "clippySorprendido.png");
        loadClippyExpression("aprobando", "clippy_aprobando.png");
        loadClippyExpression("interesado", "clippy_interesado.png");
        
        // Cargar nuevos sprites pre-horneados
        loadClippyExpression("adm_mirando", "clippy_admdetareas_mirando.png");
        loadClippyExpression("adm_preocupado", "clippy_admdetareas_preocupado.png");
        loadClippyExpression("adm_sorprendido", "clippy_admdetareas_sorprendido.png");
        loadClippyExpression("corrupto_normal", "clippy_corrupto_posenormal.png");
        loadClippyExpression("corrupto_aprobado", "clippy_corrupto_aprobado.png");
    }

    private static BufferedImage loadClippyImage(String filename) {
        try {
            InputStream is = ClippyConfrontationDrawable.class.getResourceAsStream("/assets/word/" + filename);
            if (is != null) return javax.imageio.ImageIO.read(is);
        } catch (Exception e) {
            System.err.println("No se pudo cargar imagen de Clippy en confrontación: " + filename);
        }
        return null;
    }

    private static void loadClippyExpression(String key, String filename) {
        BufferedImage img = loadClippyImage(filename);
        if (img != null) {
            clippyExpressions.put(key, img);
        }
    }

    private final ClippyConfrontation confrontation;

    public ClippyConfrontationDrawable(ClippyConfrontation confrontation) {
        this.confrontation = confrontation;
    }

    @Override
    public String getId() {
        return "clippy-confrontation";
    }

    @Override
    public Float getX() { return 0f; }

    @Override
    public Float getY() { return 0f; }

    @Override
    public Float getWidth() { return 32f; }

    @Override
    public Float getHeight() { return 24f; }

    @Override
    public int getLayer() { return 200; } // Por encima de todo

    @Override
    public Optional<String> getImagePath() {
        return Optional.empty();
    }

    @Override
    public Optional<URL> getImageUrl() {
        return Optional.empty();
    }

    @Override
    public Color getFallbackColor() {
        return Color.BLACK;
    }

    @Override
    public FallbackShape getFallbackShape() {
        return FallbackShape.RECTANGLE;
    }

    /**
     * Dibuja toda la cinemática de confrontación.
     */
    public void draw(Graphics2D g2, int panelW, int panelH) {
        if (!confrontation.isActive()) return;
        if (!confrontation.isDialoguePhase()) return;

        if (!confrontation.isCinematicActive()) {
            // ── FASE 1: Diálogos In-Game (Globos Clásicos) ──
            float camX = com.miJuego.model.CameraContext.getCameraX();
            float camY = com.miJuego.model.CameraContext.getCameraY();
            float vpW  = com.miJuego.model.CameraContext.VIEWPORT_W;
            float vpH  = com.miJuego.model.CameraContext.VIEWPORT_H;
            float sx = panelW / vpW;
            float sy = panelH / vpH;

            // 1. Dibujar Clippy Común
            int cx = Math.round((confrontation.getClippyX() - camX) * sx);
            int cy = Math.round((confrontation.getClippyY() - camY) * sy);
            int cw = Math.max(1, Math.round(1.6f * sx));
            int ch = Math.max(1, Math.round(2.0f * sy));

            g2.setColor(new Color(245, 245, 235, 180));
            g2.fillRoundRect(cx - 8, cy - 8, cw + 16, ch + 16, 12, 12);
            g2.setColor(new Color(160, 170, 180, 120));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(cx - 8, cy - 8, cw + 16, ch + 16, 12, 12);

            g2.setColor(new Color(0, 0, 0, 45));
            g2.fillOval(cx + cw / 4, cy + ch - 8, cw / 2, 6);

            String expr = "neutro";
            int idx = confrontation.getDialogueIndex();
            if (idx == 0 || idx == 1) expr = "feliz";
            else if (idx == 2) expr = "preocupado";

            BufferedImage imgComun = clippyExpressions.getOrDefault(expr, defaultClippy);
            if (imgComun != null) {
                g2.drawImage(imgComun, cx, cy, cw, ch, null);
            }

            // 2. Dibujar Duplicado (normal in-game antes de corromperse)
            int dx = Math.round((confrontation.getDuplicateX() - camX) * sx);
            int dy = Math.round((confrontation.getDuplicateY() - camY) * sy);
            int dw = Math.max(1, Math.round(1.6f * sx));
            int dh = Math.max(1, Math.round(2.0f * sy));

            g2.setColor(new Color(245, 245, 235, 180));
            g2.fillRoundRect(dx - 8, dy - 8, dw + 16, dh + 16, 12, 12);
            g2.setColor(new Color(160, 170, 180, 120));
            g2.drawRoundRect(dx - 8, dy - 8, dw + 16, dh + 16, 12, 12);

            g2.setColor(new Color(0, 0, 0, 45));
            g2.fillOval(dx + dw / 4, dy + dh - 8, dw / 2, 6);

            if (defaultClippy != null) {
                g2.drawImage(defaultClippy, dx, dy, dw, dh, null);
            }

            // 3. Dibujar Globo de diálogo del hablante activo
            String speaker = confrontation.getSpeaker();
            String[] lines = confrontation.getCurrentLine();
            if ("COMUN".equals(speaker)) {
                drawSpeechBubble(g2, cx, cy, cw, ch, lines, true);
            } else if ("CORRUPTO".equals(speaker)) {
                drawSpeechBubble(g2, dx, dy, dw, dh, lines, false);
            }

        } else {
            // ── FASE 2: Diálogos Cinemáticos FNF (Letterbox) ──
            float progress = confrontation.getTransitionProgress();

            // 1. Oscurecer fondo (Opacidad reducida a 90 para inmersión del escenario de fondo, estilo Final Fantasy)
            g2.setColor(new Color(0, 0, 0, (int) (90 * progress)));
            g2.fillRect(0, 0, panelW, panelH);

            // 2. Letterbox (Barras Negras)
            int maxBarHeight = panelH / 8;
            int barHeight = (int) (maxBarHeight * progress);
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, panelW, barHeight);
            g2.fillRect(0, panelH - barHeight, panelW, barHeight);

            // 3. Cuadro de Diálogo FNF-style abajo (Dibujado PRIMERO para que los retratos queden encima en capas)
            int bx = 240; // Desplazado al centro para no tapar los retratos
            int bh = 125;
            int by = (int) (panelH - (barHeight * progress) - bh + 15);
            int bw = panelW - 480;

            g2.setColor(new Color(20, 20, 25, 200)); // Fondo oscuro semi-transparente
            g2.fillRoundRect(bx, by, bw, bh, 12, 12);

            String speaker = confrontation.getSpeaker();
            boolean comunActive = "COMUN".equals(speaker);
            boolean corruptActive = "CORRUPTO".equals(speaker);

            if (comunActive) {
                g2.setColor(new Color(50, 150, 250)); // Azul Clip Común
            } else if (corruptActive) {
                g2.setColor(new Color(180, 50, 220)); // Púrpura Clip Corrupto
            } else {
                g2.setColor(Color.GRAY);
            }
            g2.setStroke(new BasicStroke(2.2f));
            g2.drawRoundRect(bx, by, bw, bh, 12, 12);
            g2.setStroke(new BasicStroke(1f));

            // 4. Retrato Izquierdo (Clip Común) — Dibujado SEGUNDO (sobre la caja negra)
            int idx = confrontation.getDialogueIndex();
            String comunExpr = "neutro";
            if (idx == 0 || idx == 1) comunExpr = "feliz";
            else if (idx == 2) comunExpr = "preocupado";
            else if (idx == 3) comunExpr = "sorprendido";
            else if (idx == 4) comunExpr = "preocupado";
            else if (idx == 7) comunExpr = "sorprendido";
            else if (idx == 8 || idx == 10) comunExpr = "pensando";
            else if (idx == 14) comunExpr = "interesado";
            else if (idx == 18) comunExpr = "interesado";
            else if (idx == 22) comunExpr = "aprobando";
            else if (idx == 25) comunExpr = "adm_mirando";
            else if (idx == 26 || idx == 27 || idx == 28) comunExpr = "adm_preocupado";
            else if (idx == 29) comunExpr = "adm_sorprendido";
            else if (idx == 32) comunExpr = "feliz";
            else if (idx == 33 || idx == 34) comunExpr = "sorprendido";
            else if (idx == 37 || idx == 38) comunExpr = "preocupado";
            else if (idx == 41 || idx == 42) comunExpr = "aprobando";
            else if (idx >= 46 && idx <= 47) comunExpr = "preocupado";
            else if (idx == 50) comunExpr = "preocupado";
            else if (idx == 52) comunExpr = "preocupado";
            else if (idx == 54) comunExpr = "sorprendido";
            else if (idx == 56) comunExpr = "sorprendido";
            else if (idx >= 60 && idx <= 62) comunExpr = "preocupado";
            else if (idx == 63) comunExpr = "sorprendido";
            else if (idx == 64) comunExpr = "pensando";
            else if (idx == 65) comunExpr = "preocupado";
            else if (idx == 66) comunExpr = "interesado";
            else if (idx == 67) comunExpr = "feliz";
            else if (idx == 68) comunExpr = "aprobando";

            BufferedImage imgComun = clippyExpressions.getOrDefault(commExpr(idx, comunExpr), defaultClippy);
            if (imgComun != null) {
                int portraitW = 180;
                int portraitH = 225;
                int px = 40; // Desplazado al extremo izquierdo
                int py = panelH - barHeight - portraitH - 10;

                if (comunActive) {
                    portraitW = (int) (portraitW * 1.05);
                    portraitH = (int) (portraitH * 1.05);
                    py -= 10;
                }

                int shakeX = 0;
                int shakeY = 0;
                if (idx == 32 || idx == 54 || idx == 56) {
                    shakeX = (int) (Math.random() * 6 - 3);
                    shakeY = (int) (Math.random() * 4 - 2);
                }
                int finalPx = px + shakeX;
                int finalPy = py + shakeY;

                Composite origComposite = g2.getComposite();
                float alpha = comunActive ? 1.0f : 0.45f;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * progress));

                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillOval(finalPx + portraitW / 4, finalPy + portraitH - 10, portraitW / 2, 8);

                g2.drawImage(imgComun, finalPx, finalPy, portraitW, portraitH, null);

                g2.setComposite(origComposite);
            }

            if (confrontation.getEstadoActual() != ClippyConfrontation.Estado.TALK_POST_PRIZE) {
                // 5. Retrato Derecho (Duplicado / Corrupto) — Dibujado TERCERO (sobre la caja negra)
                String corruptExpr = "neutro";
                if (confrontation.isCorrupt()) {
                    if (idx == 55 || idx == 57 || idx == 58 || idx == 59) {
                        corruptExpr = "corrupto_aprobado";
                    } else {
                        corruptExpr = "corrupto_normal";
                    }
                } else {
                    if (idx == 5 || idx == 8 || idx == 11) {
                        corruptExpr = "feliz";
                    } else if (idx == 15 || idx == 17) {
                        corruptExpr = "neutro";
                    } else if (idx >= 19 && idx <= 21) {
                        corruptExpr = "preocupado";
                    } else if (idx == 23 || idx == 24) {
                        corruptExpr = "preocupado";
                    } else if (idx == 31) {
                        corruptExpr = "preocupado";
                    } else if (idx == 35 || idx == 36) {
                        corruptExpr = "preocupado";
                    } else if (idx == 39 || idx == 40) {
                        corruptExpr = "feliz";
                    }
                }

                BufferedImage imgCorrupt = clippyExpressions.getOrDefault(corruptExpr, defaultClippy);
                if (imgCorrupt != null) {
                    int portraitW = 180;
                    int portraitH = 225;
                    int px = panelW - 40 - portraitW; // Desplazado al extremo derecho
                    int py = panelH - barHeight - portraitH - 10;

                    if (corruptActive) {
                        portraitW = (int) (portraitW * 1.05);
                        portraitH = (int) (portraitH * 1.05);
                        px -= 5;
                        py -= 10;
                    }

                    Composite origComposite = g2.getComposite();
                    float alpha = corruptActive ? 1.0f : 0.45f;
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * progress));

                    g2.setColor(new Color(0, 0, 0, 50));
                    g2.fillOval(px + portraitW / 4, py + portraitH - 10, portraitW / 2, 8);

                    int jitterX = corruptActive ? (int) (Math.random() * 6 - 3) : 0;
                    int jitterY = corruptActive ? (int) (Math.random() * 4 - 2) : 0;

                    if (confrontation.isGlitchActive()) {
                        // Cortes glitch de la imagen ya corrupta
                        if (Math.random() < 0.4) {
                            int slices = 4;
                            int sliceH = portraitH / slices;
                            for (int i = 0; i < slices; i++) {
                                int sliceJitter = (Math.random() < 0.5) ? (int) (Math.random() * 12 - 6) : 0;
                                g2.drawImage(imgCorrupt,
                                        px + jitterX + sliceJitter, py + jitterY + i * sliceH, px + portraitW + jitterX + sliceJitter, py + jitterY + (i + 1) * sliceH,
                                        0, i * (imgCorrupt.getHeight() / slices), imgCorrupt.getWidth(), (i + 1) * (imgCorrupt.getHeight() / slices),
                                        null);
                            }
                        } else {
                            g2.drawImage(imgCorrupt, px + jitterX, py + jitterY, portraitW, portraitH, null);
                        }
                    } else {
                        g2.drawImage(imgCorrupt, px + jitterX, py + jitterY, portraitW, portraitH, null);
                    }

                    g2.setComposite(origComposite);
                }
            }

            // 6. Escribir Texto del Diálogo (Dibujado al final para máxima claridad)
            g2.setFont(new Font("Tahoma", Font.BOLD, 14));
            if (comunActive) {
                g2.setColor(Color.WHITE);
                g2.drawString("CLIP COMÚN", bx + 20, by + 28);
            } else if (corruptActive) {
                g2.setColor(Color.WHITE);
                g2.drawString(confrontation.isCorrupt() ? "CLIP CORRUPTO" : "DUPLICADO", bx + 20, by + 28);
            }

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Tahoma", Font.PLAIN, 18));
            String[] lines = confrontation.getCurrentLine();
            int ty = by + 58;
            for (String line : lines) {
                g2.drawString(line, bx + 20, ty);
                ty += 22;
            }

            g2.setFont(new Font("Tahoma", Font.ITALIC, 10));
            g2.setColor(new Color(180, 180, 180));
            String hint = "Presiona ESPACIO o ENTER para continuar";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(hint, bx + bw - fm.stringWidth(hint) - 20, by + bh - 15);
        }
    }

    private String commExpr(int idx, String defVal) {
        return defVal;
    }

    private void drawSpeechBubble(Graphics2D g2, int clippyX, int clippyY, int clippyW, int clippyH, String[] lines, boolean bubbleOnRight) {
        g2.setFont(new Font("Tahoma", Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();
        int maxTextW = 160; // Ancho mínimo
        for (String line : lines) {
            maxTextW = Math.max(maxTextW, fm.stringWidth(line));
        }
        int bw = maxTextW + 24; // Ancho dinámico del globo de diálogo según el texto
        int bh = 30 + lines.length * 18 + 12;
        int by = clippyY - 20;
        int bx;

        if (bubbleOnRight) {
            bx = clippyX + clippyW + 15;
        } else {
            bx = clippyX - bw - 15;
        }

        // Sombra
        g2.setColor(new Color(0, 0, 0, 35));
        g2.fillRoundRect(bx + 3, by + 3, bw, bh, 10, 10);

        // Fondo
        g2.setColor(new Color(255, 255, 225));
        g2.fillRoundRect(bx, by, bw, bh, 10, 10);

        // Borde
        g2.setColor(new Color(110, 105, 90));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(bx, by, bw, bh, 10, 10);

        // Cola
        int tailBaseY = by + 20;
        int[] px, py;
        if (bubbleOnRight) {
            px = new int[]{bx, bx, clippyX + clippyW};
            py = new int[]{tailBaseY, tailBaseY + 12, clippyY + clippyH / 2};
        } else {
            px = new int[]{bx + bw, bx + bw, clippyX};
            py = new int[]{tailBaseY, tailBaseY + 12, clippyY + clippyH / 2};
        }

        g2.setColor(new Color(255, 255, 225));
        g2.fillPolygon(px, py, 3);
        g2.setColor(new Color(110, 105, 90));
        g2.drawLine(px[0], py[0], px[2], py[2]);
        g2.drawLine(px[1], py[1], px[2], py[2]);
        g2.setStroke(new BasicStroke(1f));

        // Escribir Texto
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Tahoma", Font.PLAIN, 12));
        int ty = by + 20;
        for (String line : lines) {
            g2.drawString(line, bx + 12, ty);
            ty += 18;
        }

        // Pista
        g2.setFont(new Font("Tahoma", Font.ITALIC, 9));
        g2.setColor(new Color(100, 100, 100));
        String hint = "ESPACIO o click para avanzar";
        fm = g2.getFontMetrics();
        g2.drawString(hint, bx + bw - fm.stringWidth(hint) - 10, by + bh - 6);
    }
}
