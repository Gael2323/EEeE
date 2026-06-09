package com.miJuego.model;

import com.game2d.model.Drawable;
import com.game2d.model.FallbackShape;

import java.awt.*;
import java.net.URL;
import java.util.Optional;

/**
 * Representa el icono físico de premio tirado en la grilla.
 * Al hacer click sobre él, se desbloquea la torre McAfee y culmina el nivel.
 */
public class DroppedPrizeDrawable implements Drawable {
    private final float x;
    private final float y;

    public DroppedPrizeDrawable(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String getId() {
        return "dropped-prize";
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
        return 1.5f;
    }

    @Override
    public Float getHeight() {
        return 1.5f;
    }

    @Override
    public int getLayer() {
        return 98;
    }

    @Override
    public Optional<String> getImagePath() {
        return Optional.of("assets/ingame/popup_prize_f1.png");
    }

    @Override
    public Optional<URL> getImageUrl() {
        return Optional.empty();
    }

    @Override
    public Color getFallbackColor() {
        return Color.YELLOW;
    }

    @Override
    public FallbackShape getFallbackShape() {
        return FallbackShape.ELLIPSE;
    }
}
