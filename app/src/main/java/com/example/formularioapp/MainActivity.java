package com.example.formularioapp;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import java.util.Calendar;
public class MainActivity extends AppCompatActivity {
    EditText txtNombre, txtApellido1, txtApellido2, txtCedula;
    Button btnFechaNacimiento, btnEnviar;
    TextView txtFechaSeleccionada;
    int dia, mes, anio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ejemplo);
// Referencias a los elementos del XML


        txtNombre = findViewById(R.id.txtNombre);
        txtApellido1 = findViewById(R.id.txtApellido1);
        txtApellido2 = findViewById(R.id.txtApellido2);
        txtCedula = findViewById(R.id.txtCedula);
        btnFechaNacimiento = findViewById(R.id.btnFechaNacimiento);
        txtFechaSeleccionada = findViewById(R.id.txtFechaSeleccionada);
        btnEnviar = findViewById(R.id.btnEnviar);

        // Selección de fecha
        btnFechaNacimiento.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            dia = c.get(Calendar.DAY_OF_MONTH);
            mes = c.get(Calendar.MONTH);
            anio = c.get(Calendar.YEAR);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        txtFechaSeleccionada.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }, anio, mes, dia);
            datePickerDialog.show();
        });

        // Botón enviar
        btnEnviar.setOnClickListener(v -> {
            String nombre = txtNombre.getText().toString();
            String apellido1 = txtApellido1.getText().toString();
            String apellido2 = txtApellido2.getText().toString();


            String cedula = txtCedula.getText().toString();
            String fecha = txtFechaSeleccionada.getText().toString();

            if (nombre.isEmpty() || apellido1.isEmpty() || apellido2.isEmpty() || cedula.isEmpty() ||
                    fecha.equals("No seleccionada")) {
                Toast.makeText(MainActivity.this, "Por favor complete todos los campos",
                        Toast.LENGTH_SHORT).show();
            } else {
                String mensaje = "Datos ingresados:\n" +
                        "Nombre: " + nombre + "\n" +
                        "Apellidos: " + apellido1 + " " + apellido2 + "\n" +
                        "Cédula: " + cedula + "\n" +
                        "Fecha Nacimiento: " + fecha;
                Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }
}
