package com.example.snackhub.ui.accueil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.snackhub.databinding.FragmentAccueilBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AccueilFragment extends Fragment {

    private FragmentAccueilBinding binding;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AccueilViewModel homeViewModel =
                new ViewModelProvider(this).get(AccueilViewModel.class);

        binding = FragmentAccueilBinding.inflate(inflater, container, false);

        // Utilise directement binding pour accéder aux vues
        FloatingActionButton selectImageButton = binding.selectImageButton;
        ImageView imageView = binding.myImageView;

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

        // Si tu utilises le textView dans le ViewModel (non utilisé ici)
        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Important : retourne binding.getRoot() qui est ta vraie vue !
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}