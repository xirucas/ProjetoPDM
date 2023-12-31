package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.Modelos.Funcionario;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        funcionario = new Funcionario(Id, GUID, nome, email, contacto, pin, imagemFuncionario, estadoFuncionarioId, estadoFuncionario);

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
                if (isInternetAvailable()){
                    funcionario.setEstadoFuncionarioId(3);
                    String request = "{"
                            + " \"Id\": \"" + Id + "\", "
                            + " \"GUID\": \"" + GUID + "\", "
                            + " \"Nome\": \"" + nome + "\", "
                            + " \"Email\": \"" + email + "\", "
                            + " \"Contacto\": \"" + contacto + "\", "
                            + " \"Pin\": \"" + pin + "\", "
                            + " \"EstadoFuncionarioId\": \"" + 3 + "\" }";
                    JsonObject body = new JsonParser().parse(request).getAsJsonObject();
                    Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().CreateOrUpdateFuncionarioAPI(body);

                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                            if (responseObj.get("Success").getAsBoolean()) {
                                Toast.makeText(getApplicationContext(), "Funcionario encerrado", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), Splash.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "Erro ao encerrar funcionario", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Erro ao encerrar funcionario", Toast.LENGTH_LONG).show();
                        }
                    });


                } else {
                    Toast.makeText(getApplicationContext(), "Sem conexão com a internet", Toast.LENGTH_LONG).show();
                }

            }
        });
        pausa_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetAvailable()) {
                    if (estadoFuncionarioId == 1) {//se tiver online
                        funcionario.setEstadoFuncionarioId(2);
                        funcionario.setEstadoFuncionario("Em Pausa");


                    } else if (estadoFuncionarioId == 2) {//se tiver em pausa
                        //codigo para retomar ao trabalho
                        funcionario.setEstadoFuncionarioId(1);
                        funcionario.setEstadoFuncionario("Online");
                    }

                    String request = "{"
                            + " \"Id\": \"" + Id + "\", "
                            + " \"GUID\": \"" + GUID + "\", "
                            + " \"Nome\": \"" + nome + "\", "
                            + " \"Email\": \"" + email + "\", "
                            + " \"Contacto\": \"" + contacto + "\", "
                            + " \"Pin\": \"" + pin + "\", "
                            + " \"EstadoFuncionarioId\": \"" + funcionario.getEstadoFuncionarioId() + "\" }";
                    JsonObject body = new JsonParser().parse(request).getAsJsonObject();
                    Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().CreateOrUpdateFuncionarioAPI(body);

                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                            if (responseObj.get("Success").getAsBoolean()) {
                                Toast.makeText(getApplicationContext(), "Estado do funcionario alterado", Toast.LENGTH_LONG).show();
                                //voltar para a main activity
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("Id", funcionario.getId());
                                intent.putExtra("Nome", funcionario.getNome());
                                intent.putExtra("Email", funcionario.getEmail());
                                intent.putExtra("Contacto", funcionario.getContacto());
                                intent.putExtra("GUID", funcionario.getGUID());
                                intent.putExtra("Pin", funcionario.getPin());
                                intent.putExtra("ImagemFuncionario", funcionario.getImagemFuncionario());
                                intent.putExtra("EstadoFuncionario", funcionario.getEstadoFuncionario());
                                intent.putExtra("EstadoFuncionarioId", funcionario.getEstadoFuncionarioId());
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "Erro ao alterar estado do funcionario", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Erro ao alterar estado do funcionario", Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(), "Sem conexão com a internet", Toast.LENGTH_LONG).show();
                }


            }
        });



    }


    private boolean isInternetAvailable(){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;

        return connected;
    }
}