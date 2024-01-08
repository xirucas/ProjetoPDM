package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.projetopdm.Modelos.Funcionario;

import java.util.ArrayList;
import java.util.List;

public class Perfil extends AppCompatActivity {

    MainActivity binding;
    Funcionario funcionario;
    Button encerrar_btn ;
    Button pausa_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        encerrar_btn = (Button) findViewById(R.id.encerrar_btn);
        pausa_btn = (Button) findViewById(R.id.pausa_btn);
        int Id = getIntent().getIntExtra("Id", 0);
        String nome = getIntent().getStringExtra("Nome");
        String email = getIntent().getStringExtra("Email");
        String GUID = getIntent().getStringExtra("GUID");
        String pin = getIntent().getStringExtra("Pin");
        String contacto = getIntent().getStringExtra("Contacto");
        String imagemFuncionario = getIntent().getStringExtra("ImagemFuncionario");
        String estadoFuncionario = getIntent().getStringExtra("EstadoFuncionario");
        int estadoFuncionarioId = getIntent().getIntExtra("EstadoFuncionarioId", 0); //1 é online

        //estadoFuncionarioId=0; //testar em caso de pausa

        if (estadoFuncionarioId != 1){
            encerrar_btn.setVisibility(View.INVISIBLE);
            pausa_btn.setText("Retornar da pausa");
        }
        if (estadoFuncionarioId == 1){
            encerrar_btn.setVisibility(View.VISIBLE);
            pausa_btn.setText("Iniciar pausa");
        }

        encerrar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Splash.class);
                startActivity(intent);
            }
        });
        pausa_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (estadoFuncionarioId==1){//se tiver online
                    //codigo para mudar o estado para pausa mas como vai estar em pausa deve desativar os botoes da lista de RMAS
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else if (estadoFuncionarioId!=1) {//se tiver em pausa
                    //codigo para retomar ao trabalho
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });



    }
}