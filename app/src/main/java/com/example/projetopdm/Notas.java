package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import com.example.projetopdm.LocalDataBase.Repositorys.NotaRMARepository;
import com.example.projetopdm.Modelos.NotaRMA;
import com.example.projetopdm.Modelos.RMA;
import com.example.projetopdm.databinding.ActivityMainBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;

import com.example.projetopdm.databinding.ActivityNotasBinding;

public class Notas extends AppCompatActivity {


    public static final int MEU_REQUEST_CODE = 1;
    ActivityNotasBinding binding;
    int RMAId;

    RMA rma = new RMA();
    NotaRMA notaRMA;
    NotaRMADao notaRMADao;

    ArrayList<NotaRMA> rmaList = new ArrayList<NotaRMA>();
    ListaAdapterRMADetails listAdapter;
    Button novaNova_btn;
    RetrofitClient retrofitClient;

    private NotaRMARepository notaRMARepository;


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

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "BaseDeDadosLocal").allowMainThreadQueries().build();

        notaRMADao = db.notaRMADao();

        Api api = retrofitClient.getMyApi();

        // Inicialização do repositório
        notaRMARepository = new NotaRMARepository(notaRMADao,api,this,RMAId);


        LinearLayout popup = findViewById(R.id.popup);
        novaNova_btn = (Button) findViewById(R.id.novaNota_btn);
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
        rmaList.clear();
        Log.d("Notas", "olaaaa");

        if (isInternetAvailable()){
            notaRMARepository.sincronizarNotasRMAs(RMAId);
        }
        loadNotas();

    }


    private void loadNotas() {

        notaRMARepository.getNotasRMAsFromLocal(new Consumer<List<NotaRMAEntity>>() {
            ;
            @Override
            public void accept(List<NotaRMAEntity> notaRmaEntities) {
                runOnUiThread(() -> {
                    Log.d("Notas", "Dentro do accept: " + notaRmaEntities.size() + " notas carregadas");


                    ArrayList<NotaRMA> notaRmaList = convertNotaRMAEntityListToNotaRMAList(notaRmaEntities);
                    listAdapter = new ListaAdapterRMADetails(Notas.this, notaRmaList, Notas.this);
                    binding.notas.setAdapter(listAdapter);
                });
            }
        });
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
}