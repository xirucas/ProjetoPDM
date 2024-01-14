package com.example.projetopdm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.Manifest;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.LocalDataBase.AppDatabase;
import com.example.projetopdm.LocalDataBase.DAOs.NotaRMADao;
import com.example.projetopdm.LocalDataBase.Entity.NotaRMAEntity;
import com.example.projetopdm.LocalDataBase.Repositorys.NotaRMARepository;
import com.example.projetopdm.Modelos.NotaRMA;
import com.example.projetopdm.Modelos.RMA;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
    NotaRMA notaRMA2;
    NotaRMADao notaRMADao;
    NotaRMARepository notaRMARepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nota);
        Intent intent= getIntent();

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "BaseDeDadosLocal").allowMainThreadQueries().build();
        notaRMADao = db.notaRMADao();
        notaRMARepository = new NotaRMARepository(notaRMADao, RetrofitClient.getInstance().getMyApi(), getApplicationContext(), intent.getIntExtra("RMAId",0));



        imageView = (ImageView) findViewById(R.id.imageView);
        img_btn=(Button) findViewById(R.id.img_btn);
        create_btn=(Button) findViewById(R.id.create_btn);
        titulo=(EditText) findViewById(R.id.Caixa_titulo);
        nota=(EditText) findViewById(R.id.Caixa_Texto);

        if (intent != null && intent.hasExtra("NotaId")) {
            int notaIdStr = intent.getIntExtra("NotaId",0);
            Log.e("Notas", "NotaId recebido: " + notaIdStr);
        }

        if (intent.getStringExtra("Update").equals("Update")){
            create_btn.setText("Atualizar");
            notaRMA2= new NotaRMA(intent.getIntExtra("NotaId",0),
                    intent.getStringExtra("NotaTitulo"),
                    intent.getStringExtra("Data"),
                    intent.getStringExtra("Descricao"),
                    intent.getIntExtra("ImagemID",0) ,
                    intent.getStringExtra("Imagem"),
                    intent.getIntExtra("RMAId",0));


            if (notaRMA2.getImagemNota() != null && !notaRMA2.getImagemNota().isEmpty()) {
                Uri uri = Uri.parse(notaRMA2.getImagemNota());
                imageView.setImageURI(uri);
            }

            titulo.setText(notaRMA2.getTitulo());
            nota.setText(notaRMA2.getNota());
        }


        if (intent.getStringExtra("Update").equals("Novo")){
            create_btn.setText("Adicionar Nota");
            notaRMA2=new NotaRMA();
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
                if (titulo.getText().toString().isEmpty() || nota.getText().toString().isEmpty()) {
                    Toast.makeText(Nota.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                } else {
                    NotaRMAEntity notaRMAEntity = createNotaFromUI();

                    // Crie uma instância do repositório
                    NotaRMARepository notaRMARepository = new NotaRMARepository(notaRMADao, RetrofitClient.getInstance().getMyApi(), getApplicationContext(), notaRMA2.getRMAId());

                    // Crie ou atualize a nota
                    notaRMARepository.createOrUpdateNota(notaRMAEntity);

                    finish();
                }
        };

    });
    }
    private NotaRMAEntity createNotaFromUI() {
        NotaRMAEntity notaRMAEntity = new NotaRMAEntity();
        // Supondo que você tenha getters e setters no NotaRMAEntity
        notaRMAEntity.setTitulo(titulo.getText().toString());
        notaRMAEntity.setNota(nota.getText().toString());
        notaRMAEntity.setImagemNota(image_uri.toString());
        notaRMAEntity.setImagemNotaId(notaRMA2.getImagemNotaId());
        notaRMAEntity.setDataCriacao(notaRMA2.getDataCriacao());
        notaRMAEntity.setRMAId(notaRMA2.getRMAId());
        // Defina outros campos necessários de acordo com os dados da UI

        // Se estiver atualizando uma nota existente, defina o ID
        if (notaRMA2 != null) {
            notaRMAEntity.setId(notaRMA2.getId());
        }

        return notaRMAEntity;
    }



    private void openCamera(){
        ContentValues values= new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"testes");
        values.put(MediaStore.Images.Media.DESCRIPTION,"APP");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE);
        imageView.setImageURI(image_uri);
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



    public Bitmap uriToBitmap(Context context, Uri uri) {
        Bitmap bitmap = null;
        try {
            // Abre um InputStream a partir da URI
            InputStream imageStream = context.getContentResolver().openInputStream(uri);

            // Converte o InputStream em um Bitmap
            bitmap = BitmapFactory.decodeStream(imageStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Comprime a imagem em um formato específico (PNG, JPEG, etc.)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Converte os bytes para Base64
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encodedImage;
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