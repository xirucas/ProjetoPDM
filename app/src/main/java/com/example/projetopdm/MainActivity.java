package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.Modelos.Funcionario;
import com.example.projetopdm.Modelos.RMA;
import com.example.projetopdm.databinding.ActivityMainBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Funcionario funcionario = null;

    ArrayList<RMA> rmaList = new ArrayList<RMA>();
    ListAdapterRMA listAdapter;
    RMA rma;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Set the root view of the binding object
        int Id = getIntent().getIntExtra("Id", 0);
        String nome = getIntent().getStringExtra("Nome");
        String email = getIntent().getStringExtra("Email");
        String GUID = getIntent().getStringExtra("GUID");
        String pin = getIntent().getStringExtra("Pin");
        String contacto = getIntent().getStringExtra("Contacto");
        String imagemFuncionario = getIntent().getStringExtra("ImagemFuncionario");

        funcionario = new Funcionario(Id, GUID, nome, email, contacto, pin, imagemFuncionario);

        setContentView(binding.getRoot());

        ImageView img = findViewById(R.id.imagemFuncionario);
        Bitmap bitmap = StringToBitMap(imagemFuncionario);
        img.setImageBitmap(bitmap);

        if(isInternetAvailable()) {
            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().GetRMASByFuncionario(funcionario.getGUID());
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                    if (responseObj.get("Success").getAsBoolean()) {
                        if (!response.body().has("RMA")){
                            Toast.makeText(MainActivity.this, "Não existem RMA's", Toast.LENGTH_SHORT).show();
                        }else{
                        JsonArray rmaListObj = response.body().get("RMA").getAsJsonArray();
                        for (int i = 0; i < rmaListObj.size(); i++) {
                            JsonObject rmaObj = rmaListObj.get(i).getAsJsonObject();
                            String dataAb = "";
                            String dataF = "";
                            if (rmaObj.get("DataAbertura") != null){
                                dataAb = (rmaObj.get("DataAbertura").getAsString());
                            }else{
                                   dataAb = "null";
                            }
                            if (rmaObj.get("DataFecho") != null){
                                dataF = (rmaObj.get("DataFecho").getAsString());
                            }
                            else{
                                dataF = "null";
                            }
                            rma = new RMA(rmaObj.get("Id").getAsInt(), rmaObj.get("RMA").getAsString(), rmaObj.get("DescricaoCliente").getAsString(),rmaObj.get("DataCriacao").getAsString() ,dataAb , dataF, rmaObj.get("EstadoRMA").getAsString(), rmaObj.get("EstadoRMAId").getAsInt(), rmaObj.get("FuncionarioId").getAsInt());
                            rmaList.add(rma);
                        }
                        listAdapter = new ListAdapterRMA(MainActivity.this, rmaList, MainActivity.this);
                        binding.listRMA.setAdapter(listAdapter);
                    }
                    } else {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Aconteceu algo errado ao tentar carregar os RMA's", Toast.LENGTH_SHORT).show();
                }
            });
        }






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