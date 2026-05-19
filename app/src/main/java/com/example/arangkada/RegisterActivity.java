package com.example.arangkada;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    // Form Views
    private LinearLayout layoutForm;
    private ImageButton btnBack;
    private EditText    etFullName, etEmail, etPhoneNumber, etCreatePassword, etConfirmPassword;
    private CheckBox    cbRememberMe;
    private Button      btnReviewDetails;

    // Verify Views
    private LinearLayout layoutVerify;
    private TextView     tvVerifyName, tvVerifyEmail, tvVerifyPhone;
    private Button       btnConfirmSignUp, btnEditDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Wire up Form layout elements
        layoutForm        = findViewById(R.id.layout_register_form);
        btnBack           = findViewById(R.id.btn_back);
        etFullName        = findViewById(R.id.et_full_name);
        etEmail           = findViewById(R.id.et_email);
        etPhoneNumber     = findViewById(R.id.et_phone_number);
        etCreatePassword  = findViewById(R.id.et_create_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        cbRememberMe      = findViewById(R.id.cb_remember_me);
        btnReviewDetails  = findViewById(R.id.btn_review_details);

        // Wire up Verify layout elements
        layoutVerify      = findViewById(R.id.layout_register_verify);
        tvVerifyName      = findViewById(R.id.tv_verify_name);
        tvVerifyEmail     = findViewById(R.id.tv_verify_email);
        tvVerifyPhone     = findViewById(R.id.tv_verify_phone);
        btnConfirmSignUp  = findViewById(R.id.btn_confirm_signup);
        btnEditDetails    = findViewById(R.id.btn_edit_details);

        // ----- Listeners -----

        // Back arrow (Top Left)
        btnBack.setOnClickListener(v -> handleBackNavigation());

        // Step 1: Review Details (Swaps view to Verify Screen)
        btnReviewDetails.setOnClickListener(v -> {
            if (validateInputs()) {
                // Populate the verification text views with user input
                tvVerifyName.setText(etFullName.getText().toString().trim());
                tvVerifyEmail.setText(etEmail.getText().toString().trim());
                tvVerifyPhone.setText(etPhoneNumber.getText().toString().trim());

                // Hide Form, Show Verification
                layoutForm.setVisibility(View.GONE);
                layoutVerify.setVisibility(View.VISIBLE);
            }
        });

        // Step 2A: Edit Details (Swaps view back to Form)
        btnEditDetails.setOnClickListener(v -> {
            layoutVerify.setVisibility(View.GONE);
            layoutForm.setVisibility(View.VISIBLE);
        });

        // Step 2B: Confirm & Create Account (Proceeds to Onboarding)
        btnConfirmSignUp.setOnClickListener(v -> {
            // TODO: Persist user credentials (DB/SharedPreferences) here before navigating
            Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInputs() {
        String fullName        = etFullName.getText().toString().trim();
        String email           = etEmail.getText().toString().trim();
        String phone           = etPhoneNumber.getText().toString().trim();
        String createPassword  = etCreatePassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhoneNumber.setError("Phone number is required");
            etPhoneNumber.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(createPassword)) {
            etCreatePassword.setError("Password is required");
            etCreatePassword.requestFocus();
            return false;
        }
        if (!createPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Handles back navigation smartly. If they are looking at the verify screen,
     * the back button will return them to the form. If they are on the form,
     * it will take them back to the Login Screen.
     */
    private void handleBackNavigation() {
        if (layoutVerify.getVisibility() == View.VISIBLE) {
            // Go back to edit form
            layoutVerify.setVisibility(View.GONE);
            layoutForm.setVisibility(View.VISIBLE);
        } else {
            // Exit activity
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        handleBackNavigation();
    }
}