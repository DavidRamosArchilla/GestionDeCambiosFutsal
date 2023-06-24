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
import java.util.Collections;
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
    private List<String> jugadores;

    public PartidoFragment() {
        // Required empty public constructor
    }

    public PartidoFragment(List<String> jugadores) {
        this.jugadores = jugadores;
        int numTitulares = Math.min(jugadores.size(), 4);
        adapterJugando = new ItemAdapter(new ArrayList<>(jugadores.subList(0,numTitulares)));
        adapterBanquillo = new ItemAdapter(new ArrayList<>(jugadores.subList(numTitulares, jugadores.size())));
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
                tiempoAlPausar.set(cronometro.getBase() - SystemClock.elapsedRealtime());
            }
        });

        Button botonCambio = view.findViewById(R.id.botonCambiarJugadores);
        botonCambio.setOnClickListener(v -> {
            if(jugadores.size()>4){
                ((CambioListerer) getActivity()).onRealizarCambio(jugadores.subList(0,4)
                        , jugadores.subList(4, jugadores.size()));
            }
            else{
                Snackbar.make(view, "No hay suficientes jugadores para hcer un cambio", Snackbar.LENGTH_LONG).show();
            }

        });
    }

    public void addItem(String nombre) {
        jugadores.add(nombre);
        int numTitulares = Math.min(jugadores.size(), 4);
        if (numTitulares == 4){
            adapterBanquillo.addItem(nombre);
        }
        else{
            adapterJugando.addItem(nombre);
        }
    }

    public void removeItem(String nombre) {
        jugadores.remove(nombre);
        adapterBanquillo.removeItem(nombre);
        adapterJugando.removeItem(nombre);
    }


    public void setAdapters(){
        recyclerViewJugando.setAdapter(adapterJugando);
        recyclerViewBanquillo.setAdapter(adapterBanquillo);
    }
    public void realizarCambio(String jugandoCambiado, String banquilloCambiado) {
        Collections.swap(jugadores, jugadores.indexOf(jugandoCambiado), jugadores.indexOf(banquilloCambiado));
        adapterBanquillo.hacerCambio(banquilloCambiado, jugandoCambiado);
        adapterJugando.hacerCambio(jugandoCambiado, banquilloCambiado);
    }

    private static class ItemAdapter extends RecyclerView.Adapter<PartidoFragment.ItemViewHolder> {

        private List<String> items;
        private List<PartidoFragment.ItemViewHolder> holders = new ArrayList<>();

        public ItemAdapter(List<String> items) {
            this.items = items;
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
            holder.getTextViewNombre().setText(items.get(position));
            if(!holders.contains(holder))
                holders.add(holder);
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
            items.add(jugadorParaAniadir);
            items.remove(jugadorParaQuitar);
            notifyDataSetChanged();

            ItemViewHolder viewHolder = holders.get(items.size() - 1);
            viewHolder.getCronometroTiempo().setBase(SystemClock.elapsedRealtime());
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