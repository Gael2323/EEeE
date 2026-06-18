package com.miJuego.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Duende extends Enemigo {

    public Duende(String id) {
        super(id, "Duende", 40.0, 10, 5);
        this.rapidez = 3.0; // Más rápido que la media
        this.damageBase = 1.0;
        this.width = 1.1f;
        this.height = 1.1f;
    }

    @Override
    public List<Enemigo> morir() {
        // En el UML: morir() -> retorna listaVacia
        return new ArrayList<>();
    }

    @Override
    public Color getFallbackColor() {
        // Color verde lima para los duendes si no tienen estados alterados activos
        if (paralizacionTimer > 0 || ralentizarTimer > 0 || fuegoTimer > 0) {
            return super.getFallbackColor();
        }
        return new Color(50, 205, 50);
    }
}
