package com.example.projetopdm;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projetopdm.Modelos.NotaRMA;
import com.example.projetopdm.Modelos.RMA;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ListaAdapterRMADetails extends ArrayAdapter<NotaRMA> {

    Notas binding;
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

        DateTimeFormatter novoFormato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String novaDataFormatada = data.format(novoFormato);

        dataNota.setText(novaDataFormatada);

        if (notaRMA.getNota().length() > 20)
            nota.setText(notaRMA.getNota().substring(0,20) + "...");
        else{
        nota.setText(notaRMA.getNota());
        }


        //LinearLayout RMA_btn = view.findViewById(R.id.RMA_btn);

        // Adicionar um OnClickListener ao LinearLayout
        /*RMA_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lógica a ser executada quando o LinearLayout for clicado
                Intent intent = new Intent(getContext(), Notas.class);
                intent.putExtra("RMAId",no.getId());
                getContext().startActivity(intent);
            }
        });*/

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

}
