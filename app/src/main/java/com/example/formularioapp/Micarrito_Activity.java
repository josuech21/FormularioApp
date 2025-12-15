package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class Micarrito_Activity extends AppCompatActivity {

    private static final String TAG = "Micarrito_Activity";

    // Vistas principales funcionales
    private TextView txtTotal;
    private Button btnProcederPago;
    private Button btnVaciarCarritoGlobal;

    // Contenedor donde se inyectan dinámicamente los artículos
    private LinearLayout layoutContenedorItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modificar_carrito_comprador);

        vincularVistas();

        // 1. Mostrar la lista de artículos al inicio
        mostrarItemsDelCarrito();

        // 2. Listener para vaciar todo el carrito
        if (btnVaciarCarritoGlobal != null) {
            btnVaciarCarritoGlobal.setOnClickListener(v -> confirmarYVaciarCarrito());
        }

        // 3. Listener para Proceder al Pago
        if (btnProcederPago != null) {
            btnProcederPago.setOnClickListener(v -> {
                if (RepositorioCarrito.getCartItems().isEmpty()) {
                    Toast.makeText(this, "El carrito está vacío. Añade artículos desde el Feed.", Toast.LENGTH_LONG).show();
                } else {
                    iniciarProcesoDePago();
                }
            });
        }
    }

    private void vincularVistas() {
        txtTotal = findViewById(R.id.txtTotal);
        btnProcederPago = findViewById(R.id.btnProcederPago);


        layoutContenedorItems = findViewById(R.id.itemCarritoContenedor);


        btnVaciarCarritoGlobal = findViewById(R.id.btnEliminarItem);

        // Ocultamos la fila de ejemplo (para asegurar que solo se muestren los elementos dinámicos)
        View filaEjemplo = findViewById(R.id.txtNombreItemCarrito);
        if (filaEjemplo != null) {
            View parent = (View) filaEjemplo.getParent();
            if (parent != null) {
                parent.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar la lista y el total cada vez que volvemos (ej: después de eliminar algo)
        mostrarItemsDelCarrito();
    }

    private void mostrarItemsDelCarrito() {
        if (layoutContenedorItems == null) return;

        layoutContenedorItems.removeAllViews(); // Limpiar vistas anteriores
        List<Producto> items = RepositorioCarrito.getCartItems();

        if (items.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Tu carrito está vacío.");
            emptyText.setGravity(Gravity.CENTER_HORIZONTAL);
            emptyText.setPadding(0, 64, 0, 64);
            emptyText.setTextSize(18);
            layoutContenedorItems.addView(emptyText);
        } else {
            for (Producto item : items) {
                // Generar la fila de forma dinámica
                layoutContenedorItems.addView(crearFilaArticulo(item));
            }
        }

        updateTotalDisplay();
    }

    private LinearLayout crearFilaArticulo(Producto item) {
        // --- Contenedor Principal (HORIZONTAL) ---
        LinearLayout filaLayout = new LinearLayout(this);
        filaLayout.setOrientation(LinearLayout.HORIZONTAL);
        filaLayout.setPadding(12, 12, 12, 12);
        filaLayout.setBackgroundColor(getResources().getColor(android.R.color.white));

        // Agregar un borde o divisor visual
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 12);
        filaLayout.setLayoutParams(params);

        // --- 1. Contenedor de Texto (Nombre y Precio) ---
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f); // Peso 1 para ocupar espacio
        textLayout.setLayoutParams(textParams);

        TextView txtNombre = new TextView(this);
        txtNombre.setText(item.getNombre());
        txtNombre.setTextSize(16);
        textLayout.addView(txtNombre);

        TextView txtPrecio = new TextView(this);
        txtPrecio.setText(String.format(Locale.getDefault(), "₡ %,.2f", item.getPrecio()));
        txtPrecio.setTextSize(14);
        textLayout.addView(txtPrecio);

        // --- 2. Botón Eliminar ---
        Button btnEliminar = new Button(this);
        btnEliminar.setText("Eliminar");
        btnEliminar.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));

        // La lógica de eliminación está aquí
        btnEliminar.setOnClickListener(v -> eliminarArticulo(item));

        // --- Añadir sub-vistas a la fila principal ---
        filaLayout.addView(textLayout);
        filaLayout.addView(btnEliminar);

        return filaLayout;
    }

    private void eliminarArticulo(Producto item) {
        RepositorioCarrito.removeItem(item);
        Toast.makeText(this, item.getNombre() + " eliminado.", Toast.LENGTH_SHORT).show();
        mostrarItemsDelCarrito(); // Recargar la lista y el total
    }

    private double calculateTotal() {
        double total = 0.0;
        for (Producto item : RepositorioCarrito.getCartItems()) {
            total += item.getPrecio();
        }
        return total;
    }

    private void updateTotalDisplay() {
        double total = calculateTotal();
        txtTotal.setText(String.format(Locale.getDefault(), "TOTAL: ₡ %,.2f", total));

        boolean isEmpty = RepositorioCarrito.getCartItems().isEmpty();
        // Desactivar el botón de pago si el carrito está vacío
        btnProcederPago.setEnabled(!isEmpty);

        // Mostrar/Ocultar el botón de vaciado global
        if (btnVaciarCarritoGlobal != null) {
            btnVaciarCarritoGlobal.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void confirmarYVaciarCarrito() {
        if (RepositorioCarrito.getCartItems().isEmpty()) return;

        RepositorioCarrito.clearCart();
        Toast.makeText(this, "Carrito vaciado con éxito.", Toast.LENGTH_SHORT).show();
        mostrarItemsDelCarrito(); // Recargar la lista (ahora vacía)
    }

    private void iniciarProcesoDePago() {
        // **IMPORTANTE**: Reemplaza 'FacturaActivity.class' por el nombre real de tu Activity de pago.
        startActivity(new Intent(Micarrito_Activity.this, FacturaActivity.class));
    }
}