package com.miJuego.model;

import java.awt.Color;

public class TorreDeHielo extends Torre implements DañoDeTorre {
    private Enemigo objetivo;
    private double efectoDeRalentizar; // Factor de ralentización (ej: 0.5f = 50% de la velocidad normal)
    private double areaAGolpear;
    private double dañoBase = 0.0; // En el enunciado dice "ralentiza, no daña"

    public TorreDeHielo(String id, float x, float y) {
        super(id, x, y, 150.0, 1000, "TorreDeHielo");
        this.efectoDeRalentizar = 0.5; // Ralentiza a la mitad de velocidad por defecto
        this.areaAGolpear = 1.2;
        this.rango = 3.5;
    }

    @Override
    public double ataque(Enemigo enemigo) {
        // Aplica ralentización por un tiempo determinado (ej: 2.5 segundos)
        enemigo.aplicarRalentizar(efectoDeRalentizar, 2.5f);
        
        // Hace daño básico (0.0 o muy bajo si se mejora)
        double dañoEfectivo = dañoBase * (nivelMejora - 1);
        enemigo.setVida(enemigo.GetVida() - dañoEfectivo);
        return dañoEfectivo;
    }

    @Override
    public void upgrade() {
        nivelMejora++;
        costoTorre += 75.0;
        rango += 0.3;
        // Hace que ralentice un poco más fuerte (ej: multiplica el factor por 0.9, es decir, el enemigo va más lento)
        efectoDeRalentizar = Math.max(0.2, efectoDeRalentizar - 0.05);
        areaAGolpear += 0.15;
    }

    public Enemigo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Enemigo objetivo) {
        this.objetivo = objetivo;
    }

    public double getEfectoDeRalentizar() {
        return efectoDeRalentizar;
    }

    public void setEfectoDeRalentizar(double efectoDeRalentizar) {
        this.efectoDeRalentizar = efectoDeRalentizar;
    }

    public double getAreaAGolpear() {
        return areaAGolpear;
    }

    public void setAreaAGolpear(double areaAGolpear) {
        this.areaAGolpear = areaAGolpear;
    }

    @Override
    public Color getFallbackColor() {
        // Color celeste/cyan para hielo
        return new Color(135, 206, 250);
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
        return java.util.List.of(new Bala(idGenerator.get(), this, target, 0.0));
    }
}
