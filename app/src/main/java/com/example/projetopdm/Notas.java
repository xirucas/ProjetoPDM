package com.example.projetopdm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.room.Room;

import android.annotation.SuppressLint;
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
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.LocalDataBase.AppDatabase;
import com.example.projetopdm.LocalDataBase.DAOs.NotaRMADao;
import com.example.projetopdm.LocalDataBase.DAOs.RMADao;
import com.example.projetopdm.LocalDataBase.Entity.NotaRMAEntity;
import com.example.projetopdm.LocalDataBase.UpdateBD;
import com.example.projetopdm.Modelos.NotaRMA;
import com.example.projetopdm.Modelos.RMA;
import com.example.projetopdm.databinding.ActivityMainBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.projetopdm.databinding.ActivityNotasBinding;
import com.google.gson.JsonParser;



public class Notas extends AppCompatActivity {


    public static final int MEU_REQUEST_CODE = 1;
    ActivityNotasBinding binding;
    ActivityMainBinding bindingMain;
    ConstraintLayout loading;
    int RMAId;
    RMA rma = new RMA();
    UpdateBD updateBD;
    RMADao rmaDao;
    NotaRMADao notaRMADao;
    ArrayList<NotaRMA> rmaList = new ArrayList<NotaRMA>();
    ListaAdapterRMADetails listAdapter;
    Button novaNova_btn;
    Button change_status_btn;
    RetrofitClient retrofitClient;
    Context contextPrincipal;
    int estadoId;
    RMA rmaX = new RMA();
    AppDatabase db;
    ArrayList<NotaRMA> notasDoRMAX = new ArrayList<>();
    boolean mudancas = false;
    boolean atualizarLista=false;
    RelativeLayout popup;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // A variável está contida nos dados da Intent
            if (data.hasExtra("AtivarAPI")){
                if (data.getBooleanExtra("AtivarAPI", false)){
                    loading.setVisibility(View.VISIBLE);
                    rmaList.clear();
                    if(!isInternetAvailable()){
                        loadNotas();
                    }
                    if (isInternetAvailable()) {
                        loadNotasAPI();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("AtivarAPI", true);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotasBinding.inflate(getLayoutInflater());
        contextPrincipal = this;
        setContentView(binding.getRoot());

        bindingMain = ActivityMainBinding.inflate(getLayoutInflater());

        loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);
        loading.setClickable(true);
        loading.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true; // Consumir o toque aqui
            }
        });

        Button backButton = findViewById(R.id.back_button);
        RMAId = getIntent().getIntExtra("RMAId",0);
        String rmaTxt= getIntent().getStringExtra("RMA");
        String rmaDataTxt=getIntent().getStringExtra("Data");
        String rmaDescricao= getIntent().getStringExtra("Descricao");
        String rmaHoras = getIntent().getStringExtra("horas");

        TextView rmaTxtView = findViewById(R.id.ticketsTitle);
        TextView rmaDataTxtView = findViewById(R.id.datarma);
        TextView rmaDescricaoTxtView = findViewById(R.id.textView3);
        TextView rmaHorasTrabalhadas = findViewById(R.id.completo);
        rmaTxtView.setText(rmaTxt);
        rmaDataTxtView.setText(rmaDataTxt);
        rmaDescricaoTxtView.setText(rmaDescricao);
        if (!rmaHoras.equals("null")) {
            //separar em horas e minutos
            String[] horas = rmaHoras.split(":");
            String minutos = horas[1];
            String horasTrabalhadas = horas[0];

            //se for menos de uma hora
            if (Integer.parseInt(horasTrabalhadas) == 0) {
                rmaHorasTrabalhadas.setText("RMA concluído em: " + minutos + " min");
            }else if (Integer.parseInt(horasTrabalhadas) >= 8) {
                //se for mais de 8 horas conta como dia
                int dias = Integer.parseInt(horasTrabalhadas) / 8;
                int horasRestantes = Integer.parseInt(horasTrabalhadas) % 8;
                rmaHorasTrabalhadas.setText("RMA concluído em: " + dias + " dias " + horasRestantes + "h:" + minutos + "min");
            } else {
                rmaHorasTrabalhadas.setText("RMA concluído em: " + horasTrabalhadas + "h:" + minutos + "min");
            }
        }else {
         rmaHorasTrabalhadas.setVisibility(View.GONE);
        }


        popup = findViewById(R.id.popup);

        popup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true; // Consumir o toque aqui
            }
        });

        Button closePopup = findViewById(R.id.closePopup);
        novaNova_btn = (Button) findViewById(R.id.novaNota_btn);
        change_status_btn = (Button) findViewById(R.id.change_status_btn);
        popup.setVisibility(View.INVISIBLE);
        mudancas= false;

        retrofitClient = RetrofitClient.getInstance();

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "BaseDeDadosLocal").fallbackToDestructiveMigration().allowMainThreadQueries().build();



        notaRMADao = db.notaRMADao();
        rmaDao = db.rmaDao();


        updateBD =new UpdateBD(this);

        if (rmaDao.getRMAById(RMAId).toRMA()!=null){
            rmaX = rmaDao.getRMAById(RMAId).toRMA();
        }


        estadoId = rmaX.getEstadoRMAId();
        closePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopup.setEnabled(false);
                popup.setVisibility(View.INVISIBLE);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                backButton.setEnabled(false);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("AtivarAPI", true);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        novaNova_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                novaNova_btn.setEnabled(false);
                Intent intent = new Intent(getApplicationContext(), Nota.class);
                intent.putExtra("RMAId", RMAId);
                intent.putExtra("estadoRMA", rma.getEstadoRMAId());
                intent.putExtra("Update","Novo");
                startActivityForResult(intent, MEU_REQUEST_CODE);
            }
        });

        change_status_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String horaEntrada = "09:00";
                String horaSaida = "18:00";
                String horaPausa = "13:00";
                String horaRetorno = "14:00";
                String horaAtual = new SimpleDateFormat("HH:mm").format(new Date());

                //verificar se está dentro do horário de trabalho
                /*if (TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaEntrada.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaEntrada.split(":")[1]))) <= TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaAtual.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaAtual.split(":")[1]))) &&
                        TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaSaida.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaSaida.split(":")[1]))) >= TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaAtual.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaAtual.split(":")[1])))) {
                    //verificar se está dentro do horário de pausa
                    if (TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaPausa.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaPausa.split(":")[1]))) <= TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaAtual.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaAtual.split(":")[1]))) &&
                            TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaRetorno.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaRetorno.split(":")[1]))) >= TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaAtual.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaAtual.split(":")[1])))) {
                        //dentro do horário de pausa
                        Toast.makeText(getApplicationContext(), "Tenha calma está na sua pausa descanse um bocado", Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    //fora do horário de trabalho
                    //se for antes do inicio
                    if (TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaEntrada.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaEntrada.split(":")[1]))) >= TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaAtual.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaAtual.split(":")[1])))) {
                        Toast.makeText(getApplicationContext(), "Tenha calma so começa a trabalhar as 9", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        //se for depois do fim
                        if (TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaSaida.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaSaida.split(":")[1]))) <= TimeUnit.MILLISECONDS.toMinutes(TimeUnit.HOURS.toMillis(Long.parseLong(horaAtual.split(":")[0])) + TimeUnit.MINUTES.toMillis(Long.parseLong(horaAtual.split(":")[1])))) {
                            Toast.makeText(getApplicationContext(), "Vá para casa e descanse ja passa da hora de saída", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }*/



                String dataAtual = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

                if (isInternetAvailable()) {
                    change_status_btn.setEnabled(false);
                    String request = "";
                    if (rmaX.getEstadoRMAId() == 2) { //1= Completo 2 = Novo 3= Em progresso
                        rmaX.setEstadoRMAId(3);
                        rmaX.setEstadoRMA("Em Progresso");
                        rmaX.setDataAbertura(dataAtual);

                        request = "{"
                                + " \"Id\": \"" + rmaX.getId() + "\", "
                                + " \"RMA\": \"" + rmaX.getRMA() + "\", "
                                + " \"DescricaoCliente\": \"" + rmaX.getDescricaoCliente() + "\", "
                                + " \"DataCriacao\": \"" + rmaX.getDataCriacao() + "\", "
                                + " \"DataAbertura\": \"" + rmaX.getDataAbertura() + "\", "
                                + " \"DataFecho\": \"" + "" + "\", "
                                + " \"HorasTrabalhadas\": \"" + "" + "\", "
                                + " \"EstadoRMA\": \"" + rmaX.getEstadoRMAId() + "\", "
                                + " \"FuncionarioId\": \"" + rmaX.getFuncionarioId() + "\" }";

                    } else if (rmaX.getEstadoRMAId() == 3) {
                        rmaX.setEstadoRMAId(1);
                        rmaX.setEstadoRMA("Completo");
                        rmaX.setDataFecho(dataAtual);

                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        try {
                            // Convertendo as strings para objetos Date
                            Date dataAbertura = format.parse(rmaX.getDataAbertura());
                            Date dataFechamento = format.parse(rmaX.getDataFecho());
                            Date dataAberturaFinal = new Date(dataAbertura.getYear(), dataAbertura.getMonth(), dataAbertura.getDate(), dataAbertura.getHours(), dataAbertura.getMinutes());
                            Date dataFechamentoFinal = new Date(dataFechamento.getYear(), dataFechamento.getMonth(), dataFechamento.getDate(), dataFechamento.getHours(), dataFechamento.getMinutes());

                            /*long different = dataFechamento.getTime() - dataAbertura.getTime();

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

                            String horasTrabalhadas ="";
                            if (elapsedDays>0){
                                horasTrabalhadas = (elapsedDays*8)+elapsedHours + ":" + elapsedMinutes;
                            } else if (elapsedHours >= 8) {

                                    horasTrabalhadas = (elapsedHours / 3) + ":" + elapsedMinutes;

                            }else {
                                horasTrabalhadas = elapsedHours + ":" + elapsedMinutes;
                            }*/

                            String horasTrabalhadas = "";
                            //chamar worktimecalculator
                            WorkTimeCalculator workTimeCalculator = new WorkTimeCalculator(dataAberturaFinal, dataFechamentoFinal);
                            workTimeCalculator.setWeekends(Calendar.SATURDAY, Calendar.SUNDAY);
                            workTimeCalculator.setWorkingTime("09:00", "18:00");
                            Double dias = workTimeCalculator.getDays();
                            Integer minutos = workTimeCalculator.getMinutes();
                            //transformar os minutos em horas e minutos e os dias em horas
                            int minutosRestantes = minutos % 60;
                            int horas = (int) (dias * 8);
                            horasTrabalhadas = horas + ":" + minutosRestantes;
                            rmaX.setHorasTrabalhadas(horasTrabalhadas);

                            request = "{"
                                    + " \"Id\": \"" + rmaX.getId() + "\", "
                                    + " \"RMA\": \"" + rmaX.getRMA() + "\", "
                                    + " \"DescricaoCliente\": \"" + rmaX.getDescricaoCliente() + "\", "
                                    + " \"DataCriacao\": \"" + rmaX.getDataCriacao() + "\", "
                                    + " \"DataAbertura\": \"" + rmaX.getDataAbertura() + "\", "
                                    + " \"DataFecho\": \"" + rmaX.getDataFecho() + "\", "
                                    + " \"HorasTrabalhadas\": \"" + rmaX.getHorasTrabalhadas() + "\", "
                                    + " \"EstadoRMA\": \"" + rmaX.getEstadoRMAId() + "\", "
                                    + " \"FuncionarioId\": \"" + rmaX.getFuncionarioId() + "\" }";

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
                            if (responseObj.get("Success").getAsBoolean()) {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("AtivarAPI", true);
                                setResult(Activity.RESULT_OK, resultIntent);

                                if (rmaX.getEstadoRMAId() == 1) {
                                    //encerrar esta janela e voltar para a main
                                    finish();
                                }
                                change_status_btn.setText("Concluir RMA");
                                change_status_btn.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.completo));
                                change_status_btn.setEnabled(true);
                                Toast.makeText(getApplicationContext(), "Estado do RMA alterado para: " + rmaX.getEstadoRMA(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Erro ao alterar estado do RMA", Toast.LENGTH_LONG).show();
                                change_status_btn.setEnabled(true);
                            }

                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Erro ao alterar estado do RMA", Toast.LENGTH_LONG).show();
                            change_status_btn.setEnabled(true);
                        }
                    });


                }else{
                    Toast.makeText(getApplicationContext(), "Só é possível alterar o estado do RMA estando com conectividade a internet.", Toast.LENGTH_LONG).show();
                }

            }
        });



        if (!isInternetAvailable()){
            Log.e("Notas","sem net, tentar carregar local");
            loadNotas();

            if (rmaX.getEstadoRMAId() == 2 || rmaX.getEstadoRMAId() == 3) {
                novaNova_btn.setVisibility(View.VISIBLE);
                change_status_btn.setVisibility(View.VISIBLE);

                if (rmaX.getEstadoRMAId() == 2) {
                    change_status_btn.setText("Iniciar RMA");
                    change_status_btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.main_blue));
                } else if (rmaX.getEstadoRMAId() == 3) {
                    change_status_btn.setText("Concluir RMA");
                    change_status_btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.completo));
                }
            } else {
                novaNova_btn.setEnabled(false);
                novaNova_btn.setVisibility(View.INVISIBLE);
                change_status_btn.setVisibility(View.GONE);
            }

            loading.setVisibility(View.INVISIBLE);

        }

        if (isInternetAvailable()) {
            if (!updateBD.mudancas(rmaX.getId())){
              loadNotasAPI();
            }
            if (updateBD.mudancas(rmaX.getId())){

                /*new CountDownTimer(3000, 1000) { // 30000ms = 30s total, 1000ms = 1s intervalo

                    public void onTick(long millisUntilFinished) {
                        // Código a executar em cada intervalo do timer, por exemplo, atualizar um TextView.
                    }

                    public void onFinish() {
                        recreate();
                    }
                }.start();
                */

                updateBD.updateBaseDeDados(rmaX.getId(), new UpdateBD.UpdateListener() {
                    @Override
                    public void onUpdateComplete() {
                        loadNotasAPI();
                    }
                });




            }
        }

    }

    private void loadNotasAPI(){
        Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().GetRMAById(RMAId);


        call.enqueue(new Callback<JsonObject>(){
            @SuppressLint("SuspiciousIndentation")
            @Override
            public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                Log.d("Notas", "Chamada para a API GetRMAById realizada com sucesso.");

                if (responseObj.get("Success").getAsBoolean()){
                    Log.d("Notas", "Dados do RMA obtidos da API com sucesso.");
                    JsonObject rmaObj = response.body().get("RMA").getAsJsonObject();
                    RMA rma =new RMA();

                    rma.setId(rmaObj.get("Id").getAsInt());
                    rma.setRMA(rmaObj.get("RMA").getAsString());
                    rma.setDescricaoCliente(rmaObj.get("DescricaoCliente").getAsString());
                    rma.setDataCriacao(rmaObj.get("DataCriacao").getAsString());
                    if (rmaObj.get("DataAbertura")!=null) rma.setDataAbertura(rmaObj.get("DataAbertura").getAsString());
                    if (rmaObj.get("DataFecho")!=null) rma.setDataFecho(rmaObj.get("DataFecho").getAsString());
                    rma.setEstadoRMA(rmaObj.get("EstadoRMA").getAsString());
                    rma.setEstadoRMAId(rmaObj.get("EstadoRMAId").getAsInt());
                    rma.setFuncionarioId(rmaObj.get("FuncionarioId").getAsInt());

                    if (rma.getEstadoRMAId() == 2 || rma.getEstadoRMAId() == 3){
                        novaNova_btn.setVisibility(View.VISIBLE);
                        change_status_btn.setVisibility(View.VISIBLE);
                        if (rma.getEstadoRMAId() == 2) {
                            change_status_btn.setText("Iniciar RMA");
                            change_status_btn.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.main_blue));
                        } else if (rma.getEstadoRMAId() == 3) {
                            change_status_btn.setText("Concluir RMA");
                            change_status_btn.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.completo));
                        }
                    } else {
                        novaNova_btn.setEnabled(false);
                        novaNova_btn.setVisibility(View.INVISIBLE);

                        change_status_btn.setVisibility(View.GONE);
                    }


                    int imgID;

                    if (response.body().has("RMANotas")) {
                        JsonArray NotasRMA = response.body().get("RMANotas").getAsJsonArray();
                        Log.d("Notas", "Notas RMA obtidas da API com sucesso.");
                        List<NotaRMAEntity> rmaListEntity = new ArrayList<>();
                        if (NotasRMA.get(0).getAsJsonObject().get("Id").getAsInt() != 0) {
                            for (int i = 0; i < NotasRMA.size(); i++) {
                                String imgBitMap= null;
                                JsonObject notaRMAObj = NotasRMA.get(i).getAsJsonObject();
                                NotaRMA notaRMA = new NotaRMA();
                                notaRMA.setId(notaRMAObj.get("Id").getAsInt());
                                notaRMA.setTitulo(notaRMAObj.get("Titulo").getAsString());
                                notaRMA.setDataCriacao(notaRMAObj.get("DataCriacao").getAsString());
                                notaRMA.setNota(notaRMAObj.get("Nota").getAsString());
                                notaRMA.setRMAId(RMAId);
                                if (notaRMAObj.get("ImagemNotaId") != null)
                                    notaRMA.setImagemNotaId(notaRMAObj.get("ImagemNotaId").getAsInt());
                                imgID=notaRMAObj.get("ImagemNotaId").getAsInt();
                                if (notaRMAObj.get("ImagemNota") != null){
                                    Log.i("Notas","Imagem " + notaRMAObj.get("ImagemNota").getAsString());
                                    notaRMA.setImagemNota(notaRMAObj.get("ImagemNota").getAsString());
                                    imgBitMap = notaRMAObj.get("ImagemNota").getAsString();

                                }
                                NotaRMAEntity notaRMAEntiTy = new NotaRMAEntity();
                                if (imgBitMap==null){
                                    notaRMAEntiTy = notaRMA.toNotaRMAEntity();
                                } else if (imgBitMap!=null) {
                                    Uri img = saveImageToStorage(imgBitMap, contextPrincipal,notaRMA.getTitulo()+" "+notaRMA.getId());
                                    notaRMAEntiTy = notaRMA.toNotaRMAEntity();

                                    notaRMAEntiTy.setImagemNota(img.toString());
                                }

                                if (notaRMADao.getNotaRMAById(notaRMAEntiTy.getId())!=null){
                                    if (notaRMADao.getNotaRMAById(notaRMAEntiTy.getId()).getOffSync()!=null){
                                        notaRMAEntiTy.setOffSync(notaRMADao.getNotaRMAById(notaRMAEntiTy.getId()).getOffSync());
                                    }
                                }

                                rmaListEntity.add(notaRMAEntiTy);

                                rmaList.add(notaRMA);

                            }

                            notaRMADao.insertAllNotas(rmaListEntity);

                        }
                    }

                    loadNotas();

                    listAdapter.notifyDataSetChanged();


                }
                loading.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Notas", "Sem conectividade de rede. Não foi possível sincronizar os dados.");
            }
        });
    }



    private void loadNotas() {



        if(!isInternetAvailable()){

            notasDoRMAX.clear();
            Log.e("Notas","id do rma "+ RMAId);
            for (NotaRMA x:convertNotaRMAEntityListToNotaRMAList(notaRMADao.getAllNotasRMA())) {
                if (x.getRMAId()==RMAId){
                    if (notaRMADao.getNotaById(x.getId()).getOffSync() != null){
                        if (!notaRMADao.getNotaById(x.getId()).getOffSync().equals("apagado") || !notaRMADao.getNotaRMAById(x.getId()).getOffSync().equals("novoApagado")){
                            Log.e("Notas","id do RMA da nota  "+ x.getRMAId());
                            notasDoRMAX.add(x);
                        }
                    }else {
                        Log.e("Notas","id do RMA da nota  "+ x.getRMAId());
                        notasDoRMAX.add(x);
                    }
                }
            }

            listAdapter = new ListaAdapterRMADetails(Notas.this,notasDoRMAX , Notas.this);
            binding.notas.setAdapter(listAdapter);
            loading.setVisibility(View.INVISIBLE);
        }else if (isInternetAvailable()){

            listAdapter = new ListaAdapterRMADetails(Notas.this, rmaList, Notas.this);
            binding.notas.setAdapter(listAdapter);
            loading.setVisibility(View.INVISIBLE);
        }


    }

    public static Uri saveImageToStorage(String base64Image, Context context, String fileName) {
        // Decodificar o base64 para Bitmap
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // Verificar se a conversão de base64 para bitmap foi bem-sucedida
        if (bitmap == null) {
            return null; // ou lançar uma exceção, dependendo da necessidade
        }

        // Obter o diretório de armazenamento externo (pode ser necessário lidar com permissões)
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(directory, fileName + ".jpg"); // ou .png

        try {
            // Comprimir e escrever o bitmap no arquivo especificado
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); // ou Bitmap.CompressFormat.PNG
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Retornar a URI do arquivo
        return Uri.fromFile(file);
    }

    public void updateBaseDeDados(){
        Log.e("Notas","aqui gayyyyyyyyy");

        ArrayList<NotaRMA> novos= new ArrayList<>();
        ArrayList<NotaRMA> modificados = new ArrayList<>();
        ArrayList<NotaRMA> apagados = new ArrayList<>();

        for (NotaRMAEntity x : notaRMADao.getNotasByRMAId(RMAId)) {
            if (x.getOffSync()!=null){
                if (x.getOffSync().equals("novo")){
                    novos.add(x.toNotaRMA());
                } else if (x.getOffSync().equals("modificado")) {
                    modificados.add(x.toNotaRMA());
                } else if (x.getOffSync().equals("apagado")) {
                    apagados.add(x.toNotaRMA());
                }
            }
        }
        for (NotaRMA x:novos) {
            String request ="";
            Log.e("Notas","teste img  "+x.getImagemNota());
            if (x.getImagemNota() != null) {
                Uri uri = Uri.parse(x.getImagemNota());
                Bitmap imagem = uriToBitmap(getApplicationContext(), uri);
                String imagemString = bitmapToString(imagem);
                x.setImagemNota(imagemString);
                request = "{"
                        + " \"Id\": \"" + 0 + "\", "
                        + " \"Titulo\": \"" + x.getTitulo() + "\", "
                        + " \"Nota\": \"" + x.getNota() + "\", "
                        + " \"RMAId\": \"" + x.getRMAId() + "\", "
                        + " \"IdImagem\": \"" + 0 + "\", "
                        + " \"Imagem\": \"" + imagemString + "\" }";
            } else {
                request = "{"
                        + " \"Id\": \"" + 0 + "\", "
                        + " \"Titulo\": \"" + x.getTitulo() + "\", "
                        + " \"Nota\": \"" + x.getNota() + "\", "
                        + " \"RMAId\": \"" + x.getRMAId() + "\", "
                        + " \"IdImagem\": \"" + 0 + "\", "
                        + " \"Imagem\": \"" + "" + "\" }";
            }

            JsonObject body = new JsonParser().parse(request).getAsJsonObject();
            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().CreateOrUpdateNotaRMA(body);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                    if (responseObj.get("Success").getAsBoolean()) {
                        Toast.makeText(getApplicationContext(), "Nota criada com sucesso", Toast.LENGTH_LONG).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("AtivarAPI", true);
                        setResult(Activity.RESULT_OK, resultIntent);

                        notaRMADao.deleteById(x.getId());
                        int id= response.body().get("NotaRMAId").getAsInt();
                        x.setId(id);
                        notaRMADao.insert(x.toNotaRMAEntity());

                        for (NotaRMA x:convertNotaRMAEntityListToNotaRMAList(notaRMADao.getNotasByRMAId(RMAId))) {
                            if (x.getRMAId()==RMAId){
                                if (notaRMADao.getNotaById(x.getId()).getOffSync() != null){
                                    if (!notaRMADao.getNotaById(x.getId()).getOffSync().equals("apagado")){
                                        Log.e("Notas","id do RMA da nota  "+ x.getRMAId());
                                        notasDoRMAX.add(x);
                                    }
                                }else {
                                    Log.e("Notas","id do RMA da nota  "+ x.getRMAId());
                                    notasDoRMAX.add(x);
                                }
                            }
                        }

                        listAdapter = new ListaAdapterRMADetails(Notas.this,notasDoRMAX , Notas.this);
                        binding.notas.setAdapter(listAdapter);
                        //teste apagar depois




                    } else {
                        Toast.makeText(getApplicationContext(), "Erro ao criar nota", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Erro ao criar nota", Toast.LENGTH_LONG).show();
                }
            });

        }//passar os novos da local para a online
        for (NotaRMA x:modificados){
            String request ="";

            if (x.getImagemNota() != null) {
                Uri uri = Uri.parse(x.getImagemNota());
                Bitmap imagem = uriToBitmap(getApplicationContext(), uri);
                String imagemString = bitmapToString(imagem);
                x.setImagemNota(imagemString);
            }
            request = "{"
                    + " \"Id\": \"" + x.getId() + "\", "
                    + " \"Titulo\": \"" + x.getTitulo() + "\", "
                    + " \"Nota\": \"" + x.getNota() + "\", "
                    + " \"RMAId\": \"" + x.getRMAId() + "\", "
                    + " \"IdImagem\": \"" + x.getImagemNotaId() + "\", "
                    + " \"Imagem\": \"" + x.getImagemNota() + "\" }";

            JsonObject body = new JsonParser().parse(request).getAsJsonObject();
            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().CreateOrUpdateNotaRMA(body);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                    if (responseObj.get("Success").getAsBoolean()) {
                        Toast.makeText(getApplicationContext(), "Nota alterada com sucesso", Toast.LENGTH_LONG).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("AtivarAPI", true);
                        setResult(Activity.RESULT_OK, resultIntent);
                        notaRMADao.deleteById(x.getId());
                    } else {
                        Toast.makeText(getApplicationContext(), "Erro ao alterar nota", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Erro ao alterar nota", Toast.LENGTH_LONG).show();
                }
            });

        }
        for (NotaRMA x:apagados){

            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().DeleteNotaRMA(x.getId());

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                    if (responseObj.get("Success").getAsBoolean()) {
                        Toast.makeText(getApplicationContext(), "Nota apagada com sucesso", Toast.LENGTH_LONG).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("AtivarAPI", true);
                        setResult(Activity.RESULT_OK, resultIntent);
                        notaRMADao.deleteById(x.getId());
                    } else {
                        Toast.makeText(getApplicationContext(), "Erro ao apagar nota", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Erro ao apagar nota", Toast.LENGTH_LONG).show();
                }
            });

        }

    }

    public boolean mudancasNaBD(){

        boolean encontrouMod = false;
        for (NotaRMAEntity x : notaRMADao.getNotasByRMAId(RMAId)) {
            if (x.getOffSync()!=null){
                if (x.getOffSync().equals("modificado") || x.getOffSync().equals("novo") || x.getOffSync().equals("apagado")){
                    encontrouMod = true;
                }
            }
        }

        return encontrouMod;

    }



    private ArrayList<NotaRMA> convertNotaRMAEntityListToNotaRMAList(List<NotaRMAEntity> notaRmaEntities) {
        ArrayList<NotaRMA> notaRmaList = new ArrayList<>();
        // Converter cada NotaRMAEntity para NotaRMA e adicionar à lista
        for (NotaRMAEntity entity : notaRmaEntities) {

            NotaRMA x= new NotaRMA(entity.getId(),entity.getTitulo(),entity.getDataCriacao(), entity.getNota(),entity.getImagemNotaId() ,entity.getImagemNota(), entity.getRMAId());
            Log.i("Notas","titulo de cada puta "+ x.getTitulo());
            notaRmaList.add(x);
        }
        Log.i("Notas","tamanho da puta "+notaRmaList.size());
        return notaRmaList;
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
}