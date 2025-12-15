package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FacturaActivity extends AppCompatActivity {

    private static final double TASA_IMPUESTO = 0.13; // Tasa de impuesto (13% para Costa Rica)

    // Vistas enlazadas al XML (activity_factura.xml)
    private LinearLayout layoutFacturaItems; // Contenedor de ítems
    private TextView txtSubtotal;            // TextView para el subtotal
    private TextView txtImpuestos;           // TextView para los impuestos
    private TextView txtTotalFactura;        // TextView para el total final
    private Button btnConfirmarPago;         // Botón para finalizar la orden

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asegúrate de que tu layout XML se llama factura_activity.xml
        setContentView(R.layout.factura_activity);

        vincularVistas();

        // 1. Cargar la lista de productos y calcular totales
        cargarResumenDeOrden();

        // 2. Listener para finalizar la orden (enlazado a R.id.btnConfirmarPago)
        if (btnConfirmarPago != null) {
            btnConfirmarPago.setOnClickListener(v -> confirmarYFinalizarOrden());
        }
    }

    private void vincularVistas() {
        // Enlace estricto de IDs del XML (factura_activity.xml)
        layoutFacturaItems = findViewById(R.id.layoutFacturaItems);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtImpuestos = findViewById(R.id.txtImpuestos);
        txtTotalFactura = findViewById(R.id.txtTotalFactura);
        btnConfirmarPago = findViewById(R.id.btnConfirmarPago);
    }

    private void cargarResumenDeOrden() {
        List<Producto> items = RepositorioCarrito.getCartItems();
        double subtotal = 0.0;

        if (items.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío. No hay orden para facturar.", Toast.LENGTH_LONG).show();
            //finish(); // Opcional: Cerrar la actividad si está vacía
            return;
        }

        if (layoutFacturaItems == null) return;
        layoutFacturaItems.removeAllViews(); // Limpia el contenedor antes de inyectar

        for (Producto item : items) {
            // Llenar el LinearLayout dinámicamente
            layoutFacturaItems.addView(crearFilaFacturaItem(item));
            subtotal += item.getPrecio();
        }

        calcularYMostrarTotales(subtotal);
    }

    private void calcularYMostrarTotales(double subtotal) {
        double impuestos = subtotal * TASA_IMPUESTO;
        double totalFinal = subtotal + impuestos;

        // Formato de moneda para una presentación limpia
        String subtotalStr = String.format(Locale.getDefault(), "Subtotal: ₡ %,.2f", subtotal);
        String impuestosStr = String.format(Locale.getDefault(), "Impuestos (%d%%): ₡ %,.2f", (int)(TASA_IMPUESTO * 100), impuestos);
        String totalStr = String.format(Locale.getDefault(), "TOTAL A PAGAR: ₡ %,.2f", totalFinal);

        // Actualización de los TextViews del pie de página
        if (txtSubtotal != null) txtSubtotal.setText(subtotalStr);
        if (txtImpuestos != null) txtImpuestos.setText(impuestosStr);
        if (txtTotalFactura != null) txtTotalFactura.setText(totalStr);
    }

    /**
     * Crea un LinearLayout horizontal para mostrar una fila de producto en la factura.
     */
    private LinearLayout crearFilaFacturaItem(Producto item) {
        LinearLayout filaLayout = new LinearLayout(this);
        filaLayout.setOrientation(LinearLayout.HORIZONTAL);
        filaLayout.setPadding(0, 8, 0, 8);

        // 1. Nombre del Producto (Peso 1 para ocupar espacio)
        TextView txtNombre = new TextView(this);
        txtNombre.setText(item.getNombre());
        txtNombre.setTextSize(14);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        txtNombre.setLayoutParams(nameParams);

        // 2. Precio del Producto (Alineado a la derecha)
        TextView txtPrecio = new TextView(this);
        txtPrecio.setText(String.format(Locale.getDefault(), "₡ %,.2f", item.getPrecio()));
        txtPrecio.setTextSize(14);
        txtPrecio.setGravity(Gravity.END);

        filaLayout.addView(txtNombre);
        filaLayout.addView(txtPrecio);

        return filaLayout;
    }

    private void confirmarYFinalizarOrden() {
        List<Producto> itemsComprados = RepositorioCarrito.getCartItems();

        if (itemsComprados.isEmpty()) {
            Toast.makeText(this, "No hay ítems pendientes de pago.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Recalcular el Total Final (para asegurar precisión)
        double subtotal = 0.0;
        for (Producto p : itemsComprados) {
            subtotal += p.getPrecio();
        }
        double impuestos = subtotal * TASA_IMPUESTO;
        double totalFinal = subtotal + impuestos;

        // 2. Crear y Registrar la Orden en el Historial
        // Se usa new ArrayList<>(itemsComprados) para crear una copia inmutable de la lista de ítems.
        OrdenCompra nuevaOrden = new OrdenCompra(
                new ArrayList<>(itemsComprados),
                totalFinal
        );
        RepositorioHistorial.agregarOrden(nuevaOrden);
        //

        // 3. Limpiar el Carrito (¡CRUCIAL!)
        RepositorioCarrito.clearCart();

        // 4. Mostrar confirmación y redirigir
        Toast.makeText(this, "¡Orden confirmada! La compra ha sido registrada.", Toast.LENGTH_LONG).show();

        // 5. Redirigir al inicio (Login_Activity o tu actividad principal)
        Intent intent = new Intent(FacturaActivity.this, Login_Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}