package com.example.projetopdm.LocalDataBase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.exifinterface.media.ExifInterface;
import androidx.room.Room;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.LocalDataBase.DAOs.NotaRMADao;
import com.example.projetopdm.LocalDataBase.DAOs.RMADao;
import com.example.projetopdm.LocalDataBase.Entity.NotaRMAEntity;
import com.example.projetopdm.LocalDataBase.Entity.RMAEntity;
import com.example.projetopdm.Modelos.NotaRMA;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;




public class UpdateBD {
    NotaRMADao notaRMADao;
    RMADao rmaDao;
    Context contextoprincipal;
    AppDatabase db;
    public Boolean canReturn=false;

    public interface UpdateListener {
        void onUpdateComplete();
    }

    public UpdateBD(Context x){
        this.contextoprincipal=x;
        db = Room.databaseBuilder(contextoprincipal, AppDatabase.class, "BaseDeDadosLocal").fallbackToDestructiveMigration().allowMainThreadQueries().build();

        notaRMADao = db.notaRMADao();
        rmaDao = db.rmaDao();
    }


    public boolean mudancas(int RMAId) {

        List<NotaRMAEntity> notas = notaRMADao.getALLNotasByRMAId(RMAId);

        if(notas!=null){
            for (NotaRMAEntity x:notas) {
                if(x.getOffSync()!=null){
                    if (x.getOffSync().equals("modificado")||x.getOffSync().equals("novo")||x.getOffSync().equals("apagado") || x.getOffSync().equals("novoApagado")){
                        return true;
                    }
                }

            }
        }

        return false;
    }

