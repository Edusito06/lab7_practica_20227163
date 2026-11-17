package com.lab6.iot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

/**
 * Pantalla 1: Inicio de sesion con Firebase UI y layout personalizado.
 * Desde la misma interfaz se puede: iniciar sesion, registrarse y recuperar la contrasena
 * (Firebase UI lo maneja dentro del flujo de correo).
 */
public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new FirebaseAuthUIActivityResultContract(), this::onSignInResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si ya inicio sesion antes, va directo a la pantalla principal
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            irAPrincipal();
            return;
        }

        setContentView(R.layout.activity_main);
        findViewById(R.id.btnIngresar).setOnClickListener(v -> lanzarFirebaseUI());

        if (savedInstanceState == null) lanzarFirebaseUI();
    }

    private void lanzarFirebaseUI() {
        List<AuthUI.IdpConfig> proveedores = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder()
                        .setRequireName(true)
                        .build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Layout personalizado: logo + textos en espanol + botones
        AuthMethodPickerLayout layout = new AuthMethodPickerLayout
                .Builder(R.layout.activity_auth_picker)
                .setEmailButtonId(R.id.btnCorreo)
                .setGoogleButtonId(R.id.btnGoogle)
                .build();

        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(proveedores)
                .setAuthMethodPickerLayout(layout)
                .setLogo(R.drawable.ic_logo)
                .setTheme(R.style.Theme_Lab6_Auth)
                .setIsSmartLockEnabled(false)
                .build();

        signInLauncher.launch(intent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            irAPrincipal();
        } else if (response == null) {
            // El usuario presiono "atras": se queda en esta pantalla con el boton Ingresar
            Toast.makeText(this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
        } else if (response.getError() != null) {
            Toast.makeText(this, "Error: " + response.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void irAPrincipal() {
        Intent i = new Intent(this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
