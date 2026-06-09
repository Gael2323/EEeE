package com.game2d.view;

import com.game2d.model.Drawable;
import com.game2d.model.FrameSnapshot;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.Comparator;
import java.util.List;

/**
 * Panel central donde se dibuja el fondo y los elementos del frame.
 */
final class GamePanel extends JPanel {

    private final ImageResolver imageResolver;
    private final BackgroundSettings background;
    private FrameSnapshot currentFrame;
    private long lastPaintTime = -1;

    /** Imagen de fondo en tamaño original (sin escalar) para el crop de la cámara. */
    private java.awt.image.BufferedImage rawBgImage;

    GamePanel(ImageResolver imageResolver, BackgroundSettings background) {
        this.imageResolver = imageResolver;
        this.background = background;
        setLayout(null);
        setFocusable(true);
        // Pre-cargar el fondo original en background
        loadRawBackground();
    }

    private String currentBgPath = null;

    private void loadRawBackground() {
        background.getImagePath().ifPresent(path -> {
            if (path.equals(currentBgPath) && rawBgImage != null) return;
            try {
                java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(path);
                if (is != null) {
                    rawBgImage = javax.imageio.ImageIO.read(is);
                    currentBgPath = path;
                }
            } catch (Exception ignored) {}
        });
    }

    void setFrame(FrameSnapshot frame) {
        this.currentFrame = frame;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        long now = System.nanoTime();
        if (lastPaintTime != -1) {
            float dt = (now - lastPaintTime) / 1_000_000_000f;
            dt = Math.min(dt, 0.1f);
            com.miJuego.model.CameraContext.tick(dt);
        }
        lastPaintTime = now;

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Aplicar offset de cámara al fondo: recortar la porción del viewport
        float camX    = com.miJuego.model.CameraContext.getCameraX();
        float camY    = com.miJuego.model.CameraContext.getCameraY();
        float vpW     = com.miJuego.model.CameraContext.VIEWPORT_W;
        float vpH     = com.miJuego.model.CameraContext.VIEWPORT_H;
        float WORLD_W = currentFrame != null ? currentFrame.getWorldWidth() : com.miJuego.model.CameraContext.getWorldW();
        float WORLD_H = currentFrame != null ? currentFrame.getWorldHeight() : com.miJuego.model.CameraContext.getWorldH();

        // Llenar el fondo de negro para los bordes cuando la cámara se aleja más allá del mapa
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, w, h);

        // Revisar si el path del fondo cambió (nuevo nivel) para recargar
        loadRawBackground();

        if (rawBgImage != null) {
            int imgW = rawBgImage.getWidth();
            int imgH = rawBgImage.getHeight();
            
            float sx = w / vpW;
            float sy = h / vpH;

            int destX = Math.round((0 - camX) * sx);
            int destY = Math.round((0 - camY) * sy);
            int destX2 = Math.round((WORLD_W - camX) * sx);
            int destY2 = Math.round((WORLD_H - camY) * sy);

            g2.drawImage(rawBgImage, destX, destY, destX2, destY2, 0, 0, imgW, imgH, null);
        } else {
            // Fallback si la imagen no cargó
            Image bg = imageResolver.resolve(background, w, h);
            g2.drawImage(bg, 0, 0, w, h, null);
        }

        if (currentFrame == null) {
            g2.dispose();
            return;
        }

        float worldW = currentFrame.getWorldWidth();
        float worldH = currentFrame.getWorldHeight();
        if (worldW <= 0 || worldH <= 0) {
            g2.dispose();
            return;
        }

        List<? extends Drawable> drawables = currentFrame.getDrawables();
        drawables.stream()
                .sorted(Comparator.comparingInt(Drawable::getLayer))
                .forEach(drawable -> paintDrawable(g2, drawable, w, h, worldW, worldH));

