package com.example.projetopdm;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Splash extends AppCompatActivity {

    private Button scan_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        scan_btn = (Button) findViewById(R.id.scan_btn);
        final Activity activity = this;
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInternetAvailable()) {
                    IntentIntegrator integrator = new IntentIntegrator(activity);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                    integrator.setCameraId(0);//traseira
                    integrator.initiateScan();
                } else {
                    Toast.makeText(getApplicationContext(), "Sem conexão com a internet", Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        ArrayList<String> guidList = new ArrayList<String>();
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                if (isInternetAvailable()){
                    Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().GetAllGUID();
                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                            if(responseObj.get("Success").getAsBoolean()){
                                JsonArray guidListObj = response.body().get("GUID").getAsJsonArray();
                                for (int i = 0; i < guidListObj.size(); i++) {
                                        String GUID = guidListObj.get(i).getAsString();
                                        guidList.add(GUID);
                                    }
                                if(guidList.contains(result.getContents())) {
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    intent.putExtra("GUID", result.getContents());
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(Splash.this, "QR inválido", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(Splash.this, "Aconteceu algo errado ao tentar ler o QR", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                alert("cancelado");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void alert(String testeID) {
        Toast.makeText(getApplicationContext(), testeID, Toast.LENGTH_LONG).show();
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
