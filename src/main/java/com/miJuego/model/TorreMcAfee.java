package com.miJuego.model;

import java.awt.Color;
import java.util.Optional;

public class TorreMcAfee extends Torre implements DañoDeTorre {
    private double dañoBase = 20.0;
    private double probabilidadEscaneo = 0.3; // 30% de probabilidad de "escanear" y hacer daño extra

    public TorreMcAfee(String id, float x, float y) {
        super(id, x, y, 175.0, 1000, "TorreMcAfee");
        this.rango = 3.8;
    }

    @Override
    public double ataque(Enemigo enemigo) {
        double dañoEfectivo = dañoBase * nivelMejora;
        
        // Efecto especial: "Escaneo profundo" — probabilidad de hacer daño extra
        if (Math.random() < probabilidadEscaneo) {
            dañoEfectivo *= 2.0; // Daño doble al "detectar amenaza"
        }
        
        enemigo.setVida(enemigo.GetVida() - dañoEfectivo);
        return dañoEfectivo;
    }

    @Override
    public void upgrade() {
        nivelMejora++;
        costoTorre += 85.0;
        rango += 0.4;
        dañoBase += 8.0;
        // Aumenta la probabilidad de escaneo profundo
        probabilidadEscaneo = Math.min(0.6, probabilidadEscaneo + 0.05);
        tiempoRecarga = Math.max(600, tiempoRecarga - 50);
    }

    public double getProbabilidadEscaneo() {
        return probabilidadEscaneo;
    }

    public void setProbabilidadEscaneo(double probabilidadEscaneo) {
        this.probabilidadEscaneo = probabilidadEscaneo;
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
