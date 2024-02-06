package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.LocalDataBase.AppDatabase;
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
    Button backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        //AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "BaseDeDadosLocal").allowMainThreadQueries().build();
        //this.deleteDatabase("BaseDeDadosLocal");

        encerrar_btn = (Button) findViewById(R.id.encerrar_btn);
        backButton = (Button) findViewById(R.id.back_button);

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

        ImageView img = findViewById(R.id.perfil_btn2);
        TextView Nome = findViewById(R.id.name);
        TextView emailFuncionario = findViewById(R.id.email);

        Nome.setText(nome);
        Bitmap bitmap = StringToBitMap(imagemFuncionario);
        img.setImageBitmap(bitmap);
        emailFuncionario.setText(email);

        //estadoFuncionarioId=0;

        if (estadoFuncionarioId != 1){
            encerrar_btn.setVisibility(View.INVISIBLE);
        }
        if (estadoFuncionarioId == 1){
            encerrar_btn.setVisibility(View.VISIBLE);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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

    public Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }
}