    public void updateBaseDeDados(int RMAid, UpdateListener listener){



        ArrayList<NotaRMA> novos= new ArrayList<>();
        ArrayList<NotaRMA> modificados = new ArrayList<>();
        ArrayList<NotaRMA> apagados = new ArrayList<>();

        List<NotaRMAEntity> notas = notaRMADao.getALLNotasByRMAId(RMAid);

        for (NotaRMAEntity x : notas ) {
            if (x.getOffSync()!=null){
                if (x.getOffSync().equals("novo")){
                    novos.add(x.toNotaRMA());
                } else if (x.getOffSync().equals("modificado")) {
                    modificados.add(x.toNotaRMA());
                } else if (x.getOffSync().equals("apagado")) {
                    apagados.add(x.toNotaRMA());
                } else if(x.getOffSync().equals("novoApagado")){
                    notaRMADao.deleteById(x.getId());
                }
            }
        }
        if (novos.size()!=0){
            String requestArray = "[";
            int count=0;
            for (NotaRMA x:novos) {
                String request ="";

                if (x.getImagemNota() != null) {
                    Uri uri = Uri.parse(x.getImagemNota());
                    Bitmap imagem = uriToBitmap(contextoprincipal, uri);
                    String imagemString = bitmapToString(imagem);
                    x.setImagemNota(imagemString);
                    request = "{"
                            + " \"Id\": \"" + 0 + "\", "
                            + " \"Titulo\": \"" + x.getTitulo() + "\", "
                            + " \"Nota\": \"" + x.getNota() + "\", "
                            + " \"RMAId\": \"" + x.getRMAId() + "\", "
                            + " \"IdImagem\": \"" + 0 + "\", "
                            + " \"Imagem\": \"" + imagemString + "\" }";
                } else {
                    request = "{"
                            + " \"Id\": \"" + 0 + "\", "
                            + " \"Titulo\": \"" + x.getTitulo() + "\", "
                            + " \"Nota\": \"" + x.getNota() + "\", "
                            + " \"RMAId\": \"" + x.getRMAId() + "\", "
                            + " \"IdImagem\": \"" + 0 + "\", "
                            + " \"Imagem\": \"" + "" + "\" }";
                } if(count>0){
                    requestArray += "," + request;
                }else {
                    requestArray += request;
                }
                count +=1;
            }
            requestArray += "]";//passar os novos da local para a online
            JsonArray body = new JsonParser().parse(requestArray).getAsJsonArray();



            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().CreateOrUpdateALL_NotasRMA(body);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();

                    Log.d("Notas", "Chamada para a API GetRMAById realizada com sucesso.");


                    if (responseObj.get("Success").getAsBoolean()) {


                        if (response.body().has("ListaNotaRMAId")) {

                            JsonArray NotasRMAId = response.body().get("ListaNotaRMAId").getAsJsonArray();


                            for (int i = 0; i < NotasRMAId.size(); i++) {
                                int idNota = NotasRMAId.get(i).getAsInt();

                                    NotaRMAEntity nota = notaRMADao.getNotaById(novos.get(i).getId());
                                    int idantigo= nota.getId();
                                    nota.setId(idNota);
                                    nota.setOffSync(null);


                                    notaRMADao.insert(nota);

                                    notaRMADao.deleteById(idantigo);


                            }
                        }
                    }else {
                        Toast.makeText(contextoprincipal, "Erro no update de notas novas", Toast.LENGTH_LONG).show();
                    }
                    if (modificados.isEmpty() && apagados.isEmpty()){
                        listener.onUpdateComplete();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(contextoprincipal, "Erro no update de notas novas", Toast.LENGTH_LONG).show();
                }
            });
        }

        if (modificados.size()!=0){
            String requestArray = "[";
            int count=0;
            for (NotaRMA x:modificados){
                String request ="";

                if (x.getImagemNota() != null) {
                    Uri uri = Uri.parse(x.getImagemNota());
                    Bitmap imagem = uriToBitmap(contextoprincipal, uri);
                    String imagemString = bitmapToString(imagem);
                    x.setImagemNota(imagemString);
                }
                request = "{"
                        + " \"Id\": \"" + x.getId() + "\", "
                        + " \"Titulo\": \"" + x.getTitulo() + "\", "
                        + " \"Nota\": \"" + x.getNota() + "\", "
                        + " \"RMAId\": \"" + x.getRMAId() + "\", "
                        + " \"IdImagem\": \"" + x.getImagemNotaId() + "\", "
                        + " \"Imagem\": \"" + x.getImagemNota() + "\" }";

                if(count>0){
                    requestArray += "," + request;
                }else {
                    requestArray += request;
                }
                count +=1;
            }
            requestArray += "]";//passar os novos da local para a online

            JsonArray body = new JsonParser().parse(requestArray).getAsJsonArray();
            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().CreateOrUpdateALL_NotasRMA(body);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();

                    Log.d("Notas", "Chamada para a API GetRMAById realizada com sucesso.");


                    if (responseObj.get("Success").getAsBoolean()) {

                        }else {
                            Toast.makeText(contextoprincipal, "Erro no update de notas modificadas", Toast.LENGTH_LONG).show();
                        }
                    if (apagados.isEmpty()){
                        listener.onUpdateComplete();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(contextoprincipal, "Erro no update de notas modificadas", Toast.LENGTH_LONG).show();
                }
            });
        }

        if (apagados.size()!=0){
            String requestArray = "[";
            int count=0;
            for (NotaRMA x:apagados){
                String request = String.valueOf(x.getId());
                if(count>0){
                    requestArray += "," + request;
                }else {
                    requestArray += request;
                }
                count +=1;
            }
            requestArray += "]";//passar os novos da local para a online

            JsonArray body = new JsonParser().parse(requestArray).getAsJsonArray();
            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().DeleteALLNotaRMAByListRMAId(body);

            call.enqueue(new Callback<JsonObject>(){

                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                    if (responseObj.get("Success").getAsBoolean()) {


                        if (response.body().has("ListaNotaRMAId")) {

                            JsonArray NotasRMAId = response.body().get("ListaNotaRMAId").getAsJsonArray();


                            for (int i = 0; i < NotasRMAId.size(); i++) {
                                int idNota = NotasRMAId.get(i).getAsInt();

                                notaRMADao.deleteById(idNota);

                            }
                        }
                        Toast.makeText(contextoprincipal, "Notas apagadas com sucesso", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(contextoprincipal, "Erro ao apagar notas", Toast.LENGTH_LONG).show();
                    }
                    listener.onUpdateComplete();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(contextoprincipal, "Erro ao apagar notas", Toast.LENGTH_LONG).show();
                }
            });

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

}
