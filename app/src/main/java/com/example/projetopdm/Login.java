package com.example.projetopdm;

import static com.example.projetopdm.LocalDataBase.FuncionarioSharedPreferences.getFuncionarioData;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.Modelos.Funcionario;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    ConstraintLayout loading;
    CardView imagemFuncionario;
    Funcionario funcionario = new Funcionario();
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        imagemFuncionario = findViewById(R.id.user);

        Funcionario x = getFuncionarioData(this);

        String GUID = getIntent().getStringExtra("GUID");

        if (x.getGUID()!=null){
            if(x.getGUID().equals(GUID)){
                Bitmap imagem = StringToBitMap(x.getImagemFuncionario());
                ImageView imageView = findViewById(R.id.perfil_btn);
                imageView.setImageBitmap(imagem);

                TextView nome = findViewById(R.id.name);
                nome.setText(x.getNome());
                loading.setVisibility(View.INVISIBLE);
            } else if (isInternetAvailable()) {
                chamarAPI(GUID);
                loading.setVisibility(View.INVISIBLE);
            }else{
                loading.setVisibility(View.INVISIBLE);
                Toast.makeText(Login.this, "Sem ligação à internet para efetuar login com id de funcionario diferente", Toast.LENGTH_SHORT).show();
            }
        }else if(isInternetAvailable()){
            chamarAPI(GUID);
            loading.setVisibility(View.INVISIBLE);
        }else {
            loading.setVisibility(View.INVISIBLE);
            Toast.makeText(Login.this, "Sem ligação à internet para efetuar login pela primeira vez", Toast.LENGTH_SHORT).show();
        }

        Button submit = findViewById(R.id.submit_btn);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText pin = findViewById(R.id.pin);
                if (x.getGUID() != null) {
                    if (pin.getText().toString().equals(x.getPin())) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("Id", x.getId());
                        intent.putExtra("Nome", x.getNome());
                        intent.putExtra("Email", x.getEmail());
                        intent.putExtra("Contacto", x.getContacto());
                        intent.putExtra("GUID", x.getGUID());
                        intent.putExtra("Pin", x.getPin());
                        intent.putExtra("ImagemFuncionario", x.getImagemFuncionario());
                        intent.putExtra("EstadoFuncionario", x.getEstadoFuncionario());
                        intent.putExtra("EstadoFuncionarioId", x.getEstadoFuncionarioId());
                        intent.putExtra("Departamento", x.getDepartamento());
                        intent.putExtra("Localizacao", x.getLocalizacao());
                        startActivity(intent);

                    } else if (isInternetAvailable()) {

                        if (pin.getText().toString().equals(funcionario.getPin())) {
                            chamarAPIparaMain();
                        } else {
                            Toast.makeText(Login.this, "Pin incorreto", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(Login.this, "Sem ligação à internet", Toast.LENGTH_SHORT).show();
                    }

                } else if (isInternetAvailable()) {

                    if (pin.getText().toString().equals(funcionario.getPin())) {
                        chamarAPIparaMain();
                    } else {
                        Toast.makeText(Login.this, "Pin incorreto", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(Login.this, "Sem ligação à internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void chamarAPIparaMain(){
        String request = "{"
                + " \"Id\": \"" + funcionario.getId() + "\", "
                + " \"GUID\": \"" + funcionario.getGUID() + "\", "
                + " \"Nome\": \"" + funcionario.getNome() + "\", "
                + " \"Email\": \"" + funcionario.getEmail() + "\", "
                + " \"Contacto\": \"" + funcionario.getContacto() + "\", "
                + " \"Pin\": \"" + funcionario.getPin() + "\", "
                + " \"EstadoFuncionarioId\": \"" + funcionario.getEstadoFuncionarioId() + "\"  }";
        JsonObject body = new JsonParser().parse(request).getAsJsonObject();
        Call<JsonObject> call2 = RetrofitClient.getInstance().getMyApi().CreateOrUpdateFuncionarioAPI(body);

        call2.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call2, Response<JsonObject> response) {
                JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                if (responseObj.get("Success").getAsBoolean()) {
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.putExtra("Id", funcionario.getId());
                    intent.putExtra("Nome", funcionario.getNome());
                    intent.putExtra("Email", funcionario.getEmail());
                    intent.putExtra("Contacto", funcionario.getContacto());
                    intent.putExtra("GUID", funcionario.getGUID());
                    intent.putExtra("Pin", funcionario.getPin());
                    intent.putExtra("ImagemFuncionario", funcionario.getImagemFuncionario());
                    intent.putExtra("EstadoFuncionario", funcionario.getEstadoFuncionario());
                    intent.putExtra("EstadoFuncionarioId", funcionario.getEstadoFuncionarioId());
                    intent.putExtra("Departamento", funcionario.getDepartamento());
                    intent.putExtra("Localizacao", funcionario.getLocalizacao());
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Erro a fazer login", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call2, Throwable t) {
                Toast.makeText(getApplicationContext(), "Erro a fazer login", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void chamarAPI(String GUID){
        Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().GetFuncionarioByGUID(GUID);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                if (responseObj.get("Success").getAsBoolean()) {
                    JsonObject FuncionarioObj = response.body().get("Funcionario").getAsJsonObject();
                    funcionario.setId(FuncionarioObj.get("Id").getAsInt());
                    funcionario.setNome(FuncionarioObj.get("Nome").getAsString());
                    funcionario.setEmail(FuncionarioObj.get("Email").getAsString());
                    funcionario.setContacto(FuncionarioObj.get("Contacto").getAsString());
                    funcionario.setGUID(FuncionarioObj.get("GUID").getAsString());
                    funcionario.setPin(FuncionarioObj.get("Pin").getAsString());
                    funcionario.setImagemFuncionario(FuncionarioObj.get("ImagemFuncionario").getAsString());
                    if (FuncionarioObj.get("EstadoFuncionarioId").getAsInt() == 2) {
                        funcionario.setEstadoFuncionario(FuncionarioObj.get("EstadoFuncionario").getAsString());
                        funcionario.setEstadoFuncionarioId(FuncionarioObj.get("EstadoFuncionarioId").getAsInt());
                    } else {
                        funcionario.setEstadoFuncionario("Online");
                        funcionario.setEstadoFuncionarioId(1);
                    }
                    funcionario.setDepartamento(FuncionarioObj.get("Departamento").getAsString());
                    funcionario.setLocalizacao(FuncionarioObj.get("Localizacao").getAsString());
                } else {
                    Toast.makeText(Login.this, "Aconteceu algo errado ao tentar carregar o funcionario", Toast.LENGTH_SHORT).show();
                }
                Bitmap imagem = StringToBitMap(funcionario.getImagemFuncionario());
                ImageView imageView = findViewById(R.id.perfil_btn);
                imageView.setImageBitmap(imagem);

                TextView nome = findViewById(R.id.name);
                nome.setText(funcionario.getNome());
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(Login.this, "Aconteceu algo errado ao tentar carregar o funcionario", Toast.LENGTH_SHORT).show();
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