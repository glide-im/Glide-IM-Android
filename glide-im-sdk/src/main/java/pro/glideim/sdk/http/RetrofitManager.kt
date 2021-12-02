package pro.glideim.sdk.http

import android.content.Context
import com.dengzii.moshisuite.MoshiSuite
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.moshi.Moshi
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class RetrofitManager private constructor(context: Context, baseUrl: String) {

    private val retrofit: Retrofit
    private val moshi: Moshi
    private val mGson: Gson

    companion object {

        private lateinit var instance: RetrofitManager

        fun init(context: Context, baseUrl: String) {
            instance = RetrofitManager(context, baseUrl)
        }

        fun <T> create(clazz: Class<T>): T {
            return instance.retrofit.create(clazz)
        }

        fun Any.toJson(): String {
            return instance.mGson.toJson(this)
        }

        fun <T> String.toObject(type: Type): T? {
            return try {
                instance.mGson.fromJson(this, type)
            } catch (e: Throwable) {
                null
            }
        }
    }

    init {

        val cacheDir = context.externalCacheDir

        mGson = GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create()

        val okHttpClient = OkHttpClient.Builder().run {
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
                .hostnameVerifier { _, _ ->
                    true
                }
            if (cacheDir != null) {
                cache(Cache(cacheDir, 1024 * 1024 * 10))
            }

            addInterceptor(LogInterceptor())
            build()
        }

        moshi = MoshiSuite.getMoshi()

        retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(mGson))
            .baseUrl(baseUrl)
            .build()
    }
}