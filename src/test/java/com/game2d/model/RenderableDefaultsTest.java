package com.game2d.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderableDefaultsTest {

    @Test
    void defaultFallbackColorIsDarkGray() {
        Renderable renderable = new Renderable() {
            @Override
            public Optional<String> getImagePath() {
                return Optional.empty();
            }

            @Override
            public Optional<URL> getImageUrl() {
                return Optional.empty();
            }
        };
        assertEquals(Color.DARK_GRAY, renderable.getFallbackColor());
        assertEquals(FallbackShape.RECTANGLE, renderable.getFallbackShape());
    }

    @Test
    void actionCanOverrideFallback() {
        Action action = new Action() {
            @Override
            public String getId() {
                return "a";
            }

            @Override
            public String getLabel() {
                return "A";
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

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
                return Color.RED;
            }
        };
        assertEquals(Color.RED, action.getFallbackColor());
    }

    @Test
    void drawableDefaultsRotationAndLayer() {
        Drawable drawable = new Drawable() {
            @Override
            public String getId() {
                return "1";
            }

            @Override
            public Float getX() {
                return 0f;
            }

            @Override
            public Float getY() {
                return 0f;
            }

            @Override
            public Float getWidth() {
                return 1f;
            }

            @Override
            public Float getHeight() {
                return 1f;
            }

            @Override
            public Optional<String> getImagePath() {
                return Optional.empty();
            }

            @Override
            public Optional<URL> getImageUrl() {
                try {
                    return Optional.of(new URL("https://example.com/x.png"));
                } catch (MalformedURLException e) {
                    return Optional.empty();
                }
            }
        };
        assertEquals(0f, drawable.getRotationRadians());
        assertEquals(0, drawable.getLayer());
        assertTrue(drawable.getImageUrl().isPresent());
    }
}
