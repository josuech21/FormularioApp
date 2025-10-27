package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Micarrito_Activity extends AppCompatActivity {

    // Componentes de la Interfaz
    private Button btnEliminarItem;
    private Button btnProcederPago;
    private EditText editCantidad;
    private TextView txtTotal;
    private TextView txtPrecioItemCarrito;

    // Variables de ejemplo para simular datos del carrito
    // En una app real, este precio vendría de la base de datos o de un objeto Articulo.
    private final double PRECIO_UNITARIO = 19250.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asegúrate de que este sea el nombre de tu archivo XML del carrito (ej: activity_carrito.xml)
        setContentView(R.layout.modificar_carrito_comprador);

        // --- 1. Referencias de Vistas ---
        btnEliminarItem = findViewById(R.id.btnEliminarItem);
        btnProcederPago = findViewById(R.id.btnProcederPago);
        editCantidad = findViewById(R.id.editCantidad);
        txtTotal = findViewById(R.id.txtTotal);
        txtPrecioItemCarrito = findViewById(R.id.txtPrecioItemCarrito);

        // Simular que se carga el precio unitario en el TextView
        txtPrecioItemCarrito.setText("₡ " + String.format("%.2f", PRECIO_UNITARIO));

        // --- 2. Implementación de Lógica ---

        // Carga el cálculo inicial del total
        calcularTotal();

        // Listener para actualizar el total cuando el usuario cambia la cantidad
        editCantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Llamamos a calcularTotal cada vez que cambia el texto
                calcularTotal();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Botón ELIMINAR
        btnEliminarItem.setOnClickListener(v -> {
            Toast.makeText(Micarrito_Activity.this, "Artículo eliminado. Recargando carrito...", Toast.LENGTH_SHORT).show();

            // Lógica de eliminación simulada:
            // 1. Aquí se eliminaría el artículo de la lista de datos.
            // 2. Si solo queda un artículo (como en el XML), se podría navegar a un carrito vacío
            //    o simplemente establecer el total a cero y ocultar el artículo visualmente.

            // Simulación simple:
            txtTotal.setText("TOTAL: ₡ 0.00");
        });

        // Botón PROCEDER AL PAGO
        btnProcederPago.setOnClickListener(v -> {
            // Se usa el valor del TextView (aunque en realidad se debería usar el valor double calculado)
            String totalText = txtTotal.getText().toString();

            if (totalText.contains("0.00")) {
                Toast.makeText(Micarrito_Activity.this, "El carrito está vacío. No se puede proceder.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(Micarrito_Activity.this, "Navegando a la pantalla de Pago...", Toast.LENGTH_LONG).show();


            // Intent pagoIntent = new Intent(Micarrito_Activity.this, PagoActivity.class);
            // startActivity(pagoIntent);
        });
    }


    private void calcularTotal() {
        String cantidadStr = editCantidad.getText().toString();

        if (cantidadStr.isEmpty()) {
            //  campo está vacío temporalmente
            txtTotal.setText("TOTAL: ₡ 0.00");
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantidadStr);

            // Simulación: Si la cantidad es 0, no hay costo
            if (cantidad <= 0) {
                txtTotal.setText("TOTAL: ₡ 0.00");
                return;
            }

            // Cálculo del subtotal (solo para el artículo simulado)
            double subtotal = PRECIO_UNITARIO * cantidad;

            // En una aplicación real, sumarías los subtotales de todos los artículos de tu lista.

            txtTotal.setText("TOTAL: ₡ " + String.format("%.2f", subtotal));

        } catch (NumberFormatException e) {
            // Esto maneja errores si el texto no puede convertirse a número
            txtTotal.setText("TOTAL: ₡ ERROR");
        }
    }
}