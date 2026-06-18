package com.miJuego.model;

import java.awt.Color;

public class Canon extends Torre implements DamageDeTorre {
    private Enemigo objetivo;
    private double areaAGolpear; // Representa el radio de explosión (splash radius)
    private double damageBase = 25.0;

    public Canon(String id, float x, float y) {
        super(id, x, y, 200.0, 2000, "Canon");
        this.areaAGolpear = 1.5;
        this.rango = 4.0;
    }

    @Override
    public double ataque(Enemigo enemigo) {
        double damageEfectivo = damageBase * nivelMejora;
        enemigo.setVida(enemigo.GetVida() - damageEfectivo);
        return damageEfectivo;
    }

    @Override
    public void upgrade() {
        nivelMejora++;
        costoTorre += 100.0;
        rango += 0.2;
        areaAGolpear += 0.2;
        damageBase += 10.0;
    }

    public Enemigo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Enemigo objetivo) {
        this.objetivo = objetivo;
    }

    public double getAreaAGolpear() {
        return areaAGolpear;
    }

    public void setAreaAGolpear(double areaAGolpear) {
        this.areaAGolpear = areaAGolpear;
    }

    @Override
    public Color getFallbackColor() {
        // Color gris oscuro metálico para el cañón
        return new Color(105, 105, 105);
    }

    @Override
    public java.util.Optional<String> getImagePath() {
        return java.util.Optional.of(getTowerSprite());
    }

    @Override
    public java.util.List<Bala> atacar(java.util.List<Enemigo> enemigosEnRango, java.util.function.Supplier<String> idGenerator) {
        if (enemigosEnRango.isEmpty()) return java.util.Collections.emptyList();
        Enemigo target = selectFirstEnemy(enemigosEnRango);
        this.setObjetivo(target);
        this.resetCooldown();
        return java.util.List.of(new Bala(idGenerator.get(), this, target, 25.0));
    }
}
