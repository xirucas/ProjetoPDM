package com.example.projetopdm.LocalDataBase.DAOs;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.projetopdm.LocalDataBase.Entity.FuncionarioEntity;

import java.util.List;

@Dao
public interface FuncionarioDao {
    @Insert
    void insert(FuncionarioEntity funcionario);

    @Query("SELECT * FROM funcionarios")
    List<FuncionarioEntity> getAllFuncionarios();

    // Adicione outros métodos conforme necessário, como update, delete, etc.
}
