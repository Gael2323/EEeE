package com.miJuego.model;

import java.awt.Color;

public class TorreFirefox extends TorreDeFuego {

    public TorreFirefox(String id, float x, float y) {
        super(id, x, y);
        this.costoTorre = 180.0;
        this.rango = 3.0;
        this.tiempoRecarga = 200; // Muy rápido (0.2s)
        this.resetCooldown();
        // El damage base se maneja en Juego.java o a través del ataque
    }

    @Override
    public Color getFallbackColor() {
        return new Color(255, 140, 0); // Naranja Firefox
    }

    @Override
    public String getSpritePrefix() {
        return "torrefirefox";
    }

    @Override
    public void upgrade() {
        super.upgrade();
        this.costoTorre += 90.0;
        this.rango += 0.2;
    }

    @Override
    public java.util.List<Bala> atacar(java.util.List<Enemigo> enemigosEnRango, java.util.function.Supplier<String> idGenerator) {
        if (enemigosEnRango.isEmpty()) return java.util.Collections.emptyList();
        Enemigo target = selectFirstEnemy(enemigosEnRango);
        this.resetCooldown();
        return java.util.List.of(new Bala(idGenerator.get(), this, target, 15.0));
    }
}
