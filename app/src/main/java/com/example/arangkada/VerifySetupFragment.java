package com.example.arangkada;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class VerifySetupFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup_verify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("ArangkadaSetup", Context.MODE_PRIVATE);

        // --- 1. Profile Data ---
        TextView tvDob          = view.findViewById(R.id.tv_verify_dob);
        TextView tvIdTypeLabel  = view.findViewById(R.id.tv_verify_id_type_label);
        TextView tvIdNo         = view.findViewById(R.id.tv_verify_id_no);
        TextView tvPlatforms    = view.findViewById(R.id.tv_verify_platforms);
        TextView tvArea         = view.findViewById(R.id.tv_verify_area);
        TextView tvDailyTarget  = view.findViewById(R.id.tv_verify_daily_target);

        tvDob.setText(prefs.getString("setup_dob", "N/A"));
        tvIdTypeLabel.setText(prefs.getString("setup_id_type", "ID") + " No.");
        tvIdNo.setText(prefs.getString("setup_id_no", "N/A"));
        tvPlatforms.setText(prefs.getString("setup_platforms", "N/A") + " (" + prefs.getString("setup_rider_type", "N/A") + ")");
        tvArea.setText(prefs.getString("setup_area", "N/A"));

        String dailyTarget = prefs.getString("setup_daily_target", "0");
        tvDailyTarget.setText(dailyTarget.isEmpty() ? "N/A" : "₱ " + dailyTarget);

        // --- 2. Motorcycle Data ---
        TextView tvPlate = view.findViewById(R.id.tv_verify_plate);
        tvPlate.setText(prefs.getString("setup_moto_plate", "N/A"));

        // --- 3. Financial Data ---
        TextView tvMonthlyGoal   = view.findViewById(R.id.tv_verify_monthly_goal);
        TextView tvSinking       = view.findViewById(R.id.tv_verify_sinking);
        TextView tvSavingsGoal   = view.findViewById(R.id.tv_verify_savings_goal);
        TextView tvPayday        = view.findViewById(R.id.tv_verify_payday);
        TextView tvPayoutMethod  = view.findViewById(R.id.tv_verify_payout_method);
        TextView tvAccountDetail = view.findViewById(R.id.tv_verify_account_details);

        String monthlyGoal = prefs.getString("setup_monthly_goal", "0");
        tvMonthlyGoal.setText(monthlyGoal.isEmpty() ? "N/A" : "₱ " + monthlyGoal);
        tvSinking.setText(prefs.getString("setup_sinking_fund", "100%"));
        tvSavingsGoal.setText(prefs.getString("setup_savings_goal", "None"));
        tvPayday.setText(prefs.getString("setup_payday", "N/A"));
        tvPayoutMethod.setText(prefs.getString("setup_payout_method", "N/A"));
        tvAccountDetail.setText(prefs.getString("setup_account_details", "N/A"));

        // --- 4. Final Confirm ---
        Button btnConfirmDashboard = view.findViewById(R.id.btn_confirm_dashboard);
        btnConfirmDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });
    }
}