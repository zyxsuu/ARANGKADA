package com.example.arangkada;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MotorcycleSetupFragment extends Fragment {

    private EditText etMotoPlate;
    private Button btnNext;
    private SetupNavigator navigator;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SetupNavigator) {
            navigator = (SetupNavigator) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup_motorcycle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etMotoPlate = view.findViewById(R.id.et_moto_plate);
        btnNext     = view.findViewById(R.id.btn_next);

        btnNext.setOnClickListener(v -> {
            String plate = etMotoPlate != null ? etMotoPlate.getText().toString().trim() : "";

            SharedPreferences prefs = requireActivity().getSharedPreferences("ArangkadaSetup", Context.MODE_PRIVATE);
            prefs.edit().putString("setup_moto_plate", plate).apply();

            if (navigator != null) navigator.goToNextStep();
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigator = null;
    }
}