package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Cuenta_acceso extends AppCompatActivity {

    private static final String TAG = "Cuenta_acceso";

    private Button btnHistorialCompras;
    private Button btnModificarDatos;
    private Button btnMetodosPago;
    private Button btnGestionProductos;
    private Button btnCerrarSesion;

    private EditText txtPerfilNombre;
    private EditText txtPerfilCorreo;
    private EditText txtPerfilContacto;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser; // Referencia al usuario actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mi_cuenta_activity); // Asegúrate de que este sea el nombre correcto

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser(); // Obtener el usuario actual

        vincularVistas();
        cargarDatosPerfilDesdeFirestore();
        configurarListeners();
    }

    private void vincularVistas() {
        btnHistorialCompras = findViewById(R.id.btnHistorialCompras);
        btnModificarDatos = findViewById(R.id.btnModificarDatos);
        btnMetodosPago = findViewById(R.id.btnMetodosPago);
        btnGestionProductos = findViewById(R.id.btnGestionProductos);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        txtPerfilNombre = findViewById(R.id.txtPerfilNombre);
        txtPerfilCorreo = findViewById(R.id.txtPerfilCorreo);
        txtPerfilContacto = findViewById(R.id.txtPerfilContacto);
    }

    private void cargarDatosPerfilDesdeFirestore() {
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Mostrar correo:
            if (txtPerfilCorreo != null) {
                txtPerfilCorreo.setText(currentUser.getEmail());
            }

            db.collection("usuarios").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombre = documentSnapshot.getString("nombre");
                            String contacto = documentSnapshot.getString("telefono");

                            if (txtPerfilNombre != null && nombre != null) {
                                txtPerfilNombre.setText(nombre);
                            }
                            if (txtPerfilContacto != null && contacto != null) {
                                txtPerfilContacto.setText(contacto);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al leer datos de Firestore: ", e);
                    });
        }
    }


    private void configurarListeners() {

        // --- 1. HISTORIAL DE COMPRAS ---
        if (btnHistorialCompras != null) {
            btnHistorialCompras.setOnClickListener(v -> {
                Intent intent = new Intent(Cuenta_acceso.this, HistorialdeCompras_Activity.class);
                startActivity(intent);
            });
        }

        // --- 2. MODIFICAR MIS DATOS (NUEVA LÓGICA) ---
        if (btnModificarDatos != null) {
            btnModificarDatos.setOnClickListener(v -> {
                actualizarDatosUsuario();
            });
        }

        // --- 3. CERRAR SESIÓN ---
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> {
                cerrarSesion();
            });
        }

        // --- Otros botones ---
        if (btnMetodosPago != null) {
            btnMetodosPago.setOnClickListener(v -> Toast.makeText(this, "Navegando a Gestión de Pagos", Toast.LENGTH_SHORT).show());
        }
        if (btnGestionProductos != null) {
            btnGestionProductos.setOnClickListener(v -> Toast.makeText(this, "Navegando a Gestión de Productos", Toast.LENGTH_SHORT).show());
        }
    }

    // =========================================================================
    // NUEVA LÓGICA DE ACTUALIZACIÓN DE DATOS
    // =========================================================================

    private void actualizarDatosUsuario() {
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            cerrarSesion();
            return;
        }

        String nuevoNombre = txtPerfilNombre.getText().toString().trim();
        String nuevoCorreo = txtPerfilCorreo.getText().toString().trim();
        String nuevoContacto = txtPerfilContacto.getText().toString().trim();

        // 1. Validación simple
        if (nuevoNombre.isEmpty() || nuevoContacto.isEmpty()) {
            Toast.makeText(this, "El nombre y el número de contacto no pueden estar vacíos.", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Actualizar el Correo en Firebase Authentication (si ha cambiado)
        if (!nuevoCorreo.equals(currentUser.getEmail())) {
            actualizarCorreoEnAuth(nuevoCorreo);
        } else {
            // Si el correo no cambió, solo actualizamos Firestore
            guardarCambiosEnFirestore(nuevoNombre, nuevoContacto);
        }
    }

    private void actualizarCorreoEnAuth(String nuevoCorreo) {
        if (currentUser != null) {
            currentUser.updateEmail(nuevoCorreo)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Correo de Firebase Auth actualizado.");
                            // Si el correo se actualizó, ahora actualizamos Firestore
                            guardarCambiosEnFirestore(txtPerfilNombre.getText().toString().trim(),
                                    txtPerfilContacto.getText().toString().trim());
                        } else {
                            Log.e(TAG, "Error al actualizar correo en Auth: ", task.getException());
                            Toast.makeText(Cuenta_acceso.this, "Error al actualizar correo. Debes reautenticarte.", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }


    private void guardarCambiosEnFirestore(String nuevoNombre, String nuevoContacto) {
        if (currentUser == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nuevoNombre);
        updates.put("telefono", nuevoContacto);

        db.collection("usuarios").document(currentUser.getUid()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Cuenta_acceso.this, "Datos de perfil actualizados con éxito.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Datos de Firestore actualizados.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar datos en Firestore: ", e);
                    Toast.makeText(Cuenta_acceso.this, "Error al guardar cambios.", Toast.LENGTH_SHORT).show();
                });
    }

    // =========================================================================
    // LÓGICA DE CIERRE DE SESIÓN
    // =========================================================================

    private void cerrarSesion() {
        mAuth.signOut();

        SharedPreferences sharedPref = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("estaLogeado", false);
        editor.apply();

        Toast.makeText(this, "Sesión cerrada con éxito.", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, Login_Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}