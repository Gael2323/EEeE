package com.miJuego.model;

public class Jugador {
    private double score; // puntaje
    private int health;   // vida
    private int moneda;   // coins

    public Jugador(double score, int health, int moneda) {
        this.score = score;
        this.health = health;
        this.moneda = moneda;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void addScore(double amount) {
        this.score += amount;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void decreaseHealth(int amount) {
        this.health = Math.max(0, this.health - amount);
    }

    public int getMoneda() {
        return moneda;
    }

    public void setMoneda(int moneda) {
        this.moneda = moneda;
    }

    public void addMoneda(int amount) {
        this.moneda += amount;
    }

    public boolean spendMoneda(int amount) {
        if (this.moneda >= amount) {
            this.moneda -= amount;
            return true;
        }
        return false;
    }
}
