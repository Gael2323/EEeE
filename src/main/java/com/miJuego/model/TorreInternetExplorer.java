package com.miJuego.model;

import java.awt.Color;

public class TorreInternetExplorer extends TorreDeHielo {

    public TorreInternetExplorer(String id, float x, float y) {
        super(id, x, y);
        this.costoTorre = 175.0; // un poco más cara o como quieras
        this.rango = 3.5;
        this.setEfectoDeRalentizar(0.4); // ralentiza bastante (60% más lento)
        // Sobreescribimos el daño base, hace poco daño
        // Como TorreDeHielo tiene un dañoBase privado que no expone un setter, 
        // vamos a hacer override del ataque si es necesario, pero Juego.java es el que hace el daño usando Bala.
    }

    @Override
    public Color getFallbackColor() {
        return new Color(30, 144, 255); // Azul característico de IE
    }

    @Override
    public void upgrade() {
        super.upgrade();
        this.costoTorre += 85.0;
        this.rango += 0.4;
    }

    @Override
    public String getSpritePrefix() {
        return "torreie";
    }
}
