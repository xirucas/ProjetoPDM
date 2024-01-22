package com.example.projetopdm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.Manifest;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
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
    ImageView imageViewPopup;
    LinearLayout popup;
    Button img_btn;
    Button create_btn;
    Button close_btn;
    Uri image_uri;
    NotaRMA notaRMA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nota);

        imageView = (ImageView) findViewById(R.id.imageView);
        img_btn=(Button) findViewById(R.id.img_btn);
        create_btn=(Button) findViewById(R.id.create_btn);
        titulo=(EditText) findViewById(R.id.Caixa_titulo);
        nota=(EditText) findViewById(R.id.Caixa_Texto);
        imageViewPopup = findViewById(R.id.imageViewPopup);
        close_btn = findViewById(R.id.closePopup);
        popup = findViewById(R.id.popupImage);
        popup.setVisibility(View.INVISIBLE);
        if (isInternetAvailable()) {
            if (getIntent().getIntExtra("NotaId", 0) != 0) {
                create_btn.setText("Atualizar Nota");
                Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().GetNotaRMAById(getIntent().getIntExtra("NotaId", 0));

                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                        if (responseObj.get("Success").getAsBoolean()) {
                            JsonObject notaObj = response.body().get("RMANota").getAsJsonObject();
                            notaRMA = new NotaRMA();
                            notaRMA.setId(notaObj.get("Id").getAsInt());
                            notaRMA.setTitulo(notaObj.get("Titulo").getAsString());
                            notaRMA.setDataCriacao(notaObj.get("DataCriacao").getAsString());
                            notaRMA.setNota(notaObj.get("Nota").getAsString());
                            notaRMA.setRMAId(notaObj.get("RMAId").getAsInt());
                            titulo.setText(notaRMA.getTitulo());
                            nota.setText(notaRMA.getNota());
                            if (notaObj.has("ImagemNota")){
                                notaRMA.setImagemNotaId(notaObj.get("ImagemNotaId").getAsInt());
                                notaRMA.setImagemNota(notaObj.get("ImagemNota").getAsString());
                                Bitmap imagem = StringToBitMap(notaRMA.getImagemNota());
                                imageView.setImageBitmap(imagem);
                                imageViewPopup.setImageBitmap(imagem);

                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Erro ao carregar nota", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "Erro ao carregar nota", Toast.LENGTH_LONG).show();
                    }
                });

           /* if (binding.notaRMA.getImagemNota() != null) {
                notaRMA.setImagemNota(getIntent().getStringExtra("ImagemNota"));
                notaRMA.setImagemNotaId(getIntent().getIntExtra("ImagemNotaId", 0));
                Bitmap imagem = StringToBitMap(notaRMA.getImagemNota());
                imageView.setImageBitmap(imagem);
            }*/
            } else {
                create_btn.setText("Criar Nota");
                imageView.setImageURI(null);
                notaRMA = new NotaRMA();
            }
        }else {
            Toast.makeText(getApplicationContext(), "Sem conexão com a internet", Toast.LENGTH_LONG).show();
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageView.getDrawable() != null) {
                    popup.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getApplicationContext(), "Sem imagem para mostrar", Toast.LENGTH_LONG).show();

                }
            }
        });



        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.setVisibility(View.INVISIBLE);
            }
        });

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
                    String request = "";
                    if (getIntent().getIntExtra("NotaId", 0) != 0) {
                        notaRMA.setNota(nota.getText().toString());
                        notaRMA.setTitulo(titulo.getText().toString());
                        if (image_uri != null) {
                            Bitmap imagem = uriToBitmap(getApplicationContext(), image_uri);
                            String imagemString = bitmapToString(imagem);
                            notaRMA.setImagemNota(imagemString);
                        }
                         request = "{"
                                + " \"Id\": \"" + notaRMA.getId() + "\", "
                                + " \"Titulo\": \"" + notaRMA.getTitulo() + "\", "
                                + " \"Nota\": \"" + notaRMA.getNota() + "\", "
                                + " \"RMAId\": \"" + getIntent().getIntExtra("RMAId",0) + "\", "
                                + " \"IdImagem\": \"" + notaRMA.getImagemNotaId() + "\", "
                                + " \"Imagem\": \"" + notaRMA.getImagemNota() + "\" }";
                    }
                    else {
                        if (image_uri != null) {
                            Bitmap imagem = uriToBitmap(getApplicationContext(), image_uri);
                            String imagemString = bitmapToString(imagem);
                            notaRMA.setImagemNota(imagemString);
                            request = "{"
                                    + " \"Id\": \"" + 0 + "\", "
                                    + " \"Titulo\": \"" + titulo.getText().toString() + "\", "
                                    + " \"Nota\": \"" + nota.getText().toString() + "\", "
                                    + " \"RMAId\": \"" + getIntent().getIntExtra("RMAId", 0) + "\", "
                                    + " \"IdImagem\": \"" + 0 + "\", "
                                    + " \"Imagem\": \"" + imagemString + "\" }";
                        } else {
                            request = "{"
                                    + " \"Id\": \"" + 0 + "\", "
                                    + " \"Titulo\": \"" + titulo.getText().toString() + "\", "
                                    + " \"Nota\": \"" + nota.getText().toString() + "\", "
                                    + " \"RMAId\": \"" + getIntent().getIntExtra("RMAId", 0) + "\", "
                                    + " \"IdImagem\": \"" + 0 + "\", "
                                    + " \"Imagem\": \"" + "" + "\" }";
                        }
                    }

                    JsonObject body = new JsonParser().parse(request).getAsJsonObject();
                    Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().CreateOrUpdateNotaRMA(body);

                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                            JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                            if (responseObj.get("Success").getAsBoolean()) {
                                Toast.makeText(getApplicationContext(), "Nota criada com sucesso", Toast.LENGTH_LONG).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("AtivarAPI", true);
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
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

    private void openPopup(){
        popup.setVisibility(View.VISIBLE);
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
            imageViewPopup.setImageURI(image_uri);
            imageView.setVisibility(View.VISIBLE);
        }
    }



    public Bitmap uriToBitmap(Context context, Uri uri) {
        Bitmap bitmap = null;
        try {
            // Abre um InputStream a partir da URI
            InputStream imageStream = context.getContentResolver().openInputStream(uri);

            // Obtém a rotação da imagem a partir das informações EXIF
            int rotation = getRotationFromExif(context, uri);

            // Converte o InputStream em um Bitmap considerando a rotação
            bitmap = BitmapFactory.decodeStream(imageStream);
            if (rotation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private int getRotationFromExif(Context context, Uri uri) {
        int rotation = 0;
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            ExifInterface exifInterface = new ExifInterface(input);

            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rotation;
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