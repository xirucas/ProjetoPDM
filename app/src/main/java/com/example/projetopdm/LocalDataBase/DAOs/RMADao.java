package com.example.projetopdm.LocalDataBase.DAOs;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.projetopdm.LocalDataBase.Entity.RMAEntity;

import java.util.List;

@Dao
public interface RMADao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRMA(RMAEntity rma);

    @Query("SELECT * FROM rmas")
    List<RMAEntity> getAllRMAs();

    @Query("SELECT * FROM rmas WHERE funcionarioId = :funcionarioId")
    List<RMAEntity> getRMAsByFuncionarioId(int funcionarioId);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RMAEntity> rmaList);

    @Query("SELECT * FROM rmas WHERE Id = :rmaId")
    RMAEntity getRMAById(int rmaId);


    // Métodos adicionais conforme necessário
}
