package com.example.cambiosfutsal;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


public class PartidoFragment extends Fragment {

    interface CambioListerer{

        void onRealizarCambio(List<String> jugando, List<String> banquillo);
    }

    private RecyclerView recyclerViewJugando;
    private RecyclerView recyclerViewBanquillo;
    private ItemAdapter adapterJugando;
    private ItemAdapter adapterBanquillo;
    private Chronometer cronometro = null;
    private AtomicLong tiempoAlPausar;
    private boolean relojPausado = true;
//    private List<String> jugadores;
    private Equipo equipo;

    public PartidoFragment() {
        // Required empty public constructor
    }

    public PartidoFragment(List<String> jugadores) {
//        this.jugadores = jugadores;
        this.equipo = new Equipo(jugadores);
        adapterJugando = new ItemAdapter(equipo.getNombresEnPista(), equipo);
        adapterBanquillo = new ItemAdapter(equipo.getNombresBanquillo(), equipo);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_partido, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cronometro = view.findViewById(R.id.cronometroPartido);

        recyclerViewJugando = view.findViewById(R.id.recyclerViewJugando);
        recyclerViewJugando.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerViewBanquillo = view.findViewById(R.id.recyclerViewBanquillo);
        recyclerViewBanquillo.setLayoutManager(new LinearLayoutManager(getContext()));

        this.setAdapters();

        Button botonPlayPausa = view.findViewById(R.id.pararIniciarTiempo);
        tiempoAlPausar = new AtomicLong();
        botonPlayPausa.setOnClickListener(v -> {
            long elapsedRealTime = SystemClock.elapsedRealtime();
            long resumeTime = elapsedRealTime + tiempoAlPausar.get();
            adapterJugando.toggleChronometers(relojPausado, elapsedRealTime);
            adapterBanquillo.toggleChronometers(relojPausado, elapsedRealTime);
            if(relojPausado){
                cronometro.setBase(resumeTime);
                cronometro.start();
                relojPausado = false;
            }
            else{
                cronometro.stop();
                relojPausado = true;
                equipo.pausarCronometros(cronometro.getBase() - elapsedRealTime);
                tiempoAlPausar.set(cronometro.getBase() - SystemClock.elapsedRealtime());
            }
        });

        Button botonCambio = view.findViewById(R.id.botonCambiarJugadores);
        botonCambio.setOnClickListener(v -> {
            if( (equipo.getTotalJugadores() > Equipo.MAX_JUGADORES_PISTA)){
                ((CambioListerer) getActivity()).onRealizarCambio(equipo.getNombresEnPista(),
                        equipo.getNombresBanquillo());
            }
            else{
                Snackbar.make(view, "No hay suficientes jugadores para hcer un cambio", Snackbar.LENGTH_LONG).show();
            }
        });
    }
    public void addItem(String nombre) {
        equipo.addJugador(nombre);
        if (equipo.getTotalJugadores() >= Equipo.MAX_JUGADORES_PISTA){
            adapterBanquillo.addItem(nombre);
        }
        else{
            adapterJugando.addItem(nombre);
        }
    }
    public void removeItem(String nombre) {
        equipo.removeJugador(nombre);
        adapterBanquillo.removeItem(nombre);
        adapterJugando.removeItem(nombre);
    }


    public void setAdapters(){
        recyclerViewJugando.setAdapter(adapterJugando);
        recyclerViewBanquillo.setAdapter(adapterBanquillo);
    }
    public void realizarCambio(String jugandoCambiado, String banquilloCambiado) {
//        Collections.swap(jugadores, jugadores.indexOf(jugandoCambiado), jugadores.indexOf(banquilloCambiado));
        equipo.hacerCambio(jugandoCambiado, banquilloCambiado);
        adapterBanquillo.hacerCambio(banquilloCambiado, jugandoCambiado);
        adapterJugando.hacerCambio(jugandoCambiado, banquilloCambiado);
    }

    private static class ItemAdapter extends RecyclerView.Adapter<PartidoFragment.ItemViewHolder> {

        private List<String> items;
        private List<PartidoFragment.ItemViewHolder> holders = new ArrayList<>();
        private Equipo equipo;
        public ItemAdapter(List<String> items, Equipo equipo) {
            this.items = items;
            this.equipo = equipo;
        }

        @NonNull
        @Override
        public PartidoFragment.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_layout, parent, false);
            PartidoFragment.ItemViewHolder viewHolder = new PartidoFragment.ItemViewHolder(view);
            if(!holders.contains(viewHolder))
                holders.add(viewHolder);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull PartidoFragment.ItemViewHolder holder, int position) {
            String nombreJugador = items.get(position);
            Jugador jugador = equipo.getJugadorPorNombre(nombreJugador);
            holder.getTextViewNombre().setText(nombreJugador);
            holder.setTiempoAlPausar(jugador.getTiempoAlPausar());
            holder.getCronometroTiempo().setBase(SystemClock.elapsedRealtime() + jugador.getTiempoAlPausar());

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void toggleChronometers(boolean start, long elapsedRealTime) {
            for (PartidoFragment.ItemViewHolder holder : holders) {
                Chronometer cronometro = holder.getCronometroTiempo();
                long resumeTime = elapsedRealTime + holder.getTiempoAlPausar();
                if (start) {
                    cronometro.setBase(resumeTime);
                    cronometro.start();
                } else {
                    cronometro.stop();
                    holder.setTiempoAlPausar(cronometro.getBase() - elapsedRealTime);
                }

            }
        }

        public void addItem(String item) {
            items.add(item);
            notifyItemInserted(items.size() - 1);
        }

        public void removeItem(String nombre) {
            items.remove(nombre);
            notifyDataSetChanged();
        }

        public void hacerCambio(String jugadorParaQuitar, String jugadorParaAniadir){
            int posicionQuitado = items.indexOf(jugadorParaQuitar);
            items.set(posicionQuitado, jugadorParaAniadir);
            notifyItemChanged(posicionQuitado);
//            ItemViewHolder viewHolder = getViewHolderByJugador(jugadorParaAniadir);
//            ItemViewHolder viewHolder = holders.get(posicionQuitado);
//            viewHolder.setTiempoAlPausar(0);
//            viewHolder.getCronometroTiempo().setBase(SystemClock.elapsedRealtime());
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewNombre;
        private Chronometer cronometroTiempo;
        private long tiempoAlPausar;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            cronometroTiempo = itemView.findViewById(R.id.cronometroJugador);
            tiempoAlPausar = 0;
        }

        public TextView getTextViewNombre() {
            return textViewNombre;
        }

        public Chronometer getCronometroTiempo() {
            return cronometroTiempo;
        }

        public long getTiempoAlPausar() {
            return tiempoAlPausar;
        }

        public void setTiempoAlPausar(long tiempoAlPausar) {
            this.tiempoAlPausar = tiempoAlPausar;
        }
    }
}