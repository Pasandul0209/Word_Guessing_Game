package com.kasthuri.word_guessing_game;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("WordGame!", MODE_PRIVATE);
        String savedName = prefs.getString("playerName...", null);
        if (savedName != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_splash);
        EditText etName = findViewById(R.id.etName);
        Button btnStart = findViewById(R.id.btnStart);

        btnStart.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString("playerName", name).apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
