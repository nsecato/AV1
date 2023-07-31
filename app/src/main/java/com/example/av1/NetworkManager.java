package com.example.av1;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkManager {

    private static final String BASE_URL = "http://10.0.2.2:8080";
    private static ServerApi serverApi;

    public static ServerApi getServerApi() {
        if (serverApi == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            serverApi = retrofit.create(ServerApi.class);
        }
        return serverApi;
    }
}

