package com.uet.parking.network.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit Client singleton
 * Cấu hình và khởi tạo Retrofit cho việc gọi API
 */
object RetrofitClient {

    private const val BASE_URL = "https://api.uet-parking.com/" // Thay đổi BASE_URL thực
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
