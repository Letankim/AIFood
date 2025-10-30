package com.example.foodapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapp.R;
import com.example.foodapp.models.User;
import com.example.foodapp.utils.SessionManager;
import com.example.foodapp.database.UserDatabaseHelper;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail;
    private Button btnSave, btnCancel;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);
        currentUser = sessionManager.getUser();

        initViews();
        loadData();
        setupClickListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void loadData() {
        etFullName.setText(currentUser.getFullName());
        etEmail.setText(currentUser.getEmail());
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setFullName(fullName);
        currentUser.setEmail(email);

        if (UserDatabaseHelper.updateUser(currentUser)) {
            sessionManager.createLoginSession(currentUser);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_user", currentUser);
            setResult(Activity.RESULT_OK, resultIntent);

            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
}