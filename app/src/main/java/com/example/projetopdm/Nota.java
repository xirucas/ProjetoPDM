package com.example.projetopdm;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.exifinterface.media.ExifInterface;
import androidx.room.Room;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.LocalDataBase.AppDatabase;
import com.example.projetopdm.LocalDataBase.DAOs.NotaRMADao;
import com.example.projetopdm.LocalDataBase.Entity.NotaRMAEntity;
import com.example.projetopdm.Modelos.NotaRMA;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    RelativeLayout popup;
    Button img_btn;
    Button create_btn;
    Button close_btn;
    Button backButton;
    TextView dataNota;
    Uri image_uri;
    NotaRMA notaRMA;
    NotaRMADao notaRMADao;
    private boolean isEditMode = false;
    int estadoRMA;
    private Context contextPrincipal;

    Uri uri;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nota);
        contextPrincipal=this;
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setVisibility(View.GONE);
        img_btn = (Button) findViewById(R.id.img_btn);
        create_btn = (Button) findViewById(R.id.create_btn);
        backButton = (Button) findViewById(R.id.back_button);
        titulo = (EditText) findViewById(R.id.Caixa_titulo);
        nota = (EditText) findViewById(R.id.Caixa_Texto);
        dataNota = (TextView) findViewById(R.id.dataCriacao);
        imageViewPopup = findViewById(R.id.imageViewPopup);
        close_btn = findViewById(R.id.closePopup);
        popup = findViewById(R.id.popupImage);
        popup.setVisibility(View.INVISIBLE);
        Intent intent= getIntent();

        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "BaseDeDadosLocal").fallbackToDestructiveMigration().allowMainThreadQueries().build();

        notaRMADao = db.notaRMADao();
        notaRMA = new NotaRMA();

        estadoRMA = getIntent().getIntExtra("estadoRMA", 0);
        Log.i("EstadoRMA", String.valueOf(estadoRMA));
        Log.i("cu", " fe "+intent.getIntExtra("NotaId",0));
            if (getIntent().getIntExtra("NotaId", 0) != 0) {
                titulo.setEnabled(false);
                nota.setEnabled(false);
                img_btn.setEnabled(false);
                create_btn.setText("Editar Nota");
                create_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.edit_icon, 0, 0, 0);
                NotaRMAEntity x ;


                x = notaRMADao.getNotaById(intent.getIntExtra("NotaId",0));
                Log.i("cu2"," yh isso "+x.getId());

                notaRMA= x.toNotaRMA();
                Log.e("notas"," img + "+notaRMA.getImagemNota());
                if (notaRMA.getImagemNota() != null && !notaRMA.getImagemNota().isEmpty()) {
                    uri = Uri.parse(notaRMA.getImagemNota());
                    imageView.setImageURI(uri);
                    imageView.setVisibility(View.VISIBLE);
                }

                titulo.setText(notaRMA.getTitulo());
                nota.setText(notaRMA.getNota());

                if (estadoRMA == 2 || estadoRMA == 3) {
                    create_btn.setVisibility(View.VISIBLE);
                    create_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            isEditMode = !isEditMode;

                            if (isEditMode) {
                                titulo.setEnabled(true);
                                nota.setEnabled(true);
                                img_btn.setEnabled(true);

                                create_btn.setText("Guardar Alterações");
                                create_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.guardar_icon, 0, 0, 0);
                            } else {
                                titulo.setEnabled(false);
                                nota.setEnabled(false);
                                img_btn.setEnabled(false);

                                create_btn.setText("Editar Nota");
                                create_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.edit_icon, 0, 0, 0);

                                saveChanges();
                            }
                        }
                    });
                }else {
                    create_btn.setVisibility(View.INVISIBLE);
                }




            } else {
                create_btn.setText("Guardar Nota");
                create_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.guardar_icon, 0, 0, 0);
                imageView.setImageURI(null);
                notaRMA = new NotaRMA();
                titulo.setFocusableInTouchMode(true);
                nota.setFocusableInTouchMode(true);
                dataNota.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);


                create_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveChanges();
                    }
                });

            }



        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageView.getDrawable() != null ) {
                    if (image_uri != null){
                        imageViewPopup.setImageURI(image_uri);
                    }
                    popup.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), "Sem imagem para mostrar", Toast.LENGTH_LONG).show();

                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        if (!Environment.isExternalStorageManager()) {
                            String[] permission = {
                                    Manifest.permission.CAMERA
                            };
                            requestPermissions(permission, CAMERA_PERMISSION_REQUEST_CODE);
                            checkExternalStoragePermission();
                        } else {
                            //ja tem as permissoes
                        }
                    } else {
                        openCamera();
                    }

                }
            }
        });

    }

    private void saveChanges() {



        // Check if both title and note are not empty
        if (titulo.getText().toString().isEmpty() || nota.getText().toString().isEmpty()) {

            Toast.makeText(Nota.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return; // Exit the method if validation fails
        }


        if (isInternetAvailable()) {
            String request = "";
            if (getIntent().getIntExtra("NotaId", 0) != 0) {
                //Editar nota existente
                notaRMA.setRMAId(getIntent().getIntExtra("RMAId", 0));
                notaRMA.setNota(nota.getText().toString());
                notaRMA.setTitulo(titulo.getText().toString());

                if (image_uri != null) {
                    Bitmap imagem = uriToBitmap(getApplicationContext(), image_uri);
                    String imagemString = bitmapToString(imagem);
                    notaRMA.setImagemNota(imagemString);
                } else if (notaRMA.getImagemNota() != null) {
                    Bitmap imagem = uriToBitmap(getApplicationContext(), uri);
                    String imagemString = bitmapToString(imagem);
                    notaRMA.setImagemNota(imagemString);
                }

                request = "{" +
                        " \"Id\": \"" + notaRMA.getId() + "\", " +
                        " \"Titulo\": \"" + notaRMA.getTitulo() + "\", " +
                        " \"Nota\": \"" + notaRMA.getNota() + "\", " +
                        " \"RMAId\": \"" + getIntent().getIntExtra("RMAId", 0) + "\", " +
                        " \"IdImagem\": \"" + notaRMA.getImagemNotaId() + "\", " +
                        " \"Imagem\": \"" + notaRMA.getImagemNota() + "\" }";
            } else {
                //Criação nova nota
                if (image_uri != null) {
                    Bitmap imagem = uriToBitmap(getApplicationContext(), image_uri);
                    String imagemString = bitmapToString(imagem);
                    notaRMA.setImagemNota(imagemString);

                    request = "{" +
                            " \"Id\": \"" + 0 + "\", " +
                            " \"Titulo\": \"" + titulo.getText().toString() + "\", " +
                            " \"Nota\": \"" + nota.getText().toString() + "\", " +
                            " \"RMAId\": \"" + getIntent().getIntExtra("RMAId", 0) + "\", " +
                            " \"IdImagem\": \"" + 0 + "\", " +
                            " \"Imagem\": \"" + imagemString + "\" }";
                }else if (notaRMA.getImagemNota() != null) {
                    Bitmap imagem = uriToBitmap(getApplicationContext(), uri);
                    String imagemString = bitmapToString(imagem);
                    notaRMA.setImagemNota(imagemString);
                    request = "{" +
                            " \"Id\": \"" + 0 + "\", " +
                            " \"Titulo\": \"" + titulo.getText().toString() + "\", " +
                            " \"Nota\": \"" + nota.getText().toString() + "\", " +
                            " \"RMAId\": \"" + getIntent().getIntExtra("RMAId", 0) + "\", " +
                            " \"IdImagem\": \"" + 0 + "\", " +
                            " \"Imagem\": \"" + imagemString + "\" }";
                } else {
                    request = "{" +
                            " \"Id\": \"" + 0 + "\", " +
                            " \"Titulo\": \"" + titulo.getText().toString() + "\", " +
                            " \"Nota\": \"" + nota.getText().toString() + "\", " +
                            " \"RMAId\": \"" + getIntent().getIntExtra("RMAId", 0) + "\", " +
                            " \"IdImagem\": \"" + 0 + "\", " +
                            " \"Imagem\": \"" + "" + "\" }";
                }
            }

            JsonObject body = new JsonParser().parse(request).getAsJsonObject();
            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().CreateOrUpdateNotaRMA(body);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();

                    if (responseObj.get("Success").getAsBoolean()) {
                        if (getIntent().getIntExtra("NotaId", 0) != 0) {
                            Toast.makeText(getApplicationContext(), "Nota alterada com sucesso", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Nota criada com sucesso", Toast.LENGTH_LONG).show();
                        }

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("AtivarAPI", true);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Erro ao criar/editar nota", Toast.LENGTH_LONG).show();

                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Erro ao criar/editar nota", Toast.LENGTH_LONG).show();

                }
            });
        } else if (!isInternetAvailable()) {
            notaRMA.setRMAId(getIntent().getIntExtra("RMAId", 0));
            notaRMA.setNota(nota.getText().toString());
            notaRMA.setTitulo(titulo.getText().toString());

            if (notaRMA.getId()==0){
                notaRMA.setId(notaRMADao.getAllNotasRMA().size()-1);//id temporario
                LocalDateTime now = LocalDateTime.now();
                // Formata a data e hora
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                String formattedDateTime = now.format(formatter);
                notaRMA.setDataCriacao(formattedDateTime);
            }

            NotaRMAEntity x = notaRMA.toNotaRMAEntity();
            if (image_uri!=null){
                x.setImagemNota(image_uri.toString());
            }
            if (x.getId()==notaRMADao.getAllNotasRMA().size()-1){//ou seja id temporario
                x.setOffSync("novo");
            }else {
                x.setOffSync("modificado");
            }
            Log.i("Notas","teste 5 "+x.getNota()+" "+ x.getDataCriacao()+" "+x.getTitulo());
            notaRMADao.insert(x);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("AtivarAPI", true);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }


    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "testes");
        values.put(MediaStore.Images.Media.DESCRIPTION, "APP");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
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

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    private boolean isInternetAvailable() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else
            connected = false;

        return connected;
    }



}