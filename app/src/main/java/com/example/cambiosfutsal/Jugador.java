package com.example.cambiosfutsal;

public class Jugador {

    private String nombre;
    private long tiempoAlPausar;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.tiempoAlPausar = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public long getTiempoAlPausar() {
        return tiempoAlPausar;
    }

    public void setTiempoAlPausar(long tiempoAlPausar) {
        this.tiempoAlPausar = tiempoAlPausar;
    }

}
