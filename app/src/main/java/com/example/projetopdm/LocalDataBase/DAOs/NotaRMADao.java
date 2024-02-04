    package com.example.projetopdm.LocalDataBase.DAOs;

    import androidx.room.Dao;
    import androidx.room.Insert;
    import androidx.room.OnConflictStrategy;
    import androidx.room.Query;
    import androidx.room.Update;

    import com.example.projetopdm.LocalDataBase.Entity.NotaRMAEntity;
    import com.example.projetopdm.LocalDataBase.Entity.RMAEntity;

    import java.util.List;

    @Dao
    public interface NotaRMADao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertAllNotas(List<NotaRMAEntity> notaRMAList);
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insert(NotaRMAEntity nota);

        @Query("SELECT * FROM notas_rma")
        List<NotaRMAEntity> getAllNotasRMA();

        @Query("SELECT * FROM notas_rma WHERE RMAId = :RMAId")
        List<NotaRMAEntity> getNotasByRMAId(int RMAId);
        @Query("SELECT * FROM notas_rma WHERE RMAId = :rmaId")
        List<NotaRMAEntity> getNotasByRMAIdWithLimit(int rmaId);
        @Query("SELECT * FROM notas_rma WHERE id = :id")
        NotaRMAEntity getNotaById(int id);

        @Query("DELETE FROM notas_rma WHERE id = :id")
        void deleteById(int id);


        @Update
        void update(NotaRMAEntity notaRMA);



        // Métodos adicionais conforme necessário
    }
