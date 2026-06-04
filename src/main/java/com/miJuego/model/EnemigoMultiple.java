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
        this.width = 1.4f;
        this.height = 1.4f;
    }

    @Override
    public List<Enemigo> morir() {
        // En el UML: morir() -> retorna enemigosComunes
        List<Enemigo> subEnemigos = new ArrayList<>();
        // Genera dos enemigos comunes más débiles y sin escudo con la animación de idiot
        String subId1 = id + "-split-" + (++splitCounter);
        String subId2 = id + "-split-" + (++splitCounter);
        
        EnemigoMiniIdiot e1 = new EnemigoMiniIdiot(subId1);
        EnemigoMiniIdiot e2 = new EnemigoMiniIdiot(subId2);
        
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
    public java.util.Optional<String> getImagePath() {
        int frame = getAnimationFrame(); // Cicla de 0 a animFrameCount-1 (5 frames)
        return java.util.Optional.of("assets/ingame/you_are_an_idiot" + frame + ".png");
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
