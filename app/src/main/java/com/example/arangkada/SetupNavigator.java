package com.example.arangkada;

/**
 * SetupNavigator — interface that fragments call to advance to the next onboarding step.
 *
 * Kept in its own file to avoid the cyclic inheritance error that occurs when
 * the interface is declared as a nested type inside SetupActivity and then
 * SetupActivity also implements it.
 *
 * Usage:
 *   - SetupActivity implements this interface.
 *   - Each fragment casts getActivity() to SetupNavigator and calls goToNextStep().
 */
public interface SetupNavigator {
    void goToNextStep();
}
