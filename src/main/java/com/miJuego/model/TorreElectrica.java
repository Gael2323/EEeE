package com.miJuego.model;

import java.awt.Color;

public class TorreElectrica extends Torre implements DañoDeTorre {
    private Enemigo objetivo;
    private double efectoDeParalizacion; // Duración de la paralización (en segundos, ej. 0.5s)
    private double areaAGolpear;
    private double dañoComun = 10.0;
    private double dañoEscudo = 40.0; // Daño extra contra escudos eléctricos

    public TorreElectrica(String id, float x, float y) {
        super(id, x, y, 220.0, 800, "TorreElectrica");
        this.efectoDeParalizacion = 0.4; // 0.4 segundos de parálisis completa
        this.areaAGolpear = 1.0;
        this.rango = 3.8;
    }

    @Override
    public double ataque(Enemigo enemigo) {
        double dañoEfectivo = dañoComun * nivelMejora;
        
        // Si es EnemigoComun y tiene escudo eléctrico
        if (enemigo instanceof EnemigoComun) {
            EnemigoComun comun = (EnemigoComun) enemigo;
            if (comun.isTieneEscudoElectrico()) {
                dañoEfectivo = dañoEscudo * nivelMejora;
                comun.perderEscudoEl(); // Le quitamos el escudo eléctrico
            }
        }
        
        enemigo.setVida(enemigo.GetVida() - dañoEfectivo);
        
        // Aplicar paralización breve
        enemigo.aplicarParalizacion((float) efectoDeParalizacion);
        
        return dañoEfectivo;
    }

    @Override
    public void upgrade() {
        nivelMejora++;
        costoTorre += 110.0;
        rango += 0.3;
        dañoComun += 5.0;
        dañoEscudo += 15.0;
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
}
