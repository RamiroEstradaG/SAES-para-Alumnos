package ziox.ramiro.saes.data.data_provider

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*


private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
    .build()

fun okHttpClient(authHeader: String = ""): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor {
        it.proceed(it.request().newBuilder().addHeader("Authorization", authHeader).build())
    }.build()

fun retrofitProvider(url: String, authHeader: String = ""): Retrofit = Retrofit.Builder()
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(okHttpClient(authHeader))
    .baseUrl(url)
    .build()