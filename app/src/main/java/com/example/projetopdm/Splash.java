package com.example.projetopdm;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.projetopdm.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Splash extends AppCompatActivity {

    private Button scan_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        scan_btn=(Button) findViewById(R.id.scan_btn);
        final Activity activity=this;
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setCameraId(0);//traseira
                integrator.initiateScan();
            }
        });


    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result= IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (result!=null){
            if (result.getContents()!=null){
                alert(result.getContents());
                String testeid="194a2d1c-b8bc-41f1-9135-9a643d1390b3";
                if (result.getContents().equals(testeid)){

                    Intent intent = new Intent(this, Login.class);
                    startActivity(intent);
                }


            }else {
                alert("cancelado");
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void alert (String testeID){
        Toast.makeText(getApplicationContext(),testeID,Toast.LENGTH_LONG).show();
    }





}