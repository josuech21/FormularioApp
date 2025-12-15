package com.example.formularioapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
// 🛑 IMPORTACIÓN DE TU CLASE MODELO (Ajusta la ruta si es necesario)
import com.example.formularioapp.Producto;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Locale;

public class FeedActivity extends AppCompatActivity {

    private static final String TAG = "FeedActivity";

    // ... (Variables de navegación y componentes)
    private TextView txtMiCuenta;
    private TextView txtMiCarrito;
    private TextView txtVender;
    private TextView txtFeed;
    private Button btnRefrescarFeed;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter<Producto, ProductoViewHolder> adapter;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.feed_app);

        db = FirebaseFirestore.getInstance();

        // 1. Referencias de Vistas de Navegación
        txtMiCuenta = findViewById(R.id.txtMiCuenta);
        txtMiCarrito = findViewById(R.id.txtMiCarrito);
        txtVender = findViewById(R.id.txtVender);
        txtFeed = findViewById(R.id.txtFeed);

        // Referencia del RecyclerView
        recyclerView = findViewById(R.id.recyclerViewArticulos);

        // 2. VINCULAR Y ASIGNAR LISTENER AL BOTÓN
        btnRefrescarFeed = findViewById(R.id.btnRefrescarFeed);

        if (btnRefrescarFeed != null) {
            btnRefrescarFeed.setOnClickListener(v -> refrescarFeed());
        }

        // Lógica de Navegación
        txtMiCuenta.setOnClickListener(v -> startActivity(new Intent(FeedActivity.this, MiCuentaActivity.class)));
        txtMiCarrito.setOnClickListener(v -> startActivity(new Intent(FeedActivity.this, Micarrito_Activity.class)));
        txtVender.setOnClickListener(v -> abrirPublicarArticuloActivity());

        setupRecyclerView();
    }

    public void refrescarFeed() {
        if (adapter != null) {
            adapter.stopListening();
            adapter.startListening();
            Toast.makeText(this, "Feed refrescado.", Toast.LENGTH_SHORT).show();
        }
    }


    private void setupRecyclerView() {
        // 1. Crear la Query
        Query query = db.collection("productos")
                .whereGreaterThan("stock", 0L)
                .orderBy("nombre", Query.Direction.ASCENDING);

        // 2. Configurar las opciones
        FirestoreRecyclerOptions<Producto> options = new FirestoreRecyclerOptions.Builder<Producto>()
                .setQuery(query, Producto.class)
                .build();

        // 3. Crear el adaptador
        adapter = new FirestoreRecyclerAdapter<Producto, ProductoViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ProductoViewHolder holder, int position, @NonNull Producto model) {

                String productoId = getSnapshots().getSnapshot(position).getId();
                model.setProductoId(productoId);

                holder.txtNombre.setText(model.getNombre());
                holder.txtPrecio.setText(String.format(Locale.getDefault(), "₡ %,.2f", model.getPrecio()));

                // 🟢 CARGAR IMAGEN CON MANEJO DE ERROR
                holder.cargarImagen(model.getFotoUrl());

                // Lógica de Disponibilidad
                boolean disponible = model.getStock() > 0;
                // Si la clase Producto tiene el campo 'disponible', es mejor usarlo: model.isDisponible();
                holder.txtDisponibilidad.setVisibility(disponible ? View.VISIBLE : View.GONE);
                holder.txtNoDisponible.setVisibility(disponible ? View.GONE : View.VISIBLE);

                holder.btnAgregar.setEnabled(disponible);
                holder.btnComprar.setEnabled(disponible);

                holder.txtFecha.setText("Publicado recientemente");

                holder.btnAgregar.setOnClickListener(v -> agregarArticuloAlCarrito(model));
                holder.btnComprar.setOnClickListener(v -> comprarArticulo(model));

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
        // Asegúrate de que RepositorioCarrito maneja bien la clase Producto actualizada
        // RepositorioCarrito.addItem(producto);
        Toast.makeText(this, "Añadido al carrito: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
    }

    private void comprarArticulo(Producto producto) {
        // RepositorioCarrito.addItem(producto);
        Toast.makeText(this, "Comprando y navegando al carrito.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(FeedActivity.this, Micarrito_Activity.class));
    }

    // --- CLASE INTERNA: VIEWHOLDER ---
    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtPrecio, txtDisponibilidad, txtNoDisponible, txtFecha;
        ImageView imagen;
        Button btnAgregar, btnComprar;

        public ProductoViewHolder(View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imgItemProducto);
            txtNombre = itemView.findViewById(R.id.txtItemNombre);
            txtPrecio = itemView.findViewById(R.id.txtItemPrecio);
            txtDisponibilidad = itemView.findViewById(R.id.txtItemDisponibilidad);
            txtNoDisponible = itemView.findViewById(R.id.txtItemNoDisponible);
            txtFecha = itemView.findViewById(R.id.txtItemFecha);
            btnAgregar = itemView.findViewById(R.id.btnItemAgregar);
            btnComprar = itemView.findViewById(R.id.btnItemComprar);
            // Si has añadido txtUbicacion, asegúrate de añadirlo aquí:
            // txtUbicacion = itemView.findViewById(R.id.txtItemUbicacion);
        }

        // 🟢 MÉTODO CARGAR IMAGEN CON MANEJO DE ERRORES ROBUSTO
        public void cargarImagen(String fotoUrl) {
            // 1. Verificación inicial de la URL
            if (fotoUrl == null || fotoUrl.trim().isEmpty()) {
                imagen.setImageResource(android.R.drawable.ic_menu_gallery); // Imagen por defecto si no hay URL
                return;
            }

            Glide.with(itemView.getContext())
                    .load(fotoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(android.R.drawable.progress_horizontal)
                    .error(android.R.drawable.ic_delete) // Imagen si el fetch falla
                    // 2. Agregar un Listener para manejo explícito de fallos
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(
                                @org.jetbrains.annotations.Nullable GlideException e,
                                Object model,
                                Target<Drawable> target,
                                boolean isFirstResource) {

                            // Registra el error pero permite que el .error() de Glide se ejecute
                            Log.e(TAG, "Error al cargar imagen de URL: " + fotoUrl, e);
                            return false; // Retornar false permite que Glide continúe al error()
                        }

                        @Override
                        public boolean onResourceReady(
                                Drawable resource,
                                Object model,
                                Target<Drawable> target,
                                DataSource dataSource,
                                boolean isFirstResource) {
                            return false; // Permite que Glide pinte la imagen
                        }
                    })
                    .into(imagen);
        }
    }
}