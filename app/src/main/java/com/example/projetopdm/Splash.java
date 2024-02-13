package com.example.projetopdm;
import static com.example.projetopdm.LocalDataBase.FuncionarioSharedPreferences.getFuncionarioData;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.icu.lang.UCharacter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.Modelos.Funcionario;
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
    public void onBackPressed() {
        finishAffinity();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        scan_btn = (Button) findViewById(R.id.scan_btn);
        final Activity activity = this;
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    IntentIntegrator integrator = new IntentIntegrator(activity);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);

                    integrator.setBeepEnabled(false);
                    integrator.setBarcodeImageEnabled(true);
                    integrator.setOrientationLocked(true);

                    integrator.setCameraId(0);//traseira
                    integrator.initiateScan();
            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        ArrayList<String> guidList = new ArrayList<String>();
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                Funcionario x = getFuncionarioData(this);
                if (x.getGUID()!=null){
                    if (x.getGUID().equals(result.getContents())){
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        intent.putExtra("GUID", result.getContents());
                        startActivity(intent);
                    }
                    else if (isInternetAvailable()){
                        ChamarApi(result,guidList);
                    }else {
                        Toast.makeText(Splash.this, "Não é o ultimo utilizador vai presisar de internet para acessar", Toast.LENGTH_SHORT).show();
                    }
                } else if (isInternetAvailable()) {
                    ChamarApi(result,guidList);
                }else {
                    Toast.makeText(Splash.this, "Não é o ultimo utilizador vai presisar de internet para acessar", Toast.LENGTH_SHORT).show();
                }
            } else {
                alert("cancelado");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
    private void ChamarApi(IntentResult result,ArrayList<String> guidList){
        Log.e("Splash","nao é o ultimo utilizador vai presisar de net para acessar");
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
                        Toast.makeText(Splash.this, "QR inválido -> " + result.getContents(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(Splash.this, "Aconteceu algo errado ao tentar ler o QR", Toast.LENGTH_SHORT).show();
            }
        });
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
