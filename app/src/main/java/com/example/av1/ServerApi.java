package com.example.av1;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ServerApi {
    @POST("/send")
    Call<Void> sendJson(@Body JSONObject data);


    @GET("/receive")
    Call<JsonObject> receiveJson();

}
