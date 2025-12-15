package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.appbar.MaterialToolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import java.util.Collections;
import java.util.List;

public class HistorialdeCompras_Activity extends AppCompatActivity {

    private TextView txtMensajeVacio;
    private LinearLayout containerHistorial;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Asignar el layout de historial
        setContentView(R.layout.historial_compras);

        // Inicializar vistas
        txtMensajeVacio = findViewById(R.id.txtMensajeVacio);
        containerHistorial = findViewById(R.id.containerHistorial);
        toolbar = findViewById(R.id.toolbarHistorial);

        setupToolbar();
        loadHistorialData();
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setTitle("Historial de Compras");
            // Configura el botón de retroceso
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void loadHistorialData() {
        // --- LÓGICA DE CARGA DE DATOS REAL (Reemplazar la simulación) ---
        List<String> listaDeCompras = obtenerListaDeComprasDelUsuario(); // Obtiene la lista (real o simulada)

        boolean historialVacio = (listaDeCompras == null || listaDeCompras.isEmpty());

        if (historialVacio) {
            // Caso 1: Historial Vacío
            if (txtMensajeVacio != null) {
                txtMensajeVacio.setText("Aún no se han generado compras de artículos" );
                txtMensajeVacio.setVisibility(View.VISIBLE);
            }
            if (containerHistorial != null) {
                containerHistorial.setVisibility(View.GONE);
            }
        } else {
            // Caso 2: Historial con Datos (Aquí se cargaría el RecyclerView)
            if (txtMensajeVacio != null) {
                txtMensajeVacio.setVisibility(View.GONE);
            }
            if (containerHistorial != null) {
                containerHistorial.setVisibility(View.VISIBLE);
                // Aquí iría la lógica para mostrar 'listaDeCompras' en un adaptador
            }
        }
    }

    // Función de Simulación: Actualmente devuelve una lista vacía.
    private List<String> obtenerListaDeComprasDelUsuario() {
        // return List.of("Item A", "Item B"); // Descomenta para probar el caso lleno
        return Collections.emptyList(); // Mantiene la simulación de historial vacío
    }
}