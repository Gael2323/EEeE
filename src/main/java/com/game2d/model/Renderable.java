package com.game2d.model;

import java.awt.Color;
import java.net.URL;
import java.util.Optional;

/**
 * Describe cómo obtener una imagen para dibujar algo en pantalla.
 * Si la imagen no carga, la vista usa {@link #getFallbackColor()} y {@link #getFallbackShape()}.
 */
public interface Renderable {

    Optional<String> getImagePath();

    Optional<URL> getImageUrl();

    default Color getFallbackColor() {
        return Color.DARK_GRAY;
    }

    default FallbackShape getFallbackShape() {
        return FallbackShape.RECTANGLE;
    }
}
