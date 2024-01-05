package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.Modelos.RMA;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class testapis extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testapis);
        ArrayList<RMA> rmaList = new ArrayList<RMA>();
        if(isInternetAvailable()){
            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().GetRMASByFuncionario("095a76ab-7ee1-47da-a5cb-da8b09f1f887");
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                    if (responseObj.get("Success").getAsBoolean()) {
                        Toast.makeText(testapis.this, "Success", Toast.LENGTH_SHORT).show();
                        JsonArray rmaListObj = response.body().get("RMA").getAsJsonArray();
                        for (int i = 0; i < rmaListObj.size(); i++) {
                            JsonObject rmaObj = rmaListObj.get(i).getAsJsonObject();
                            RMA rma = new RMA();
                            rma.setId(rmaObj.get("Id").getAsInt());
                            rma.setRMA(rmaObj.get("RMA").getAsString());
                            rma.setDescricaoCliente(rmaObj.get("DescricaoCliente").getAsString());
                            if (rmaObj.get("DataAbertura")!=null) rma.setDataAbertura(rmaObj.get("DataAbertura").getAsString());
                            if (rmaObj.get("DataFecho")!=null) rma.setDataFecho(rmaObj.get("DataFecho").getAsString());
                            rma.setEstadoRMA(rmaObj.get("EstadoRMA").getAsString());
                            rma.setEstadoRMAId(rmaObj.get("EstadoRMAId").getAsInt());
                            rma.setFuncionarioId(rmaObj.get("FuncionarioId").getAsInt());
                            rmaList.add(rma);
                        }
                        ListView listView = findViewById(R.id.listRMA);
                        ArrayAdapter<RMA> adapter = new ArrayAdapter<RMA>(testapis.this, android.R.layout.simple_list_item_1, rmaList);
                        listView.setAdapter(adapter);


                    } else {
                        Toast.makeText(testapis.this, "Error", Toast.LENGTH_SHORT).show();
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