package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.content.Intent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login_Activity extends AppCompatActivity {

    // CAMPOS DE ENTRADA DE DATOS (EditText)
    EditText txtEmailLogin;
    EditText txtPasswordLogin;

    // BOTONES Y ENLACES (Button, TextView)
    Button btnLogin;
    TextView txtOlvidoPassword;
    TextView txtIrARegistro; // Este TextView debe existir en el XML si quieres ir a Registro

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Conecta esta Activity con el layout de Login
        setContentView(R.layout.iniciar_sesion); // Asegúrate de que este es el nombre de tu archivo XML de Login

        // --- 1. Referencias a los elementos del XML ---
        txtEmailLogin = findViewById(R.id.txtEmailLogin);
        txtPasswordLogin = findViewById(R.id.txtPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        txtOlvidoPassword = findViewById(R.id.txtOlvidoPassword);




        // --- 2. Lógica del Botón INICIAR SESIÓN (MODIFICADA) ---
        btnLogin.setOnClickListener(v -> {
            String email = txtEmailLogin.getText().toString().trim();
            String password = txtPasswordLogin.getText().toString();

            // Llama a la función de validación
            if (validarCampos(email, password)) {

                // Lógica simulada de autenticación exitosa (Aquí iría la llamada real al backend)
                if (simularAutenticacion(email, password)) {

                    Toast.makeText(Login_Activity.this, "¡Bienvenido! Sesión iniciada.", Toast.LENGTH_LONG).show();


                    // ** LÓGICA DE NAVEGACIÓN SEGURA AL FEED **

                    Intent feedIntent = new Intent(Login_Activity.this, FeedActivity.class);

                    feedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    // 3. Iniciar FeedActivity
                    startActivity(feedIntent);

                } else {
                    Toast.makeText(Login_Activity.this, "Error de autenticación. Verifique sus credenciales.", Toast.LENGTH_LONG).show();
                }
            }
        });


        // --- 3. Lógica del Enlace ¿OLVIDASTE TU CONTRASEÑA? ---
        txtOlvidoPassword.setOnClickListener(v -> {
            Toast.makeText(Login_Activity.this, "Redirigiendo a recuperación de contraseña...",
                    Toast.LENGTH_SHORT).show();
            // Implementación pendiente: Navegación a la Activity de recuperación
        });


        // --- 4. Lógica del Enlace IR A REGISTRO (Si el TextView existe en tu layout) ---
        if (txtIrARegistro != null) {
            txtIrARegistro.setOnClickListener(v -> {
                Intent intent = new Intent(Login_Activity.this, Registrarse_Activity.class);
                startActivity(intent);
            });
        }
    }


    /**
     * Función para realizar las validaciones del formulario de Login.
     * @return true si ambos campos son válidos, false si no lo son.
     */
    private boolean validarCampos(String email, String password) {

        // 1. Validación de campos vacíos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Debe ingresar su correo y contraseña",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        // 2. Validación de formato de correo electrónico
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            Toast.makeText(this, "Por favor, introduzca un correo electrónico válido",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        // Si ambas validaciones pasan
        return true;
    }

    /**
     * Función simulada para verificar credenciales. REEMPLAZAR con lógica real.
     */
    private boolean simularAutenticacion(String email, String password) {
        // Por ahora, siempre devuelve true si los campos están llenos y son válidos.
        // En un caso real, esto sería una llamada a tu servidor o Firebase.
        return true;
    }
}