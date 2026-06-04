package com.miJuego.model;

import java.awt.Color;

public class TorreDeFuego extends Torre implements DañoDeTorre {
    private Enemigo objetivo;
    private double dañoPorQuemadura;
    private double areaAGolpear; // Splash/rango del efecto de fuego en área
    private double dañoBase = 5.0;

    public TorreDeFuego(String id, float x, float y) {
        super(id, x, y, 180.0, 1200, "TorreDeFuego");
        this.dañoPorQuemadura = 10.0; // Daño por segundo del quemado
        this.areaAGolpear = 1.0;
        this.rango = 3.2;
    }

    @Override
    public double ataque(Enemigo enemigo) {
        double dañoEfectivo = dañoBase * nivelMejora;
        enemigo.setVida(enemigo.GetVida() - dañoEfectivo);
        
        // Aplicar efecto de fuego en el enemigo (ej. 3 segundos de duración)
        double dañoDps = dañoPorQuemadura * nivelMejora;
        enemigo.aplicarFuego(dañoDps, 3.0f);
        
        return dañoEfectivo;
    }

    @Override
    public void upgrade() {
        nivelMejora++;
        costoTorre += 90.0;
        rango += 0.3;
        dañoPorQuemadura += 5.0;
        areaAGolpear += 0.1;
    }

    public Enemigo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Enemigo objetivo) {
        this.objetivo = objetivo;
    }

    public double getDañoPorQuemadura() {
        return dañoPorQuemadura;
    }

    public void setDañoPorQuemadura(double dañoPorQuemadura) {
        this.dañoPorQuemadura = dañoPorQuemadura;
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
