package com.example.formularioapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.TextView;
import android.util.Log;
import android.graphics.Color;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PublicarArticulo_Activity extends AppCompatActivity {

    private static final String TAG = "PublicarArticulo_Act";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageReference storageRef;

    // --- UI Components ---
    private Button btnModificar;
    private Button btnEliminar;
    private Button btnPublicar;
    private Button btnSeleccionarFoto;
    private ImageView imgProductoDetalle;
    private Switch switchDisponible;

    private EditText txtNombreProducto;
    private EditText txtPrecioProducto; // Ahora es EditText
    private EditText txtDetalleProducto;
    private TextView txtNombreVendedor; // Se mantiene como TextView (solo lectura)

    private String productoId = null;
    private boolean isModoEdicion = false;
    private Uri imagenUriSeleccionada;
    private String fotoUrlActual = "";
    private String nombreVendedorActual = "";

    // URL de imagen por defecto
    private static final String DEFAULT_IMAGE_URL = "https://via.placeholder.com/300?text=Sin+Imagen";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.publicar_vendedor);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("imagenes_productos");

        if (currentUser == null) {
            Toast.makeText(this, "Debe iniciar sesión para gestionar productos.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        verificarRolVendedor();
    }

    private void verificarRolVendedor() {
        db.collection("usuarios").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Boolean esVendedor = documentSnapshot.getBoolean("esVendedor");
                    if (esVendedor != null && esVendedor) {
                        String nombre = documentSnapshot.getString("nombre");
                        if (nombre != null) {
                            this.nombreVendedorActual = nombre;
                        }
                        inicializarActivity();
                    } else {
                        Toast.makeText(this, "Acceso denegado: No tienes permisos de vendedor.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al verificar el rol del usuario.", e);
                    Toast.makeText(this, "Error al conectar con la base de datos.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void inicializarActivity() {
        vincularVistas();

        // 1. Establecer el nombre del vendedor (solo lectura)
        if (!nombreVendedorActual.isEmpty()) {
            txtNombreVendedor.setText(this.nombreVendedorActual);
        }

        productoId = getIntent().getStringExtra("PRODUCTO_ID");

        if (productoId != null) {
            cargarDatosArticulo(productoId);
            configurarModoVisualizacion();
        } else {
            configurarModoPublicacionInicial();
        }

        // --- Listeners: Configuración aquí ---
        btnModificar.setOnClickListener(v -> alternarModoEdicion());
        btnEliminar.setOnClickListener(v -> confirmarYEliminarArticulo());
        btnSeleccionarFoto.setOnClickListener(v -> verificarPermisosYSeleccionarImagen());

        // Lógica de Publicación/Edición SIN subida a Storage
        btnPublicar.setOnClickListener(v -> {
            btnPublicar.setEnabled(false);
            if (productoId == null) {
                // Modo Publicación Nueva
                guardarNuevoProducto(DEFAULT_IMAGE_URL);
            } else if (isModoEdicion) {
                // Modo Edición
                actualizarProductoExistente(fotoUrlActual.isEmpty() ? DEFAULT_IMAGE_URL : fotoUrlActual);
            }
        });

        switchDisponible.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (productoId != null) {
                actualizarDisponibilidad(productoId, isChecked);
            }
        });
    }

    private void vincularVistas() {
        btnModificar = findViewById(R.id.btnModificar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnPublicar = findViewById(R.id.btnAgregarCarrito); // Usa el mismo ID del botón grande
        switchDisponible = findViewById(R.id.switchDisponible);

        // Editables
        txtNombreProducto = findViewById(R.id.txtNombreProducto);
        txtPrecioProducto = findViewById(R.id.txtPrecioProducto); // <<<< EDITTEXT
        txtDetalleProducto = findViewById(R.id.txtDetalleProducto);

        // Solo lectura
        txtNombreVendedor = findViewById(R.id.txtNombreVendedor);

        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        imgProductoDetalle = findViewById(R.id.imgProductoDetalle);

        // Configuración de InputTypes
        configurarCampoEditable(txtNombreProducto, EditorInfo.IME_ACTION_NEXT, InputType.TYPE_CLASS_TEXT);
        configurarCampoEditable(txtPrecioProducto, EditorInfo.IME_ACTION_NEXT, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        configurarCampoEditable(txtDetalleProducto, EditorInfo.IME_ACTION_DONE, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
    }

    private void configurarCampoEditable(TextView tv, int imeAction, int inputType) {
        tv.setFocusable(true);
        tv.setFocusableInTouchMode(true);
        tv.setImeOptions(imeAction);
        tv.setInputType(inputType);
    }

    private void configurarModoVisualizacion() {
        isModoEdicion = false;
        btnModificar.setVisibility(View.VISIBLE);
        btnEliminar.setVisibility(View.VISIBLE);
        switchDisponible.setVisibility(View.VISIBLE);

        btnPublicar.setVisibility(View.GONE);
        btnSeleccionarFoto.setVisibility(View.GONE);
        aplicarEstilosEdicion(false);
    }

    private void configurarModoPublicacionInicial() {
        isModoEdicion = true;
        btnModificar.setVisibility(View.GONE);
        btnEliminar.setVisibility(View.GONE);
        switchDisponible.setVisibility(View.GONE);

        btnPublicar.setText("PUBLICAR ARTÍCULO");
        btnPublicar.setVisibility(View.VISIBLE);
        btnSeleccionarFoto.setVisibility(View.VISIBLE);
        aplicarEstilosEdicion(true);

        txtNombreProducto.setText("");
        txtPrecioProducto.setText("");
        txtDetalleProducto.setText("");
    }

    private void alternarModoEdicion() {
        isModoEdicion = !isModoEdicion;

        if (isModoEdicion) {
            btnModificar.setText("CANCELAR");
            btnPublicar.setText("GUARDAR CAMBIOS");
            btnPublicar.setVisibility(View.VISIBLE);
            btnSeleccionarFoto.setVisibility(View.VISIBLE);
            aplicarEstilosEdicion(true);
        } else {
            btnModificar.setText("MODIFICAR");
            btnPublicar.setVisibility(View.GONE);
            btnSeleccionarFoto.setVisibility(View.GONE);
            aplicarEstilosEdicion(false);
            cargarDatosArticulo(productoId);
            imagenUriSeleccionada = null;
        }
    }

    private void aplicarEstilosEdicion(boolean editable) {
        int colorFondo = editable ? Color.parseColor("#FFFBE5") : Color.TRANSPARENT;

        TextView[] camposEditables = {txtNombreProducto, txtPrecioProducto, txtDetalleProducto};
        for (TextView tv : camposEditables) {
            tv.setBackgroundColor(colorFondo);
            tv.setFocusable(editable);
            tv.setFocusableInTouchMode(editable);
        }
    }

    // --- LÓGICA DE FOTO ---

    private void verificarPermisosYSeleccionarImagen() {
        String permisoRequerido = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES :
                Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permisoRequerido) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permisoRequerido}, PERMISSION_REQUEST_CODE);
        } else {
            seleccionarImagen();
        }
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imagenUriSeleccionada = data.getData();
            imgProductoDetalle.setImageURI(imagenUriSeleccionada);
            Toast.makeText(this, "Imagen seleccionada.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- LÓGICA DE PERSISTENCIA FIREBASE ---

    private String getText(TextView textView) {
        return textView.getText() != null ? textView.getText().toString().trim() : "";
    }

    private double parsePrecio(String precioStr) {
        try {
            // Limpia el string de caracteres no numéricos excepto el punto
            String cleanedStr = precioStr.replaceAll("[^0-9.]", "");
            if (cleanedStr.isEmpty()) return 0.0;
            return Double.parseDouble(cleanedStr);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error al parsear precio: " + precioStr, e);
            return 0.0;
        }
    }

    private Map<String, Object> validarYObtenerDatosProducto(boolean isNewProduct) {
        String nombre = getText(txtNombreProducto);
        double precio = parsePrecio(getText(txtPrecioProducto));
        String detalle = getText(txtDetalleProducto);

        if (nombre.isEmpty() || precio <= 0) {
            btnPublicar.setEnabled(true);
            Toast.makeText(this, "Complete nombre y precio válidos.", Toast.LENGTH_SHORT).show();
            return null;
        }

        Map<String, Object> productoData = new HashMap<>();
        productoData.put("nombre", nombre);
        productoData.put("detalle", detalle);
        productoData.put("precio", precio);

        if (isNewProduct) {
            productoData.put("stock", 1L); // Usar Long para consistencia
            productoData.put("vendedorId", currentUser.getUid());
        }

        return productoData;
    }

    private void guardarNuevoProducto(String fotoUrl) {
        Map<String, Object> productoData = validarYObtenerDatosProducto(true);
        if (productoData == null) return;

        productoData.put("fotoUrl", fotoUrl);

        db.collection("productos")
                .add(productoData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Artículo publicado con éxito en Firestore.", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnPublicar.setEnabled(true);
                    Log.e(TAG, "Error al guardar en Firestore", e);
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void actualizarProductoExistente(String nuevaFotoUrl) {
        if (productoId == null) return;

        Map<String, Object> updates = validarYObtenerDatosProducto(false);
        if (updates == null) return;

        updates.put("fotoUrl", nuevaFotoUrl);

        db.collection("productos").document(productoId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Artículo actualizado con éxito en Firestore.", Toast.LENGTH_SHORT).show();
                    configurarModoVisualizacion();
                    btnPublicar.setEnabled(true);
                    imagenUriSeleccionada = null;
                    setResult(RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    btnPublicar.setEnabled(true);
                    Log.e(TAG, "Error al actualizar", e);
                });
    }

    private void cargarDatosArticulo(String id) {
        db.collection("productos").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String nombre = documentSnapshot.getString("nombre");
                        Double precioDouble = documentSnapshot.getDouble("precio");
                        String detalle = documentSnapshot.getString("detalle");
                        Long stockLong = documentSnapshot.getLong("stock");

                        if (nombre != null) txtNombreProducto.setText(nombre);
                        if (detalle != null) txtDetalleProducto.setText(detalle);

                        double precio = (precioDouble != null) ? precioDouble : 0.0;
                        txtPrecioProducto.setText(String.format(Locale.getDefault(), "%.2f", precio));

                        long stock = (stockLong != null) ? stockLong : 0;
                        switchDisponible.setChecked(stock > 0);

                        fotoUrlActual = documentSnapshot.getString("fotoUrl");
                        String urlToLoad = (fotoUrlActual != null && !fotoUrlActual.isEmpty()) ? fotoUrlActual : DEFAULT_IMAGE_URL;
                        Glide.with(this).load(urlToLoad).into(imgProductoDetalle);

                    } else {
                        Toast.makeText(this, "Artículo no encontrado.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando artículo", e);
                    Toast.makeText(this, "Error al cargar datos del artículo.", Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarDisponibilidad(String id, boolean estaDisponible) {
        long stock = estaDisponible ? 1L : 0L;
        Map<String, Object> updates = new HashMap<>();
        updates.put("stock", stock);

        db.collection("productos").document(id)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    String estado = estaDisponible ? "DISPONIBLE" : "VENDIDO";
                    Toast.makeText(this, "Estado actualizado a: " + estado, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al actualizar disponibilidad", e));
    }

    private void confirmarYEliminarArticulo() {
        if (productoId == null) return;

        db.collection("productos").document(productoId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Artículo eliminado con éxito.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar", e));
    }
}