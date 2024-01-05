package com.example.projetopdm.BackEnd;

import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
public interface Api {

    String BASE_URL = "https://personal-qxrslhd4.outsystemscloud.com/SweetRepair_API/rest/API/";

    @Headers({
            "Content-type: application/json; charset=utf-8"
    })

    @GET("GetRMASByFuncionario")
    Call<JsonObject> GetRMASByFuncionario(@Query("GUID") String GUID);

    @GET("GetAllGUID")
    Call<JsonObject> GetAllGUID();
    @GET("GetFuncionarioByGUID")
    Call<JsonObject> GetFuncionarioByGUID(@Query("GUID") String GUID);
}
