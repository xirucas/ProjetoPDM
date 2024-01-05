package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.example.projetopdm.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Set the root view of the binding object
        
        setContentView(binding.getRoot());
    }
}