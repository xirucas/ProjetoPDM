package com.example.projetopdm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projetopdm.Modelos.RMA;

import java.util.ArrayList;

public class ListAdapterRMA extends ArrayAdapter<RMA> {
    MainActivity binding;
    public ListAdapterRMA(@NonNull Context context, ArrayList<RMA> dataArrayList, MainActivity binding) {
        super(context, R.layout.list_rma, dataArrayList);
        this.binding = binding;
    }

    @NonNull
    @Override
public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        RMA rma = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_rma, parent, false);
        }

        TextView rmaId = view.findViewById(R.id.rma);
        TextView rmaDescricao = view.findViewById(R.id.descricao);
        TextView rmaDataCriacao = view.findViewById(R.id.data);
        TextView rmaEstado = view.findViewById(R.id.estado);
        assert rma != null;
        rmaId.setText(rma.getRMA());
        rmaDescricao.setText(rma.getDescricaoCliente());
        rmaDataCriacao.setText(rma.getDataCriacao());
        rmaEstado.setText(rma.getEstadoRMA());

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
