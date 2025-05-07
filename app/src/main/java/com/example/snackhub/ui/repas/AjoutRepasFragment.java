package com.example.snackhub.ui.repas;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.snackhub.R;
import com.example.snackhub.databinding.FragmentAjoutRepasBinding;
import com.google.android.material.button.MaterialButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AjoutRepasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AjoutRepasFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Spinner spinner;

    public AjoutRepasFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AjoutRepasFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AjoutRepasFragment newInstance(String param1, String param2) {
        AjoutRepasFragment fragment = new AjoutRepasFragment();
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

    private FragmentAjoutRepasBinding binding;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_ajout_repas, container, false);

        spinner = rootView.findViewById(R.id.filterSpinner);

        String[] categories = {"Sandwich", "Plat chaud", "Dessert", "Boisson"};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, categories);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);

        MaterialButton selectImageButton = rootView.findViewById(R.id.btn_choisir_image);
        ImageView imageView = rootView.findViewById(R.id.image_repas);

        // Initialise le launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImageUri = data.getData();
                            imageView.setImageURI(selectedImageUri);
                        }
                    }
                }
        );

        // Clique sur le bouton
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
        return rootView;// inflater.inflate(R.layout.fragment_ajout_repas, container, false);
    }
}