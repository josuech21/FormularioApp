package com.example.formularioapp; // REEMPLAZA esto con el nombre de tu paquete

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Geolocator extends AppCompatActivity {

    private EditText editLatitud, editLongitud;
    private Button btnBuscar;
    private TextView textResultado;
    private List<CentroComercial> centros;

    // Clase interna para modelar un Centro Comercial
    private static class CentroComercial {
        String nombre;
        double latitud;
        double longitud;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.codpractica);

        // 1. Inicializar vistas
        editLatitud = findViewById(R.id.editLatitud);
        editLongitud = findViewById(R.id.editLongitud);
        btnBuscar = findViewById(R.id.btnBuscar);
        textResultado = findViewById(R.id.textResultado);
        centros = new ArrayList<>();

        // 2. Cargar y parsear datos XML al inicio
        loadCentrosComerciales();

        // 3. Configurar el listener del botón de búsqueda
        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findNearestMall();
            }
        });
    }


    private void loadCentrosComerciales() {
        try {
            // Obtener el InputStream del archivo XML en res/raw/centros_comerciales_data.xml
            InputStream is = getResources().openRawResource(com.google.firebase.R.raw.firebase_common_keep);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, null);

            int eventType = parser.getEventType();
            CentroComercial currentCentro = null;
            String tagName;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("centro".equals(tagName)) {
                            currentCentro = new CentroComercial();
                        } else if (currentCentro != null) {
                            if ("nombre".equals(tagName)) {
                                currentCentro.nombre = parser.nextText();
                            } else if ("latitud".equals(tagName)) {
                                currentCentro.latitud = Double.parseDouble(parser.nextText());
                            } else if ("longitud".equals(tagName)) {
                                currentCentro.longitud = Double.parseDouble(parser.nextText());
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("centro".equals(tagName) && currentCentro != null) {
                            centros.add(currentCentro);
                        }
                        break;
                }
                eventType = parser.next();
            }

            is.close();
            Log.d("MainActivity", "Centros cargados: " + centros.size());

        } catch (XmlPullParserException | IOException e) {
            Toast.makeText(this, "Error al cargar datos XML. Verifica que res/raw/centros_comerciales_data.xml exista.", Toast.LENGTH_LONG).show();
            Log.e("MainActivity", "Error parsing XML", e);
        }
    }


    private void findNearestMall() {
        // 1. Obtención de coordenadas del usuario
        String latStr = editLatitud.getText().toString();
        String lonStr = editLongitud.getText().toString();

        if (latStr.isEmpty() || lonStr.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa ambas coordenadas.", Toast.LENGTH_SHORT).show();
            textResultado.setText("Ingresa tus coordenadas y presiona Buscar.");
            return;
        }

        double userLat, userLon;
        try {
            userLat = Double.parseDouble(latStr);
            userLon = Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Coordenadas inválidas. Solo se permiten números.", Toast.LENGTH_SHORT).show();
            textResultado.setText("Formato de coordenadas inválido. Usa números decimales.");
            return;
        }

        if (centros.isEmpty()) {
            textResultado.setText("Error: No se pudieron cargar los datos de los centros comerciales.");
            return;
        }

        // 2. Crear objeto Location para el punto del usuario
        Location userLocation = new Location("user");
        userLocation.setLatitude(userLat);
        userLocation.setLongitude(userLon);

        // 3. Inicializar variables de búsqueda
        CentroComercial nearestMall = null;
        float minDistance = Float.MAX_VALUE; // Distancia inicial máxima (en metros)

        // 4. Iterar y calcular distancias
        for (CentroComercial mall : centros) {
            Location mallLocation = new Location("mall");
            mallLocation.setLatitude(mall.latitud);
            mallLocation.setLongitude(mall.longitud);

            // Calcula la distancia en metros usando el método de Location (Haversine optimizado)
            float distanceInMeters = userLocation.distanceTo(mallLocation);

            if (distanceInMeters < minDistance) {
                minDistance = distanceInMeters;
                nearestMall = mall;
            }
        }

        // 5. Mostrar el resultado
        if (nearestMall != null) {
            // Convertir metros a kilómetros con formato
            double distanceInKm = minDistance / 1000.0;
            String resultText = String.format(Locale.getDefault(),
                    "¡Ubicado!\nEl centro comercial más cercano es:\n\n" +
                            "▶ Nombre: %s\n" +
                            "▶ Ubicación: %s\n" +
                            "▶ Distancia aproximada: %.2f km",
                    nearestMall.nombre, nearestMall.latitud > 0 ? "Costa Rica" : "N/A", distanceInKm); // Provincia no se parseó en esta versión, así que se usa una aproximación simple

            textResultado.setText(resultText);
        } else {
            textResultado.setText("No se pudo encontrar un centro comercial cercano en la lista.");
        }
    }
}