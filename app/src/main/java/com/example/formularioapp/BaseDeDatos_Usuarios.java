package com.example.formularioapp;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class BaseDeDatos_Usuarios {

    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "usuarios";


    public BaseDeDatos_Usuarios() {
        this.db = FirebaseFirestore.getInstance();
    }


    public Task<Void> guardarUsuario(String userId, String nombre, String email, String telefono) {

        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombre", nombre);
        usuario.put("email", email);
        usuario.put("telefono", telefono);
        usuario.put("fecha_registro", System.currentTimeMillis());


        return db.collection(COLLECTION_NAME).document(userId).set(usuario);
    }
}
