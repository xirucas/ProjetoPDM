    package com.example.projetopdm;

    import static com.example.projetopdm.LocalDataBase.FuncionarioSharedPreferences.getFuncionarioData;
    import static com.example.projetopdm.LocalDataBase.FuncionarioSharedPreferences.saveFuncionarioData;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.lifecycle.LiveData;
    import androidx.lifecycle.Observer;
    import androidx.lifecycle.ViewModelProvider;
    import androidx.room.Room;

    import android.content.Context;
    import android.content.Intent;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.net.ConnectivityManager;
    import android.net.NetworkInfo;
    import android.os.AsyncTask;
    import android.os.Bundle;
    import android.util.Base64;
    import android.view.View;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.Toast;
    import androidx.lifecycle.ViewModel;

    import com.example.projetopdm.BackEnd.Api;
    import com.example.projetopdm.BackEnd.RetrofitClient;
    import com.example.projetopdm.LocalDataBase.AppDatabase;
    import com.example.projetopdm.LocalDataBase.DAOs.RMADao;
    import com.example.projetopdm.LocalDataBase.Entity.RMAEntity;
    import com.example.projetopdm.LocalDataBase.MainViewModel;
    import com.example.projetopdm.LocalDataBase.Repositorys.RMARepository;
    import com.example.projetopdm.Modelos.Funcionario;
    import com.example.projetopdm.Modelos.RMA;
    import com.example.projetopdm.databinding.ActivityMainBinding;
    import com.google.gson.JsonArray;
    import com.google.gson.JsonObject;

    import java.util.ArrayList;
    import android.util.Log;

    import java.util.List;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;
    import java.util.function.Consumer;

    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;

    public class MainActivity extends AppCompatActivity {

        ActivityMainBinding binding;
        Funcionario funcionario;

        ArrayList<RMA> rmaList ;
        List<RMAEntity> rmasLocais;
        ListAdapterRMA listAdapter;
        RMA rma;

        RMADao rmaDao;
        Api api;

        AppDatabase db;
        RetrofitClient retrofitClient;
        private MainViewModel mainViewModel;
        private RMARepository rmaRepository;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            initializeFuncionarioFromIntent();
            initializeDatabaseAndViewModel();
            setupPerfilButton();
            displayFuncionarioImage();

            //mainViewModel.getRMAsLocais().observe(this, this::onRMAEntitiesChanged);
            // Inicialização do ViewModel e Repository
            mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
            rmaRepository = new RMARepository(rmaDao,api,this,funcionario.getGUID());

            // Carregar dados dos RMAs
            loadRMAs();
            if (isInternetAvailable()) {
                new SincronizarRMAsTask().execute();
            } else {
                Toast.makeText(MainActivity.this, "Sem conexão com a internet.", Toast.LENGTH_SHORT).show();
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
            if(!funcionario.getGUID().equals(x.getGUID())){
                this.deleteDatabase("BaseDeDadosLocal");
                saveFuncionarioData(this,funcionario);
            }
        }
        private void initializeDatabaseAndViewModel() {
            retrofitClient = RetrofitClient.getInstance();
            db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "BaseDeDadosLocal").allowMainThreadQueries().build();
            rmaDao = db.rmaDao();
            api = retrofitClient.getMyApi();

            mainViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                    .get(MainViewModel.class);
            mainViewModel.init(rmaDao, api, this, funcionario.getGUID());
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
        private void onRMAEntitiesChanged(List<RMAEntity> rmaEntities) {
            if (rmaEntities != null) {
                rmaList = convertRMAEntityListToRMAList(rmaEntities);
                listAdapter = new ListAdapterRMA(MainActivity.this, rmaList, MainActivity.this);
                binding.listRMA.setAdapter(listAdapter);
            } else {
                Toast.makeText(MainActivity.this, "Erro ao carregar RMAs locais.", Toast.LENGTH_SHORT).show();
            }
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
        private class SincronizarRMAsTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RMARepository rmaRepository = new RMARepository(rmaDao, api, MainActivity.this, funcionario.getGUID());
                        rmaRepository.sincronizarRMAs(); // Esta chamada é assíncrona e atualiza o banco de dados local
                    }
                });
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mainViewModel.getRMAsLocais().observe(MainActivity.this, new Observer<List<RMAEntity>>() {
                    @Override
                    public void onChanged(List<RMAEntity> rmaEntities) {
                        if (rmaEntities != null) {
                            rmaList = convertRMAEntityListToRMAList(rmaEntities);
                            listAdapter = new ListAdapterRMA(MainActivity.this, rmaList, MainActivity.this);
                            binding.listRMA.setAdapter(listAdapter);
                            Toast.makeText(MainActivity.this, "Dados sincronizados com sucesso!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }


        private void sincronizarRMAs() {
            try {
                // Move as operações do banco de dados para aqui
                retrofitClient = RetrofitClient.getInstance();
                db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "BaseDeDadosLocal").allowMainThreadQueries().build();
                rmaDao = db.rmaDao();
                api = retrofitClient.getMyApi();

                // Crie uma instância do RMARepository
                RMARepository rmaRepository = new RMARepository(rmaDao, api, MainActivity.this, getIntent().getStringExtra("GUID"));

                // Verifique a conectividade novamente antes de sincronizar
                if (isInternetAvailable()) {
                    rmaRepository.sincronizarRMAs();
                } else {
                    // Trate o caso em que não há conectividade
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Sem conexão com a internet.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                // Após a sincronização, retorne a lista de RMAs locais
                final List<RMAEntity> rmasLocais = rmaRepository.getRMAsFromLocal();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rmasLocais != null) {
                            // Atualize a UI com a lista de RMAs locais
                            rmaList = convertRMAEntityListToRMAList(rmasLocais);
                            listAdapter = new ListAdapterRMA(MainActivity.this, rmaList, MainActivity.this);
                            binding.listRMA.setAdapter(listAdapter);

                            // Agora, você pode adicionar um Toast para verificar se a lista está sendo carregada corretamente
                            Toast.makeText(MainActivity.this, "RMAs locais carregados com sucesso!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Trate o erro aqui, se necessário
                            Log.e("Error", "Erro ao obter a lista de RMAs locais.");
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("Error", "Erro ao acessar o banco de dados em segundo plano: " + e.getMessage());
            }
        }

        private void loadRMAs() {
            rmaRepository.getRMAsFromLocal(new Consumer<List<RMAEntity>>() {
                @Override
                public void accept(List<RMAEntity> rmaEntities) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<RMA> rmaListConverted = convertRMAEntityListToRMAList(rmaEntities);
                            listAdapter = new ListAdapterRMA(MainActivity.this, rmaListConverted, MainActivity.this);
                            binding.listRMA.setAdapter(listAdapter);
                        }
                    });
                }
            });


    }
    }