package com.example.projetopdm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.Manifest;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.Modelos.NotaRMA;
import com.example.projetopdm.Modelos.RMA;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Call;
import retrofit2.Callback;


public class Nota extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001; // Use any unique value
    private static final int IMAGE_CAPTURE_CODE = 1001;

    Notas binding;
    EditText titulo;
    EditText nota;
    ImageView imageView;
    Button img_btn;
    Button create_btn;
    Uri image_uri;
    NotaRMA notaRMA;
    Boolean creating=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nota);

        imageView = (ImageView) findViewById(R.id.imageView);
        img_btn=(Button) findViewById(R.id.img_btn);
        create_btn=(Button) findViewById(R.id.create_btn);
        titulo=(EditText) findViewById(R.id.Caixa_titulo);
        nota=(EditText) findViewById(R.id.Caixa_Texto);

        if (getIntent().getIntExtra("Id", 0) != 0) {
            notaRMA = new NotaRMA(getIntent().getIntExtra("Id", 0), getIntent().getStringExtra("Titulo"), getIntent().getStringExtra("DataCriacao"), getIntent().getStringExtra("Nota"), getIntent().getIntExtra("ImagemNotaId", 0), getIntent().getStringExtra("ImagemNota"), getIntent().getIntExtra("RMAId", 0));
            titulo.setText(notaRMA.getTitulo());
            nota.setText(notaRMA.getNota());
            creating=true;
        }
        img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ){
                                if (!Environment.isExternalStorageManager()) {
                                    String [] permission={Manifest.permission.CAMERA};
                                    requestPermissions(permission, CAMERA_PERMISSION_REQUEST_CODE);
                                    checkExternalStoragePermission();
                                }else {
                                    //ja tem as permissoes
                                }
                            }else {
                                openCamera();
                            }

                    }
            }
        });

        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (titulo.getText().toString().isEmpty() || nota.getText().toString().isEmpty()){
                    Toast.makeText(Nota.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            }else {
                    String request = "{"
                            + " \"Id\": \"" + notaRMA.getId() + "\", "
                            + " \"Titulo\": \"" + notaRMA.getTitulo() + "\", "
                            + " \"Nota\": \"" + notaRMA.getNota() + "\", "
                            + " \"RMAId\": \"" + notaRMA.getRMAId() + "\", "
                            + " \"IdImagem\": \"" + notaRMA.getImagemNotaId()
                            + " \"Imagem\": \"" + notaRMA.getImagemNota() + "\" }";
                    JsonObject body = new JsonParser().parse(request).getAsJsonObject();
                    Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().CreateOrUpdateNotaRMA(body);

                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                            JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                            if (responseObj.get("Success").getAsBoolean()) {
                                Toast.makeText(getApplicationContext(), "Nota criada com sucesso", Toast.LENGTH_LONG).show();
                                binding.finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Erro ao criar nota", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Erro ao criar nota", Toast.LENGTH_LONG).show();
                        }
                    });
                }
        };

    });
    }



    private void openCamera(){
        ContentValues values= new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"testes");
        values.put(MediaStore.Images.Media.DESCRIPTION,"APP");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE);
    }
    private void checkExternalStoragePermission() {
        // Check for external storage management permission on Android 11 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            // Request external storage management permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        } else {
            // External storage management permission is granted or not needed
            Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();

        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // CAMERA permission granted, now check for external storage management permission
                checkExternalStoragePermission();
            } else {
                // Handle the case where CAMERA permission is denied
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            imageView.setImageURI(image_uri);
            imageView.setVisibility(View.VISIBLE);

        }
    }
}