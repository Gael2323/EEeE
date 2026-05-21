package com.game2d.view;

import com.game2d.model.FallbackShape;
import com.game2d.model.Renderable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
/**
 * Carga imágenes desde archivo local o URL. Si falla, dibuja una figura de color plano.
 */
public interface ImageResolver {

    Image resolve(Renderable renderable, int widthPx, int heightPx);

    default Image createFallback(Renderable renderable, int widthPx, int heightPx) {
        int w = Math.max(1, widthPx);
        int h = Math.max(1, heightPx);
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(renderable.getFallbackColor());
        if (renderable.getFallbackShape() == FallbackShape.ELLIPSE) {
            g.fillOval(0, 0, w, h);
        } else {
            g.fillRect(0, 0, w, h);
        }
        g.dispose();
        return image;
    }

    static ImageResolver createDefault() {
        return ImageResolvers.getInstance().getResolver();
    }
}
