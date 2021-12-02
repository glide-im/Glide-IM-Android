package pro.glideim.sdk.http

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

class LogInterceptor : Interceptor {

    companion object {
        private const val TAG = "LogInterceptor"
        private val TYPE_JSON: MediaType = "application/json;charset=UTF-8".toMediaType()
    }

    private var mLogger: Logger

    init {
        mLogger = object : Logger {
            override fun i(tag: String, log: String) {
                Log.i(TAG, "i: $log")
            }

            override fun e(tag: String, log: String) {
                Log.e(TAG, "e: $log")
            }
        }
    }

    interface Logger {
        fun i(tag: String, log: String)
        fun e(tag: String, log: String)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()

        mLogger.i(TAG, "${originRequest.method} ${originRequest.url}")
        logHeaders(originRequest)
        logParameter(originRequest)

        val response = chain.proceed(originRequest)

        val nResponse = response.newBuilder()

        if (!response.isSuccessful) {
            mLogger.e(TAG, "$response")
        } else {
            if (response.body?.contentType() == TYPE_JSON) {
                val string = response.body?.string()
                nResponse.body(string?.toResponseBody(TYPE_JSON))
                mLogger.i(TAG, string.orEmpty())
            }
        }
        mLogger.i(
            TAG,
            "${response.request.method} ${response.request.url}" +
                    " ${response.code} ${response.message} ${response.body?.contentType()}"
        )
        return nResponse.build()
    }

    private fun logParameter(request: Request) {
        var log = ""
        val requestBody = request.body

        if (requestBody is FormBody) {
            val stringBuilder = StringBuilder()
            stringBuilder.append("{\n")
            val size = requestBody.size
            for (i in 0 until size) {
                stringBuilder.append("\t ${requestBody.name(i)}: ${requestBody.value(i)},\n")
            }
            stringBuilder.trimEnd('\n')
            stringBuilder.trimEnd(',')
            stringBuilder.append("\n}")
            log = stringBuilder.toString().replace("{\n}", "{}")

        } else if (TYPE_JSON == requestBody?.contentType()) {

        }
    }

    private fun logHeaders(request: Request) {
        val headers = request.headers
        val builder = StringBuilder()
        builder.append("Request Headers:")
        if (headers.size > 0) {
            builder.append("\n")
        }
        for (key in headers.names()) {
            builder.append("$key: ${headers[key]}")
        }
        mLogger.i(TAG, builder.toString())
    }
}