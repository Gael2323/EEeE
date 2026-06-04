package com.miJuego.model;

public class ActualTowerContext {
    private static String nombreTorre = "Ninguna (Presioná 1-7)";
    private static int hoverX = -1;
    private static int hoverY = -1;

    public static String getNombreTorre() {
        return nombreTorre;
    }

    public static void setNombreTorre(String nombre) {
        nombreTorre = nombre;
    }

    public static int getHoverX() {
        return hoverX;
    }

    public static void setHoverX(int x) {
        hoverX = x;
    }

    public static int getHoverY() {
        return hoverY;
    }

    public static void setHoverY(int y) {
        hoverY = y;
    }
}