        g2.dispose();
    }

    private void paintDrawable(Graphics2D g2, Drawable drawable, int panelW, int panelH,
                               float worldW, float worldH) {
        // Viewport de la cámara
        float camX = com.miJuego.model.CameraContext.getCameraX();
        float camY = com.miJuego.model.CameraContext.getCameraY();
        float vpW  = com.miJuego.model.CameraContext.VIEWPORT_W;
        float vpH  = com.miJuego.model.CameraContext.VIEWPORT_H;

        // Escalar a píxeles usando el viewport (NO el mundo completo)
        float sx = panelW / vpW;
        float sy = panelH / vpH;

        boolean isEnemy = drawable.getClass().getSimpleName().contains("Enemigo") ||
                          drawable.getClass().getSimpleName().contains("PopUp") ||
                          drawable.getClass().getSimpleName().contains("Duende");

        float drawX = drawable.getX();
        float drawY = drawable.getY();
        if (isEnemy) {
            drawX -= drawable.getWidth() / 2f;
            drawY -= drawable.getHeight() / 2f;
        } else if (drawable.getId().equals("cursor") ||
                   drawable.getId().startsWith("torre-") ||
                   drawable.getClass().getSimpleName().contains("Torre")) {
            // Centrar la torre (ancho=2, alto=2) en la celda del grid (ancho=1, alto=1)
            drawX -= 0.5f;
            drawY -= 0.5f;
        }

        // Aplicar offset de cámara
        int x = Math.round((drawX - camX) * sx);
        int y = Math.round((drawY - camY) * sy);
        int dw = Math.max(1, Math.round(drawable.getWidth()  * sx));
        int dh = Math.max(1, Math.round(drawable.getHeight() * sy));

        // Frustum culling: descartar objetos fuera del viewport
        if (x + dw < 0 || x > panelW || y + dh < 0 || y > panelH) return;

        // Si es el drawable del tutorial interactivo de Clippy, usar su dibujado personalizado
        if (drawable instanceof com.miJuego.model.ClippyTutorialDrawable clippyDrawable) {
            clippyDrawable.draw(g2, x, y, dw, dh);
            return;
        }

        // Si es el drawable de la confrontación cinemática, usar su dibujado personalizado a pantalla completa
        if (drawable instanceof com.miJuego.model.ClippyConfrontationDrawable confrontationDrawable) {
            confrontationDrawable.draw(g2, panelW, panelH);
            return;
        }

        // Si es el segundo Clippy (duplicado/corrupto), usar su dibujado de grid personalizado
        if (drawable instanceof com.miJuego.model.SecondClippyDrawable secondClippyDrawable) {
            secondClippyDrawable.draw(g2, x, y, dw, dh);
            return;
        }

        // Si es un highlight, dibujarlo en perspectiva isométrica (rombo) directamente sobre el panel
        String id = drawable.getId();
        if (id != null && id.contains("highlight")) {
            g2.setColor(drawable.getFallbackColor());
            int[] px = { x + dw / 2, x + dw,     x + dw / 2, x };
            int[] py = { y,         y + dh / 2, y + dh,     y + dh / 2 };
            g2.fillPolygon(px, py, 4);

            // Borde del rombo con mayor opacidad para definir la celda
            Color col = drawable.getFallbackColor();
            g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), Math.min(255, col.getAlpha() + 70)));
            g2.setStroke(new java.awt.BasicStroke(1.5f));
            g2.drawPolygon(px, py, 4);
            return;
        }

        Image img = imageResolver.resolve(drawable, dw, dh);

        java.awt.Composite originalComposite = g2.getComposite();
        boolean isCursor = drawable.getId().equals("cursor");
        if (isCursor) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.70f));
        }

        g2.drawImage(img, x, y, dw, dh, null);

        if (isCursor) {
            g2.setComposite(originalComposite);
        }
    }

    float toWorldX(int pixelX) {
        if (currentFrame == null) return pixelX;
        float vpW  = com.miJuego.model.CameraContext.VIEWPORT_W;
        float camX = com.miJuego.model.CameraContext.getCameraX();
        return pixelX * vpW / Math.max(1, getWidth()) + camX;
    }

    float toWorldY(int pixelY) {
        if (currentFrame == null) return pixelY;
        float vpH  = com.miJuego.model.CameraContext.VIEWPORT_H;
        float camY = com.miJuego.model.CameraContext.getCameraY();
        return pixelY * vpH / Math.max(1, getHeight()) + camY;
    }
}
