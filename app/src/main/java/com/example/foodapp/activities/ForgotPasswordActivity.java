package com.example.foodapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapp.R;
import com.example.foodapp.database.UserDatabaseHelper;
//import com.example.foodapp.utils.EmailSender;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSend, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendResetEmail());
        btnBack.setOnClickListener(v -> finish());
    }

    private void sendResetEmail() {
        String email = etEmail.getText().toString().trim();

        if(email.isEmpty()) {
            Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (UserDatabaseHelper.getUserByEmail(email) == null) {
            Toast.makeText(this, "This email is not registered!", Toast.LENGTH_SHORT).show();
            return;
        }

        String newPassword = generateRandomPassword(8);

        boolean updated = UserDatabaseHelper.updatePassword(email, newPassword);
        if (!updated) {
            Toast.makeText(this, "Failed to update password!", Toast.LENGTH_SHORT).show();
            return;
        }

//        new EmailSender(this, email, newPassword).execute();
//
//        Toast.makeText(this, "Sending new password...", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}