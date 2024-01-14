    package com.example.projetopdm.LocalDataBase;

    import androidx.room.Database;
    import androidx.room.RoomDatabase;

    import com.example.projetopdm.LocalDataBase.DAOs.FuncionarioDao;
    import com.example.projetopdm.LocalDataBase.DAOs.NotaRMADao;
    import com.example.projetopdm.LocalDataBase.DAOs.RMADao;
    import com.example.projetopdm.LocalDataBase.Entity.FuncionarioEntity;
    import com.example.projetopdm.LocalDataBase.Entity.NotaRMAEntity;
    import com.example.projetopdm.LocalDataBase.Entity.RMAEntity;

    @Database(entities = {FuncionarioEntity.class, NotaRMAEntity.class, RMAEntity.class}, version = 1)
    public abstract class AppDatabase extends RoomDatabase {
        public abstract FuncionarioDao funcionarioDao();
        public abstract NotaRMADao notaRMADao();
        public abstract RMADao rmaDao();
    }