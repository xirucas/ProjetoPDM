package com.example.projetopdm.LocalDataBase;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;

import com.example.projetopdm.BackEnd.Api;
import com.example.projetopdm.LocalDataBase.DAOs.RMADao;
import com.example.projetopdm.LocalDataBase.Repositorys.RMARepository;
import com.example.projetopdm.LocalDataBase.Entity.RMAEntity;

import java.util.List;

public class MainViewModel extends ViewModel {
    private RMADao RMADao;
    private Api myApi;
    private Context context;
    private String FuncionarioGUID;
    private RMARepository rmaRepository;
    private LiveData<List<RMAEntity>> rmasLocais; // LiveData to observe changes in the local RMAs

    public MainViewModel(RMADao RMADao, Api myApi, Context context,String FuncionarioGUID) {
        // Initialize the repository here
        rmaRepository = new RMARepository(RMADao , myApi , context ,FuncionarioGUID );

        // Initialize LiveData for local RMAs
        rmasLocais = converterListaParaLiveData(rmaRepository.getRMAsFromLocal());
    }
    public MainViewModel() {
        // Construtor vazio
    }
    // Getter for LiveData to observe local RMAs
    public LiveData<List<RMAEntity>> getRMAsLocais() {
        return rmasLocais;
    }
    public void init(RMADao RMADao, Api myApi, Context context,String FuncionarioGUID) {
        rmaRepository = new RMARepository(RMADao, myApi, context, FuncionarioGUID);

        // Depois de garantir que RMARepository foi inicializado, obtenha RMAs locais
        List<RMAEntity> rmasLocaisList = rmaRepository.getRMAsFromLocal();

        // Converter a lista para LiveData
        rmasLocais = converterListaParaLiveData(rmasLocaisList);
    }



    public LiveData<List<RMAEntity>> converterListaParaLiveData(List<RMAEntity> lista) {
        MutableLiveData<List<RMAEntity>> liveData = new MutableLiveData<>();
        liveData.setValue(lista); // Define a lista como o valor do LiveData
        return liveData;
    }
}
