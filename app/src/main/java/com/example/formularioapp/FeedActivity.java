package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class FeedActivity extends AppCompatActivity {

    // Componentes de la barra de navegación superior
    private TextView txtMiCuenta;
    private TextView txtMiCarrito;

    // ELIMINAMOS las declaraciones individuales de botones (btnAgregarChaqueta, etc.)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_app);

        // --- 1. Referencias de Vistas de Navegación ---
        txtMiCuenta = findViewById(R.id.txtMiCuenta);
        txtMiCarrito = findViewById(R.id.txtMiCarrito);

        // YA NO NECESITAS findViewById para los botones de artículo, el XML lo maneja.

        // --- 2. Lógica de Navegación de la Barra Superior ---

        // Navegación a Mi Cuenta
        txtMiCuenta.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, MiCuentaActivity.class);
            startActivity(intent);
        });

        // Navegación a Mi Carrito
        txtMiCarrito.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, Micarrito_Activity.class);
            startActivity(intent);
        });


    }



    }
