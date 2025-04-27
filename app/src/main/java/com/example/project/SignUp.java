// SignUp.java
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
import com.android.volley.toolbox.JsonObjectRequest;
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
    private static final String URL_SIGNUP = "http://10.0.2.2/Soufra_Share/users.php";
    private static final String URL_UPLOAD = "http://10.0.2.2/Soufra_Share/upload_profile_picture.php";
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri profileImageUri;
    private String uploadedProfilePicturePath;

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

        btnSelectProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Select Profile Image button clicked");
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });
    }


    private void checkPermission(String permission, int requestCode) {
        String permissionToCheck = permission;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionToCheck = Manifest.permission.READ_MEDIA_IMAGES;
        }
        Log.d(TAG, "checkPermission called with permission: " + permissionToCheck + ", requestCode: " + requestCode);
        if (ContextCompat.checkSelfPermission(SignUp.this, permissionToCheck) == PackageManager.PERMISSION_DENIED) {
            Log.d(TAG, "Storage permission (" + permissionToCheck + ") is NOT granted");
            if (ActivityCompat.shouldShowRequestPermissionRationale(SignUp.this, permissionToCheck)) {
                Log.d(TAG, "Should show request permission rationale for " + permissionToCheck);
                // Explain to the user why we need the permission
                Toast.makeText(SignUp.this, "Storage permission is needed to select a profile picture.", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "Should NOT show request permission rationale for " + permissionToCheck + " (likely denied with 'Don't ask again')");
                Toast.makeText(SignUp.this, "Storage permission is disabled. Please enable it in the app settings to select a profile picture.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
            }
            Log.d(TAG, "Requesting storage permission: " + permissionToCheck);
            ActivityCompat.requestPermissions(SignUp.this, new String[]{permissionToCheck}, requestCode);
        } else {
            Log.d(TAG, "Storage permission (" + permissionToCheck + ") is already GRANTED");
            openFileChooser();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult called with requestCode: " + requestCode);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Storage Permission has been GRANTED by the user");
                Toast.makeText(SignUp.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
                openFileChooser();
            } else {
                Log.d(TAG, "Storage Permission has been DENIED by the user");
                Toast.makeText(SignUp.this, "Storage Permission Denied. You won't be able to select a profile picture.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openFileChooser() {
        Log.d(TAG, "openFileChooser called");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult called with requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            Log.d(TAG, "Image selected. URI: " + profileImageUri);
            Picasso.get().load(profileImageUri).into(profileImageView);
            uploadProfilePicture();
        }
    }

    private void uploadProfilePicture() {
        Log.d(TAG, "uploadProfilePicture called");
        if (profileImageUri == null) {
            Toast.makeText(this, "Please select a profile image first.", Toast.LENGTH_SHORT).show();
            return;
        }

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, URL_UPLOAD,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            if (jsonObject.getBoolean("success")) {
                                Toast.makeText(SignUp.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                uploadedProfilePicturePath = jsonObject.getString("file_path");
                                Log.d(TAG, "Profile picture uploaded successfully. Path: " + uploadedProfilePicturePath);
                            } else {
                                Toast.makeText(SignUp.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Profile picture upload failed on server: " + jsonObject.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(SignUp.this, "Error parsing upload response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error parsing upload response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(SignUp.this, "Profile picture upload failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Volley Error during profile picture upload: " + error.toString());
                    }
                });

        try {
            InputStream inputStream = getContentResolver().openInputStream(profileImageUri);
            byte[] imgData = getBytes(inputStream);
            String mimeType = getContentResolver().getType(profileImageUri);

            String filename = "profile_" + System.currentTimeMillis();
            String extension = "";

            if (mimeType != null) {
                if (mimeType.equals("image/jpeg")) {
                    extension = ".jpg";
                } else if (mimeType.equals("image/png")) {
                    extension = ".png";
                } else if (mimeType.equals("image/gif")) {
                    extension = ".gif";
                }
            } else {
                extension = ".jpg";
            }

            String fullFilename = filename + extension;

            volleyMultipartRequest.addFile("profile_image", fullFilename, imgData, mimeType);
            Log.d(TAG, "Added file to multipart request with filename: " + fullFilename);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error getting image data: " + e.getMessage());
            Toast.makeText(this, "Error reading image file", Toast.LENGTH_SHORT).show();
            return;
        }

        requestQueue.add(volleyMultipartRequest);
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void signUpUser() {
        Log.d(TAG, "signUpUser called");
        final String fullName = edFullName.getText().toString().trim();
        final String username = edUsername.getText().toString().trim();
        final String email = edEmail.getText().toString().trim();
        final String password = editTextTextPassword.getText().toString().trim();
        final String location = edLocation.getText().toString().trim();
        final String nationalId = edNationalId.getText().toString().trim();
        final String birthData = edBirthData.getText().toString().trim();
        final String phoneNum = edPhoneNum.getText().toString().trim();
        final String profilePicture = uploadedProfilePicturePath;

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
        if (profilePicture != null && !profilePicture.isEmpty()) {
            params.put("profile_picture", profilePicture);
            Log.d(TAG, "Including profile picture path in signup request: " + profilePicture);
        }

        JSONObject jsonObject = new JSONObject(params);
        Log.d(TAG, "Signup JSON object: " + jsonObject.toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_SIGNUP, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String message = response.getString("message");
                            Toast.makeText(SignUp.this, message, Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Signup successful. Response message: " + message);
                            startActivity(new Intent(SignUp.this, MainActivity.class)); // Replace with your login activity
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(SignUp.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error parsing signup response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(SignUp.this, "Sign up failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Signup failed: " + error.getMessage());
                        Log.e(TAG, "Volley Error: " + error.toString());
                    }
                });

        requestQueue.add(request);
    }
}