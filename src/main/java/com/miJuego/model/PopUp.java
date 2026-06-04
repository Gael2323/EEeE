package com.miJuego.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Enemigo del nivel Word: una ventana emergente de internet (Pop-Up).
 *
 * <p>Visualmente es una ventanita de error/anuncio antiguo que avanza
 * sobre las líneas de texto del documento. Usa el sprite de pop-up
 * generado para la demo.</p>
 */
public class PopUp extends Enemigo {

    /** Variantes visuales del Pop-Up (distintos anuncios de internet). */
    public enum Variante {
        ERROR,     // Ventana de error Windows XP
        PREMIO,    // "YOU WON A PRIZE!"
        DESCARGA   // "FREE DOWNLOAD!"
    }

    private final Variante variante;

    public PopUp(String id, Variante variante) {
        super(id, "PopUp", 40.0, 10, 5);
        this.rapidez  = 2.5;
        this.dañoBase = 1.0;
        this.variante = variante;
        this.width = 1.15f;
        this.height = 1.15f;
    }

    /** Constructor simple — usa variante ERROR por defecto. */
    public PopUp(String id) {
        this(id, Variante.ERROR);
    }

    @Override
    public List<Enemigo> morir() {
        return new ArrayList<>();
    }

    /**
     * Animación de caminar con 5 frames (0-4) ciclando.
     *
     * <p>El {@link com.game2d.view.DefaultImageResolver} cachea imágenes por nombre,
     * así que todos los frames se cargan una sola vez y la alternancia es instantánea.</p>
     */
    @Override
    public Optional<String> getImagePath() {
        int frame = getAnimationFrame();

        String base = switch (variante) {
            case ERROR    -> "assets/ingame/cartelErrorDerecha";
            case PREMIO   -> "assets/ingame/CartelAnuncioPremio";
            case DESCARGA -> "assets/ingame/PopUpDownload";
        };
        return Optional.of(base + frame + ".png");
    }

    @Override
    public Color getFallbackColor() {
        if (paralizacionTimer > 0 || ralentizarTimer > 0 || fuegoTimer > 0) {
            return super.getFallbackColor();
        }
        return switch (variante) {
            case ERROR    -> new Color(220, 60,  60);  // Rojo error
            case PREMIO   -> new Color(255, 200, 0);   // Amarillo premio
            case DESCARGA -> new Color(40,  180, 40);  // Verde descarga
        };
    }
}
