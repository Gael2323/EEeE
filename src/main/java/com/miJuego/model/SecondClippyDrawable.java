package com.miJuego.model;

import com.game2d.model.Drawable;
import com.game2d.model.FallbackShape;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

/**
 * Representa al segundo Clippy (duplicado/corrupto) en la grilla del juego.
 * Soporta renderizado normal y corrupto con efectos de glitch visual.
 */
public class SecondClippyDrawable implements Drawable {
    private final float x;
    private final float y;
    private final boolean corrupt;

    private static BufferedImage clippyImg;
    private static BufferedImage corruptImg;

    static {
        try {
            InputStream is = SecondClippyDrawable.class.getResourceAsStream("/assets/word/clippy_sprite.png");
            if (is != null) {
                clippyImg = javax.imageio.ImageIO.read(is);
            }
            InputStream is2 = SecondClippyDrawable.class.getResourceAsStream("/assets/word/clippy_corrupto_posenormal.png");
            if (is2 != null) {
                corruptImg = javax.imageio.ImageIO.read(is2);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen de Clippy para SecondClippyDrawable.");
        }
    }

    public SecondClippyDrawable(float x, float y) {
        this(x, y, false);
    }

    public SecondClippyDrawable(float x, float y, boolean corrupt) {
        this.x = x;
        this.y = y;
        this.corrupt = corrupt;
    }

    @Override
    public String getId() {
        return "second-clippy";
    }

    @Override
    public Float getX() {
        return x;
    }

    @Override
    public Float getY() {
        return y;
    }

    @Override
    public Float getWidth() {
        return 1.6f;
    }

    @Override
    public Float getHeight() {
        return 2.0f;
    }

    @Override
    public int getLayer() {
        return 99;
    }

    @Override
    public Optional<String> getImagePath() {
        return Optional.of(corrupt ? "assets/word/clippy_corrupto_posenormal.png" : "assets/word/clippy_sprite.png");
    }

    @Override
    public Optional<URL> getImageUrl() {
        return Optional.empty();
    }

    @Override
    public Color getFallbackColor() {
        return corrupt ? new Color(130, 0, 180) : new Color(220, 180, 0);
    }

    @Override
    public FallbackShape getFallbackShape() {
        return FallbackShape.RECTANGLE;
    }

    /**
     * Dibuja al segundo Clippy. Aplica el sprite corrupto pre-horneado
     * y efecto glitch de vibración si está corrupto.
     */
    public void draw(Graphics2D g2, int cx, int cy, int cw, int ch) {
        // Dibujar sombra
        g2.setColor(new Color(0, 0, 0, 45));
        g2.fillOval(cx + cw / 4, cy + ch - 8, cw / 2, 6);

        BufferedImage img = corrupt ? corruptImg : clippyImg;
        if (img == null) {
            g2.setColor(getFallbackColor());
            g2.fillRect(cx, cy, cw, ch);
            return;
        }

        if (!corrupt) {
            g2.drawImage(img, cx, cy, cw, ch, null);
        } else {
            int jitterX = (int) (Math.random() * 6 - 3);
            int jitterY = (int) (Math.random() * 4 - 2);

            if (Math.random() < 0.4) {
                int slices = 3;
                int sliceH = ch / slices;
                for (int i = 0; i < slices; i++) {
                    int sliceJitter = (Math.random() < 0.5) ? (int) (Math.random() * 8 - 4) : 0;
                    g2.drawImage(img,
                            cx + jitterX + sliceJitter, cy + jitterY + i * sliceH, cx + cw + jitterX + sliceJitter, cy + jitterY + (i + 1) * sliceH,
                            0, i * (img.getHeight() / slices), img.getWidth(), (i + 1) * (img.getHeight() / slices),
                            null);
                }
            } else {
                g2.drawImage(img, cx + jitterX, cy + jitterY, cw, ch, null);
            }
        }
    }
}
