package com.miJuego.model;

import java.awt.Color;

public class TorreElectrica extends Torre implements DamageDeTorre {
    private Enemigo objetivo;
    private double efectoDeParalizacion; // Duración de la paralización (en segundos, ej. 0.5s)
    private double areaAGolpear;
    private double damageComun = 10.0;
    private double damageEscudo = 40.0; // Damage extra contra escudos eléctricos

    public TorreElectrica(String id, float x, float y) {
        super(id, x, y, 220.0, 800, "TorreElectrica");
        this.efectoDeParalizacion = 0.4; // 0.4 segundos de parálisis completa
        this.areaAGolpear = 1.0;
        this.rango = 3.8;
    }

    @Override
    public double ataque(Enemigo enemigo) {
        if (enemigo instanceof Ares ares) {
            ares.activarEscudo();
        }
        double damageEfectivo = damageComun * nivelMejora;
        
        // Si es EnemigoComun y tiene escudo eléctrico
        if (enemigo instanceof EnemigoComun) {
            EnemigoComun comun = (EnemigoComun) enemigo;
            if (comun.isTieneEscudoElectrico()) {
                damageEfectivo = damageEscudo * nivelMejora;
                comun.perderEscudoEl(); // Le quitamos el escudo eléctrico
            }
        }
        
        // Reducción drástica para Ares con escudo activo
        if (enemigo instanceof Ares ares && ares.isShieldActive()) {
            damageEfectivo *= 0.05; // 95% de reducción
        }
        
        enemigo.setVida(enemigo.GetVida() - damageEfectivo);
        
        // Aplicar paralización breve (a menos que sea Ares con escudo activo)
        if (!(enemigo instanceof Ares ares && ares.isShieldActive())) {
            enemigo.aplicarParalizacion((float) efectoDeParalizacion);
        }
        
        return damageEfectivo;
    }

    @Override
    public void upgrade() {
        nivelMejora++;
        costoTorre += 110.0;
        rango += 0.3;
        damageComun += 5.0;
        damageEscudo += 15.0;
        efectoDeParalizacion = Math.min(1.0, efectoDeParalizacion + 0.1);
    }

    public Enemigo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Enemigo objetivo) {
        this.objetivo = objetivo;
    }

    public double getEfectoDeParalizacion() {
        return efectoDeParalizacion;
    }

    public void setEfectoDeParalizacion(double efectoDeParalizacion) {
        this.efectoDeParalizacion = efectoDeParalizacion;
    }

    public double getAreaAGolpear() {
        return areaAGolpear;
    }

    public void setAreaAGolpear(double areaAGolpear) {
        this.areaAGolpear = areaAGolpear;
    }

    @Override
    public Color getFallbackColor() {
        // Color amarillo eléctrico
        return new Color(255, 215, 0);
    }

    @Override
    public java.util.Optional<String> getImagePath() {
        return java.util.Optional.of(getTowerSprite());
    }

    protected Enemigo selectElectricTarget(java.util.List<Enemigo> enemigosEnRango) {
        for (Enemigo e : enemigosEnRango) {
            if (e instanceof Ares ares && ares.isShieldActive()) {
                return ares;
            }
        }
        return selectFirstEnemy(enemigosEnRango);
    }

    @Override
    public java.util.List<Bala> atacar(java.util.List<Enemigo> enemigosEnRango, java.util.function.Supplier<String> idGenerator) {
        if (enemigosEnRango.isEmpty()) return java.util.Collections.emptyList();
        Enemigo target = selectElectricTarget(enemigosEnRango);
        this.setObjetivo(target);
        this.resetCooldown();
        return java.util.List.of(new Bala(idGenerator.get(), this, target, 10.0));
    }
}
