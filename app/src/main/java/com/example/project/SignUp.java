package com.example.project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private EditText edFullName, edUsername, edEmail, editTextTextPassword, edLocation, edNationalId, edBirthData, edPhoneNum;
    private Button btnSignUp, btnSelectProfileImage;
    private ImageView profileImageView;
    private RequestQueue requestQueue;
    private static final String SIGNUP_URL = "http://10.0.2.2/Soufra_Share/signup.php";
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri profileImageUri;

    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final String TAG = "SignUpActivity";

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
        btnSelectProfileImage = findViewById(R.id.btnSelectProfileImage);
        profileImageView = findViewById(R.id.profileImageView);

        requestQueue = Volley.newRequestQueue(this);

        btnSelectProfileImage.setOnClickListener(v -> checkPermission());

        btnSignUp.setOnClickListener(v -> signUpUser());
    }

    private void checkPermission() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openFileChooser();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(this, "Storage permission is needed to select a profile picture.", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
            }
            ActivityCompat.requestPermissions(this, new String[]{permission}, STORAGE_PERMISSION_CODE);
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            Picasso.get().load(profileImageUri).into(profileImageView);
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void signUpUser() {
        if (profileImageUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        final String fullName = edFullName.getText().toString().trim();
        final String username = edUsername.getText().toString().trim();
        final String email = edEmail.getText().toString().trim();
        final String password = editTextTextPassword.getText().toString().trim();
        final String location = edLocation.getText().toString().trim();
        final String nationalId = edNationalId.getText().toString().trim();
        final String birthData = edBirthData.getText().toString().trim();
        final String phoneNum = edPhoneNum.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() ||
                location.isEmpty() || nationalId.isEmpty() || birthData.isEmpty() || phoneNum.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, SIGNUP_URL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(new String(response.data));
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(SignUp.this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                            startActivity(new Intent(SignUp.this, SignIn.class));
                            finish();
                        } else {
                            Toast.makeText(SignUp.this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(SignUp.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(SignUp.this, "Signup failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Volley error: " + error.getMessage());
                });

        // Add all form fields as string params
        multipartRequest.addStringParam("full_name", fullName);
        multipartRequest.addStringParam("username", username);
        multipartRequest.addStringParam("email", email);
        multipartRequest.addStringParam("password", password);
        multipartRequest.addStringParam("location", location);
        multipartRequest.addStringParam("national_id", nationalId);
        multipartRequest.addStringParam("birth_data", birthData);
        multipartRequest.addStringParam("phone_num", phoneNum);
        multipartRequest.addStringParam("is_cook", "0");

        // Add profile image
        try {
            InputStream inputStream = getContentResolver().openInputStream(profileImageUri);
            byte[] fileData = getBytes(inputStream);
            String mimeType = getContentResolver().getType(profileImageUri);
            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            multipartRequest.addFile("profile_image", fileName, fileData, mimeType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        requestQueue.add(multipartRequest);
    }
}
