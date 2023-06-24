package com.example.cambiosfutsal;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CambiosFragment.Listener, PartidoFragment.CambioListerer {

    private Toolbar toolbar;
    private List<String> jugadores;
    private PartidoFragment partidoFragment = null;
    public final String PREFERENCES_JUGADORES = "my_prefs_jugadores";
    public final String SHARED_PREFS_FILE = "file_saved_prefs_jugadores";
    private CambiosFragment cambiosFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        jugadores = getAllNames();

        this.loadToolbar();
        if (partidoFragment == null)
            partidoFragment = new PartidoFragment(jugadores);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_view, partidoFragment).commit();

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
        partidoFragment.addItem(name);
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
//        partidoFragment.setJugadores(jugadoresActuales);
//        partidoFragment.addItem();
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
    public void onCloseFragment(String jugandoCambiado, String banquilloCambiado) {
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, partidoFragment).commit();
        partidoFragment.realizarCambio(jugandoCambiado, banquilloCambiado);
        getSupportFragmentManager().beginTransaction().remove(cambiosFragment).commit();
    }


    @Override
    public void onRealizarCambio(List<String> jugando, List<String> banquillo) {
        cambiosFragment = new CambiosFragment(jugando, banquillo);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_view, cambiosFragment).commit();
    }
}
