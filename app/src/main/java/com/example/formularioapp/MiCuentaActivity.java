package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button; // Importar Button
import android.widget.Toast; // Importar Toast
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth; // Importar FirebaseAuth

public class MiCuentaActivity extends AppCompatActivity {

    // Instancia de Firebase Authentication
    private FirebaseAuth mAuth;
    // Declaración del botón
    private Button btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mi_cuenta_activity);

        // 1. Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 2. Configurar la Toolbar para volver atrás
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 3. Referenciar el botón de Cerrar Sesión (Asegúrate que el ID es 'btnCerrarSesion')
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        // 4. Implementar el Listener del botón
        btnCerrarSesion.setOnClickListener(v -> {

            // Llama a la función que maneja el cierre de sesión
            cerrarSesion();
        });
    }

    // --- Lógica de Cierre de Sesión ---
    private void cerrarSesion() {
        // Cierra la sesión del usuario en Firebase
        mAuth.signOut();

        Toast.makeText(this, "Sesión cerrada. Vuelva pronto.", Toast.LENGTH_LONG).show();

        // Navegar a la Activity de Login
        Intent intent = new Intent(MiCuentaActivity.this, Login_Activity.class);

        // Limpiar la pila de actividades para que el usuario no pueda volver con el botón "Atrás"
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finalizar la actividad actual
        finish();
    }
}