package com.example.formularioapp;

// Importaciones de Android y soporte
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

// Importaciones de Firebase
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

// Importaciones de Google Maps y Ubicación
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


public class PublicarArticulo_Activity extends AppCompatActivity
        implements OnMapReadyCallback, OnMapClickListener {

    // Se elimina la variable TAG

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    // UI Components
    private ImageView imgFoto;
    private EditText txtNombreArticulo, txtPrecio, txtDescripcion, txtUbicacionManual;
    private Button btnPublicar, btnSeleccionarFoto;

    // Foto
    private Uri fotoUri;

    // Google Maps y Ubicación
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng ubicacionSeleccionada;
    private Marker ubicacionMarker;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private ActivityResultLauncher<Intent> photoPickerLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.publicar_vendedor);

        // Inicialización de Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference().child("imagenes_productos");

        // Inicialización de Vistas
        imgFoto = findViewById(R.id.imgFoto);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        txtNombreArticulo = findViewById(R.id.txtNombreArticulo);
        txtPrecio = findViewById(R.id.txtPrecio);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        btnPublicar = findViewById(R.id.btnPublicarArtículo);
        txtUbicacionManual = findViewById(R.id.txtUbicacionManual);

        // Inicialización de Google Maps y Ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtener el fragmento del mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragmentContainer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Configurar Lanzadores de Resultados
        configurarLanzadores();

        // Listeners
        btnSeleccionarFoto.setOnClickListener(v -> seleccionarFoto());
        btnPublicar.setOnClickListener(v -> publicarArticulo());
    }

    private void configurarLanzadores() {
        // Lanzador para permisos de ubicación
        locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION) != null &&
                            result.get(Manifest.permission.ACCESS_FINE_LOCATION);

                    if (fineLocationGranted) {
                        obtenerUbicacionActual();
                    } else {
                        Toast.makeText(this, "Permiso de ubicación denegado. Usando San José como defecto.", Toast.LENGTH_LONG).show();
                        centrarMapa(new LatLng(9.9347, -84.0875), "San José, Costa Rica");
                    }
                });

        // Lanzador para seleccionar imagen de la galería
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        fotoUri = result.getData().getData();
                        // Mostrar la foto en el ImageView
                        Glide.with(this).load(fotoUri).into(imgFoto);
                    }
                });
    }

    // ====================================================================
    // LÓGICA DE GOOGLE MAPS Y UBICACIÓN
    // ====================================================================

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Se usa 'this' ya que implementamos OnMapClickListener
        mMap.setOnMapClickListener(this);

        // Manejo de arrastre del pin
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            public void onMarkerDragStart(@NonNull Marker marker) {}
            public void onMarkerDrag(@NonNull Marker marker) {}
            public void onMarkerDragEnd(@NonNull Marker marker) {
                actualizarMarcadorYTexto(marker.getPosition());
            }
        });

        solicitarPermisosYObtenerUbicacion();
    }

    // Método REQUERIDO: Se llama cuando el usuario hace clic en el mapa
    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        actualizarMarcadorYTexto(latLng);
    }

    private void solicitarPermisosYObtenerUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void obtenerUbicacionActual() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            centrarMapa(currentLatLng, "Mi Ubicación Aproximada");
                        } else {
                            centrarMapa(new LatLng(9.9347, -84.0875), "San José, CR");
                        }
                    });
        } catch (SecurityException e) {
            // Se usa la cadena literal
            Log.e("PublicarArticulo", "Permiso de ubicación no otorgado", e);
        }
    }

    private void centrarMapa(LatLng latLng, String titulo) {
        if (mMap == null) return;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        actualizarMarcadorYTexto(latLng);
    }

    private void actualizarMarcadorYTexto(LatLng latLng) {
        if (mMap == null) return;

        ubicacionSeleccionada = latLng;

        if (ubicacionMarker != null) {
            ubicacionMarker.remove();
        }

        ubicacionMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Ubicación del Artículo")
                .draggable(true));

        obtenerDireccionDesdeLatLng(latLng);
    }

    private void obtenerDireccionDesdeLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latLng.latitude, latLng.longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();

                // Formar una dirección legible (ej: Ciudad, País o Sub-localidad)
                if (returnedAddress.getLocality() != null) {
                    strReturnedAddress.append(returnedAddress.getLocality()).append(", ");
                }
                if (returnedAddress.getAdminArea() != null) {
                    strReturnedAddress.append(returnedAddress.getAdminArea());
                }

                txtUbicacionManual.setText(strReturnedAddress.toString().trim());

            } else {
                txtUbicacionManual.setText("Ubicación Desconocida");
            }
        } catch (IOException e) {
            // Se usa la cadena literal
            Log.e("PublicarArticulo", "Geocoding error", e);
            txtUbicacionManual.setText("Error al obtener dirección.");
        }
    }


    // ====================================================================
    // LÓGICA DE PUBLICACIÓN Y FIREBASE
    // ====================================================================

    private void seleccionarFoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        photoPickerLauncher.launch(intent);
    }

    private boolean validarCampos() {
        if (TextUtils.isEmpty(txtNombreArticulo.getText())) {
            txtNombreArticulo.setError("Ingrese el nombre.");
            return false;
        }
        if (TextUtils.isEmpty(txtPrecio.getText())) {
            txtPrecio.setError("Ingrese el precio.");
            return false;
        }
        if (TextUtils.isEmpty(txtDescripcion.getText())) {
            txtDescripcion.setError("Ingrese la descripción.");
            return false;
        }
        if (fotoUri == null) {
            Toast.makeText(this, "Seleccione una foto para el artículo.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (ubicacionSeleccionada == null || TextUtils.isEmpty(txtUbicacionManual.getText())) {
            Toast.makeText(this, "Seleccione la ubicación en el mapa.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void publicarArticulo() {
        if (!validarCampos()) {
            return;
        }

        btnPublicar.setEnabled(false);
        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show();

        // Refuerzo: Doble validación de la URI para prevenir el error "Object does not exist"
        if (fotoUri == null || TextUtils.isEmpty(fotoUri.toString())) {
            Toast.makeText(this, "Error de foto: La URI seleccionada es inválida.", Toast.LENGTH_LONG).show();
            btnPublicar.setEnabled(true);
            return;
        }


        // 1. Subir la imagen a Firebase Storage
        StorageReference fotoRef = storageRef.child(UUID.randomUUID().toString());
        fotoRef.putFile(fotoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // 2. Obtener la URL de descarga
                    fotoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String fotoUrl = uri.toString();
                        // 3. Guardar los datos en Firestore
                        guardarArticuloEnFirestore(fotoUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    // Se usa la cadena literal
                    Log.e("PublicarArticulo", "Error al subir imagen (Storage): " + e.getMessage(), e);
                    String errorMsg = "Error al subir: " + (e.getMessage() != null ? e.getMessage() : "Fallo desconocido.");
                    Toast.makeText(PublicarArticulo_Activity.this, errorMsg, Toast.LENGTH_LONG).show();
                    btnPublicar.setEnabled(true);
                });
    }

    private void guardarArticuloEnFirestore(String fotoUrl) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Debe iniciar sesión para publicar.", Toast.LENGTH_LONG).show();
            btnPublicar.setEnabled(true);
            return;
        }

        String vendedorId = mAuth.getCurrentUser().getUid();

        Map<String, Object> producto = new HashMap<>();
        producto.put("nombre", txtNombreArticulo.getText().toString().trim());
        producto.put("descripcion", txtDescripcion.getText().toString().trim());
        producto.put("precio", Double.parseDouble(txtPrecio.getText().toString().trim()));
        producto.put("stock", 1L); // Stock inicial
        producto.put("fotoUrl", fotoUrl);
        producto.put("vendedorId", vendedorId);

        // DATOS DE UBICACIÓN
        producto.put("ubicacion", txtUbicacionManual.getText().toString().trim());
        producto.put("latitud", ubicacionSeleccionada.latitude);
        producto.put("longitud", ubicacionSeleccionada.longitude);

        db.collection("productos").add(producto)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PublicarArticulo_Activity.this, "Artículo publicado con éxito!", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra la actividad y vuelve al Feed
                })
                .addOnFailureListener(e -> {
                    // Se usa la cadena literal
                    Log.e("PublicarArticulo", "Error al guardar en Firestore", e);
                    Toast.makeText(PublicarArticulo_Activity.this, "Error al publicar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnPublicar.setEnabled(true);
                });
    }
}