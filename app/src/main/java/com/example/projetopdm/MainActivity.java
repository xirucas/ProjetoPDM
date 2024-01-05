package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.projetopdm.Modelos.Funcionario;
import com.example.projetopdm.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Funcionario funcionario = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
       setContentView(binding.getRoot()); // Set the root view of the binding object
        int Id = getIntent().getIntExtra("Id", 0);
        String nome = getIntent().getStringExtra("nome");
        String email = getIntent().getStringExtra("email");
        String GUID = getIntent().getStringExtra("GUID");
        String pin = getIntent().getStringExtra("pin");
        String contacto = getIntent().getStringExtra("contacto");
        String imagemFuncionario = getIntent().getStringExtra("imagemFuncionario");

        funcionario = new Funcionario(Id, GUID, nome, email, contacto, pin, imagemFuncionario);
        
       setContentView(binding.getRoot());
    }
}