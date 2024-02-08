package com.example.projetopdm.LocalDataBase;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.projetopdm.Modelos.Funcionario;
public class FuncionarioSharedPreferences {

    private static final String PREFS_NAME = "FuncionarioPrefs";

    public static void saveFuncionarioData(Context context, Funcionario funcionario) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("id", funcionario.getId());
        editor.putString("GUID", funcionario.getGUID());
        editor.putString("nome", funcionario.getNome());
        editor.putString("email", funcionario.getEmail());
        editor.putString("contacto", funcionario.getContacto());
        editor.putString("pin", funcionario.getPin());
        editor.putString("imagemFuncionario", funcionario.getImagemFuncionario());
        editor.putInt("estadoFuncionarioId", funcionario.getEstadoFuncionarioId());
        editor.putString("estadoFuncionario", funcionario.getEstadoFuncionario());

        editor.apply();
    }

    public static Funcionario getFuncionarioData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        Funcionario funcionario = new Funcionario();
        funcionario.setId(sharedPreferences.getInt("id", -1)); // -1 como valor padrão
        funcionario.setGUID(sharedPreferences.getString("GUID", null));
        funcionario.setNome(sharedPreferences.getString("nome", null));
        funcionario.setEmail(sharedPreferences.getString("email", null));
        funcionario.setContacto(sharedPreferences.getString("contacto", null));
        funcionario.setPin(sharedPreferences.getString("pin", null));
        funcionario.setImagemFuncionario(sharedPreferences.getString("imagemFuncionario", null));
        funcionario.setEstadoFuncionarioId(sharedPreferences.getInt("estadoFuncionarioId", -1));
        funcionario.setEstadoFuncionario(sharedPreferences.getString("estadoFuncionario", null));
        return funcionario;
    }
}
