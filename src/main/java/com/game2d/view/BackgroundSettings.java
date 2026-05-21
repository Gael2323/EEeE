package com.game2d.view;

import com.game2d.model.FallbackShape;
import com.game2d.model.Renderable;

import java.awt.Color;
import java.net.URL;
import java.util.Optional;

/**
 * Fondo fijo de la pantalla de juego. No viene del modelo: el alumno puede cambiar
 * la ruta o URL editando esta clase (o llamando a los setters al iniciar su aplicación).
 */
public final class BackgroundSettings implements Renderable {

    private static final BackgroundSettings INSTANCE = new BackgroundSettings();

    private String imagePath = "assets/background.png";
    private URL imageUrl = null;
    private Color fallbackColor = new Color(30, 30, 40);
    private FallbackShape fallbackShape = FallbackShape.RECTANGLE;

    private BackgroundSettings() {
    }

    public static BackgroundSettings getInstance() {
        return INSTANCE;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setImageUrl(URL imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setFallbackColor(Color fallbackColor) {
        this.fallbackColor = fallbackColor;
    }

    @Override
    public Optional<String> getImagePath() {
        return imagePath == null || imagePath.isBlank()
                ? Optional.empty()
                : Optional.of(imagePath);
    }

    @Override
    public Optional<URL> getImageUrl() {
        return Optional.ofNullable(imageUrl);
    }

    @Override
    public Color getFallbackColor() {
        return fallbackColor;
    }

    @Override
    public FallbackShape getFallbackShape() {
        return fallbackShape;
    }
}
