package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

// IMPORTACIONES DE FIREBASE
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.HashMap;
import java.util.Map;

public class Metododepago_Activity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private static final String TAG = "MetodoPagoActivity";

    // Vistas del Formulario de Adición
    private TextInputEditText etNumeroTarjeta;
    private TextInputEditText etFechaVencimiento;
    private TextInputEditText etCvv;
    private TextInputEditText etNombreTitular;
    private Button btnGuardarTarjeta;

    // Vistas de la Interfaz Dinámica (Contenedores y Botones de Alternancia)
    private LinearLayout layoutFormularioTarjeta;
    private LinearLayout layoutTarjetasGuardadas;
    private TextView tvTarjetaGuardada;
    private Button btnEliminarTarjeta;
    private Button btnAddNuevaTarjeta;
    private Button btnCancelarRegistro;

    private String tarjetaActualId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gestiondepagos_activity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Configuración de Toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        vincularVistas();

        // CONFIGURAR LISTENERS DE ALTERNANCIA DE VISTAS
        btnAddNuevaTarjeta.setOnClickListener(v -> mostrarFormulario(true));
        btnCancelarRegistro.setOnClickListener(v -> mostrarFormulario(false));

        // Cargar los datos guardados al iniciar la Activity
        cargarUltimoMetodoPago();

        // Aplica el auto-formato MM/AA en el campo de texto editable
        aplicarFormatoFecha();

        // Listener de Guardado
        btnGuardarTarjeta.setOnClickListener(v -> {
            validarYGuardarTarjeta();
        });
    }

    private void vincularVistas() {
        // Vistas de la Interfaz Dinámica
        layoutFormularioTarjeta = findViewById(R.id.layoutFormularioTarjeta);
        layoutTarjetasGuardadas = findViewById(R.id.layoutTarjetasGuardadas);
        tvTarjetaGuardada = findViewById(R.id.tvTarjetaGuardada);
        btnEliminarTarjeta = findViewById(R.id.btnEliminarTarjeta);
        btnAddNuevaTarjeta = findViewById(R.id.btnAddNuevaTarjeta);
        btnCancelarRegistro = findViewById(R.id.btnCancelarRegistro);

        // Vistas del Formulario
        etNumeroTarjeta = findViewById(R.id.etNumeroTarjeta);
        etFechaVencimiento = findViewById(R.id.etFechaVencimiento);
        etCvv = findViewById(R.id.etCvv);
        etNombreTitular = findViewById(R.id.etNombreTitular);
        btnGuardarTarjeta = findViewById(R.id.btnGuardarTarjeta);
    }

    /**
     * Muestra u oculta el formulario de adición de tarjeta y ajusta la visibilidad de los contenedores.
     */
    private void mostrarFormulario(boolean mostrar) {
        // Muestra/Oculta el formulario
        layoutFormularioTarjeta.setVisibility(mostrar ? View.VISIBLE : View.GONE);

        // Oculta/Muestra la zona de tarjetas guardadas
        layoutTarjetasGuardadas.setVisibility(mostrar ? View.GONE : View.VISIBLE);
    }

    /**
     * Carga el método de pago más reciente del usuario y lo muestra en la UI.
     */
    private void cargarUltimoMetodoPago() {
        if (currentUser == null) {
            tvTarjetaGuardada.setText("Debe iniciar sesión para ver sus métodos de pago.");
            btnEliminarTarjeta.setVisibility(View.GONE);
            mostrarFormulario(false);
            return;
        }

        db.collection("usuarios").document(currentUser.getUid())
                .collection("metodos_pago")
                .orderBy("creadoEn", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        tarjetaActualId = doc.getId();

                        String ultimosDigitos = doc.getString("ultimosDigitos");
                        String vencimiento = doc.getString("vencimiento");

                        tvTarjetaGuardada.setText("Tarjeta guardada: **** " + ultimosDigitos + " (" + vencimiento + ")");
                        btnEliminarTarjeta.setVisibility(View.VISIBLE);

                        btnEliminarTarjeta.setOnClickListener(v -> eliminarMetodoPago(tarjetaActualId));

                    } else {
                        // Si no hay tarjetas
                        tvTarjetaGuardada.setText("No hay métodos de pago guardados. Añade uno.");
                        btnEliminarTarjeta.setVisibility(View.GONE);
                    }

                    // Aseguramos que la vista inicial sea la de tarjetas guardadas
                    mostrarFormulario(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar métodos de pago", e);
                    tvTarjetaGuardada.setText("Error al cargar los datos.");
                    mostrarFormulario(false);
                });
    }

    /**
     * Aplica un TextWatcher al campo de fecha de vencimiento (MM/AA) para
     * insertar automáticamente el '/' después de los dos primeros dígitos.
     */
    private void aplicarFormatoFecha() {
        etFechaVencimiento.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                String input = s.toString();

                // Si la longitud es 2 (MM) Y la barra '/' no está presente, la insertamos.
                if (input.length() == 2 && input.indexOf('/') == -1) {
                    isFormatting = true;
                    s.insert(2, "/");
                    isFormatting = false;
                }
            }
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    // --- LÓGICA DE VALIDACIÓN Y PERSISTENCIA ---

    private void validarYGuardarTarjeta() {
        if (currentUser == null) {
            Toast.makeText(this, "Debe iniciar sesión para guardar un método de pago.", Toast.LENGTH_SHORT).show();
            return;
        }

        String numTarjeta = getText(etNumeroTarjeta);
        String fechaVencimientoInput = getText(etFechaVencimiento);
        String cvv = getText(etCvv);
        String titular = getText(etNombreTitular);

        String fechaLimpia = fechaVencimientoInput.replaceAll("[^0-9]", "");

        // Validación flexible: Tarjeta (mín. 12), Fecha (exactamente 4 digitos), CVV (3 o 4)
        if (numTarjeta.length() < 12 || fechaLimpia.length() != 4 || cvv.length() < 3 || cvv.length() > 4 || titular.isEmpty()) {
            Toast.makeText(this, "Verifique: Tarjeta (mín. 12), Fecha (MM/AA) o CVV (3/4 dígitos).", Toast.LENGTH_LONG).show();
            return;
        }

        String mes = fechaLimpia.substring(0, 2);
        String anio = fechaLimpia.substring(2, 4);

        try {
            int mesNum = Integer.parseInt(mes);
            if (mesNum < 1 || mesNum > 12) {
                Toast.makeText(this, "Mes de vencimiento inválido. Debe ser entre 01 y 12.", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error en el formato de la fecha.", Toast.LENGTH_LONG).show();
            return;
        }

        String ultimosCuatroDigitos = numTarjeta.substring(numTarjeta.length() - 4);
        String fechaCompleta = mes + "/" + anio;

        Map<String, Object> metodoPago = new HashMap<>();
        metodoPago.put("ultimosDigitos", ultimosCuatroDigitos);
        metodoPago.put("vencimiento", fechaCompleta);
        metodoPago.put("titular", titular);
        metodoPago.put("tipo", "Tarjeta");
        metodoPago.put("creadoEn", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("metodos_pago")
                .add(metodoPago)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Tarjeta ****" + ultimosCuatroDigitos + " guardada con éxito.", Toast.LENGTH_LONG).show();

                    // Refrescar y volver a la vista de tarjeta guardada
                    cargarUltimoMetodoPago();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar el método de pago", e);
                    Toast.makeText(this, "Error al guardar el método de pago.", Toast.LENGTH_LONG).show();
                });
    }

    public void eliminarMetodoPago(String metodoPagoId) {
        if (currentUser == null) return;

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("metodos_pago")
                .document(metodoPagoId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Método de pago eliminado con éxito.", Toast.LENGTH_SHORT).show();

                    // Refrescar la vista
                    tarjetaActualId = null;
                    cargarUltimoMetodoPago();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al eliminar el método de pago", e);
                    Toast.makeText(this, "Error al eliminar el método de pago.", Toast.LENGTH_SHORT).show();
                });
    }
}