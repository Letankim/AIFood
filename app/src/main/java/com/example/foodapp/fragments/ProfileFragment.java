package com.example.foodapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.foodapp.R;
import com.example.foodapp.activities.ChangePasswordActivity;
import com.example.foodapp.activities.EditProfileActivity;
import com.example.foodapp.activities.LoginActivity;
import com.example.foodapp.utils.SessionManager;
import com.example.foodapp.models.User;
import com.google.android.material.card.MaterialCardView;

public class ProfileFragment extends Fragment {
    private TextView tvUsername, tvEmail, tvFullName;
    private MaterialCardView editProfileCard, changePasswordCard, logoutCard; // **FIX: MaterialCardView**
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        currentUser = sessionManager.getUser();

        if (currentUser == null) {
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
            return;
        }

        setupUI();
        setupClickListeners();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            currentUser = (User) data.getSerializableExtra("updated_user");
            sessionManager.createLoginSession(currentUser);
            setupUI();
            android.util.Log.d("ProfileFragment", "✅ Profile updated - UI refreshed!");
        }
    }

    private void initViews(View view) {
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvFullName = view.findViewById(R.id.tvFullName);
        editProfileCard = view.findViewById(R.id.editProfileCard);
        changePasswordCard = view.findViewById(R.id.changePasswordCard);
        logoutCard = view.findViewById(R.id.logoutCard);
    }

    private void setupUI() {
        tvUsername.setText(currentUser.getUsername());
        tvEmail.setText(currentUser.getEmail());
        tvFullName.setText(currentUser.getFullName());
    }

    private void setupClickListeners() {
        editProfileCard.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            startActivityForResult(intent, 1);
        });

        changePasswordCard.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class)));

        logoutCard.setOnClickListener(v -> logoutUser());
    }

    private void logoutUser() {
        sessionManager.logout();
        startActivity(new Intent(requireContext(), LoginActivity.class));
        requireActivity().finish();
    }
}