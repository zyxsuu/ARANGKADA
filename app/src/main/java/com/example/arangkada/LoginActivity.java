package com.example.arangkada;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText  etEmailOrPhone;
    private EditText  etPassword;
    private CheckBox  cbRememberMe;
    private TextView  tvForgotPassword;
    private TextView  tvCreateAccount;
    private Button    btnLogin;
    private Button    btnGoogol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Wire up views
        etEmailOrPhone   = findViewById(R.id.et_email_or_phone);
        etPassword       = findViewById(R.id.et_password);
        cbRememberMe     = findViewById(R.id.cb_remember_me);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvCreateAccount  = findViewById(R.id.tv_create_account);
        btnLogin         = findViewById(R.id.btn_login);
        btnGoogol        = findViewById(R.id.btn_googol);

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v ->
                    Toast.makeText(this, "Password reset coming soon!", Toast.LENGTH_SHORT).show()
            );
        }

        // NEW: Navigate to RegisterActivity
        if (tvCreateAccount != null) {
            tvCreateAccount.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                if (validateInputs()) {
                    navigateToMain();
                }
            });
        }

        if (btnGoogol != null) {
            btnGoogol.setOnClickListener(v -> navigateToMain());
        }
    }

    private boolean validateInputs() {
        if (etEmailOrPhone == null || etPassword == null) return true;

        String emailOrPhone = etEmailOrPhone.getText().toString().trim();
        String password     = etPassword.getText().toString();

        if (TextUtils.isEmpty(emailOrPhone)) {
            etEmailOrPhone.setError("Email or phone number is required");
            etEmailOrPhone.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}