package com.camerasecuritysystem.client.models

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IWeatherAPIService {
  @GET("data/2.5/weather")
  fun listWeather(
      @Query("q") cityName: String,
      @Query("appid") apiKey: String
  ): Call<Weather>
}
