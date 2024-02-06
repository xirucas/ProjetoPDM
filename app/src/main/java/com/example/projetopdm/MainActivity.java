package com.example.projetopdm;

import static com.example.projetopdm.LocalDataBase.FuncionarioSharedPreferences.getFuncionarioData;
import static com.example.projetopdm.LocalDataBase.FuncionarioSharedPreferences.saveFuncionarioData;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.room.Room;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
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
import com.example.projetopdm.LocalDataBase.Entity.RMAEntity;
import com.example.projetopdm.Modelos.Funcionario;
import com.example.projetopdm.Modelos.RMA;
import com.example.projetopdm.databinding.ActivityMainBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
        if(!funcionario.getGUID().equals(x.getGUID())){
            this.deleteDatabase("BaseDeDadosLocal");
            saveFuncionarioData(this,funcionario);
        }
    }
    private void initializeDatabaseAndViewModel() {
        retrofitClient = RetrofitClient.getInstance();
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "BaseDeDadosLocal").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        rmaDao = db.rmaDao();
        notaRMADao = db.notaRMADao();
        api = retrofitClient.getMyApi();

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
                    rmaEntity.getFuncionarioId()
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
                            RMAEntity rma = new RMAEntity(rmaObj.get("Id").getAsInt(), rmaObj.get("RMA").getAsString(), rmaObj.get("DescricaoCliente").getAsString(), rmaObj.get("DataCriacao").getAsString(), dataAb, dataF, rmaObj.get("EstadoRMA").getAsString(), rmaObj.get("EstadoRMAId").getAsInt(), rmaObj.get("FuncionarioId").getAsInt());
                            RMA x =  new RMA(rmaObj.get("Id").getAsInt(), rmaObj.get("RMA").getAsString(), rmaObj.get("DescricaoCliente").getAsString(), rmaObj.get("DataCriacao").getAsString(), dataAb, dataF, rmaObj.get("EstadoRMA").getAsString(), rmaObj.get("EstadoRMAId").getAsInt(), rmaObj.get("FuncionarioId").getAsInt());
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

}