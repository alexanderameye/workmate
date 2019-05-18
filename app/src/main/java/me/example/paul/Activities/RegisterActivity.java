package me.example.paul.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import me.example.paul.AESCrypt;
import me.example.paul.R;
import me.example.paul.SessionManager;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameFieldText, emailFieldText, passwordFieldText, passwordConfirmationFieldText;
    private ProgressDialog progressDialog;

    SessionManager sessionManager;

    private String registerUserUrl = "https://studev.groept.be/api/a18_sd308/AddUser/";
    private String getUserUrl = "https://studev.groept.be/api/a18_sd308/GetUser/";
    private RequestQueue serverQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);

        usernameFieldText = findViewById(R.id.usernameFieldText);
        emailFieldText = findViewById(R.id.emailRegisterText);
        passwordFieldText = findViewById(R.id.passwordRegisterText);
        passwordConfirmationFieldText = findViewById(R.id.passwordConfirmationRegisterText);
        progressDialog = new ProgressDialog(this);

        final Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUser();
            }
        });
        serverQueue = Volley.newRequestQueue(this);


        emailFieldText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String email = emailFieldText.getText().toString().trim();
                if (!isValidEmail(email)) {
                    emailFieldText.setBackgroundResource(R.drawable.input_box_incorrect);
                } else {
                    emailFieldText.setBackgroundResource(R.drawable.input_box_correct);
                }
            }
        });

        passwordFieldText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = passwordFieldText.getText().toString().trim();

                if (password.length() < 6) {
                    passwordFieldText.setBackgroundResource(R.drawable.input_box_incorrect);
                } else {
                    passwordFieldText.setBackgroundResource(R.drawable.input_box_correct);
                }
            }
        });

        passwordConfirmationFieldText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = passwordFieldText.getText().toString().trim();
                String passwordConfirmation = passwordConfirmationFieldText.getText().toString().trim();

                if (!password.equals(passwordConfirmation)) {
                    passwordConfirmationFieldText.setBackgroundResource(R.drawable.input_box_incorrect);
                } else {
                    passwordConfirmationFieldText.setBackgroundResource(R.drawable.input_box_correct);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void RegisterUser() {
        final String username = usernameFieldText.getText().toString().trim();
        final String email = emailFieldText.getText().toString().trim();
        final String password = passwordFieldText.getText().toString().trim();
        final String passwordConfirmation = passwordConfirmationFieldText.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirmation.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Some fields were left empty", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(passwordConfirmation)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            passwordConfirmationFieldText.requestFocus();
        } else if (password.length() < 6) {
            Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            passwordFieldText.requestFocus();

        } else if (!isValidEmail(email)) {
            Toast.makeText(RegisterActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
        } else {
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getUserUrl + email, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if (response.length() > 0) {
                        Toast.makeText(RegisterActivity.this, "Account with email already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            Register(username, password, email);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            serverQueue.add(request);
        }
    }

    public void Register(final String username, final String password, final String email) throws Exception {

        progressDialog.setMessage("Registration in progress");
        progressDialog.show();

        String encryptedPassword = AESCrypt.encrypt(password);

        StringRequest request = new StringRequest(Request.Method.POST, registerUserUrl + username + "/" + email + "/" + encryptedPassword, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                sessionManager.createSession(username, email);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("username", username);
                map.put("email", email);
                map.put("password", password);
                return map;
            }
        };
        serverQueue.add(request);
    }

    public static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}