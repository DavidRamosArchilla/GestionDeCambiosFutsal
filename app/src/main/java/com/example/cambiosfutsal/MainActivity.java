package com.example.cambiosfutsal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends AppCompatActivity implements CambiosFragment.Listener {
    private RecyclerView recyclerViewJugando;
    private RecyclerView recyclerViewBanquillo;
    private Toolbar toolbar;
    private List<String> jugadores;
    private ItemAdapter adapterJugando;
    private ItemAdapter adapterBanquillo;
    private AtomicLong tiempoAlPausar;
    private boolean relojPausado;
    public final String PREFERENCES_JUGADORES = "my_prefs_jugadores";
    public final String SHARED_PREFS_FILE = "file_saved_prefs_jugadores";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        jugadores = getAllNames();
        relojPausado = true;
        Chronometer cronometro = findViewById(R.id.cronometroPartido);

        recyclerViewJugando = findViewById(R.id.recyclerViewJugando);
        recyclerViewJugando.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewBanquillo = findViewById(R.id.recyclerViewBanquillo);
        recyclerViewBanquillo.setLayoutManager(new LinearLayoutManager(this));

        this.setAdapters();
        this.loadToolbar();

        Button botonPlayPausa = findViewById(R.id.pararIniciarTiempo);
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

        Button botonCambio = findViewById(R.id.botonCambiarJugadores);
        botonCambio.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction().add(R.id.linearLayout, new CambiosFragment()).commit();
        });
    }

    private void loadToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.black, getTheme()));

        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.add_jugador) {// Handle button click
                showAddNameDialog();
                return true;
            }
            else if(item.getItemId() == R.id.eliminar_jugador){
                showRemoveNameDialog();
                return true;
            }
            return false;
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    private void showAddNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Añade el jugador");

        // Set up the input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the "OK" button
        builder.setPositiveButton("Añadir", (dialog, which) -> {
            // Get the input value and save it as a name
            String name = input.getText().toString().trim();
            saveName(name);
        });

        // Set up the "Cancel" button
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        // Show the dialog
        builder.show();
    }
    private void showRemoveNameDialog() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
        ArrayList<String> jugadoresActuales = getJugadoresEnSharedPrefs(sharedPreferences);

        final String[] nameArray = jugadoresActuales.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Name");

        // Set up the list of names
        builder.setItems(nameArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Remove the selected name
                removeName(nameArray[which]);
            }
        });

        // Show the dialog
        builder.show();
    }

    private void saveName(String name) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ArrayList<String> jugadoresActuales = getJugadoresEnSharedPrefs(sharedPreferences);
        jugadoresActuales.add(name);
        try {
            editor.putString(PREFERENCES_JUGADORES, ObjectSerializer.serialize(jugadoresActuales));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        editor.apply();
        Log.i("tras añadir", jugadoresActuales.toString());
        actualizarLista(jugadoresActuales);
    }

    private void removeName(String name) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, MODE_PRIVATE);
        ArrayList<String> jugadoresActuales = getJugadoresEnSharedPrefs(sharedPreferences);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Remove the name from the list
        jugadoresActuales.remove(name);

        try {
            editor.putString(PREFERENCES_JUGADORES, ObjectSerializer.serialize(jugadoresActuales));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        editor.apply();
        actualizarLista(jugadoresActuales);
    }

    private void actualizarLista(ArrayList<String> jugadoresActuales) {
        this.jugadores = jugadoresActuales;
        this.setAdapters();
    }

    private void setAdapters(){
        int numTitulares = Math.min(jugadores.size(), 4);
        adapterJugando = new ItemAdapter(jugadores.subList(0,numTitulares));
        adapterBanquillo = new ItemAdapter(jugadores.subList(numTitulares, jugadores.size()));
        recyclerViewJugando.setAdapter(adapterJugando);
        recyclerViewBanquillo.setAdapter(adapterBanquillo);
    }

    private List<String> getAllNames() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, MODE_PRIVATE);
        ArrayList<String> jugadoresActuales = getJugadoresEnSharedPrefs(sharedPreferences);
        // Get the set of names from shared preferences
        Log.i("obtenidos", jugadoresActuales.toString());
        Collections.sort(jugadoresActuales);
        return jugadoresActuales;
    }

    private ArrayList<String> getJugadoresEnSharedPrefs(SharedPreferences sharedPreferences){
        ArrayList<String> jugadoresActuales = new ArrayList<>();
        // Get the existing list of names from shared preferences
        try {
            jugadoresActuales = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString(PREFERENCES_JUGADORES,
                    ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return jugadoresActuales;
    }

    @Override
    public void onCloseFragment() {
        getSupportFragmentManager().popBackStack();
    }


    private static class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private List<String> items;
        private List<ItemViewHolder> holders = new ArrayList<>();

        public ItemAdapter(List<String> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_layout, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            holder.getTextViewNombre().setText(items.get(position));
//            if (!holder.getCronometroTiempo().isActivated()){
//                holder.getCronometroTiempo().start();
//            }
            if(!holders.contains(holder))
                holders.add(holder);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void toggleChronometers(boolean start, long elapsedRealTime) {
            for (ItemViewHolder holder : holders) {
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
