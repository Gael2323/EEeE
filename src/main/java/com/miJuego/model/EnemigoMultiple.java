package com.miJuego.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class EnemigoMultiple extends Enemigo {
    private static int splitCounter = 0;

    public EnemigoMultiple(String id) {
        super(id, "EnemigoMultiple", 150.0, 30, 20);
        this.rapidez = 1.4; // Más lento, pero con más vida y se divide
        this.dañoBase = 2.0; // Hace más daño si llega a la base
    }

    @Override
    public List<Enemigo> morir() {
        // En el UML: morir() -> retorna enemigosComunes
        List<Enemigo> subEnemigos = new ArrayList<>();
        // Genera dos enemigos comunes más débiles y sin escudo
        String subId1 = id + "-split-" + (++splitCounter);
        String subId2 = id + "-split-" + (++splitCounter);
        
        EnemigoComun e1 = new EnemigoComun(subId1, false);
        EnemigoComun e2 = new EnemigoComun(subId2, false);
        
        // Les bajamos la vida base a la mitad para representar que son hijos más débiles
        e1.setVida(40.0);
        e2.setVida(40.0);
        
        // Heredan el waypoint de destino
        e1.setWaypointIndex(this.waypointIndex);
        e2.setWaypointIndex(this.waypointIndex);
        
        // El primer hijo se coloca un poco adelante y el otro un poco atrás
        e1.setPosicion(this.x, this.y);
        e2.setPosicion(this.x - 0.2f, this.y);
        
        subEnemigos.add(e1);
        subEnemigos.add(e2);
        
        return subEnemigos;
    }

    @Override
    public Color getFallbackColor() {
        if (paralizacionTimer > 0 || ralentizarTimer > 0 || fuegoTimer > 0) {
            return super.getFallbackColor();
        }
        // Color fucsia oscuro para enemigo múltiple
        return new Color(139, 0, 139);
    }
}
