package com.miJuego.model;

import java.awt.Color;
import java.util.Optional;

public class TorreMcAfee extends Torre implements DañoDeTorre {
    private double dañoComun = 17.25;

    public TorreMcAfee(String id, float x, float y) {
        super(id, x, y, 80.0, 500, "TorreMcAfee");
        this.rango = 3.5;
    }

    @Override
    public double ataque(Enemigo enemigo) {
        // Hace daño básico al enemigo y lo devuelve (15% más que TorreComun)
        double dañoEfectivo = dañoComun * nivelMejora;
        enemigo.setVida(enemigo.GetVida() - dañoEfectivo);
        return dañoEfectivo;
    }

    @Override
    public void upgrade() {
        nivelMejora++;
        costoTorre += 50.0;
        rango += 0.5;
        // Reducimos el tiempo de recarga ligeramente
        tiempoRecarga = Math.max(500, tiempoRecarga - 80);
    }

    @Override
    public String getTowerSprite() {
        if (currentTarget == null) {
            return "assets/ingame/torremc_reposo.png";
        }
        return super.getTowerSprite();
    }

    @Override
    public String getSpritePrefix() {
        return "torremc"; // Usa los sprites torremc0.png a torremc7.png
    }

    @Override
    public Color getFallbackColor() {
        // Color rojo McAfee
        return new Color(200, 30, 30);
    }

    @Override
    public Optional<String> getImagePath() {
        return Optional.of(getTowerSprite());
    }
}
