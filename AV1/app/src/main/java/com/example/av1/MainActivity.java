package com.example.av1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton carButton = findViewById(R.id.car_button);
        ImageButton truckButton = findViewById(R.id.truck_button);

        carButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("tipoVeiculo", "carro");
            startActivity(intent);
        });

        truckButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("tipoVeiculo", "caminhao");
            startActivity(intent);
        });
    }
}
