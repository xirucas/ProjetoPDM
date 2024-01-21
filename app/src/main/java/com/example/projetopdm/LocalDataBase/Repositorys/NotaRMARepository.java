package com.example.projetopdm.LocalDataBase.Repositorys;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.collection.ArraySet;

import com.example.projetopdm.BackEnd.Api;
import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.ListaAdapterRMADetails;
import com.example.projetopdm.LocalDataBase.DAOs.NotaRMADao;
import com.example.projetopdm.LocalDataBase.DAOs.RMADao;
import com.example.projetopdm.LocalDataBase.Entity.NotaRMAEntity;
import com.example.projetopdm.LocalDataBase.Entity.RMAEntity;
import com.example.projetopdm.Modelos.NotaRMA;
import com.example.projetopdm.Modelos.RMA;
import com.example.projetopdm.Nota;
import com.example.projetopdm.Notas;
import com.example.projetopdm.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class NotaRMARepository {

    private NotaRMADao NotaRMADao;
    private Api myApi;
    private Context context;
    private int RMAid;
    private int currentPage = 0; // Inicialmente começa do zero
    private static final int ITEMS_PER_PAGE = 5; // Número de itens por página


    RMAEntity rma;
    NotaRMAEntity notaRMA;

    public NotaRMARepository(NotaRMADao NotaRMADao, Api myApi, Context context, int RMAid) {
        this.NotaRMADao = NotaRMADao;
        this.myApi = myApi;
        this.context = context;
        this.RMAid=RMAid;
    }

    private class InsertNotaAsyncTask extends AsyncTask<Void, Void, Void> {
        private NotaRMADao asyncTaskDao;
        private NotaRMAEntity notaRMAEntity;

        InsertNotaAsyncTask(NotaRMADao dao, NotaRMAEntity nota) {
            asyncTaskDao = dao;
            notaRMAEntity = nota;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            asyncTaskDao.insert(notaRMAEntity);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            sendNotaToServer(notaRMAEntity);
        }
    }
    private class UpdateNotaAsyncTask extends AsyncTask<Void, Void, Void> {
        private NotaRMADao asyncTaskDao;
        private NotaRMAEntity notaRMAEntity;

        UpdateNotaAsyncTask(NotaRMADao dao, NotaRMAEntity nota) {
            asyncTaskDao = dao;
            notaRMAEntity = nota;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            asyncTaskDao.update(notaRMAEntity);
            return null;
        }
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            sendNotaToServer(notaRMAEntity);
        }
    }

    public void sincronizarNotasRMAs(int RMAId) {

        // Lógica para verificar a conectividade de rede
        if (isInternetAvailable()) {

            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().GetRMAById(RMAId);


            call.enqueue(new Callback<JsonObject>(){
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                    Log.d("Notas", "Chamada para a API GetRMAById realizada com sucesso.");

                    if (responseObj.get("Success").getAsBoolean()){
                        Log.d("Notas", "Dados do RMA obtidos da API com sucesso.");
                        JsonObject rmaObj = response.body().get("RMA").getAsJsonObject();
                        RMA rma =new RMA();

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
                            Log.d("Notas", "Notas RMA obtidas da API com sucesso.");
                            List<NotaRMAEntity>rmaList = new ArrayList<>();
                            if (NotasRMA.get(0).getAsJsonObject().get("Id").getAsInt() != 0) {
                                for (int i = 0; i < NotasRMA.size(); i++) {
                                    JsonObject notaRMAObj = NotasRMA.get(i).getAsJsonObject();
                                    notaRMA = new NotaRMAEntity();
                                    notaRMA.setId(notaRMAObj.get("Id").getAsInt());
                                    notaRMA.setTitulo(notaRMAObj.get("Titulo").getAsString());
                                    notaRMA.setDataCriacao(notaRMAObj.get("DataCriacao").getAsString());
                                    notaRMA.setNota(notaRMAObj.get("Nota").getAsString());
                                    notaRMA.setRMAId(notaRMAObj.get("RMAId").getAsInt());
                                    if (notaRMAObj.get("ImagemNotaId") != null)
                                        notaRMA.setImagemNotaId(notaRMAObj.get("ImagemNotaId").getAsInt());
                                    if (notaRMAObj.get("ImagemNota") != null){
                                        Log.i("Notas","Imagem " + notaRMAObj.get("ImagemNota").getAsString());
                                        notaRMA.setImagemNota(notaRMAObj.get("ImagemNota").getAsString());
                                        Uri uriLocal = saveBitmapToFile(context, notaRMA.getImagemNota(), notaRMA.getId() + " -> " + notaRMA.getNota());
                                        notaRMA.setImagemNota(uriLocal.toString());
                                    }


                                    rmaList.add(notaRMA);
                                    Log.i("Notas",notaRMA.getTitulo()+" "+notaRMA.getImagemNota());
                                }
                                NotaRMADao.insertAllNotas(rmaList);
                            }
                        }


                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("Notas", "Sem conectividade de rede. Não foi possível sincronizar os dados.");
                }
            });





        }else {
            //nah net
        }

    }

    public List<NotaRMAEntity> getNotasRMAsFromLocal() {

            List<NotaRMAEntity> notaRmaEntities = NotaRMADao.getNotasByRMAIdWithLimit(this.RMAid);
            return notaRmaEntities;
    }


    private boolean isInternetAvailable(){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            connected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return connected;
    }

    public static Uri saveBitmapToFile(Context context, String bitmapAsString, String fileName) {
        if (bitmapAsString == null || bitmapAsString.isEmpty()) {
            Log.e("NotaRMARepository", "A string do bitmap é nula ou vazia");
            return null;
        }

        try {
            byte[] bitmapBytes = android.util.Base64.decode(bitmapAsString, android.util.Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

            if (bitmap != null) {
                File file = new File(context.getFilesDir(), fileName);
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                return Uri.fromFile(file);
            } else {
                Log.e("NotaRMARepository", "Falha na decodificação do bitmap");
                return null;
            }
        } catch (IOException e) {
            Log.e("NotaRMARepository", "Erro ao salvar o arquivo", e);
            return null;
        }
    }

    private void sendNotaToServer(NotaRMAEntity notaRMAEntity) {
        // Enviar a nota para o servidor via API usando Retrofit
        // Crie um JsonObject com os dados da notaRMAEntity e envie-o para a API
        // Exemplo:

        String ImgBitmap = convertUriStringToBitmapString(context.getApplicationContext(), notaRMAEntity.getImagemNota());

        JsonObject body = new JsonObject();
        body.addProperty("Id", notaRMAEntity.getId());
        body.addProperty("Titulo", notaRMAEntity.getTitulo());
        body.addProperty("DataCriacao", notaRMAEntity.getDataCriacao());
        body.addProperty("Nota", notaRMAEntity.getNota());
        body.addProperty("ImagemNotaId", notaRMAEntity.getImagemNotaId());
        body.addProperty("ImagemNota", ImgBitmap);
        body.addProperty("RMAId", notaRMAEntity.getRMAId());

        Call<JsonObject> call = myApi.CreateOrUpdateNotaRMA(body);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                if (responseObj.get("Success").getAsBoolean()) {
                    Log.e("Notas","A nota foi enviada com sucesso para o servidor");
                    // A nota foi enviada com sucesso para o servidor
                } else {
                    // Tratar erro ao enviar a nota
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Tratar erro na chamada da API
            }
        });
    }
    public void createOrUpdateNota(NotaRMAEntity notaRMAEntity) {
        if (notaRMAEntity.getId() == 0) {
            // Criação
            new InsertNotaAsyncTask(NotaRMADao, notaRMAEntity).execute();
        } else {
            // Atualização
            new UpdateNotaAsyncTask(NotaRMADao, notaRMAEntity).execute();
        }
    }
    public String convertUriStringToBitmapString(Context context, String uriString) {
        try {
            Uri imageUri = Uri.parse(uriString);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



}

