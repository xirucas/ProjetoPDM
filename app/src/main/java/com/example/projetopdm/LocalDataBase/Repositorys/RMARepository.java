package com.example.projetopdm.LocalDataBase.Repositorys;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.lifecycle.LiveData;

import kotlin.reflect.KCallable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import com.example.projetopdm.BackEnd.Api;
import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.LocalDataBase.DAOs.FuncionarioDao;
import com.example.projetopdm.LocalDataBase.DAOs.NotaRMADao;
import com.example.projetopdm.LocalDataBase.DAOs.RMADao;
import com.example.projetopdm.LocalDataBase.Entity.FuncionarioEntity;
import com.example.projetopdm.LocalDataBase.Entity.NotaRMAEntity;
import com.example.projetopdm.LocalDataBase.Entity.RMAEntity;
import com.example.projetopdm.MainActivity;
import com.example.projetopdm.Modelos.RMA;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


public class RMARepository {

    private RMADao RMADao;
    private Api myApi;
    private Context context;
    private String FuncionarioGUID;

    RMAEntity rma;

    public RMARepository(RMADao RMADao, Api myApi, Context context,String FuncionarioGUID) {
        this.RMADao = RMADao;
        this.myApi = myApi;
        this.context = context;
        this.FuncionarioGUID=FuncionarioGUID;
    }
    public interface SincronizarRMAsCallback {
        void onSuccess(List<RMAEntity> rmaEntities);
        void onFailure(Throwable t);
    }

    public void sincronizarRMAs () {
        // Lógica para verificar a conectividade de rede



            // Chamar a API e atualizar a base de dados local
            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().GetRMASByFuncionario(this.FuncionarioGUID);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        JsonObject responseObject = response.body();

                        if (responseObject.has("RMA")) {
                            JsonArray rmaListObj = response.body().get("RMA").getAsJsonArray();
                            List<RMAEntity> rmaList = new ArrayList<>();

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
                                rma = new RMAEntity(rmaObj.get("Id").getAsInt(), rmaObj.get("RMA").getAsString(), rmaObj.get("DescricaoCliente").getAsString(),rmaObj.get("DataCriacao").getAsString() ,dataAb , dataF, rmaObj.get("EstadoRMA").getAsString(), rmaObj.get("EstadoRMAId").getAsInt(), rmaObj.get("FuncionarioId").getAsInt());
                                rmaList.add(rma);
                            }
                            RMADao.insertAll(rmaList);
                            Log.e("MainActivity","sync feito lista size "+RMADao.getAllRMAs().size());
                        }

                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    // Tratar falhas
                }
            });
    }
    public List<RMAEntity> getRMAsFromLocal() {
        return RMADao.getAllRMAs(); // Suponha que você tenha um método getAllRMAs() no seu RMADao para buscar todos os RMAs.
    }
    public void getRMAsFromLocal(Consumer<List<RMAEntity>> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<RMAEntity> rmaEntities = RMADao.getAllRMAs();
            callback.accept(rmaEntities);
        });
        executor.shutdown();
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

}

