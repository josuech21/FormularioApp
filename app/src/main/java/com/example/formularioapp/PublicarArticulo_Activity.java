package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import android.view.View;
import android.widget.TextView; // Para mostrar el nombre del artículo
import android.widget.RatingBar; // Para la barra de rating
import android.widget.ImageView; // Para la imagen

public class PublicarArticulo_Activity extends AppCompatActivity {

    // Componentes de la Interfaz
    private Button btnModificar;
    private Button btnEliminar;
    private Button btnPublicar; // El botón grande que dice "Publicar Artículo"
    private Switch switchDisponible;

    // Elementos de visualización (para referencia)
    private TextView txtNombreProducto;

    // Simulación de un ID de Artículo (en una app real se recibiría por Intent)
    private final int ARTICULO_ID = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asumiendo que el nombre de tu archivo XML de esta vista es 'activity_detalle_vendedor.xml'
        // o similar. Debes usar el nombre correcto.
        setContentView(R.layout.publicar_vendedor);

        // --- 1. Referencias de Vistas ---
        btnModificar = findViewById(R.id.btnModificar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnPublicar = findViewById(R.id.btnAgregarCarrito); // Usa el ID del botón grande
        switchDisponible = findViewById(R.id.switchDisponible);
        txtNombreProducto = findViewById(R.id.txtNombreProducto);

        // Simular la carga inicial de datos (para fines de prueba)
        txtNombreProducto.setText("Chaqueta de Cuero Vintage");
        btnPublicar.setText("GUARDAR CAMBIOS"); // Se cambia el texto para reflejar su función de edición

        // --- 2. Lógica de los Botones de Acción ---

        // A) Botón MODIFICAR (Generalmente implica habilitar campos de texto)
        btnModificar.setOnClickListener(v -> {
            Toast.makeText(this, "Modo de edición activado. Modifique los campos.", Toast.LENGTH_SHORT).show();

            // Lógica real: Habilitar o mostrar los EditTexts necesarios para cambiar nombre, precio, etc.
            // Por ejemplo: txtNombreProducto.setEnabled(true);
        });

        // B) Botón ELIMINAR
        btnEliminar.setOnClickListener(v -> {
            // Lógica real: Abrir un diálogo de confirmación antes de eliminar permanentemente
            confirmarYEliminarArticulo(ARTICULO_ID, txtNombreProducto.getText().toString());
        });

        // C) Botón PUBLICAR / GUARDAR CAMBIOS
        btnPublicar.setOnClickListener(v -> {

            Toast.makeText(this, "Artículo '" + txtNombreProducto.getText().toString() + "' actualizado y guardado.",
                    Toast.LENGTH_LONG).show();

            // Opcional: Volver al Feed o a la lista de artículos del vendedor
            // finish();
        });

        // --- 3. Lógica del Switch de Disponibilidad ---
        switchDisponible.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String estado = isChecked ? "DISPONIBLE" : "VENDIDO";
            Toast.makeText(this, "Estado del artículo cambiado a: " + estado, Toast.LENGTH_SHORT).show();

            // Lógica real:
            // Actualizar el campo 'disponible' en la base de datos del artículo con ARTICULO_ID
        });
    }

    private void confirmarYEliminarArticulo(int id, String nombre) {
        Toast.makeText(this, "Eliminando " + nombre + " (ID: " + id + ")...", Toast.LENGTH_LONG).show();


        finish(); // Cierra la actividad para simular que el artículo ya no está
    }
}