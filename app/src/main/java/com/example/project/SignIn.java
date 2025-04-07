package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignIn extends AppCompatActivity {

    private EditText edEmail, editTextTextPassword;
    private Button btnSignIn, btnSignUpRedirect;
    private static final String PREF_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        edEmail = findViewById(R.id.edEmail);
        editTextTextPassword = findViewById(R.id.editTextTextPassword);
        btnSignIn = findViewById(R.id.btnSignIp);
        btnSignUpRedirect = findViewById(R.id.btnSignUpRedirect);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = edEmail.getText().toString().trim();
                final String password = editTextTextPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignIn.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // For now, we'll simulate a successful login
                SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
                editor.putBoolean(KEY_IS_LOGGED_IN, true);
                editor.apply(); // Or editor.commit()

                // Navigate to MainActivity
                Intent intent = new Intent(SignIn.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close SignIn activity
            }
        });

        btnSignUpRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
            }
        });
    }
}