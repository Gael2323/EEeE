package com.miJuego.model;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Clase genérica que representa una oleada de enemigos.
 * @param <T> Tipo de enemigo que compone esta oleada (restringido a clases que hereden de Enemigo).
 */
public class Oleada<T extends Enemigo> {
    private Queue<T> enemigos;
    private int numeroOleada;
    private boolean isBossWave;
    private boolean isCinematicWave;

    public Oleada(int numeroOleada) {
        this.numeroOleada = numeroOleada;
        this.enemigos = new LinkedList<>();
        this.isBossWave = false;
        this.isCinematicWave = false;
    }

    public void addEnemigo(T enemigo) {
        this.enemigos.add(enemigo);
    }

    public T pollEnemigo() {
        return this.enemigos.poll();
    }

    public boolean isEmpty() {
        return this.enemigos.isEmpty();
    }

    public int size() {
        return this.enemigos.size();
    }

    public int getNumeroOleada() {
        return numeroOleada;
    }

    public void setBossWave(boolean bossWave) {
        this.isBossWave = bossWave;
    }

    public boolean isBossWave() {
        return isBossWave;
    }

    public void setCinematicWave(boolean cinematicWave) {
        this.isCinematicWave = cinematicWave;
    }

    public boolean isCinematicWave() {
        return isCinematicWave;
    }
}
