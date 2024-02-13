package com.example.projetopdm;
import android.widget.Filter;
import android.widget.LinearLayout;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.projetopdm.Modelos.RMA;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;


public class ListAdapterRMA extends ArrayAdapter<RMA> {
    MainActivity binding;
    private ArrayList<RMA> originalList;
    private ArrayList<RMA> filteredList;
    private ItemFilter mFilter = new ItemFilter();

    public ListAdapterRMA(@NonNull Context context, ArrayList<RMA> dataArrayList, MainActivity binding) {
        super(context, R.layout.list_rma, dataArrayList);
        this.binding = binding;
        this.originalList = new ArrayList<>(dataArrayList);
        this.filteredList = new ArrayList<>(dataArrayList);
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
        if (rma.getDescricaoCliente().length() > 35)
            rmaDescricao.setText(rma.getDescricaoCliente().substring(0, 35) + "...");
        else {
            rmaDescricao.setText(rma.getDescricaoCliente());
        }

        rmaDataCriacao.setText(rma.getDataCriacao());
        rmaEstado.setText(rma.getEstadoRMA());


        LinearLayout RMA_btn = view.findViewById(R.id.RMA_btn);

        // Adicionar um OnClickListener ao LinearLayout

            RMA_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RMA_btn.setEnabled(false);
                    Intent intent = new Intent(binding.getApplicationContext(), Notas.class);
                    intent.putExtra("RMAId",rma.getId());
                    intent.putExtra("RMA",rma.getRMA());
                    intent.putExtra("Descricao",rma.getDescricaoCliente());
                    intent.putExtra("estadoRMA",rma.getEstadoRMAId());
                    intent.putExtra("Data",rma.getDataCriacao());
                    intent.putExtra("horas",rma.getHorasTrabalhadas());
                    binding.startActivityForResult(intent, binding.MEU_REQUEST_CODE);
                }
            });

        //mudar cor do estado
        if (rma.getEstadoRMAId() == 1) {
            //completo
            rmaEstado.setTextColor(ContextCompat.getColor(binding.getApplicationContext(),R.color.completo));
        } else if (rma.getEstadoRMAId() == 2) {
            //novo
            rmaEstado.setTextColor(ContextCompat.getColor(binding.getApplicationContext(),R.color.novo));
        } else {
            //progresso
            rmaEstado.setTextColor(ContextCompat.getColor(binding.getApplicationContext(),R.color.progresso));
        }

        return view;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            ArrayList<RMA> filteredItems = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredItems.addAll(originalList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (RMA rma : originalList) {
                    if (rma.getRMA().toLowerCase().contains(filterPattern.toLowerCase())) {
                        filteredItems.add(rma);
                    }
                }
            }

            results.values = filteredItems;
            results.count = filteredItems.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            binding.listAdapter.clear();
            binding.listAdapter.addAll((ArrayList<RMA>) results.values);
            binding.listAdapter.notifyDataSetChanged();
        }


    }


}
