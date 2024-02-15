package com.ksr.mealhuteats.service;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DirectionAPI {
    @GET("json")
    Call<JsonObject> getJson(
            @Query("origin") String origin,
            @Query("destination") String path,
            @Query("alternatives") boolean alternatives,
            @Query("key") String key);
}
