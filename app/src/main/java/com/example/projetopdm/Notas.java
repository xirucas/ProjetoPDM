package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.Modelos.NotaRMA;
import com.example.projetopdm.Modelos.RMA;
import com.example.projetopdm.databinding.ActivityMainBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

import com.example.projetopdm.databinding.ActivityNotasBinding;

public class Notas extends AppCompatActivity {

    ActivityNotasBinding binding;
    int RMAId;

    RMA rma = new RMA();
    NotaRMA notaRMA;

    ArrayList<NotaRMA> rmaList = new ArrayList<NotaRMA>();
    ListaAdapterRMADetails listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotasBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        RMAId = getIntent().getIntExtra("RMAId",0);

        LinearLayout popup = findViewById(R.id.popup);
        popup.setVisibility(View.INVISIBLE);

        if (isInternetAvailable()){
            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().GetRMAById(RMAId);

            call.enqueue(new Callback<JsonObject>(){
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                    if (responseObj.get("Success").getAsBoolean()){

                        JsonObject rmaObj = response.body().get("RMA").getAsJsonObject();

                        rma.setId(rmaObj.get("Id").getAsInt());
                        rma.setRMA(rmaObj.get("RMA").getAsString());
                        rma.setDescricaoCliente(rmaObj.get("DescricaoCliente").getAsString());
                        rma.setDataCriacao(rmaObj.get("DataCriacao").getAsString());
                        if (rmaObj.get("DataAbertura")!=null) rma.setDataAbertura(rmaObj.get("DataAbertura").getAsString());
                        if (rmaObj.get("DataFecho")!=null) rma.setDataFecho(rmaObj.get("DataFecho").getAsString());
                        rma.setEstadoRMA(rmaObj.get("EstadoRMA").getAsString());
                        rma.setEstadoRMAId(rmaObj.get("EstadoRMAId").getAsInt());
                        rma.setFuncionarioId(rmaObj.get("FuncionarioId").getAsInt());

                        TextView rMA = findViewById(R.id.ticketsTitle);
                        TextView dataRma = findViewById(R.id.datarma);
                        TextView descricao = findViewById(R.id.textView3);

                        rMA.setText(rma.getRMA());
                        dataRma.setText(rma.getDataCriacao());
                        descricao.setText(rma.getDescricaoCliente());

                        if (response.body().has("RMANotas")) {
                            JsonArray NotasRMA = response.body().get("RMANotas").getAsJsonArray();

                            for (int i = 0; i < NotasRMA.size(); i++) {
                                JsonObject notaRMAObj = NotasRMA.get(i).getAsJsonObject();
                                notaRMA = new NotaRMA();
                                notaRMA.setId(notaRMAObj.get("Id").getAsInt());
                                notaRMA.setTitulo(notaRMAObj.get("Titulo").getAsString());
                                notaRMA.setDataCriacao(notaRMAObj.get("DataCriacao").getAsString());
                                notaRMA.setNota(notaRMAObj.get("Nota").getAsString());
                                notaRMA.setRMAId(notaRMAObj.get("RMAId").getAsInt());
                                if (notaRMAObj.get("ImagemNotaId") != null)
                                    notaRMA.setImagemNotaId(notaRMAObj.get("ImagemNotaId").getAsInt());
                                if (notaRMAObj.get("ImagemNota") != null)
                                    notaRMA.setImagemNota(notaRMAObj.get("ImagemNota").getAsString());
                                rmaList.add(notaRMA);
                            }
                            listAdapter = new ListaAdapterRMADetails(Notas.this, rmaList, Notas.this);
                            binding.notas.setAdapter(listAdapter);
                        }

                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(Notas.this, "Aconteceu algo errado ao tentar carregar o RMA", Toast.LENGTH_SHORT).show();
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
}