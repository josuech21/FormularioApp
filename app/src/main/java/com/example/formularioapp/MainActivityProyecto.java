package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;


public class MainActivityProyecto extends AppCompatActivity {

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


        txtNombreRegistro = findViewById(R.id.txtNombreRegistro);
        txtEmailRegistro = findViewById(R.id.txtEmailRegistro);
        txtPasswordRegistro = findViewById(R.id.txtPasswordRegistro);
        txtTelefonoRegistro = findViewById(R.id.txtTelefonoRegistro);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        txtIrALogin = findViewById(R.id.txtIrALogin);


        btnRegistrar.setOnClickListener(v -> {
            String nombre = txtNombreRegistro.getText().toString();
            String email = txtEmailRegistro.getText().toString();
            String password = txtPasswordRegistro.getText().toString();
            String telefono = txtTelefonoRegistro.getText().toString();

            // Validación de campos vacíos
            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || telefono.isEmpty()) {

                Toast.makeText(MainActivityProyecto.this, "Debe completar todos los campos para registrarse",
                        Toast.LENGTH_LONG).show();
            }
            // Validación de longitud de contraseña
            else if (password.length() < 8) {

                Toast.makeText(MainActivityProyecto.this, "La contraseña debe tener al menos 8 caracteres",
                        Toast.LENGTH_LONG).show();
            }
            else {

                String mensaje = "Registrando nuevo usuario:\n" + nombre + " (" + email + ")";

                Toast.makeText(MainActivityProyecto.this, mensaje, Toast.LENGTH_LONG).show();
                // Aquí iría la lógica para enviar datos a la base de datos y luego navegar a Login o Feed
            }
        });


        txtIrALogin.setOnClickListener(v -> {

            Toast.makeText(MainActivityProyecto.this, "Volviendo a la pantalla de Login",
                    Toast.LENGTH_SHORT).show();

        });
    }
}