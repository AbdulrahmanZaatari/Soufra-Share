package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private EditText edFullName, edUsername, edEmail, editTextTextPassword, edLocation, edNationalId, edBirthData, edPhoneNum;
    private Button btnSignUp;
    private RequestQueue requestQueue;
    private static final String URL_SIGNUP = "http://10.0.2.2/Soufra_Share/users.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edFullName = findViewById(R.id.edFullName);
        edUsername = findViewById(R.id.edUsername);
        edEmail = findViewById(R.id.edEmail);
        editTextTextPassword = findViewById(R.id.editTextTextPassword);
        edLocation = findViewById(R.id.edLocation);
        edNationalId = findViewById(R.id.edNationalId);
        edBirthData = findViewById(R.id.edBirthData);
        edPhoneNum = findViewById(R.id.edPhoneNum);
        btnSignUp = findViewById(R.id.btnSignUp);

        requestQueue = Volley.newRequestQueue(this);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });
    }

    private void signUpUser() {
        final String fullName = edFullName.getText().toString().trim();
        final String username = edUsername.getText().toString().trim();
        final String email = edEmail.getText().toString().trim();
        final String password = editTextTextPassword.getText().toString().trim();
        final String location = edLocation.getText().toString().trim();
        final String nationalId = edNationalId.getText().toString().trim();
        final String birthData = edBirthData.getText().toString().trim();
        final String phoneNum = edPhoneNum.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || location.isEmpty() || nationalId.isEmpty() || birthData.isEmpty() || phoneNum.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("full_name", fullName);
        params.put("username", username);
        params.put("email", email);
        params.put("password", password);
        params.put("location", location);
        params.put("national_id", nationalId);
        params.put("birth_data", birthData);
        params.put("phone_num", phoneNum);
        params.put("is_cook", "0"); // Default value

        JSONObject jsonObject = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_SIGNUP, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String message = response.getString("message");
                            Toast.makeText(SignUp.this, message, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(SignUp.this, MainActivity.class)); // Replace with your login activity
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(SignUp.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(SignUp.this, "Sign up failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("Volley Error", error.toString());
                    }
                });

        requestQueue.add(request);
    }
}