package com.example.snackhub.ui.repas;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

import com.example.snackhub.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ModifierRepasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ModifierRepasFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ModifierRepasFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ModifierRepasFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ModifierRepasFragment newInstance(String param1, String param2) {
        ModifierRepasFragment fragment = new ModifierRepasFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_modifier_repas, container, false);

        EditText editTextNom = view.findViewById(R.id.edit_nom_repas);
        EditText editTextDescription = view.findViewById(R.id.edit_description_repas);
        EditText editPrixRepas = view.findViewById(R.id.edit_prix_repas);
        Spinner spinnerCategorie = view.findViewById(R.id.filterSpinner);
        ImageView imageRepas = view.findViewById(R.id.image_repas);
        Switch switchDisponible = view.findViewById(R.id.switchDisponible);

        Bundle args = getArguments();
        if (args != null) {
            String repasId = args.getString("repasId");
            String repasNom = args.getString("repasNom");
            String repasDescription = args.getString("repasDescription");
            String repasCategorie = args.getString("repasCategorie");
            String repasPrix = args.getString("repasPrix");
            int repasImage = args.getInt("repasImage");
            boolean repasDisponible = args.getBoolean("repasDisponible");

            editTextNom.setText(repasNom);
            editTextDescription.setText(repasDescription);
            editPrixRepas.setText(repasPrix);

            // Pour la catégorie : sélectionner la bonne valeur dans le spinner
            if (repasCategorie != null) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                        R.array.filter_options, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategorie.setAdapter(adapter);

                // Sélectionner la bonne position dans le spinner
                int spinnerPosition = adapter.getPosition(repasCategorie);
                spinnerCategorie.setSelection(spinnerPosition);
            }

            // Affichage de l'image
            imageRepas.setImageResource(repasImage);

            // Mise à jour de la disponibilité
            if (switchDisponible != null) {
                switchDisponible.setChecked(repasDisponible);
            }
        }
        return view;
    }
}