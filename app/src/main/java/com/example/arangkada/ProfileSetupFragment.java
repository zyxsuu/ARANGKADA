package com.example.arangkada;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ProfileSetupFragment extends Fragment {

    private EditText  etDateOfBirth;
    private Spinner   spinnerValidIdType;
    private EditText  etIdNumber;
    private EditText  etAreaOfOperation;
    private EditText  etDailyEarningsTarget;

    private ImageView ivIdPhoto;
    private TextView  tvUploadLabel;
    private Uri       selectedImageUri = null;

    private Button    btnFullTime;
    private Button    btnPartTime;
    private Button    btnFreelance;
    private Button    btnNext;

    private com.google.android.material.chip.Chip chipFoodpanda;
    private com.google.android.material.chip.Chip chipLalamove;
    private com.google.android.material.chip.Chip chipMaxim;
    private com.google.android.material.chip.Chip chipGrab;
    private com.google.android.material.chip.Chip chipOthers;

    private String selectedRiderType = null;
    private SetupNavigator navigator;

    // --- Image Picker Setup ---
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivIdPhoto.setImageURI(uri);
                    ivIdPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ivIdPhoto.clearColorFilter(); // Remove old color filters if any
                    ivIdPhoto.setImageTintList(null); // CRITICAL: This removes the `app:tint` XML attribute so the photo renders in full color instead of a flat grey box!
                    if (tvUploadLabel != null) {
                        tvUploadLabel.setText("Photo Selected");
                        tvUploadLabel.setTextColor(android.graphics.Color.parseColor("#F5A623")); // Turn yellow
                    }
                }
            }
    );

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SetupNavigator) {
            navigator = (SetupNavigator) context;
        } else {
            throw new RuntimeException(context + " must implement SetupNavigator");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etDateOfBirth         = view.findViewById(R.id.et_date_of_birth);
        spinnerValidIdType    = view.findViewById(R.id.spinner_valid_id_type);
        etIdNumber            = view.findViewById(R.id.et_id_number);
        etAreaOfOperation     = view.findViewById(R.id.et_area_of_operation);
        etDailyEarningsTarget = view.findViewById(R.id.et_daily_earnings_target);

        btnFullTime           = view.findViewById(R.id.btn_full_time);
        btnPartTime           = view.findViewById(R.id.btn_part_time);
        btnFreelance          = view.findViewById(R.id.btn_freelance);
        btnNext               = view.findViewById(R.id.btn_next);

        ImageButton btnCalendar       = view.findViewById(R.id.btn_calendar);
        View layoutUploadId           = view.findViewById(R.id.layout_upload_id);
        ivIdPhoto                     = view.findViewById(R.id.iv_id_photo);
        tvUploadLabel                 = view.findViewById(R.id.tv_upload_label);

        // --- CALENDAR DATE PICKER ---
        View.OnClickListener datePickerListener = v -> showDatePicker();
        etDateOfBirth.setOnClickListener(datePickerListener);
        btnCalendar.setOnClickListener(datePickerListener);

        // --- PHILIPPINE VALID ID SPINNER ---
        String[] validIds = {
                "PhilSys ID (National ID)",
                "Driver's License",
                "Passport",
                "UMID",
                "Postal ID",
                "Voter's ID",
                "PRC ID",
                "TIN ID"
        };
        ArrayAdapter<String> idAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, validIds);
        idAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerValidIdType.setAdapter(idAdapter);

        // --- ID IMAGE UPLOAD ---
        layoutUploadId.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // --- DYNAMIC CHIP COLORS ---
        chipFoodpanda = view.findViewById(R.id.chip_foodpanda);
        chipLalamove  = view.findViewById(R.id.chip_lalamove);
        chipMaxim     = view.findViewById(R.id.chip_maxim);
        chipGrab      = view.findViewById(R.id.chip_grab);
        chipOthers    = view.findViewById(R.id.chip_others);

        int unselectedBgColor   = android.graphics.Color.parseColor("#2A3F8F");
        int unselectedTextColor = android.graphics.Color.parseColor("#AABBDD");

        setupDynamicChipColor(chipFoodpanda, android.graphics.Color.parseColor("#C2185B"), android.graphics.Color.WHITE, unselectedBgColor, unselectedTextColor);
        setupDynamicChipColor(chipLalamove,  android.graphics.Color.parseColor("#E65100"), android.graphics.Color.WHITE, unselectedBgColor, unselectedTextColor);
        setupDynamicChipColor(chipMaxim,     android.graphics.Color.parseColor("#FFD600"), android.graphics.Color.BLACK, unselectedBgColor, unselectedTextColor);
        setupDynamicChipColor(chipGrab,      android.graphics.Color.parseColor("#00B14F"), android.graphics.Color.WHITE, unselectedBgColor, unselectedTextColor);
        setupDynamicChipColor(chipOthers,    android.graphics.Color.parseColor("#F5A623"), android.graphics.Color.BLACK, unselectedBgColor, unselectedTextColor);

        // --- RIDER TYPE TOGGLES ---
        View.OnClickListener riderTypeListener = v -> {
            int selectedYellow = android.graphics.Color.parseColor("#FFD600");
            int selectedText   = android.graphics.Color.BLACK;
            int unselectedBlue = android.graphics.Color.parseColor("#2A3F8F");
            int unselectedText = android.graphics.Color.WHITE;

            Button[] allButtons = {btnFullTime, btnPartTime, btnFreelance};
            for (Button btn : allButtons) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(unselectedBlue));
                btn.setTextColor(unselectedText);
                btn.setSelected(false);
            }

            v.setSelected(true);
            v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedYellow));
            ((Button) v).setTextColor(selectedText);

            if (v.getId() == R.id.btn_full_time) selectedRiderType = "Full-Time";
            else if (v.getId() == R.id.btn_part_time) selectedRiderType = "Part-Time";
            else if (v.getId() == R.id.btn_freelance) selectedRiderType = "Freelance";
        };

        btnFullTime.setOnClickListener(riderTypeListener);
        btnPartTime.setOnClickListener(riderTypeListener);
        btnFreelance.setOnClickListener(riderTypeListener);

        // --- NEXT NAVIGATION ---
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                try {
                    if (validateInputs()) {
                        saveProfileData();
                        navigator.goToNextStep();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error navigating: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        // --- MAYBE LATER NAVIGATION ---
        TextView btnMaybeLater = view.findViewById(R.id.btn_maybe_later);
        if (btnMaybeLater != null) {
            btnMaybeLater.setOnClickListener(v -> {
                // If they skip setup, just mark as stub and drop to main
                android.content.SharedPreferences mainPrefs = requireActivity().getSharedPreferences("ArangkadaPrefs", Context.MODE_PRIVATE);
                
                String registeredEmail = mainPrefs.getString("auth_email", "User App");
                mainPrefs.edit().putString("user_name", registeredEmail.split("@")[0]).apply();
                
                android.content.Intent intent = new android.content.Intent(getActivity(), MainActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) getActivity().finish();
            });
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        // Default to showing calendar 18 years ago, standard for work profiles
        calendar.add(Calendar.YEAR, -18);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
            etDateOfBirth.setText(date);
        }, year, month, day);
        dialog.show();
    }

    private void saveProfileData() {
        List<String> selectedPlatforms = new ArrayList<>();
        if (chipFoodpanda != null && chipFoodpanda.isChecked()) selectedPlatforms.add("FoodPanda");
        if (chipLalamove != null && chipLalamove.isChecked())  selectedPlatforms.add("Lalamove");
        if (chipMaxim != null && chipMaxim.isChecked())     selectedPlatforms.add("Maxim");
        if (chipGrab != null && chipGrab.isChecked())      selectedPlatforms.add("Grab");
        // Others chip check was crashing on layout sometimes if not rendered securely, replacing with null check.
        if (chipOthers != null && chipOthers.isChecked())    selectedPlatforms.add("Others");

        String platformsString = TextUtils.join(", ", selectedPlatforms);
        if (platformsString.isEmpty()) platformsString = "None selected";

        SharedPreferences prefs = requireActivity().getSharedPreferences("ArangkadaSetup", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("setup_dob", etDateOfBirth.getText().toString().trim())
                .putString("setup_id_type", spinnerValidIdType.getSelectedItem().toString())
                .putString("setup_id_no", etIdNumber.getText().toString().trim())
                .putString("setup_platforms", platformsString)
                .putString("setup_rider_type", selectedRiderType)
                .putString("setup_area", etAreaOfOperation.getText().toString().trim())
                .putString("setup_daily_target", etDailyEarningsTarget.getText().toString().trim())
                .putString("setup_image_uri", selectedImageUri != null ? selectedImageUri.toString() : "")
                .apply();
    }

    private void setupDynamicChipColor(com.google.android.material.chip.Chip chip, int selectedBg, int selectedText, int unselectedBg, int unselectedText) {
        if (chip != null) {
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(selectedBg));
                    chip.setTextColor(selectedText);
                } else {
                    chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(unselectedBg));
                    chip.setTextColor(unselectedText);
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigator = null;
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etDateOfBirth.getText())) {
            etDateOfBirth.setError("Required");
            return false;
        }
        if (TextUtils.isEmpty(etIdNumber.getText())) {
            etIdNumber.setError("ID Number is required");
            etIdNumber.requestFocus();
            return false;
        }
        if (selectedRiderType == null) {
            Toast.makeText(getContext(), "Select a rider type", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etAreaOfOperation.getText())) {
            etAreaOfOperation.setError("Required");
            return false;
        }
        // Notice we do NOT force the image upload to be required. This prevents the app from blocking
        // a user if they haven't granted gallery permissions yet or want to upload it later.
        return true;
    }
}