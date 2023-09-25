package com.example.cambiosfutsal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Equipo {
    private List<Jugador> jugadoresEnPista;
    private List<Jugador> jugadoresEnBanquillo;
    public static final int MAX_JUGADORES_PISTA = 4;

    public Equipo(List<String> jugadores) {
        jugadoresEnPista = new ArrayList<>();
        jugadoresEnBanquillo = new ArrayList<>();
        int numTitulares = Math.min(jugadores.size(), MAX_JUGADORES_PISTA);
        for(int i=0; i<numTitulares; i++){
            jugadoresEnPista.add(new Jugador(jugadores.get(i)));
        }
        for(int i=numTitulares; i<jugadores.size(); i++){
            jugadoresEnBanquillo.add(new Jugador(jugadores.get(i)));
        }
    }

    public List<String> getNombresBanquillo(){
        List<String> jugadores = new ArrayList<>();
        for(Jugador j: this.jugadoresEnBanquillo){
            jugadores.add(j.getNombre());
        }
        return jugadores;
    }

    public List<String> getNombresEnPista(){
        List<String> jugadores = new ArrayList<>();
        for(Jugador j: this.jugadoresEnPista){
            jugadores.add(j.getNombre());
        }
        return jugadores;
    }

    public void hacerCambio(String jugandoCambiado, String banquilloCambiado) {
        int posicionJugando = this.getPosicionJugador(jugandoCambiado, jugadoresEnPista);
        int posicionBanquillo = this.getPosicionJugador(banquilloCambiado, jugadoresEnBanquillo);
        Jugador jugadorEnPista = jugadoresEnPista.get(posicionJugando);
        Jugador jugadorEnBanquillo = jugadoresEnBanquillo.get(posicionBanquillo);
        jugadorEnBanquillo.setTiempoAlPausar(0);
        jugadorEnPista.setTiempoAlPausar(0);
        jugadoresEnPista.set(posicionJugando, jugadorEnBanquillo);
        jugadoresEnBanquillo.set(posicionBanquillo, jugadorEnPista);
    }

    private int getPosicionJugador(String nombre, List<Jugador> lista){
        int posicion = 0;
        for(Jugador j: lista){
            if(j.getNombre().equals(nombre))
                return posicion;
            posicion++;
        }
        return -1;
    }

    public Jugador getJugadorPorNombre(String nombreJugador) {
        for(Jugador j: Stream.concat(jugadoresEnPista.stream(), jugadoresEnBanquillo.stream())
                .collect(Collectors.toList())){
            if(j.getNombre().equals(nombreJugador))
                return j;
        }
        return null;
    }

    public void pausarCronometros(long tiempoAlPausar) {
        for(Jugador j: Stream.concat(jugadoresEnPista.stream(), jugadoresEnBanquillo.stream())
                .collect(Collectors.toList())){
            j.setTiempoAlPausar(tiempoAlPausar);
        }
    }

    public int getTotalJugadores(){
        return jugadoresEnPista.size() + jugadoresEnBanquillo.size();
    }

    public void addJugador(String nombre){
        int totalJugadores = getTotalJugadores();
        if(totalJugadores < MAX_JUGADORES_PISTA)
            jugadoresEnPista.add(new Jugador(nombre));
        else
            jugadoresEnBanquillo.add(new Jugador(nombre));
    }

    public void removeJugador(String nombre){
        Jugador jugador = getJugadorPorNombre(nombre);
        if(jugadoresEnBanquillo.contains(jugador))
            jugadoresEnBanquillo.remove(jugador);
        else
            jugadoresEnPista.remove(jugador);
    }
}
