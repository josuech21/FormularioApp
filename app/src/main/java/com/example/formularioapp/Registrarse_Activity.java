package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.content.Intent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Registrarse_Activity extends AppCompatActivity {

    // CAMPOS DE ENTRADA DE DATOS (EditText)
    EditText txtNombreRegistro;
    EditText txtEmailRegistro;
    EditText txtPasswordRegistro;
    EditText txtTelefonoRegistro;

    // BOTONES Y ENLACES (Button, TextView)
    Button btnRegistrar;
    TextView txtIrALogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrarse_usuario_nuevo);


        // --- 1. Referencias a los elementos del XML ---
        txtNombreRegistro = findViewById(R.id.txtNombreRegistro);
        txtEmailRegistro = findViewById(R.id.txtEmailRegistro);
        txtPasswordRegistro = findViewById(R.id.txtPasswordRegistro);
        txtTelefonoRegistro = findViewById(R.id.txtTelefonoRegistro);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        txtIrALogin = findViewById(R.id.txtIrALogin);


        // --- 2. Lógica del Botón REGISTRAR (Implementación de la navegación) ---
        btnRegistrar.setOnClickListener(v -> {
            String nombre = txtNombreRegistro.getText().toString().trim();
            String email = txtEmailRegistro.getText().toString().trim();
            String password = txtPasswordRegistro.getText().toString();
            String telefono = txtTelefonoRegistro.getText().toString().trim();

            // Llama a la función de validación
            if (validarCampos(nombre, email, password, telefono)) {

                // --- PUNTO CRÍTICO: LÓGICA DE REGISTRO REAL ---
                // Aquí iría el código para guardar el usuario en la base de datos (Firebase/SQLite/API)

                // Suponiendo que la operación de guardado es exitosa:
                String mensaje = "¡Registro Exitoso!\nUsuario: " + nombre + " (" + email + ")";
                Toast.makeText(Registrarse_Activity.this, mensaje, Toast.LENGTH_LONG).show();

                // *************************************************************************
                // **** IMPLEMENTACIÓN SOLICITADA: NAVEGACIÓN A LA PANTALLA DE LOGIN ****
                // *************************************************************************

                // 1. Crear el Intent para ir a Login_Activity
                Intent intent = new Intent(Registrarse_Activity.this, Login_Activity.class);

                // Opcional pero RECOMENDADO: Limpiar la pila de actividades.
                // Esto asegura que el usuario no pueda presionar 'Atrás' y volver
                // a la pantalla de registro después de un registro exitoso.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                // 2. Iniciar la actividad de Login
                startActivity(intent);

                // 3. Finalizar (cerrar) la actividad de Registro
                finish();
            }
            // Si la validación falla, no se hace nada y la Activity se queda.
        });


        // --- 3. Lógica del Enlace IR A LOGIN (Se mantiene) ---
        txtIrALogin.setOnClickListener(v -> {
            // Navegación real a la pantalla de Login
            Intent intent = new Intent(Registrarse_Activity.this, Login_Activity.class);
            startActivity(intent);
            // Cierra la pantalla de registro para no volver al presionar 'Atrás'
            finish();
        });
    }


    /**
     * Función para realizar todas las validaciones del formulario.
     * @return true si todos los campos son válidos, false si no lo son.
     */
    private boolean validarCampos(String nombre, String email, String password, String telefono) {

        // 1. Validación de campos vacíos
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Debe completar todos los campos para registrarse",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        // 2. Validación de email (estructura)
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[_A-Za-z0-9]+)*(\\.[_A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            Toast.makeText(this, "Por favor, introduzca un correo electrónico válido",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        // 3. Validación de longitud de contraseña
        if (password.length() < 8) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        // Si todas las validaciones pasan
        return true;
    }
}