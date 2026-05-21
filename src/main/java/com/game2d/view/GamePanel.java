package com.game2d.view;

import com.game2d.model.Drawable;
import com.game2d.model.FrameSnapshot;

import javax.swing.JPanel;
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

    GamePanel(ImageResolver imageResolver, BackgroundSettings background) {
        this.imageResolver = imageResolver;
        this.background = background;
        setLayout(null);
        setFocusable(true);
    }

    void setFrame(FrameSnapshot frame) {
        this.currentFrame = frame;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        Image bg = imageResolver.resolve(background, w, h);
        g2.drawImage(bg, 0, 0, w, h, null);

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
        float sx = panelW / worldW;
        float sy = panelH / worldH;

        int x = Math.round(drawable.getX() * sx);
        int y = Math.round(drawable.getY() * sy);
        int dw = Math.max(1, Math.round(drawable.getWidth() * sx));
        int dh = Math.max(1, Math.round(drawable.getHeight() * sy));

        Image img = imageResolver.resolve(drawable, dw, dh);
        g2.drawImage(img, x, y, dw, dh, null);
    }

    float toWorldX(int pixelX) {
        if (currentFrame == null) {
            return pixelX;
        }
        float worldW = currentFrame.getWorldWidth();
        return pixelX * worldW / Math.max(1, getWidth());
    }

    float toWorldY(int pixelY) {
        if (currentFrame == null) {
            return pixelY;
        }
        float worldH = currentFrame.getWorldHeight();
        return pixelY * worldH / Math.max(1, getHeight());
    }
}
