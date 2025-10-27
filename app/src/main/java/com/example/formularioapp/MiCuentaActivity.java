package com.example.formularioapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.appbar.MaterialToolbar;

public class
MiCuentaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mi_cuenta_activity);


        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}
