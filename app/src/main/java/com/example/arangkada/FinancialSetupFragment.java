package com.example.arangkada;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FinancialSetupFragment extends Fragment {

    private EditText etMonthlyIncomeGoal;
    private SeekBar  seekBarSinkingFund;
    private TextView tvSinkingFundPercent;
    private EditText etSavingsGoal;

    private View btnPaydayDaily, btnPaydayWeekly, btnPaydayBiweekly, btnPaydayMonthly;
    private Button btnPayoutGCash, btnPayoutMaya, btnPayoutBankTransfer, btnPayoutCash;

    private EditText etGCashNumber;
    private View     layoutGCashNumber;

    private EditText etMayaNumber;
    private View     layoutMayaNumber;

    private Spinner  spinnerBank;
    private EditText etBankAccountNumber;
    private View     layoutBankTransfer;

    private Button btnFinish;

    private String selectedPaydaySchedule = null;
    private String selectedPayoutMethod   = null;
    private SetupNavigator navigator;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SetupNavigator) navigator = (SetupNavigator) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup_financial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etMonthlyIncomeGoal  = view.findViewById(R.id.et_monthly_income_goal);
        seekBarSinkingFund   = view.findViewById(R.id.seekbar_sinking_fund);
        tvSinkingFundPercent = view.findViewById(R.id.tv_sinking_fund_percent);
        etSavingsGoal        = view.findViewById(R.id.et_savings_goal);

        btnPaydayDaily     = view.findViewById(R.id.btn_payday_daily);
        btnPaydayWeekly    = view.findViewById(R.id.btn_payday_weekly);
        btnPaydayBiweekly  = view.findViewById(R.id.btn_payday_biweekly);
        btnPaydayMonthly   = view.findViewById(R.id.btn_payday_monthly);

        btnPayoutGCash       = view.findViewById(R.id.btn_payout_gcash);
        btnPayoutMaya        = view.findViewById(R.id.btn_payout_maya);
        btnPayoutBankTransfer= view.findViewById(R.id.btn_payout_bank_transfer);
        btnPayoutCash        = view.findViewById(R.id.btn_payout_cash);

        etGCashNumber       = view.findViewById(R.id.et_gcash_number);
        layoutGCashNumber   = view.findViewById(R.id.layout_gcash_number);
        etMayaNumber        = view.findViewById(R.id.et_maya_number);
        layoutMayaNumber    = view.findViewById(R.id.layout_maya_number);
        spinnerBank         = view.findViewById(R.id.spinner_bank);
        etBankAccountNumber = view.findViewById(R.id.et_bank_account_number);
        layoutBankTransfer  = view.findViewById(R.id.layout_bank_transfer);

        btnFinish = view.findViewById(R.id.btn_finish);

        String[] localBanks = {"BDO Unibank", "BPI", "Metrobank", "UnionBank", "Landbank", "Security Bank", "RCBC"};
        ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, localBanks);
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBank.setAdapter(bankAdapter);

        if (seekBarSinkingFund != null) {
            seekBarSinkingFund.setMax(100);
            seekBarSinkingFund.setProgress(100);
            if (tvSinkingFundPercent != null) tvSinkingFundPercent.setText("100%");

            seekBarSinkingFund.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (tvSinkingFundPercent != null) tvSinkingFundPercent.setText(progress + "%");
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        View.OnClickListener paydayListener = v -> {
            btnPaydayDaily.setSelected(false); btnPaydayWeekly.setSelected(false);
            btnPaydayBiweekly.setSelected(false); btnPaydayMonthly.setSelected(false);

            v.setSelected(true);
            if (v.getId() == R.id.btn_payday_daily)         selectedPaydaySchedule = "Daily";
            else if (v.getId() == R.id.btn_payday_weekly)   selectedPaydaySchedule = "Weekly";
            else if (v.getId() == R.id.btn_payday_biweekly) selectedPaydaySchedule = "Bi-weekly";
            else if (v.getId() == R.id.btn_payday_monthly)  selectedPaydaySchedule = "Monthly";
        };
        btnPaydayDaily.setOnClickListener(paydayListener);
        btnPaydayWeekly.setOnClickListener(paydayListener);
        btnPaydayBiweekly.setOnClickListener(paydayListener);
        btnPaydayMonthly.setOnClickListener(paydayListener);

        View.OnClickListener payoutListener = v -> {
            btnPayoutGCash.setSelected(false); btnPayoutMaya.setSelected(false);
            btnPayoutBankTransfer.setSelected(false); btnPayoutCash.setSelected(false);

            layoutGCashNumber.setVisibility(View.GONE);
            layoutMayaNumber.setVisibility(View.GONE);
            layoutBankTransfer.setVisibility(View.GONE);

            v.setSelected(true);
            if (v.getId() == R.id.btn_payout_gcash) {
                selectedPayoutMethod = "GCash";
                layoutGCashNumber.setVisibility(View.VISIBLE);
            } else if (v.getId() == R.id.btn_payout_maya) {
                selectedPayoutMethod = "Maya";
                layoutMayaNumber.setVisibility(View.VISIBLE);
            } else if (v.getId() == R.id.btn_payout_bank_transfer) {
                selectedPayoutMethod = "Bank Transfer";
                layoutBankTransfer.setVisibility(View.VISIBLE);
            } else if (v.getId() == R.id.btn_payout_cash) {
                selectedPayoutMethod = "Cash";
            }
        };
        btnPayoutGCash.setOnClickListener(payoutListener);
        btnPayoutMaya.setOnClickListener(payoutListener);
        btnPayoutBankTransfer.setOnClickListener(payoutListener);
        btnPayoutCash.setOnClickListener(payoutListener);

        btnFinish.setOnClickListener(v -> {
            if (validateInputs()) {
                String accountDetails = "Cash on Hand";
                if ("GCash".equals(selectedPayoutMethod)) accountDetails = etGCashNumber.getText().toString().trim();
                else if ("Maya".equals(selectedPayoutMethod)) accountDetails = etMayaNumber.getText().toString().trim();
                else if ("Bank Transfer".equals(selectedPayoutMethod)) accountDetails = spinnerBank.getSelectedItem().toString() + " - " + etBankAccountNumber.getText().toString().trim();

                String savingsGoal = etSavingsGoal.getText().toString().trim();
                if (savingsGoal.isEmpty()) savingsGoal = "None";

                SharedPreferences prefs = requireActivity().getSharedPreferences("ArangkadaSetup", Context.MODE_PRIVATE);
                prefs.edit()
                        .putString("setup_monthly_goal", etMonthlyIncomeGoal.getText().toString().trim())
                        .putString("setup_sinking_fund", tvSinkingFundPercent.getText().toString())
                        .putString("setup_savings_goal", savingsGoal)
                        .putString("setup_payday", selectedPaydaySchedule)
                        .putString("setup_payout_method", selectedPayoutMethod)
                        .putString("setup_account_details", accountDetails)
                        .apply();

                if (navigator != null) navigator.goToNextStep();
            }
        });
    }

    private boolean validateInputs() {
        if (etMonthlyIncomeGoal.getText().toString().trim().isEmpty()) {
            etMonthlyIncomeGoal.setError("Required"); return false;
        }
        if (selectedPaydaySchedule == null) {
            Toast.makeText(getContext(), "Select a payday schedule", Toast.LENGTH_SHORT).show(); return false;
        }
        if (selectedPayoutMethod == null) {
            Toast.makeText(getContext(), "Select a payout method", Toast.LENGTH_SHORT).show(); return false;
        }
        if ("GCash".equals(selectedPayoutMethod) && etGCashNumber.getText().toString().trim().isEmpty()) {
            etGCashNumber.setError("Required"); return false;
        }
        if ("Maya".equals(selectedPayoutMethod) && etMayaNumber.getText().toString().trim().isEmpty()) {
            etMayaNumber.setError("Required"); return false;
        }
        if ("Bank Transfer".equals(selectedPayoutMethod) && etBankAccountNumber.getText().toString().trim().isEmpty()) {
            etBankAccountNumber.setError("Required"); return false;
        }
        return true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigator = null;
    }
}