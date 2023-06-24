package com.example.cambiosfutsal;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;


public class CambiosFragment extends Fragment {

    interface Listener {
        void onCloseFragment(String jugandoCambiado, String banquilloCambiado);

        void onCloseFragment();
    }

    private List<String> listaJugando;
    private List<String> listaBanquillo;
    private ListView banqulloListView;
    private ListView jugandoListView;
    private String jugandoCambiado;
    private String banquilloCambiado;

    public CambiosFragment() {
        // Required empty public constructor

    }

    public CambiosFragment(List<String> listaJugando, List<String> listaBanquillo) {
        this.listaJugando = listaJugando;
        this.listaBanquillo = listaBanquillo;
        this.jugandoCambiado = null;
        this.banquilloCambiado = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_cambios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        banqulloListView = view.findViewById(R.id.listaBanquillo);
        jugandoListView = view.findViewById(R.id.ListaJugando);

        // Create and set the adapters for the lists
        ArrayAdapter<String> banquilloAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_single_choice, listaBanquillo);
        banqulloListView.setAdapter(banquilloAdapter);

        ArrayAdapter<String> jugandoAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_single_choice, listaJugando);
        jugandoListView.setAdapter(jugandoAdapter);

        // Set item click listeners for the lists
        banqulloListView.setOnItemClickListener((parent, viewOnClick, position, id) -> {
            // Handle item selection
            String selectedItem = listaBanquillo.get(position);
            // Perform any desired actions with the selected item
            banquilloCambiado = selectedItem;
        });

        jugandoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewOnClick, int position, long id) {
                // Handle item selection
                String selectedItem = listaJugando.get(position);
                // Perform any desired actions with the selected item
                jugandoCambiado = selectedItem;
            }
        });

        // Set click listener for the button

        Button botonConfirmar = view.findViewById(R.id.botonCerrarFragment);
        botonConfirmar.setOnClickListener(v -> {
            if((jugandoCambiado != null) && (banquilloCambiado != null)){
                ((Listener) getActivity()).onCloseFragment(jugandoCambiado, banquilloCambiado);
            }
            else{
                Snackbar.make(view, "Selecciona jugadores para cmabiar", Snackbar.LENGTH_LONG).show();
            }

        });
        Button botonCancelar = view.findViewById(R.id.botonCancelar);
        botonCancelar.setOnClickListener(v -> {
            ((Listener) getActivity()).onCloseFragment();
        });
    }

    // Method to update the lists with new items
//    public void updateLists(List<String> leftItems, List<String> rightItems) {
//        leftListItems.clear();
//        leftListItems.addAll(leftItems);
//        leftListAdapter.notifyDataSetChanged();
//
//        rightListItems.clear();
//        rightListItems.addAll(rightItems);
//        rightListAdapter.notifyDataSetChanged();
//    }

}