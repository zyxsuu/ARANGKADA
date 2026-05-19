package com.example.arangkada;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * SetupActivity — hosts the 4-step onboarding flow after registration.
 * Links to: activity_setup.xml
 *
 * Step flow:
 * Step 1 → ProfileSetupFragment    (profile details + rider work info)
 * Step 2 → MotorcycleSetupFragment (motorcycle details)
 * Step 3 → FinancialSetupFragment  (financial preferences)
 * Step 4 → VerifySetupFragment     (final summary review screen)
 */
public class SetupActivity extends AppCompatActivity implements SetupNavigator {

    private static final int CONTAINER_ID = R.id.fragment_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        if (savedInstanceState == null) {
            loadFragment(new ProfileSetupFragment(), false);
        }
    }

    /**
     * Determines the next fragment based on what is currently on top of the back-stack
     * and replaces the container with it.
     */
    @Override
    public void goToNextStep() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment current   = fm.findFragmentById(CONTAINER_ID);

        Fragment next;
        if (current instanceof ProfileSetupFragment) {
            next = new MotorcycleSetupFragment();
        } else if (current instanceof MotorcycleSetupFragment) {
            next = new FinancialSetupFragment();
        } else if (current instanceof FinancialSetupFragment) {
            next = new VerifySetupFragment(); // Advances to the new summary view
        } else {
            return;
        }

        loadFragment(next, true);
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(CONTAINER_ID, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}