package com.example.projetopdm;

import static com.example.projetopdm.LocalDataBase.FuncionarioSharedPreferences.getFuncionarioData;
import static com.example.projetopdm.LocalDataBase.FuncionarioSharedPreferences.saveFuncionarioData;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
import android.widget.Button;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    ArrayList<RMA> originalRmaList = new ArrayList<RMA>();
    ListAdapterRMA listAdapter;
    RMA rma;
    Button allButton, novoButton, progressoButton, completoButton;
    int estadoId = 0;
    int rmasCompletos = 0;
    String horasTotais="";


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // A variável está contida nos dados da Intent
            if (data.hasExtra("AtivarAPI")){
                if (data.getBooleanExtra("AtivarAPI", false)){
                    rmaList.clear();
                    originalRmaList.clear();
                    initializeDatabaseAndViewModel();
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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

        //justDoIT();

        //filtrar por estados

        allButton = findViewById(R.id.all_button);
        novoButton = findViewById(R.id.novo_button);
        progressoButton = findViewById(R.id.progresso_button);
        completoButton = findViewById(R.id.completo_button);

        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                estadoId = 0;
                filterListByEstado(estadoId);
                Context context = v.getContext();

                allButton.setBackgroundResource(R.drawable.button_active);
                allButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.main_blue));

                novoButton.setBackgroundResource(R.drawable.button_deactivated);
                novoButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_blue));

                progressoButton.setBackgroundResource(R.drawable.button_deactivated);
                progressoButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_blue));

                completoButton.setBackgroundResource(R.drawable.button_deactivated);
                completoButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_blue));
            }
        });

        novoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                estadoId = 2;
                filterListByEstado(estadoId);

                Context context = v.getContext();

                novoButton.setBackgroundResource(R.drawable.button_active);
                novoButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.main_blue));

                allButton.setBackgroundResource(R.drawable.button_deactivated);
                allButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_blue));

                progressoButton.setBackgroundResource(R.drawable.button_deactivated);
                progressoButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_blue));

                completoButton.setBackgroundResource(R.drawable.button_deactivated);
                completoButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_blue));
            }
        });

        progressoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                estadoId = 3;
                filterListByEstado(estadoId);

                Context context = v.getContext();

                progressoButton.setBackgroundResource(R.drawable.button_active);
                progressoButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.main_blue));

                allButton.setBackgroundResource(R.drawable.button_deactivated);
                allButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_blue));

                novoButton.setBackgroundResource(R.drawable.button_deactivated);
                novoButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_blue));

                completoButton.setBackgroundResource(R.drawable.button_deactivated);
                completoButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_blue));
            }
        });

        completoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                estadoId = 1;
                filterListByEstado(estadoId);

                Context context = v.getContext();

                completoButton.setBackgroundResource(R.drawable.button_active);
                completoButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.main_blue));

                novoButton.setBackgroundResource(R.drawable.button_deactivated);
                novoButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_blue));

                progressoButton.setBackgroundResource(R.drawable.button_deactivated);
                progressoButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_blue));

                allButton.setBackgroundResource(R.drawable.button_deactivated);
                allButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.secondary_blue));
            }
        });
    }

    private void filterListByEstado(int estadoId) {
        // Create a new list to store filtered items


        // Check if estadoId is 0 (representing "Todos")
        if (estadoId == 0) {
            // Show all states
            showAllStates();
            return;
        }

        // Loop through the original list and add items that match the selected filter
        ArrayList<RMA> filteredData = new ArrayList<>();
        for (RMA rma : originalRmaList) {
            if (rma.getEstadoRMAId() == estadoId) {
                filteredData.add(rma);
            }
        }
        listAdapter.clear();
        listAdapter.addAll(filteredData);
        listAdapter.notifyDataSetChanged();
    }

    private void showAllStates() {
        listAdapter.clear();
        listAdapter.addAll(originalRmaList);
        listAdapter.notifyDataSetChanged();
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
            rmasCompletos = rmaDao.getRMAsCompletosByFuncionarioId(funcionario.getId());
            List<String> horas = new ArrayList<>();
            for (RMA rma:rmaList){
                originalRmaList.add(rma);
                if(!rma.getHorasTrabalhadas().equals("null")){
                    horas.add(rma.getHorasTrabalhadas());
                }
            }
            if (!horas.isEmpty()){
                horasTotais = somaHoras(horas);
            }
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
            intent.putExtra("RMACompletos", rmasCompletos);
            intent.putExtra("HorasTotais", horasTotais);
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
            for (RMA rma:rmaList){
                originalRmaList.add(rma);
            }
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
                        List<String> horasTotal = new ArrayList<>();
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
                                horasTotal.add(horas);
                            } else {
                                horas = "null";
                            }
                            RMAEntity rma = new RMAEntity(rmaObj.get("Id").getAsInt(), rmaObj.get("RMA").getAsString(), rmaObj.get("DescricaoCliente").getAsString(), rmaObj.get("DataCriacao").getAsString(), dataAb, dataF, rmaObj.get("EstadoRMA").getAsString(), rmaObj.get("EstadoRMAId").getAsInt(), rmaObj.get("FuncionarioId").getAsInt(),horas);
                            RMA x =  new RMA(rmaObj.get("Id").getAsInt(), rmaObj.get("RMA").getAsString(), rmaObj.get("DescricaoCliente").getAsString(), rmaObj.get("DataCriacao").getAsString(), dataAb, dataF, rmaObj.get("EstadoRMA").getAsString(), rmaObj.get("EstadoRMAId").getAsInt(), rmaObj.get("FuncionarioId").getAsInt(),horas);
                            rmaListEnt.add(rma);
                            rmaList.add(x);
                            originalRmaList.add(x);
                        }

                        if (!horasTotal.isEmpty()){
                             horasTotais = somaHoras(horasTotal);
                        }

                        rmaDao.insertAll(rmaListEnt);
                        SincronizarRMAsTask();
                    }
                    if (response.body().get("RMACompletos").getAsInt()!=0){
                        rmasCompletos = response.body().get("RMACompletos").getAsInt();
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

    public static String somaHoras(List<String> horas) {
        // Inicializa a soma como zero
        LocalTime sum = LocalTime.of(0, 0);

        // Itera sobre a lista de horários e soma
        for (String h : horas) {
            LocalTime localTime = parseFlexibleTimeFormat(h);
            if (localTime != null) {
                sum = sum.plusHours(localTime.getHour()).plusMinutes(localTime.getMinute());
            }
        }

        // Formata a soma para saída
        return formatTimeBasedOnPattern(sum);
    }

    public static LocalTime parseFlexibleTimeFormat(String timeString) {
        // Define múltiplos padrões para tentar o parse
        String[] patterns = {"H:m", "HH:mm", "H:mm", "HH:m"};

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalTime.parse(timeString, formatter);
            } catch (DateTimeParseException e) {
                // Se o parse falhar, tente o próximo padrão
            }
        }
        // Retorna null se nenhum padrão for bem-sucedido
        return null;
    }
    public static String formatTimeBasedOnPattern(LocalTime time) {
        boolean hourNeedsPadding = time.getHour() < 10;
        boolean minuteNeedsPadding = time.getMinute() < 10;

        String pattern = (hourNeedsPadding ? "HH" : "H") + ":" + (minuteNeedsPadding ? "mm" : "m");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return time.format(formatter);
    }
}