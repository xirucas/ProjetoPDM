package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.Api;
import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.LocalDataBase.AppDatabase;
import com.example.projetopdm.LocalDataBase.DAOs.NotaRMADao;
import com.example.projetopdm.LocalDataBase.Entity.NotaRMAEntity;

import com.example.projetopdm.Modelos.NotaRMA;
import com.example.projetopdm.Modelos.RMA;
import com.example.projetopdm.databinding.ActivityMainBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;

import com.example.projetopdm.databinding.ActivityNotasBinding;
import com.google.gson.JsonParser;

public class Notas extends AppCompatActivity {


    public static final int MEU_REQUEST_CODE = 1;
    ActivityNotasBinding binding;
    int RMAId;

    RMA rma ;
    NotaRMA notaRMA;
    NotaRMADao notaRMADao;

    ArrayList<NotaRMA> rmaList;
    ListaAdapterRMADetails listAdapter;
    Button novaNova_btn;
    RetrofitClient retrofitClient;
    Context context;




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == Activity.RESULT_OK) {
            // A variável está contida nos dados da Intent
            if (data.hasExtra("AtivarAPI")){
                if (data.getBooleanExtra("AtivarAPI", false)){
                    rmaList.clear();
                    loadNotas();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rmaList= new ArrayList<>();
        context= this;
        binding = ActivityNotasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        RMAId = getIntent().getIntExtra("RMAId",0);

        String rmaTxt= getIntent().getStringExtra("RMA");
        String rmaDataTxt=getIntent().getStringExtra("Data");
        String rmaDescricao= getIntent().getStringExtra("Descriçao");

        TextView ticketsTitle = (TextView) findViewById(R.id.ticketsTitle);
        ticketsTitle.setText(rmaTxt);
        TextView datarma = (TextView)findViewById(R.id.datarma);
        datarma.setText(rmaDataTxt);
        TextView textView3 = (TextView)findViewById(R.id.textView3);
        textView3.setText(rmaDescricao);

        retrofitClient = RetrofitClient.getInstance();

        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "BaseDeDadosLocal").fallbackToDestructiveMigration().allowMainThreadQueries().build();

        notaRMADao = db.notaRMADao();

        Api api = retrofitClient.getMyApi();

        // Inicialização do repositório



        LinearLayout popup = findViewById(R.id.popup);
        novaNova_btn = (Button) findViewById(R.id.novaNota_btn);
        Log.e("Notas","tamanho da local -> "+notaRMADao.getAllNotasRMA().size());
        novaNova_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Nota.class);
                intent.putExtra("Update","Novo");
                intent.putExtra("RMAId",RMAId);
                startActivity(intent);

            }
        });
        popup.setVisibility(View.INVISIBLE);
        //rmaList.clear();
        if (!isInternetAvailable()){
            Log.e("Notas","sem net, tentar carregar local");
            loadNotas();
        }

        if (isInternetAvailable()) {

            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().GetRMAById(RMAId);


            call.enqueue(new Callback<JsonObject>(){
                @SuppressLint("SuspiciousIndentation")
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

                        String imgBitMap= null;
                        int imgID;

                        if (response.body().has("RMANotas")) {
                            JsonArray NotasRMA = response.body().get("RMANotas").getAsJsonArray();
                            Log.d("Notas", "Notas RMA obtidas da API com sucesso.");
                            List<NotaRMAEntity>rmaListEntity = new ArrayList<>();
                            if (NotasRMA.get(0).getAsJsonObject().get("Id").getAsInt() != 0) {
                                for (int i = 0; i < NotasRMA.size(); i++) {
                                    JsonObject notaRMAObj = NotasRMA.get(i).getAsJsonObject();
                                    NotaRMA notaRMA = new NotaRMA();
                                    notaRMA.setId(notaRMAObj.get("Id").getAsInt());
                                    notaRMA.setTitulo(notaRMAObj.get("Titulo").getAsString());
                                    notaRMA.setDataCriacao(notaRMAObj.get("DataCriacao").getAsString());
                                    notaRMA.setNota(notaRMAObj.get("Nota").getAsString());
                                    notaRMA.setRMAId(RMAId);
                                    if (notaRMAObj.get("ImagemNotaId") != null)
                                        notaRMA.setImagemNotaId(notaRMAObj.get("ImagemNotaId").getAsInt());
                                        imgID=notaRMAObj.get("ImagemNotaId").getAsInt();
                                    if (notaRMAObj.get("ImagemNota") != null){
                                        Log.i("Notas","Imagem " + notaRMAObj.get("ImagemNota").getAsString());
                                        notaRMA.setImagemNota(notaRMAObj.get("ImagemNota").getAsString());
                                        imgBitMap = notaRMAObj.get("ImagemNota").getAsString();

                                    }
                                    NotaRMAEntity notaRMAEntiTy = new NotaRMAEntity();
                                    if (imgBitMap==null){
                                        notaRMAEntiTy = notaRMA.toNotaRMAEntity();
                                    } else if (imgBitMap!=null) {
                                        Uri img = saveImageToStorage(imgBitMap,context,notaRMA.getTitulo()+" "+notaRMA.getId());
                                        notaRMAEntiTy = notaRMA.toNotaRMAEntity();
                                        notaRMAEntiTy.setImagemNota(img.toString());
                                    }


                                    rmaListEntity.add(notaRMAEntiTy);

                                    rmaList.add(notaRMA);

                                }
                                notaRMADao.insertAllNotas(rmaListEntity);
                                loadNotas();
                            }
                        }


                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("Notas", "Sem conectividade de rede. Não foi possível sincronizar os dados.");
                }
            });





        }else {// vai buscar os dados á base de dados online e coloca na BDLocal
            //nah net
        }


    }



    private void loadNotas() {
        if(!isInternetAvailable()){
            Log.i("Notas"," "+notaRMADao.getAllNotasRMA().size());
            ArrayList<NotaRMA> notasDoRMAX = new ArrayList<>();
            Log.e("Notas","id do rma "+ RMAId);
            for (NotaRMA x:convertNotaRMAEntityListToNotaRMAList(notaRMADao.getAllNotasRMA())) {
                if (x.getRMAId()==RMAId){
                    Log.e("Notas","id do RMA da nota  "+ x.getRMAId());
                    notasDoRMAX.add(x);
                }
            }

            listAdapter = new ListaAdapterRMADetails(Notas.this,notasDoRMAX , Notas.this);
            binding.notas.setAdapter(listAdapter);
        }


        if (isInternetAvailable()){
            if (notaRMADao.getNotasByRMAId(RMAId)!=null){
                if (mudancasNaBD()){
                    updateBaseDeDados();
                }
            }
        listAdapter = new ListaAdapterRMADetails(Notas.this, rmaList, Notas.this);
        binding.notas.setAdapter(listAdapter);
        }


    }
    public static Uri saveImageToStorage(String base64Image, Context context, String fileName) {
        // Decodificar o base64 para Bitmap
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // Verificar se a conversão de base64 para bitmap foi bem-sucedida
        if (bitmap == null) {
            return null; // ou lançar uma exceção, dependendo da necessidade
        }

        // Obter o diretório de armazenamento externo (pode ser necessário lidar com permissões)
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(directory, fileName + ".jpg"); // ou .png

        try {
            // Comprimir e escrever o bitmap no arquivo especificado
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); // ou Bitmap.CompressFormat.PNG
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Retornar a URI do arquivo
        return Uri.fromFile(file);
    }

    public void updateBaseDeDados(){
        ArrayList<NotaRMA> novos= new ArrayList<>();
        ArrayList<NotaRMA> modificados = new ArrayList<>();
        for (NotaRMAEntity x : notaRMADao.getNotasByRMAId(RMAId)) {
            if (x.getOffSync()!=null){
                if (x.getOffSync().equals("novo")){
                    novos.add(x.toNotaRMA());
                } else if (x.getOffSync().equals("modificado")) {
                    modificados.add(x.toNotaRMA());
                }
            }
        }
        for (NotaRMA x:novos) {
            String request ="";
            if (x.getImagemNota() != null) {
                Bitmap imagem = uriToBitmap(x.getImagemNota(),getApplicationContext() );
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
                        notaRMADao.deleteById(x.getId());//depois deleta o mesmo da local
                    } else {
                        Toast.makeText(getApplicationContext(), "Erro ao criar nota", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Erro ao criar nota", Toast.LENGTH_LONG).show();
                }
            });

        }//passar os novos da local para a online
        for (NotaRMA x:modificados){
            String request ="";

                if (x.getImagemNota() != null) {
                    Bitmap imagem = uriToBitmap(x.getImagemNota(),getApplicationContext() );
                    String imagemString = bitmapToString(imagem);
                    x.setImagemNota(imagemString);
                    x.setImagemNota(imagemString);
                }
                request = "{"
                        + " \"Id\": \"" + x.getId() + "\", "
                        + " \"Titulo\": \"" + x.getTitulo() + "\", "
                        + " \"Nota\": \"" + x.getNota() + "\", "
                        + " \"RMAId\": \"" + x.getRMAId() + "\", "
                        + " \"IdImagem\": \"" + x.getImagemNotaId() + "\", "
                        + " \"Imagem\": \"" + x.getImagemNota() + "\" }";

            JsonObject body = new JsonParser().parse(request).getAsJsonObject();
            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().CreateOrUpdateNotaRMA(body);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                    if (responseObj.get("Success").getAsBoolean()) {
                        Toast.makeText(getApplicationContext(), "Nota alterada com sucesso", Toast.LENGTH_LONG).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("AtivarAPI", true);
                        setResult(Activity.RESULT_OK, resultIntent);
                        NotaRMAEntity y= x.toNotaRMAEntity();
                        y.setOffSync("null");
                        notaRMADao.update(y);
                    } else {
                        Toast.makeText(getApplicationContext(), "Erro ao alterar nota", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Erro ao alterar nota", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    public boolean mudancasNaBD(){
        if(notaRMADao.getNotasByRMAId(RMAId).size()>rmaList.size()){
            return true;
        }
        boolean encontrouMod = false;
        for (NotaRMAEntity x : notaRMADao.getNotasByRMAId(RMAId)) {
            if (x.getOffSync()!=null){
                if (x.getOffSync().equals("modificado")){
                    encontrouMod = true;
                }
            }
        }
        return encontrouMod;

    }

    private ArrayList<NotaRMA> convertNotaRMAEntityListToNotaRMAList(List<NotaRMAEntity> notaRmaEntities) {
        ArrayList<NotaRMA> notaRmaList = new ArrayList<>();
        // Converter cada NotaRMAEntity para NotaRMA e adicionar à lista
        for (NotaRMAEntity entity : notaRmaEntities) {

            NotaRMA x= new NotaRMA(entity.getId(),entity.getTitulo(),entity.getDataCriacao(), entity.getNota(),entity.getImagemNotaId() ,entity.getImagemNota(), entity.getRMAId());
            Log.i("Notas","titulo de cada puta "+ x.getTitulo());
            notaRmaList.add(x);
        }
        Log.i("Notas","tamanho da puta "+notaRmaList.size());
        return notaRmaList;
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
    public static Bitmap uriToBitmap(String uriString, Context context) {
        try {
            Uri imageUri = Uri.parse(uriString);
            InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
            return BitmapFactory.decodeStream(imageStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}