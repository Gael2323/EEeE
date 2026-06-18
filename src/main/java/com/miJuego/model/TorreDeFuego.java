package com.miJuego.model;

import java.awt.Color;

public abstract class TorreDeFuego extends Torre implements DamageDeTorre {
    private Enemigo objetivo;
    private double damagePorQuemadura;
    private double areaAGolpear; // Splash/rango del efecto de fuego en área
    private double damageBase = 5.0;

    public TorreDeFuego(String id, float x, float y) {
        super(id, x, y, 180.0, 1200, "TorreDeFuego");
        this.damagePorQuemadura = 10.0; // Damage por segundo del quemado
        this.areaAGolpear = 1.0;
        this.rango = 3.2;
    }

    @Override
    public double ataque(Enemigo enemigo) {
        double damageEfectivo = damageBase * nivelMejora;
        enemigo.setVida(enemigo.GetVida() - damageEfectivo);
        
        // Aplicar efecto de fuego en el enemigo (ej. 3 segundos de duración)
        double damageDps = damagePorQuemadura * nivelMejora;
        enemigo.aplicarFuego(damageDps, 3.0f);
        
        return damageEfectivo;
    }

    @Override
    public void upgrade() {
        nivelMejora++;
        costoTorre += 90.0;
        rango += 0.3;
        damagePorQuemadura += 5.0;
        areaAGolpear += 0.1;
    }

    public Enemigo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Enemigo objetivo) {
        this.objetivo = objetivo;
    }

    public double getDamagePorQuemadura() {
        return damagePorQuemadura;
    }

    public void setDamagePorQuemadura(double damagePorQuemadura) {
        this.damagePorQuemadura = damagePorQuemadura;
    }

    public double getAreaAGolpear() {
        return areaAGolpear;
    }

    public void setAreaAGolpear(double areaAGolpear) {
        this.areaAGolpear = areaAGolpear;
    }

    @Override
    public Color getFallbackColor() {
        // Color naranja fuego
        return new Color(255, 69, 0);
    }

    @Override
    public java.util.Optional<String> getImagePath() {
        return java.util.Optional.of(getTowerSprite());
    }
}
