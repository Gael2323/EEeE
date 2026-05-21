package com.game2d.view;

import com.game2d.model.FallbackShape;
import com.game2d.model.Renderable;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ImageResolverTest {

    @Test
    void createFallbackRectangleUsesRenderableColor() {
        ImageResolver resolver = ImageResolver.createDefault();
        Renderable renderable = new Renderable() {
            @Override
            public Optional<String> getImagePath() {
                return Optional.of("/no-existe.png");
            }

            @Override
            public java.util.Optional<java.net.URL> getImageUrl() {
                return Optional.empty();
            }

            @Override
            public Color getFallbackColor() {
                return Color.BLUE;
            }
        };

        Image image = resolver.createFallback(renderable, 20, 10);
        assertNotNull(image);
        BufferedImage buffered = (BufferedImage) image;
        assertEquals(20, buffered.getWidth());
        assertEquals(10, buffered.getHeight());
        assertEquals(Color.BLUE.getRGB(), buffered.getRGB(5, 5));
    }

    @Test
    void resolveUsesFallbackWhenPathIsInvalid() {
        ImageResolver resolver = ImageResolver.createDefault();
        Renderable renderable = new Renderable() {
            @Override
            public Optional<String> getImagePath() {
                return Optional.of("archivo-inexistente-xyz.png");
            }

            @Override
            public java.util.Optional<java.net.URL> getImageUrl() {
                return Optional.empty();
            }

            @Override
            public Color getFallbackColor() {
                return Color.GREEN;
            }

            @Override
            public FallbackShape getFallbackShape() {
                return FallbackShape.ELLIPSE;
            }
        };

        Image image = resolver.resolve(renderable, 8, 8);
        assertNotNull(image);
        assertEquals(Color.GREEN.getRGB(), ((BufferedImage) image).getRGB(4, 4));
    }
}
