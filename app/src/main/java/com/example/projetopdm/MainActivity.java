package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.Modelos.Funcionario;
import com.example.projetopdm.Modelos.RMA;
import com.example.projetopdm.databinding.ActivityMainBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout loading;
    public static final int MEU_REQUEST_CODE = 1;
    ActivityMainBinding binding;
    Funcionario funcionario;

    SearchView searchView;

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
                    API();
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
        int Id = getIntent().getIntExtra("Id", 0);
        String nome = getIntent().getStringExtra("Nome");
        String email = getIntent().getStringExtra("Email");
        String GUID = getIntent().getStringExtra("GUID");
        String pin = getIntent().getStringExtra("Pin");
        String contacto = getIntent().getStringExtra("Contacto");
        String imagemFuncionario = getIntent().getStringExtra("ImagemFuncionario");
        String estadoFuncionario = getIntent().getStringExtra("EstadoFuncionario");
        int estadoFuncionarioId = getIntent().getIntExtra("EstadoFuncionarioId", 0);

        funcionario = new Funcionario(Id, GUID, nome, email, contacto, pin, imagemFuncionario, estadoFuncionarioId, estadoFuncionario);

        setContentView(binding.getRoot());

        ImageView perfil_btn = findViewById(R.id.perfil_btn);
        searchView = findViewById(R.id.searchView);

        // Adicionar um OnClickListener ao LinearLayout
        perfil_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), Perfil.class);
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
            }
        });
        ImageView img = findViewById(R.id.perfil_btn);
        Bitmap bitmap = StringToBitMap(imagemFuncionario);
        img.setImageBitmap(bitmap);

        API();


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

    private void API(){
        if(isInternetAvailable()) {
            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().GetRMASByFuncionario(funcionario.getGUID());
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                    if (responseObj.get("Success").getAsBoolean()) {
                        if (!response.body().has("RMA")){
                            Toast.makeText(MainActivity.this, "Não existem RMA's", Toast.LENGTH_SHORT).show();
                        }else{
                            JsonArray rmaListObj = response.body().get("RMA").getAsJsonArray();
                            for (int i = 0; i < rmaListObj.size(); i++) {
                                JsonObject rmaObj = rmaListObj.get(i).getAsJsonObject();
                                String dataAb = "";
                                String dataF = "";
                                if (rmaObj.get("DataAbertura") != null){
                                    dataAb = (rmaObj.get("DataAbertura").getAsString());
                                }else{
                                    dataAb = "null";
                                }
                                if (rmaObj.get("DataFecho") != null){
                                    dataF = (rmaObj.get("DataFecho").getAsString());
                                }
                                else{
                                    dataF = "null";
                                }
                                rma = new RMA(rmaObj.get("Id").getAsInt(), rmaObj.get("RMA").getAsString(), rmaObj.get("DescricaoCliente").getAsString(),rmaObj.get("DataCriacao").getAsString() ,dataAb , dataF, rmaObj.get("EstadoRMA").getAsString(), rmaObj.get("EstadoRMAId").getAsInt(), rmaObj.get("FuncionarioId").getAsInt());
                                if (rmaObj.get("HorasTrabalhadas")!=null) rma.setHorasTrabalhadas(rmaObj.get("HorasTrabalhadas").getAsString());
                                rmaList.add(rma);
                            }
                            listAdapter = new ListAdapterRMA(MainActivity.this, rmaList, MainActivity.this);
                            binding.listRMA.setAdapter(listAdapter);
                            loading.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        loading.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Aconteceu algo errado ao tentar carregar os RMA's", Toast.LENGTH_SHORT).show();
                    loading.setVisibility(View.INVISIBLE);
                }
            });
        }else {
            Toast.makeText(MainActivity.this, "Sem ligação à internet", Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.INVISIBLE);
        }
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