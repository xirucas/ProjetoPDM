package com.example.projetopdm;

import static com.example.projetopdm.LocalDataBase.FuncionarioSharedPreferences.getFuncionarioData;
import static com.example.projetopdm.LocalDataBase.FuncionarioSharedPreferences.saveFuncionarioData;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.exifinterface.media.ExifInterface;
import androidx.room.Room;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.Api;
import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.LocalDataBase.AppDatabase;
import com.example.projetopdm.LocalDataBase.DAOs.NotaRMADao;
import com.example.projetopdm.LocalDataBase.DAOs.RMADao;
import com.example.projetopdm.LocalDataBase.Entity.NotaRMAEntity;
import com.example.projetopdm.LocalDataBase.Entity.RMAEntity;
import com.example.projetopdm.Modelos.Funcionario;
import com.example.projetopdm.Modelos.NotaRMA;
import com.example.projetopdm.Modelos.RMA;
import com.example.projetopdm.databinding.ActivityMainBinding;
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

public class MainActivity extends AppCompatActivity {

    ConstraintLayout loading;
    public static final int MEU_REQUEST_CODE = 1;
    ActivityMainBinding binding;
    Funcionario funcionario;

    SearchView searchView;
    RMADao rmaDao;
    NotaRMADao notaRMADao;
    Api api;
    AppDatabase db;
    RetrofitClient retrofitClient;
    private boolean isConnectedPreviously = false;

