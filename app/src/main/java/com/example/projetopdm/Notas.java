package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.Modelos.NotaRMA;
import com.example.projetopdm.Modelos.RMA;
import com.example.projetopdm.databinding.ActivityMainBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.projetopdm.databinding.ActivityNotasBinding;
import com.google.gson.JsonParser;



public class Notas extends AppCompatActivity {

    ConstraintLayout loading;
    public static final int MEU_REQUEST_CODE = 1;
    ActivityNotasBinding binding;
    ActivityMainBinding bindingMain;
    int RMAId;

    RMA rma = new RMA();
    NotaRMA notaRMA;

    ArrayList<NotaRMA> rmaList = new ArrayList<NotaRMA>();
    ListaAdapterRMADetails listAdapter;
    Button novaNova_btn;
    Button change_status_btn;

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
        binding = ActivityNotasBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

bindingMain = ActivityMainBinding.inflate(getLayoutInflater());


        RMAId = getIntent().getIntExtra("RMAId",0);
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);
        LinearLayout popup = findViewById(R.id.popup);
        Button closePopup = findViewById(R.id.closePopup);
        novaNova_btn = (Button) findViewById(R.id.novaNota_btn);
        change_status_btn = (Button) findViewById(R.id.change_status_btn);
        popup.setVisibility(View.INVISIBLE);
        rmaList.clear();

        API();

        closePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void API(){
        if (isInternetAvailable()){
            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().GetRMAById(RMAId);

            call.enqueue(new Callback<JsonObject>(){
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                    if (responseObj.get("Success").getAsBoolean()){

                        JsonObject rmaObj = response.body().get("RMA").getAsJsonObject();

                        rma.setId(rmaObj.get("Id").getAsInt());
                        rma.setRMA(rmaObj.get("RMA").getAsString());
                        rma.setDescricaoCliente(rmaObj.get("DescricaoCliente").getAsString());
                        rma.setDataCriacao(rmaObj.get("DataCriacao").getAsString());
                        if (rmaObj.get("DataAbertura")!=null) rma.setDataAbertura(rmaObj.get("DataAbertura").getAsString());
                        if (rmaObj.get("DataFecho")!=null) rma.setDataFecho(rmaObj.get("DataFecho").getAsString());
                        if (rmaObj.get("HorasTrabalhadas")!=null) rma.setHorasTrabalhadas(rmaObj.get("HorasTrabalhadas").getAsString());
                        rma.setEstadoRMA(rmaObj.get("EstadoRMA").getAsString());
                        rma.setEstadoRMAId(rmaObj.get("EstadoRMAId").getAsInt());
                        rma.setFuncionarioId(rmaObj.get("FuncionarioId").getAsInt());

                        TextView rMA = findViewById(R.id.ticketsTitle);
                        TextView dataRma = findViewById(R.id.datarma);
                        TextView descricao = findViewById(R.id.textView3);

                        rMA.setText(rma.getRMA());
                        dataRma.setText(rma.getDataCriacao());
                        descricao.setText(rma.getDescricaoCliente());

                        if (response.body().has("RMANotas")) {
                            JsonArray NotasRMA = response.body().get("RMANotas").getAsJsonArray();
                            if (NotasRMA.get(0).getAsJsonObject().get("Id").getAsInt() != 0) {
                                for (int i = 0; i < NotasRMA.size(); i++) {
                                    JsonObject notaRMAObj = NotasRMA.get(i).getAsJsonObject();
                                    notaRMA = new NotaRMA();
                                    notaRMA.setId(notaRMAObj.get("Id").getAsInt());
                                    notaRMA.setTitulo(notaRMAObj.get("Titulo").getAsString());
                                    notaRMA.setDataCriacao(notaRMAObj.get("DataCriacao").getAsString());
                                    notaRMA.setNota(notaRMAObj.get("Nota").getAsString());
                                    notaRMA.setRMAId(notaRMAObj.get("RMAId").getAsInt());
                                    if (notaRMAObj.get("ImagemNotaId") != null)
                                        notaRMA.setImagemNotaId(notaRMAObj.get("ImagemNotaId").getAsInt());
                                    if (notaRMAObj.get("ImagemNota") != null)
                                        notaRMA.setImagemNota(notaRMAObj.get("ImagemNota").getAsString());
                                    rmaList.add(notaRMA);
                                }
                                listAdapter = new ListaAdapterRMADetails(Notas.this, rmaList, Notas.this);
                                binding.notas.setAdapter(listAdapter);
                                loading.setVisibility(View.INVISIBLE);
                            }
                        }


                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(Notas.this, "Aconteceu algo errado ao tentar carregar o RMA", Toast.LENGTH_SHORT).show();
                    loading.setVisibility(View.INVISIBLE);
                }
            });


            novaNova_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Nota.class);
                    intent.putExtra("RMAId", RMAId);
                    startActivityForResult(intent, MEU_REQUEST_CODE);
                }
            });

            change_status_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String horaEntrada= "09:00";
                    String horaSaida= "18:00";
                    String horaPausa= "13:00";
                    String horaRetorno= "14:00";
                    String horaAtual = new SimpleDateFormat("HH:mm").format(new Date());

                    //verificar se está dentro do horário de trabalho
                        if (TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaEntrada.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaEntrada.split(":")[1]))) <= TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaAtual.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaAtual.split(":")[1]))) &&
                                TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaSaida.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaSaida.split(":")[1]))) >= TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaAtual.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaAtual.split(":")[1])))) {
                            //verificar se está dentro do horário de pausa
                            if (TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaPausa.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaPausa.split(":")[1]))) <= TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaAtual.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaAtual.split(":")[1]))) &&
                                    TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaRetorno.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaRetorno.split(":")[1]))) >= TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaAtual.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaAtual.split(":")[1])))) {
                                //dentro do horário de pausa
                                Toast.makeText(getApplicationContext(), "Tenha calma está na sua pausa descanse um bocado", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }else {
                            //fora do horário de trabalho
                            //se for antes do inicio
                            if (TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaEntrada.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaEntrada.split(":")[1]))) >= TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaAtual.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaAtual.split(":")[1])))) {
                                Toast.makeText(getApplicationContext(), "Tenha calma so começa a trabalhar as 9", Toast.LENGTH_LONG).show();
                                return;
                            }else {
                                //se for depois do fim
                                if (TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaSaida.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaSaida.split(":")[1]))) <= TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaAtual.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaAtual.split(":")[1])))) {
                                    Toast.makeText(getApplicationContext(), "Vá para casa e descanse ja passa da hora de saída", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                        }

                    loading.setVisibility(View.VISIBLE);
                    String dataAtual = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
                    String request = "";
                    if (rma.getEstadoRMAId() == 2){ //1= Completo 2 = Novo 3= Em progresso
                        rma.setEstadoRMAId(3);
                        rma.setEstadoRMA("Em Progresso");
                        rma.setDataAbertura(dataAtual);

                        request = "{"
                                + " \"Id\": \"" + rma.getId() + "\", "
                                + " \"RMA\": \"" + rma.getRMA() + "\", "
                                + " \"DescricaoCliente\": \"" + rma.getDescricaoCliente() + "\", "
                                + " \"DataCriacao\": \"" + rma.getDataCriacao() + "\", "
                                + " \"DataAbertura\": \"" + rma.getDataAbertura() + "\", "
                                + " \"DataFecho\": \"" + "" + "\", "
                                + " \"HorasTrabalhadas\": \"" + "" + "\", "
                                + " \"EstadoRMA\": \"" + rma.getEstadoRMAId() + "\", "
                                + " \"FuncionarioId\": \"" + rma.getFuncionarioId() + "\" }";

                    }else if (rma.getEstadoRMAId() == 3) {
                        rma.setEstadoRMAId(1);
                        rma.setEstadoRMA("Completo");
                        rma.setDataFecho(dataAtual);

                        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                        try {
                            // Convertendo as strings para objetos Date
                            Date dataAbertura = format.parse(rma.getDataAbertura());
                            Date dataFechamento = format.parse(rma.getDataFecho());

                            long different = dataFechamento.getTime() - dataAbertura.getTime();

                            long secondsInMilli = 1000;
                            long minutesInMilli = secondsInMilli * 60;
                            long hoursInMilli = minutesInMilli * 60;
                            long daysInMilli = hoursInMilli * 24;

                            long elapsedDays = different / daysInMilli;
                            different = different % daysInMilli;

                            long elapsedHours = different / hoursInMilli;
                            different = different % hoursInMilli;

                            long elapsedMinutes = different / minutesInMilli;
                            different = different % minutesInMilli;

                            String horasTrabalhadas = (elapsedDays*8)+elapsedHours + ":" + elapsedMinutes;

                            rma.setHorasTrabalhadas(horasTrabalhadas);

                            request = "{"
                                    + " \"Id\": \"" + rma.getId() + "\", "
                                    + " \"RMA\": \"" + rma.getRMA() + "\", "
                                    + " \"DescricaoCliente\": \"" + rma.getDescricaoCliente() + "\", "
                                    + " \"DataCriacao\": \"" + rma.getDataCriacao() + "\", "
                                    + " \"DataAbertura\": \"" + rma.getDataAbertura() + "\", "
                                    + " \"DataFecho\": \"" + rma.getDataFecho() + "\", "
                                    + " \"HorasTrabalhadas\": \"" + rma.getHorasTrabalhadas() + "\", "
                                    + " \"EstadoRMA\": \"" + rma.getEstadoRMAId() + "\", "
                                    + " \"FuncionarioId\": \"" + rma.getFuncionarioId() + "\" }";

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    JsonObject body = new JsonParser().parse(request).getAsJsonObject();
                    Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().CreateOrUpdate_RMA(body);

                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                            if (responseObj.get("Success").getAsBoolean()){
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("AtivarAPI", true);
                                setResult(Activity.RESULT_OK, resultIntent);
                                if (rma.getEstadoRMAId()==1){
                                    //encerrar esta janela e voltar para a main
                                    finish();
                                }
                                loading.setVisibility(View.INVISIBLE);
                                Toast.makeText(getApplicationContext(), "Estado do RMA alterado para: " + rma.getEstadoRMA(), Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "Erro ao alterar estado do RMA", Toast.LENGTH_LONG).show();
                                loading.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Erro ao alterar estado do RMA", Toast.LENGTH_LONG).show();
                            loading.setVisibility(View.INVISIBLE);
                        }
                    });






                }
            });




        }else {
            Toast.makeText(Notas.this, "Não tem acesso à internet", Toast.LENGTH_SHORT).show();
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
}