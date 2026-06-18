package com.miJuego.model;

import java.awt.Color;
import java.util.Optional;

public class TorreComun extends Torre implements DamageDeTorre {
    private double damageComun = 15.0;

    public TorreComun(String id, float x, float y) {
        super(id, x, y, 100.0, 500, "TorreComun");
        this.rango = 3.5;
    }

    @Override
    public double ataque(Enemigo enemigo) {
        // Hace damage básico al enemigo y lo devuelve
        double damageEfectivo = damageComun * nivelMejora;
        enemigo.setVida(enemigo.GetVida() - damageEfectivo);
        return damageEfectivo;
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
    public Color getFallbackColor() {
        // Color azulado para torre común
        return new Color(70, 130, 180);
    }
    @Override
    public Optional<String> getImagePath() {
        return Optional.of(getTowerSprite());
    }

    @Override
    public java.util.List<Bala> atacar(java.util.List<Enemigo> enemigosEnRango, java.util.function.Supplier<String> idGenerator) {
        if (enemigosEnRango.isEmpty()) return java.util.Collections.emptyList();
        Enemigo target = selectFirstEnemy(enemigosEnRango);
        this.resetCooldown();
        return java.util.List.of(new Bala(idGenerator.get(), this, target, 15.0));
    }
}
