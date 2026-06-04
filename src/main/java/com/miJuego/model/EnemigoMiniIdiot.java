package com.miJuego.model;

import java.awt.Color;
import java.util.Optional;

/**
 * Versión miniatura del enemigo "You Are An Idiot" que aparece al dividirse
 * el EnemigoMultiple. Es un EnemigoComun pero visualmente conserva la estética
 * característica animada del idiot.
 */
public class EnemigoMiniIdiot extends EnemigoComun {

    public EnemigoMiniIdiot(String id) {
        super(id, false); // Nace sin escudo eléctrico
        this.width = 0.9f;
        this.height = 0.9f;
    }

    @Override
    public Optional<String> getImagePath() {
        int frame = getAnimationFrame(); // Cicla de 0 a 4
        return Optional.of("assets/ingame/you_are_an_idiot" + frame + ".png");
    }

    @Override
    public Color getFallbackColor() {
        if (paralizacionTimer > 0 || ralentizarTimer > 0 || fuegoTimer > 0) {
            return super.getFallbackColor();
        }
        return new Color(139, 0, 139); // Fucsia/violeta oscuro como su padre
    }
}
