package com.miJuego.model;

import java.awt.Color;

public class TorreFuerte extends Torre implements DamageDeTorre {
    private Enemigo objetivo;
    private double damageBase = 80.0;

    public TorreFuerte(String id, float x, float y) {
        super(id, x, y, 250.0, 3500, "TorreFuerte");
        this.rango = 5.0;
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
        costoTorre += 125.0;
        damageBase += 40.0;
        rango += 0.5;
        tiempoRecarga = Math.max(1500, tiempoRecarga - 300);
    }

    public Enemigo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Enemigo objetivo) {
        this.objetivo = objetivo;
    }

    @Override
    public Color getFallbackColor() {
        // Color rojo oscuro para torre fuerte
        return new Color(139, 0, 0);
    }

    @Override
    public java.util.Optional<String> getImagePath() {
        return java.util.Optional.of(getTowerSprite());
    }

    @Override
    public java.util.List<Bala> atacar(java.util.List<Enemigo> enemigosEnRango, java.util.function.Supplier<String> idGenerator) {
        if (enemigosEnRango.isEmpty()) return java.util.Collections.emptyList();
        Enemigo strongTarget = enemigosEnRango.get(0);
        for (Enemigo e : enemigosEnRango) {
            if (e.GetVida() > strongTarget.GetVida()) {
                strongTarget = e;
            }
        }
        this.setObjetivo(strongTarget);
        this.resetCooldown();
        return java.util.List.of(new Bala(idGenerator.get(), this, strongTarget, 80.0));
    }
}
