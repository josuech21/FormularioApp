package com.example.formularioapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// --- INICIO: IMPORTACIONES DE GLIDE CORREGIDAS ---
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;


import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeedActivity extends AppCompatActivity {

    private static final String TAG = "FeedActivity";

    // Componentes de la barra de navegación superior (mantengo como variables de clase)
    private TextView txtMiCuenta;
    private TextView txtMiCarrito;
    private TextView txtVender;
    private TextView txtFeed;

    // Componentes para el Feed Dinámico
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter<Producto, ProductoViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_app);

        db = FirebaseFirestore.getInstance();

        // --- 1. Referencias de Vistas de Navegación ---
        txtMiCuenta = findViewById(R.id.txtMiCuenta);
        txtMiCarrito = findViewById(R.id.txtMiCarrito);
        txtVender = findViewById(R.id.txtVender);
        txtFeed = findViewById(R.id.txtFeed);

        // Referencia del RecyclerView (ID del layout feed_app.xml)
        recyclerView = findViewById(R.id.recyclerViewArticulos);

        // Navegación (Se mantiene la lógica existente)
        txtMiCuenta.setOnClickListener(v -> startActivity(new Intent(FeedActivity.this, MiCuentaActivity.class)));
        txtMiCarrito.setOnClickListener(v -> startActivity(new Intent(FeedActivity.this, Micarrito_Activity.class)));
        txtVender.setOnClickListener(v -> abrirPublicarArticuloActivity());

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        // 1. Crear la Query: Solo productos disponibles (stock > 0L)
        Query query = db.collection("productos")
                .whereGreaterThan("stock", 0L)
                .orderBy("nombre", Query.Direction.ASCENDING);

        // 2. Configurar las opciones del adaptador
        FirestoreRecyclerOptions<Producto> options = new FirestoreRecyclerOptions.Builder<Producto>()
                .setQuery(query, Producto.class)
                .build();

        // 3. Crear el adaptador
        adapter = new FirestoreRecyclerAdapter<Producto, ProductoViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ProductoViewHolder holder, int position, @NonNull Producto model) {

                holder.txtNombre.setText(model.getNombre());
                holder.txtPrecio.setText(String.format(Locale.getDefault(), "₡%.2f", model.getPrecio()));

                // --- OPTIMIZACIÓN GLIDE: Llamada al método corregido del ViewHolder ---
                holder.cargarImagen(model.getFotoUrl());
                // -------------------------------------------------------------------

                // --- Lógica de Fidelidad Visual (Disponibilidad) ---
                boolean disponible = model.getStock() > 0;

                holder.txtDisponibilidad.setVisibility(disponible ? View.VISIBLE : View.GONE);
                holder.txtNoDisponible.setVisibility(disponible ? View.GONE : View.VISIBLE);
                holder.btnAgregar.setEnabled(disponible);
                holder.btnComprar.setEnabled(disponible);

                // La fecha se mantiene simulada, o se usa un campo 'timestamp' de Firestore
                holder.txtFecha.setText("Publicado el " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

                // Listeners para los botones
                holder.btnAgregar.setOnClickListener(v -> agregarArticuloAlCarrito(model));
                holder.btnComprar.setOnClickListener(v -> comprarArticulo(model));

                // Listener para ver detalles del producto (clic en la CardView completa)
                holder.itemView.setOnClickListener(v -> {
                    Toast.makeText(FeedActivity.this, "Ver detalles de: " + model.getNombre(), Toast.LENGTH_SHORT).show();
                });
            }

            @NonNull
            @Override
            public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_producto, parent, false);
                return new ProductoViewHolder(view);
            }
        };

        // 4. Configurar RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    private void abrirPublicarArticuloActivity() {
        startActivity(new Intent(FeedActivity.this, PublicarArticulo_Activity.class));
    }

    private void agregarArticuloAlCarrito(Producto producto) {
        Toast.makeText(this, "Añadido: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
    }

    private void comprarArticulo(Producto producto) {
        Toast.makeText(this, "Comprando y navegando al carrito.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(FeedActivity.this, Micarrito_Activity.class));
    }

    // --- CLASE INTERNA: VIEWHOLDER CON GLIDE CORREGIDO ---
    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtPrecio, txtDisponibilidad, txtNoDisponible, txtFecha;
        ImageView imagen;
        Button btnAgregar, btnComprar;

        public ProductoViewHolder(View itemView) {
            super(itemView);
            // Vínculos a los IDs en item_producto.xml
            imagen = itemView.findViewById(R.id.imgItemProducto);
            txtNombre = itemView.findViewById(R.id.txtItemNombre);
            txtPrecio = itemView.findViewById(R.id.txtItemPrecio);
            txtDisponibilidad = itemView.findViewById(R.id.txtItemDisponibilidad);
            txtNoDisponible = itemView.findViewById(R.id.txtItemNoDisponible);
            txtFecha = itemView.findViewById(R.id.txtItemFecha);
            btnAgregar = itemView.findViewById(R.id.btnItemAgregar);
            btnComprar = itemView.findViewById(R.id.btnItemComprar);
        }

        public void cargarImagen(String fotoUrl) {

            // Verificación para evitar crash si la URL es nula o vacía
            if (fotoUrl == null || fotoUrl.isEmpty()) {
                // Si no hay URL, mostramos la imagen de recurso local (chaquetacuero)
                // como un valor predeterminado si el placeholder genérico no es deseado.
                imagen.setImageResource(R.drawable.chaquetacuero);
                return;
            }

            Glide.with(itemView.getContext())
                    .load(fotoUrl) // ESTO CARGA LA FOTO SELECCIONADA POR EL VENDEDOR (via Firestore URL)
                    // OPTIMIZACIÓN: Usa DiskCacheStrategy.ALL para caché en disco y evitar re-descargas
                    .diskCacheStrategy(DiskCacheStrategy.ALL)


                    // Placeholder: Imagen temporal mientras carga. Usamos la imagen local si quieres consistencia.
                    .placeholder(R.drawable.google)

                    // Error: Imagen si la URL de Firebase falla.
                    .error(android.R.drawable.btn_dialog)
                    .into(imagen);
        }
    }
}