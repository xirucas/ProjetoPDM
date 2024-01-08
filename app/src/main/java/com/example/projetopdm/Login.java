package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.TextView;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.Modelos.Funcionario;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    Funcionario funcionario = new Funcionario();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        String GUID = getIntent().getStringExtra("GUID");

        if(isInternetAvailable()){
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
                        funcionario.setEstadoFuncionario("Online");
                        funcionario.setEstadoFuncionarioId(1);
                    } else {
                        Toast.makeText(Login.this, "Error", Toast.LENGTH_SHORT).show();
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

        Button submit = findViewById(R.id.submit_btn);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText pin = findViewById(R.id.pin);
                if(pin.getText().toString().equals(funcionario.getPin())){
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
                    startActivity(intent);
                }
                else{
                    Toast.makeText(Login.this, "Pin incorreto", Toast.LENGTH_SHORT).show();
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