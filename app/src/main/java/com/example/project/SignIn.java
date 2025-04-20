package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log; // Import Log for debugging
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
    private static final String KEY_LOGIN_TIMESTAMP = "loginTimestamp";

    private RequestQueue requestQueue;
    private static final String URL_SIGNIN = "http://10.0.2.2/soufra_share/users.php?action=signin";

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
                    Log.d("SignInRequest", "Request Body: " + requestBody.toString()); // Log request body
                } catch (JSONException e) {
                    Log.e("SignInRequest", "JSONException creating request body", e);
                    Toast.makeText(SignIn.this, "Error creating request", Toast.LENGTH_SHORT).show();
                    return;
                }
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_SIGNIN, requestBody,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("SignInResponse", "Response: " + response.toString()); // Log successful response
                                try {
                                    // Check if the response contains the "success" key
                                    if (!response.has("success")) {
                                        Log.e("SignInResponse", "Response missing 'success' key");
                                        Toast.makeText(SignIn.this, "Invalid response from server", Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    boolean success = response.getBoolean("success");
                                    String message = response.optString("message", "Unknown status"); // Use optString for safety

                                    if (success) {
                                        if (!response.has("user_id")) {
                                            Log.e("SignInResponse", "Response missing 'user_id' key on success");
                                            Toast.makeText(SignIn.this, "Login successful, but user ID missing.", Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        int userId = response.getInt("user_id");
                                        Log.i("SignIn", "Sign-in successful for user ID: " + userId);
                                        Toast.makeText(SignIn.this, message, Toast.LENGTH_SHORT).show(); // Show success message
                                        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        long loginTime = System.currentTimeMillis(); // Get current time in milliseconds
                                        editor.putLong(KEY_LOGIN_TIMESTAMP, loginTime); // Save the timestamp
                                        editor.putInt("user_id", userId);
                                        editor.apply();
                                        Log.d("SignIn", "Saved login timestamp: " + loginTime + ", User ID: " + userId);
                                        Intent intent = new Intent(SignIn.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Log.w("SignIn", "Sign-in failed: " + message);
                                        Toast.makeText(SignIn.this, message, Toast.LENGTH_LONG).show();
                                    }

                                } catch (JSONException e) {
                                    Log.e("SignInResponse", "JSONException parsing response", e);
                                    Toast.makeText(SignIn.this, "Error parsing server response", Toast.LENGTH_LONG).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("SignInError", "Volley error: " + error.toString(), error);
                                String errorMessage = "Sign-in failed. ";
                                if (error.networkResponse != null) {
                                    errorMessage += "Status Code: " + error.networkResponse.statusCode;
                                } else if (error.getMessage() != null) {
                                    errorMessage += error.getMessage();
                                } else {
                                    errorMessage += "Please check your connection.";
                                }
                                Toast.makeText(SignIn.this, errorMessage, Toast.LENGTH_LONG).show();
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