package com.example.foodapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.foodapp.R;

public class ToolsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tools, container, false);

        Button suggestButton = view.findViewById(R.id.button_suggest);
        Button scanButton = view.findViewById(R.id.button_scan);

        suggestButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new IngredientSuggestionFragment())
                    .addToBackStack(null)
                    .commit();
        });

        scanButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ImageScanFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}