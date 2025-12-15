package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.appbar.MaterialToolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialdeCompras_Activity extends AppCompatActivity {

    private TextView txtMensajeVacio;
    private LinearLayout containerHistorial;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


            // Configura el botón de retroceso
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void loadHistorialData() {
        // Cargar los datos del RepositorioHistorial
        List<OrdenCompra> listaDeOrdenes = RepositorioHistorial.getHistorial();

        boolean historialVacio = listaDeOrdenes.isEmpty();

        if (historialVacio) {
            // Caso 1: Historial Vacío
            if (txtMensajeVacio != null) {
                txtMensajeVacio.setText("Aún no se han generado compras de artículos.");
                txtMensajeVacio.setVisibility(View.VISIBLE);
            }
            if (containerHistorial != null) {
                containerHistorial.setVisibility(View.GONE);
            }
        } else {
            // Caso 2: Historial con Datos (Inyección dinámica)
            if (txtMensajeVacio != null) {
                txtMensajeVacio.setVisibility(View.GONE);
            }
            if (containerHistorial != null) {
                containerHistorial.setVisibility(View.VISIBLE);
                containerHistorial.removeAllViews(); // Limpiar el contenedor

                // Inyectar cada orden en el contenedor
                for (OrdenCompra orden : listaDeOrdenes) {
                    containerHistorial.addView(crearTarjetaDeOrden(orden));
                }
            }
        }
    }


    private View crearTarjetaDeOrden(OrdenCompra orden) {

        // 1. Inflar el layout de la tarjeta usando el LayoutInflater
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardLayout = inflater.inflate(R.layout.item_orden_historial, containerHistorial, false);

        // 2. Encontrar las vistas dentro del layout inflado
        TextView tvIdFecha = cardLayout.findViewById(R.id.tvHistorialIdFecha);
        TextView tvDetalleItems = cardLayout.findViewById(R.id.tvHistorialDetalleItems);
        TextView tvTotal = cardLayout.findViewById(R.id.tvHistorialTotal);

        // 3. Formatear la hora de la compra (Solo HH:mm)
        // 🟢 Se utiliza la fecha de la orden, pero se formatea solo para mostrar la hora
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String fechaYHoraCompraStr = sdf.format(new Date(orden.getFechaCompra()));

        // Título/Fecha de la Orden
        tvIdFecha.setText("Orden N° " + orden.getIdOrden().substring(0, 8) + " - FECHA: " + fechaYHoraCompraStr);

        // Detalle de Items (Muestra los primeros dos ítems y el conteo)
        int itemCount = orden.getItemsComprados().size();
        String detalleItems;

        if (itemCount == 0) {
            detalleItems = "Sin ítems registrados";
        } else if (itemCount == 1) {
            detalleItems = "Producto: " + orden.getItemsComprados().get(0).getNombre();
        } else {
            // Muestra los nombres de los primeros dos productos y el conteo restante
            detalleItems = String.format(Locale.getDefault(), "Items: %s, %s y %d más...",
                    orden.getItemsComprados().get(0).getNombre(),
                    orden.getItemsComprados().get(1).getNombre(),
                    itemCount - 2);
        }
        tvDetalleItems.setText(detalleItems);

        // Total Final
        tvTotal.setText(String.format(Locale.getDefault(), "TOTAL PAGADO: ₡ %,.2f", orden.getTotalFinal()));

        return cardLayout;
    }
}