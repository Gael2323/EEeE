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
        this.animFrameCount = 8; // La animación completa de caminata direccional tiene 8 frames
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

        if (variante == Variante.ERROR) {
            if (currentOctant == 6) {
                // Para la izquierda (octante 6), el usuario añadió "img X.png"
                return Optional.of("assets/ingame/enemies/popup_error/img " + frame + ".png");
            } else {
                // Mapeo corregido según las indicaciones
                int fileIndex = switch (currentOctant) {
                    case 0 -> 6; // Arriba (0) usa los sprites marcados con 6
                    case 1 -> 7; // Arriba-Derecha (1) usa los sprites marcados con 7
                    case 2 -> 0; // Derecha (2) usa los sprites marcados con 0
                    case 3 -> 1; // Abajo-Derecha (3) usa los sprites marcados con 1
                    case 4 -> 2; // Abajo (4) usa los sprites marcados con 2
                    case 5 -> 3; // Abajo-Izquierda (5) usa los sprites marcados con 3
                    case 7 -> 5; // Arriba-Izquierda (7) usa los sprites marcados con 5
                    default -> 2;
                };
                return Optional.of("assets/ingame/enemies/popup_error/popup_error_" + fileIndex + "_" + frame + ".png");
            }
        } else {
            // Premio y Descarga aún tienen los sprites genéricos viejos de 5 frames (0 a 4)
            int oldFrame = frame % 5;
            String base = (variante == Variante.PREMIO) ? "assets/ingame/CartelAnuncioPremio" : "assets/ingame/PopUpDownload";
            return Optional.of(base + oldFrame + ".png");
        }
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
