package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.Modelos.NotaRMA;
import com.example.projetopdm.Modelos.RMA;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class Notas extends AppCompatActivity {
    int RMAId;

    RMA rma = new RMA();
    NotaRMA notaRMA;

    ArrayList<NotaRMA> rmaList = new ArrayList<NotaRMA>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notas);

        RMAId = getIntent().getIntExtra("RMAId",0);

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

                        if (response.body().has("RMANotas")) {
                            JsonArray NotasRMA = response.body().get("RMANotas").getAsJsonArray();

                            for (int i = 0; i < NotasRMA.size(); i++) {
                                JsonObject notaRMAObj = NotasRMA.get(i).getAsJsonObject();
                                notaRMA = new NotaRMA();
                                notaRMA.setId(notaRMAObj.get("Id").getAsInt());
                                notaRMA.setNota(notaRMAObj.get("Nota").getAsString());
                                notaRMA.setRMAId(notaRMAObj.get("RMAId").getAsInt());
                                if (notaRMAObj.get("ImagemNotaId") != null)
                                    notaRMA.setImagemNotaId(notaRMAObj.get("ImagemNotaId").getAsInt());
                                if (notaRMAObj.get("ImagemNota") != null)
                                    notaRMA.setImagemNota(notaRMAObj.get("ImagemNota").getAsString());
                                rmaList.add(notaRMA);
                            }
                        }

                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

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