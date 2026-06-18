package com.miJuego.model;

import java.awt.Color;
import java.util.Optional;

public class InvocadoCorrupted extends Duende {

    private final String fileType;

    public InvocadoCorrupted(String id, String fileType) {
        super(id);
        this.enemyType = "InvocadoCorrupted";
        this.fileType = fileType;
        this.vida = 50.0;
        this.monedasGeneradas = 5;
        this.scoreGenerado = 5;
        this.rapidez = 3.0; // Rápido como el Duende
        this.damageBase = 1.0;
        this.width = 1.0f;
        this.height = 1.0f;
    }

    public String getFileType() {
        return fileType;
    }

    @Override
    public Optional<String> getImagePath() {
        int spriteOctant;
        switch (currentOctant) {
            case 0: spriteOctant = 0; break;
            case 1: spriteOctant = 1; break;
            case 2: spriteOctant = 1; break; // Derecha -> Arriba-Derecha
            case 3: spriteOctant = 4; break; // Abajo-Derecha -> Abajo
            case 4: spriteOctant = 4; break;
            case 5: spriteOctant = 5; break;
            case 6: spriteOctant = 5; break; // Izquierda -> Abajo-Izquierda
            case 7: spriteOctant = 0; break; // Arriba-Izquierda -> Arriba
            default: spriteOctant = 4; break;
        }
        return Optional.of("assets/ingame/enemies/corrupted_folder/Corrupted_" + fileType + spriteOctant + ".png");
    }

    @Override
    public Color getFallbackColor() {
        if (paralizacionTimer > 0 || ralentizarTimer > 0 || fuegoTimer > 0) {
            return super.getFallbackColor();
        }
        return new Color(128, 0, 128); // Morado corrupción
    }
}
