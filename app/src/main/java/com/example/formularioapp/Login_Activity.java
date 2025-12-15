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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Login_Activity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // Componentes de UI
    private EditText txtEmailLogin, txtPasswordLogin;
    private Button btnLogin;
    private TextView txtIrARegistro;
    // REMOVIDO: Button btnGoogleLogin

    // Instancia de Firebase Authentication
    private FirebaseAuth mAuth;
    // REMOVIDO: GoogleSignInClient mGoogleSignInClient
    // REMOVIDO: ActivityResultLauncher<Intent> googleSignInLauncher


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iniciar_sesion);

        // 1. Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 2. Inicializar Vistas
        txtEmailLogin = findViewById(R.id.txtEmailLogin);
        txtPasswordLogin = findViewById(R.id.txtPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        txtIrARegistro = findViewById(R.id.txtIrARegistro);
        // REMOVIDO: Vincular btnGoogleLogin

        // 3. Configurar Listeners
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesion();
            }
        });

        txtIrARegistro.setOnClickListener(v -> {
            Intent intent = new Intent(Login_Activity.this, Registrarse_Activity.class);
            startActivity(intent);
        });

        // REMOVIDO: Listener de btnGoogleLogin
    }


    @Override
    public void onStart() {
        super.onStart();
        // Verifica si el usuario está actualmente logueado (non-null) y actualiza la UI.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // Si el usuario ya está logueado, lo enviamos directamente al Feed
            irAHomeActivity();
        }
    }

    private boolean validarCampos() {
        String email = txtEmailLogin.getText().toString().trim();
        String password = txtPasswordLogin.getText().toString();

        // 1. Validación de campos vacíos
        if (TextUtils.isEmpty(email)) {
            txtEmailLogin.setError("Ingrese su correo electrónico.");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            txtPasswordLogin.setError("Ingrese su contraseña.");
            return false;
        }

        // 2. Validación de formato de correo electrónico
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[_A-Za-z0-9]+)*(\\.[_A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            txtEmailLogin.setError("Por favor, introduzca un correo electrónico válido");
            return false;
        }

        // 3. Validación de longitud de contraseña
        if (password.length() < 6) {
            txtPasswordLogin.setError("La contraseña debe tener al menos 6 caracteres.");
            return false;
        }

        // Si todas las validaciones pasan
        return true;
    }

    // REMOVIDO: iniciarSesionConGoogle()
    // REMOVIDO: handleSignInResult(Task<GoogleSignInAccount> completedTask)

    private void iniciarSesion() {
        // 1. Ejecutar validación de campos
        if (!validarCampos()) {
            return;
        }

        String email = txtEmailLogin.getText().toString().trim();
        String password = txtPasswordLogin.getText().toString().trim();

        // Deshabilitar botón y mostrar mensaje de progreso
        btnLogin.setEnabled(false);
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();

        // 2. Llamada a Firebase para iniciar sesión
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login exitoso
                            Log.d(TAG, "signInWithEmail:success");
                            irAHomeActivity();
                        } else {
                            // Si el login falla (contraseña incorrecta, usuario no existe)
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String error = task.getException() != null ? task.getException().getMessage() : "Credenciales inválidas.";
                            Toast.makeText(Login_Activity.this, "Error de inicio de sesión: " + error,
                                    Toast.LENGTH_LONG).show();
                            btnLogin.setEnabled(true);
                        }
                    }
                });
    }


    private void irAHomeActivity() {

        Intent intent = new Intent(Login_Activity.this, FeedActivity.class);

        // Limpiar la pila de actividades para que el usuario no pueda volver al Login con el botón de retroceso
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}