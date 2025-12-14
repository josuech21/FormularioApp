package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class MiCuentaActivity extends AppCompatActivity {

    private static final String TAG = "MiCuentaActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private MaterialButton btnCerrarSesion;
    private TextInputEditText txtPerfilNombre;
    private TextInputEditText txtPerfilCorreo;
    private TextInputEditText txtPerfilContacto;
    private MaterialButton btnHistorialCompras;
    private MaterialButton btnModificarDatos; // El botón interruptor
    private MaterialButton btnMetodosPago;
    private MaterialButton btnGestionProductos;

    private boolean modoEdicionActivo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mi_cuenta_activity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        vincularVistas();
        establecerEstadoEdicion(false);
        cargarDatosPerfilDesdeFirestore();
        configurarListenersAdicionales();
    }

    private void vincularVistas() {
        txtPerfilNombre = findViewById(R.id.etNombre);
        txtPerfilCorreo = findViewById(R.id.etCorreo);
        txtPerfilContacto = findViewById(R.id.etTelefono);

        btnHistorialCompras = findViewById(R.id.btnHistorial);
        btnModificarDatos = findViewById(R.id.btnModificar);
        btnMetodosPago = findViewById(R.id.btnMetodosPago);
        btnGestionProductos = findViewById(R.id.btnProductos);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
    }

    private void establecerEstadoEdicion(boolean habilitar) {
        modoEdicionActivo = habilitar;

        txtPerfilNombre.setEnabled(habilitar);
        txtPerfilContacto.setEnabled(habilitar);
        txtPerfilCorreo.setEnabled(habilitar);

        if (habilitar) {
            btnModificarDatos.setText("GUARDAR CAMBIOS");
            Toast.makeText(this, "Modo edición activado. Realice los cambios.", Toast.LENGTH_SHORT).show();
        } else {
            btnModificarDatos.setText("MODIFICAR MIS DATOS (CONTRASEÑA, ETC.)");
        }
    }



    private void cargarDatosPerfilDesdeFirestore() {
        if (currentUser != null) {
            String userId = currentUser.getUid();

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
                        } else {
                            Log.w(TAG, "Documento de usuario no encontrado en Firestore.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al leer datos de Firestore: ", e);
                        Toast.makeText(this, "Error al cargar el perfil.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            cerrarSesion();
        }
    }

    private void configurarListenersAdicionales() {

        // 1. Historial de Compras
        if (btnHistorialCompras != null) {
            btnHistorialCompras.setOnClickListener(v -> {
                Intent intent = new Intent(MiCuentaActivity.this, HistorialdeCompras_Activity.class);
                startActivity(intent);
            });
        }

        // 2. LÓGICA DEL BOTÓN INTERRUPTOR (MODIFICAR/GUARDAR)
        if (btnModificarDatos != null) {
            btnModificarDatos.setOnClickListener(v -> {
                if (modoEdicionActivo) {
                    actualizarDatosUsuario();
                } else {
                    establecerEstadoEdicion(true);
                }
            });
        }

        // 3. GESTIONAR MÉTODOS DE PAGO (NAVEGACIÓN)
        if (btnMetodosPago != null) {
            btnMetodosPago.setOnClickListener(v -> {
                // Navegación a la Activity dedicada para métodos de pago
                Intent intent = new Intent(MiCuentaActivity.this, Metododepago_Activity.class);
                startActivity(intent);
            });
        }

        // 4. GESTIÓN DE PRODUCTOS (Placeholder para el siguiente paso)
        if (btnGestionProductos != null) {
            btnGestionProductos.setOnClickListener(v -> {
                Toast.makeText(this, "Navegando a Gestión de Productos (Pendiente)", Toast.LENGTH_SHORT).show();

            });
        }

        // 5. Cerrar Sesión
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> {
                cerrarSesion();
            });
        }
    }



    private void actualizarDatosUsuario() {
        if (currentUser == null) {
            cerrarSesion();
            return;
        }

        String nuevoNombre = txtPerfilNombre.getText() != null ? txtPerfilNombre.getText().toString().trim() : "";
        String nuevoCorreo = txtPerfilCorreo.getText() != null ? txtPerfilCorreo.getText().toString().trim() : "";
        String nuevoContacto = txtPerfilContacto.getText() != null ? txtPerfilContacto.getText().toString().trim() : "";

        if (nuevoNombre.isEmpty() || nuevoContacto.isEmpty() || nuevoCorreo.isEmpty()) {
            Toast.makeText(this, "Todos los campos deben estar llenos.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!nuevoCorreo.equals(currentUser.getEmail())) {
            actualizarCorreoEnAuth(nuevoCorreo, nuevoNombre, nuevoContacto);
        } else {
            guardarCambiosEnFirestore(nuevoNombre, nuevoContacto);
        }
    }

    private void actualizarCorreoEnAuth(String nuevoCorreo, String nombre, String contacto) {
        if (currentUser != null) {
            currentUser.updateEmail(nuevoCorreo)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Correo de Firebase Auth actualizado.");
                            guardarCambiosEnFirestore(nombre, contacto);
                        } else {
                            Log.e(TAG, "Error al actualizar correo en Auth: ", task.getException());
                            Toast.makeText(MiCuentaActivity.this, "Error al actualizar correo. Debes reautenticarte.", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MiCuentaActivity.this, "Datos de perfil actualizados con éxito.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Datos de Firestore actualizados.");

                    establecerEstadoEdicion(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar datos en Firestore: ", e);
                    Toast.makeText(MiCuentaActivity.this, "Error al guardar cambios. Intente de nuevo.", Toast.LENGTH_SHORT).show();
                });
    }


    private void cerrarSesion() {
        mAuth.signOut();

        SharedPreferences sharedPref = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("estaLogeado", false);
        editor.apply();

        Toast.makeText(this, "Sesión cerrada. Vuelva pronto.", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(MiCuentaActivity.this, Login_Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}