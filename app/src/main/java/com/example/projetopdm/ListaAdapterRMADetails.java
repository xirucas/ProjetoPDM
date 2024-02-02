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

public class ListaAdapterRMADetails extends ArrayAdapter<NotaRMA> {

    Notas binding;

    LinearLayout notas ;
    public ListaAdapterRMADetails(@NonNull Context context, ArrayList<NotaRMA> dataArrayList, Notas binding) {
        super(context, R.layout.list_detalhes_rma, dataArrayList);
        this.binding = binding;
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
        deleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout popup = binding.findViewById(R.id.popup);
                popup.setVisibility(View.VISIBLE);

                TextView confirmarid = binding.findViewById(R.id.confirmarid);

                confirmarid.setText("Tem a certeza que pretende eliminar a nota " + notaRMA.getTitulo() + "?");

                Button confirm = binding.findViewById(R.id.confirmar);

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isInternetAvailable()){
                            Call<JsonObject> call = RetrofitClient.getInstance().getMyApi().DeleteNotaRMA(notaRMA.getId());
                            call.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                    JsonObject responseObj = response.body().get("Result").getAsJsonObject();
                                    if (responseObj.get("Success").getAsBoolean()){
                                        int pos = binding.listAdapter.getPosition(notaRMA);
                                        binding.listAdapter.remove(notaRMA);
                                        popup.setVisibility(View.INVISIBLE);
                                        binding.listAdapter.notifyDataSetChanged();
                                        Toast.makeText(binding, "Nota eliminada com sucesso", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(binding, "Erro ao eliminar nota", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    Toast.makeText(binding, "Erro ao eliminar nota", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }else {
                            RelativeLayout popup = binding.findViewById(R.id.popup);
                            popup.setVisibility(View.INVISIBLE);
                            Toast.makeText(binding, "Não tem acesso à internet", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });

        notas= view.findViewById(R.id.notas);

        notas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(binding.getApplicationContext(), Nota.class);
                intent.putExtra("NotaId",notaRMA.getId());
                intent.putExtra("RMAId",notaRMA.getRMAId());
                binding.startActivityForResult(intent, binding.MEU_REQUEST_CODE);
            }
        });



        /*android.widget.ImageView edit = convertView.findViewById(R.id.left_view);
        android.widget.ImageView delete = convertView.findViewById(R.id.right_view);*/

        /*edit.setOnClickListener(view1 -> {
            android.content.Intent intent = new android.content.Intent(getContext(), activity_db_update.class);
            intent.putExtra("title", binding.getSessionName());
            intent.putExtra("name", rma.getDescricaoCliente());
            intent.putExtra("id", rma.getId());
            getContext().startActivity(intent);
            binding.finish();
        });*/


        return view;
    }

    private boolean isInternetAvailable(){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)binding.getSystemService(Context.CONNECTIVITY_SERVICE);
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
