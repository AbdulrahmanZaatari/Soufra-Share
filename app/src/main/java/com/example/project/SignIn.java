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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class SignIn extends AppCompatActivity {

    private EditText edEmail, editTextTextPassword;
    private Button btnSignIn, btnSignUpRedirect;
    private static final String PREF_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private RequestQueue requestQueue;
    private static final String URL_SIGNIN = "http://10.0.2.2/soufra_share/users.php?action=signin"; // Updated URL

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
        requestQueue = Volley.newRequestQueue(this);

        edEmail = findViewById(R.id.edEmail);
        editTextTextPassword = findViewById(R.id.editTextTextPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
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

                JSONObject requestBody = new JSONObject();
                try {
                    requestBody.put("email", email);
                    requestBody.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_SIGNIN, requestBody,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    boolean success = response.getBoolean("success");
                                    String message = response.getString("message");

                                    if (success) {
                                        // Sign-in successful, update SharedPreferences and navigate to MainActivity
                                        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
                                        editor.putBoolean(KEY_IS_LOGGED_IN, true);
                                        editor.apply();
                                        startActivity(new Intent(SignIn.this, MainActivity.class));
                                        finish();
                                    } else {
                                        // Sign-in failed, show an error message
                                        Toast.makeText(SignIn.this, message, Toast.LENGTH_LONG).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(SignIn.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Toast.makeText(SignIn.this, "Sign-in failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                requestQueue.add(request);
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