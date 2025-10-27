package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// Esta Activity maneja la lógica de la pantalla de perfil/cuenta.
public class Cuenta_acceso extends AppCompatActivity {

    // DECLARACIÓN DE ELEMENTOS INTERACTIVOS
    private Button btnHistorialCompras;
    private Button btnModificarDatos;
    private Button btnMetodosPago;
    private Button btnGestionProductos;
    private Button btnCerrarSesion;

    // DECLARACIÓN DE CAMPOS DE SOLO LECTURA (PARA ACCESO A DATOS)
    private EditText txtPerfilNombre;
    private EditText txtPerfilCorreo;
    private EditText txtPerfilContacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Conecta esta Activity con el layout de Mi Cuenta
        setContentView(R.layout.cuenta_acceso);

        // --- 1. Referencias a los elementos del XML ---
        btnHistorialCompras = findViewById(R.id.btnHistorialCompras);
        btnModificarDatos = findViewById(R.id.btnModificarDatos);
        btnMetodosPago = findViewById(R.id.btnMetodosPago);
        btnGestionProductos = findViewById(R.id.btnGestionProductos);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        // Inicialización de campos de perfil (para mostrar/acceder a datos)
        txtPerfilNombre = findViewById(R.id.txtPerfilNombre);
        txtPerfilCorreo = findViewById(R.id.txtPerfilCorreo);
        txtPerfilContacto = findViewById(R.id.txtPerfilContacto);

        // CERRAR SESIÓN
        btnCerrarSesion.setOnClickListener(v -> {


            Toast.makeText(Cuenta_acceso.this, "Cerrando sesión. Volviendo a Login.", Toast.LENGTH_LONG).show();

            // 2. Navegar a LoginActivity y limpiar toda la pila anterior (Feed, Cuenta, etc.)
            Intent intent = new Intent(this, Login_Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpia la pila
            startActivity(intent);
            finish();
        });
    }
}