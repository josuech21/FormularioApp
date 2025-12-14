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

    private Button btnEliminarItem;
    private Button btnProcederPago;
    private EditText editCantidad;
    private TextView txtTotal;
    private TextView txtPrecioItemCarrito;

    private final double PRECIO_UNITARIO = 19250.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modificar_carrito_comprador);

        btnEliminarItem = findViewById(R.id.btnEliminarItem);
        btnProcederPago = findViewById(R.id.btnProcederPago);
        editCantidad = findViewById(R.id.editCantidad);
        txtTotal = findViewById(R.id.txtTotal);
        txtPrecioItemCarrito = findViewById(R.id.txtPrecioItemCarrito);

        txtPrecioItemCarrito.setText("₡ " + String.format("%.2f", PRECIO_UNITARIO));

        if (editCantidad.getText().toString().isEmpty()) {
            editCantidad.setText("1");
        }

        calcularTotal();

        editCantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularTotal();
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnEliminarItem.setOnClickListener(v -> {
            Toast.makeText(Micarrito_Activity.this, "Artículo eliminado. Recargando carrito...", Toast.LENGTH_SHORT).show();
            editCantidad.setText("0");
        });

        btnProcederPago.setOnClickListener(v -> {
            int cantidadActual = obtenerCantidadDelArticulo();

            if (cantidadActual <= 0) {
                Toast.makeText(Micarrito_Activity.this, "El carrito está vacío. Añade al menos un artículo.", Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(Micarrito_Activity.this, "Navegando a la pantalla de Pago...", Toast.LENGTH_LONG).show();
            // Intent pagoIntent = new Intent(Micarrito_Activity.this, PagoActivity.class);
            // startActivity(pagoIntent);
        });
    }

    private int obtenerCantidadDelArticulo() {
        String cantidadStr = editCantidad.getText().toString().trim();
        if (cantidadStr.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(cantidadStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void calcularTotal() {
        int cantidad = obtenerCantidadDelArticulo();

        if (cantidad <= 0) {
            txtTotal.setText("TOTAL: ₡ 0.00");
            return;
        }

        double subtotal = PRECIO_UNITARIO * cantidad;
        txtTotal.setText("TOTAL: ₡ " + String.format("%.2f", subtotal));
    }
}