package com.example.projetopdm;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projetopdm.BackEnd.RetrofitClient;
import com.example.projetopdm.LocalDataBase.DAOs.NotaRMADao;
import com.example.projetopdm.LocalDataBase.Entity.NotaRMAEntity;
import com.example.projetopdm.LocalDataBase.Entity.RMAEntity;
import com.example.projetopdm.Modelos.NotaRMA;
import com.example.projetopdm.Modelos.RMA;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.jar.JarEntry;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.projetopdm.databinding.ListDetalhesRmaBinding;

public class ListaAdapterRMADetails extends ArrayAdapter<NotaRMA> {
    ListDetalhesRmaBinding binding;

    Notas bindingNotas;
    LinearLayout notas ;
    public ListaAdapterRMADetails(@NonNull Context context, ArrayList<NotaRMA> dataArrayList, Notas binding) {
        super(context, R.layout.list_detalhes_rma, dataArrayList);
        this.bindingNotas = binding;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {

        NotaRMA notaRMA = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_detalhes_rma, parent, false);
        }

        TextView titulo = view.findViewById(R.id.tituloNotas);
        TextView dataNota = view.findViewById(R.id.dataNotas);
        TextView nota = view.findViewById(R.id.descricao);
        assert notaRMA != null;
        titulo.setText(notaRMA.getTitulo());

        // xxxx-xx-xx retirar T xx:xx e retirar o resto da datetime

        String dataOriginal = notaRMA.getDataCriacao();

        DateTimeFormatter formatoOriginal = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

        LocalDateTime data = LocalDateTime.parse(dataOriginal, formatoOriginal);

        DateTimeFormatter novoFormato = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        String novaDataFormatada = data.format(novoFormato);

        dataNota.setText(novaDataFormatada);

        if (notaRMA.getNota().length() > 35)
            nota.setText(notaRMA.getNota().substring(0,35) + "...");
        else{
        nota.setText(notaRMA.getNota());
        }


        Button deleteBt = view.findViewById(R.id.deleteBt);

        // Adicionar um OnClickListener ao LinearLayout
        if (!(bindingNotas.estadoId == 2 || bindingNotas.estadoId == 3)){
            if (!isInternetAvailable()){
                deleteBt.setVisibility(View.INVISIBLE);
                deleteBt.setEnabled(false);
            }

        }
        deleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout popup = bindingNotas.findViewById(R.id.popup);
                popup.setVisibility(View.VISIBLE);

                TextView confirmarid = bindingNotas.findViewById(R.id.confirmarid);

                confirmarid.setText("Tem a certeza que pretende eliminar a nota " + notaRMA.getTitulo() + "?");

                Button confirm = bindingNotas.findViewById(R.id.confirmar);

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isInternetAvailable()){
                            NotaRMADao notaRMADao = bindingNotas.db.notaRMADao();
                            NotaRMAEntity notaRMAEntity = notaRMA.toNotaRMAEntity();
                            notaRMAEntity.setOffSync("apagado");
                            notaRMADao.insert(notaRMAEntity);
                            if (bindingNotas.listAdapter.getPosition(notaRMA) != -1){
                                bindingNotas.listAdapter.remove(notaRMA);
                                popup.setVisibility(View.INVISIBLE);
                                bindingNotas.listAdapter.notifyDataSetChanged();
                                Toast.makeText(bindingNotas, "Nota eliminada com sucesso", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (isInternetAvailable()){
                            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().DeleteNotaRMA(notaRMA.getId());
                            call.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                                    if (responseObj.get("Success").getAsBoolean()){
                                        NotaRMADao notaRMADao = bindingNotas.db.notaRMADao();
                                        notaRMADao.deleteById(notaRMA.getId());

                                        int pos = bindingNotas.listAdapter.getPosition(notaRMA);
                                        bindingNotas.listAdapter.remove(notaRMA);
                                        popup.setVisibility(View.INVISIBLE);
                                        bindingNotas.listAdapter.notifyDataSetChanged();
                                        Toast.makeText(bindingNotas, "Nota eliminada com sucesso", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(bindingNotas, "Erro ao eliminar nota", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    Toast.makeText(bindingNotas, "Erro ao eliminar nota", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                });


            }
        });

        notas= view.findViewById(R.id.notas);

        notas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(bindingNotas.getApplicationContext(), Nota.class);
                intent.putExtra("NotaId",notaRMA.getId());
                intent.putExtra("RMAId",notaRMA.getRMAId());
                intent.putExtra("NotaTitulo",notaRMA.getTitulo());
                intent.putExtra("Descricao",notaRMA.getNota());
                intent.putExtra("Data",notaRMA.getDataCriacao());
                intent.putExtra("ImagemID",notaRMA.getImagemNotaId());
                intent.putExtra("estadoRMA",bindingNotas.estadoId);
                intent.putExtra("Update","Update");
                bindingNotas.startActivityForResult(intent, bindingNotas.MEU_REQUEST_CODE);
            }
        });

        return view;
    }

    private boolean isInternetAvailable(){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)bindingNotas.getSystemService(Context.CONNECTIVITY_SERVICE);
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
