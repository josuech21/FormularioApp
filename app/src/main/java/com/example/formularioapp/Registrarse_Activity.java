package com.example.formularioapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// Importaciones de Firebase
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore; // ¡NUEVA IMPORTACIÓN!

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Registrarse_Activity extends AppCompatActivity {

    private static final String TAG = "RegistroActivity";

    // Componentes de UI
    private EditText txtNombreRegistro, txtEmailRegistro, txtPasswordRegistro, txtTelefonoRegistro;
    private Button btnRegistrar;
    private TextView txtIrALogin;

    // Instancias de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore; // Instancia de Firestore para guardar datos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrarse_usuario_nuevo);

        // 1. Inicializar Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance(); // Inicializar Firestore

        // 2. Inicializar Vistas
        txtNombreRegistro = findViewById(R.id.txtNombreRegistro);
        txtEmailRegistro = findViewById(R.id.txtEmailRegistro);
        txtPasswordRegistro = findViewById(R.id.txtPasswordRegistro);
        txtTelefonoRegistro = findViewById(R.id.txtTelefonoRegistro);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        txtIrALogin = findViewById(R.id.txtIrALogin);

        // 3. Configurar Listener del botón
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });

        // 4. Lógica del Enlace IR A LOGIN
        txtIrALogin.setOnClickListener(v -> {
            Intent intent = new Intent(Registrarse_Activity.this, Login_Activity.class);
            startActivity(intent);
            finish();
        });
    }


    private boolean validarCampos() {
        String nombre = txtNombreRegistro.getText().toString().trim();
        String email = txtEmailRegistro.getText().toString().trim();
        String password = txtPasswordRegistro.getText().toString();
        String telefono = txtTelefonoRegistro.getText().toString().trim();

        // 1. Validación de campos vacíos
        if (TextUtils.isEmpty(nombre)) {
            txtNombreRegistro.setError("Se requiere el nombre.");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            txtEmailRegistro.setError("Se requiere el correo electrónico.");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            txtPasswordRegistro.setError("Se requiere la contraseña.");
            return false;
        }
        if (TextUtils.isEmpty(telefono)) {
            txtTelefonoRegistro.setError("Se requiere el teléfono.");
            return false;
        }

        // 2. Validación de email (estructura)
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[_A-Za-z0-9]+)*(\\.[_A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            txtEmailRegistro.setError("Por favor, introduzca un correo electrónico válido");
            return false;
        }


        if (password.length() < 6) {
            txtPasswordRegistro.setError("La contraseña debe tener al menos 6 caracteres (Firebase).");
            return false;
        }

        // Si todas las validaciones pasan
        return true;
    }



    private void registrarUsuario() {
        // 1. Validar campos
        if (!validarCampos()) {
            return;
        }

        // Obtener textos después de la validación
        final String nombre = txtNombreRegistro.getText().toString().trim();
        final String email = txtEmailRegistro.getText().toString().trim();
        String password = txtPasswordRegistro.getText().toString();
        final String telefono = txtTelefonoRegistro.getText().toString().trim();

        // Deshabilitar botón y mostrar mensaje de progreso
        btnRegistrar.setEnabled(false);
        Toast.makeText(this, "Registrando usuario...", Toast.LENGTH_SHORT).show();


        // 2. Crear el usuario en Firebase Authentication (Email/Password)
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro exitoso en Authentication
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            // 3. Guardar el perfil en Firestore
                            if (user != null) {
                                // 3.1. Crear el objeto Map (perfil del usuario)
                                Map<String, Object> userProfile = new HashMap<>();
                                userProfile.put("uid", user.getUid());
                                userProfile.put("nombre", nombre);
                                userProfile.put("email", email);
                                userProfile.put("telefono", telefono);
                                userProfile.put("is_vendedor", false); // Opcional: añade un campo booleano

                                // 3.2. Guardar el documento en la colección 'usuarios'
                                mFirestore.collection("usuarios")
                                        .document(user.getUid()) // Usar el UID como ID del documento
                                        .set(userProfile)
                                        .addOnCompleteListener(firestoreTask -> {
                                            btnRegistrar.setEnabled(true); // Habilitar de nuevo al finalizar la operación

                                            if (firestoreTask.isSuccessful()) {
                                                Log.d(TAG, "Documento de usuario guardado en Firestore!");
                                                Toast.makeText(Registrarse_Activity.this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();

                                                // 4. Redirigir al usuario
                                                Intent intent = new Intent(Registrarse_Activity.this, Login_Activity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // Fallo al guardar en Firestore
                                                Log.w(TAG, "Error al guardar datos de perfil en Firestore", firestoreTask.getException());
                                                // Nota: Si esto falla, el usuario ya existe en Auth, por lo que es mejor
                                                // mostrar un error y que lo intente de nuevo o borrar el usuario de Auth.
                                                Toast.makeText(Registrarse_Activity.this, "Error al guardar datos de perfil. Intente de nuevo.", Toast.LENGTH_LONG).show();
                                            }
                                        });

                            }
                        } else {
                            // 3. Si falla el registro en Authentication (ej. email ya registrado, contraseña débil, etc.)
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido.";
                            Toast.makeText(Registrarse_Activity.this, "Fallo el registro: " + error,
                                    Toast.LENGTH_LONG).show();
                            btnRegistrar.setEnabled(true);
                        }
                    }
                });
    }
}