    ArrayList<RMA> rmaList = new ArrayList<RMA>();
    ListAdapterRMA listAdapter;
    RMA rma;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // A variável está contida nos dados da Intent
            if (data.hasExtra("AtivarAPI")){
                if (data.getBooleanExtra("AtivarAPI", false)){
                    rmaList.clear();
                    initializeDatabaseAndViewModel();
                }
            }
        }
    }







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Set the root view of the binding object
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);
        initializeFuncionarioFromIntent();
        initializeDatabaseAndViewModel();

        setupPerfilButton();
        displayFuncionarioImage();

        setContentView(binding.getRoot());


        ImageView perfil_btn = findViewById(R.id.perfil_btn);
        searchView = findViewById(R.id.searchView);

        ImageView img = findViewById(R.id.perfil_btn);
        Bitmap bitmap = StringToBitMap(funcionario.getImagemFuncionario());
        img.setImageBitmap(bitmap);
        loading.setVisibility(View.INVISIBLE);

        //filtrar pelo titulo

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Expandir o SearchView
                searchView.setIconified(false);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Chamada para atualizar o filtro na sua lista
                listAdapter.getFilter().filter(newText);
                return true;
            }
        });

        justDoIT();


    }

    private void justDoIT() {
        ArrayList<RMAEntity> rmasModificados = new ArrayList<>();
        if(notaRMADao.getAllNotasRMA()!=null){
            for (NotaRMAEntity x:notaRMADao.getAllNotasRMA()) {
                if(x.getOffSync()!=null){
                    if (x.getOffSync().equals("modificado")||x.getOffSync().equals("novo")||x.getOffSync().equals("apagado")){
                        if(!rmasModificados.contains(rmaDao.getRMAById(x.getRMAId()))){
                            rmasModificados.add(rmaDao.getRMAById(x.getRMAId()));
                        }
                    }
                }

            }
        }

        if (rmasModificados.size()!=0){
            updateBaseDeDados();
        }

    }

    public void updateBaseDeDados(){
        Log.e("Notas","aqui gayyyyyyyyy");

        ArrayList<NotaRMA> novos= new ArrayList<>();
        ArrayList<NotaRMA> modificados = new ArrayList<>();
        ArrayList<NotaRMA> apagados = new ArrayList<>();

        for (NotaRMAEntity x : notaRMADao.getAllNotasRMA()) {
            if (x.getOffSync()!=null){
                if (x.getOffSync().equals("novo")){
                    novos.add(x.toNotaRMA());
                } else if (x.getOffSync().equals("modificado")) {
                    modificados.add(x.toNotaRMA());
                } else if (x.getOffSync().equals("apagado")) {
                    apagados.add(x.toNotaRMA());
                }
            }
        }
        for (NotaRMA x:novos) {
            String request ="";
            Log.e("Notas","teste img  "+x.getImagemNota());
            if (x.getImagemNota() != null) {
                Uri uri = Uri.parse(x.getImagemNota());
                Bitmap imagem = uriToBitmap(getApplicationContext(), uri);
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

                        notaRMADao.deleteById(x.getId());
                        int id= response.body().get("NotaRMAId").getAsInt();
                        x.setId(id);
                        notaRMADao.insert(x.toNotaRMAEntity());




                        //teste apagar depois




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
                Uri uri = Uri.parse(x.getImagemNota());
                Bitmap imagem = uriToBitmap(getApplicationContext(), uri);
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
                        notaRMADao.deleteById(x.getId());
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
        for (NotaRMA x:apagados){

            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().DeleteNotaRMA(x.getId());

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                    if (responseObj.get("Success").getAsBoolean()) {
                        Toast.makeText(getApplicationContext(), "Nota apagada com sucesso", Toast.LENGTH_LONG).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("AtivarAPI", true);
                        setResult(Activity.RESULT_OK, resultIntent);
                        notaRMADao.deleteById(x.getId());
                    } else {
                        Toast.makeText(getApplicationContext(), "Erro ao apagar nota", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Erro ao apagar nota", Toast.LENGTH_LONG).show();
                }
            });

        }

    }



    private void initializeFuncionarioFromIntent() {
        Funcionario x = getFuncionarioData(this);

        int id = getIntent().getIntExtra("Id", 0);
        String nome = getIntent().getStringExtra("Nome");
        String email = getIntent().getStringExtra("Email");
        String guid = getIntent().getStringExtra("GUID");
        String pin = getIntent().getStringExtra("Pin");
        String contacto = getIntent().getStringExtra("Contacto");
        String imagemFuncionario = getIntent().getStringExtra("ImagemFuncionario");
        String estadoFuncionario = getIntent().getStringExtra("EstadoFuncionario");
        int estadoFuncionarioId = getIntent().getIntExtra("EstadoFuncionarioId", 0);

        // Inicialização do objeto Funcionario com os dados obtidos
        funcionario = new Funcionario(id, guid, nome, email, contacto, pin, imagemFuncionario, estadoFuncionarioId, estadoFuncionario);
        if (x!=null){
            if(!funcionario.getGUID().equals(x.getGUID())){
                this.deleteDatabase("BaseDeDadosLocal");
                saveFuncionarioData(this,funcionario);
            }
        }

    }
    private void initializeDatabaseAndViewModel() {

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "BaseDeDadosLocal").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        rmaDao = db.rmaDao();
        notaRMADao = db.notaRMADao();


        if (!isInternetAvailable()){
            rmaList = convertRMAEntityListToRMAList(rmaDao.getAllRMAs());
        }

        listAdapter = new ListAdapterRMA(MainActivity.this, rmaList, MainActivity.this);
        binding.listRMA.setAdapter(listAdapter);
        Toast.makeText(MainActivity.this, "Dados sincronizados com sucesso! -->"+ rmaList.size(), Toast.LENGTH_SHORT).show();
        sincronizarRMAs();

    }

    private void setupPerfilButton() {
        ImageView perfilBtn = findViewById(R.id.perfil_btn);
        perfilBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Perfil.class);
            intent.putExtra("Id", funcionario.getId());
            intent.putExtra("Nome", funcionario.getNome());
            intent.putExtra("Email", funcionario.getEmail());
            intent.putExtra("Contacto", funcionario.getContacto());
            intent.putExtra("GUID", funcionario.getGUID());
            intent.putExtra("Pin", funcionario.getPin());
            intent.putExtra("ImagemFuncionario", funcionario.getImagemFuncionario());
            intent.putExtra("EstadoFuncionario", funcionario.getEstadoFuncionario());
            intent.putExtra("EstadoFuncionarioId", funcionario.getEstadoFuncionarioId());
            startActivity(intent);
        });
    }
    private void displayFuncionarioImage() {
        ImageView img = findViewById(R.id.perfil_btn);
        Bitmap bitmap = StringToBitMap(funcionario.getImagemFuncionario());
        if (bitmap != null) {
            img.setImageBitmap(bitmap);
        }
    }

    public ArrayList<RMA> convertRMAEntityListToRMAList(List<RMAEntity> rmaEntityList) {
        ArrayList<RMA> rmaList = new ArrayList<>();


        for (RMAEntity rmaEntity : rmaEntityList) {
            RMA rma = new RMA(
                    rmaEntity.getId(),
                    rmaEntity.getRMA(),
                    rmaEntity.getDescricaoCliente(),
                    rmaEntity.getDataCriacao(),
                    rmaEntity.getDataAbertura(),
                    rmaEntity.getDataFecho(),
                    rmaEntity.getEstadoRMA(),
                    rmaEntity.getEstadoRMAId(),
                    rmaEntity.getFuncionarioId(),
                    rmaEntity.getHorasTrabalhadas()
            );
            rmaList.add(rma);
        }

        return rmaList;
    }

    // Classe SincronizarRMAsTask
    public  void SincronizarRMAsTask()  {


        List<RMAEntity> rmaEntities = rmaDao.getAllRMAs();
        Toast.makeText(MainActivity.this, "aqui -->"+ rmaEntities.size(), Toast.LENGTH_SHORT).show();

        if (!isInternetAvailable()){
            rmaList = convertRMAEntityListToRMAList(rmaEntities);
        }

        listAdapter = new ListAdapterRMA(MainActivity.this, rmaList, MainActivity.this);
        binding.listRMA.setAdapter(listAdapter);

        Toast.makeText(MainActivity.this, "Dados sincronizados com sucesso! -->"+ rmaList.size(), Toast.LENGTH_SHORT).show();
    }

    public void sincronizarRMAs () {
        // Lógica para verificar a conectividade de rede



        // Chamar a API e atualizar a base de dados local
        Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().GetRMASByFuncionario(funcionario.getGUID());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject responseObject = response.body();

                    if (responseObject.has("RMA")) {
                        JsonArray rmaListObj = response.body().get("RMA").getAsJsonArray();
                        List<RMAEntity> rmaListEnt = new ArrayList<>();

                        for (int i = 0; i < rmaListObj.size(); i++) {
                            JsonObject rmaObj = rmaListObj.get(i).getAsJsonObject();
                            String dataAb = "";
                            String dataF = "";
                            String horas = "";
                            if (rmaObj.get("DataAbertura") != null){
                                dataAb = (rmaObj.get("DataAbertura").getAsString());
                            } else {
                                dataAb = "null";
                            }
                            if (rmaObj.get("DataFecho") != null){
                                dataF = (rmaObj.get("DataFecho").getAsString());
                            } else {
                                dataF = "null";
                            }
                            if (rmaObj.get("HorasTrabalhadas") != null){
                                horas = (rmaObj.get("HorasTrabalhadas").getAsString());
                            } else {
                                horas = "null";
                            }
                            RMAEntity rma = new RMAEntity(rmaObj.get("Id").getAsInt(), rmaObj.get("RMA").getAsString(), rmaObj.get("DescricaoCliente").getAsString(), rmaObj.get("DataCriacao").getAsString(), dataAb, dataF, rmaObj.get("EstadoRMA").getAsString(), rmaObj.get("EstadoRMAId").getAsInt(), rmaObj.get("FuncionarioId").getAsInt(),horas);
                            RMA x =  new RMA(rmaObj.get("Id").getAsInt(), rmaObj.get("RMA").getAsString(), rmaObj.get("DescricaoCliente").getAsString(), rmaObj.get("DataCriacao").getAsString(), dataAb, dataF, rmaObj.get("EstadoRMA").getAsString(), rmaObj.get("EstadoRMAId").getAsInt(), rmaObj.get("FuncionarioId").getAsInt(),horas);
                            rmaListEnt.add(rma);
                            rmaList.add(x);
                        }


                        rmaDao.insertAll(rmaListEnt);
                        SincronizarRMAsTask();
                    }

                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Tratar falhas
            }
        });


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