package cn.vi1zen.vupdateapp_library.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import cn.vi1zen.vupdateapp_library.interfaces.DownloadListener
import cn.vi1zen.vupdateapp_library.net.ProgressResponseBody
import okhttp3.*
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


/**
 * @author <a href="mailto:wenzhen@snqu.com">vi1zen</a>
 * @version 1.0
 * @description 下载工具类
 * @time 2020/7/22 16:08
 * @update
 */
class DownloadUtil private constructor() {

    private val okHttpBuilder by lazy { getOkHttpDefaultBuilder() }
    private val uiHandler by lazy { Handler(Looper.getMainLooper()) }
    private var isDownloading:Boolean = false

    private lateinit var filePath: String
    private val callList = arrayListOf<Call>()

    companion object {
        private val instance  = DownloadUtil()
        private lateinit var requestBuilder: Request.Builder
        fun instance(): DownloadUtil {
            requestBuilder = Request.Builder()
            return instance
        }
    }

    /**
     * 设置请求Url
     *
     * @param url
     * @return
     */
    fun url(url: String): DownloadUtil {
        requestBuilder.url(url)
        return instance
    }

    /**
     * 下载文件保存的路径
     *
     * @param filePath
     * @return
     */
    fun downloadPath(filePath: String): DownloadUtil {
        this.filePath = filePath
        return instance
    }

    /**
     * 请求Tag
     *
     * @param tag
     * @return
     */
    fun tag(tag: Any?): DownloadUtil {
        requestBuilder.tag(tag)
        return instance
    }

    /**
     * 设置请求头
     *
     * @param headersMap
     * @return
     */
    fun headers(headersMap: Map<String, String>): DownloadUtil {
        headersMap.forEach { entry ->
            requestBuilder.addHeader(entry.key, entry.value)
        }
        return instance
    }

    /**
     * 设置下载监听
     *
     * @param downloadListener
     * @return
     */
    fun start(downloadListener: DownloadListener) {
        this.isDownloading = true
        val tempFile = File(filePath)
        if(tempFile.exists()){
            tempFile.delete()
        }
        okHttpBuilder.interceptors().clear()
        okHttpBuilder.addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val response = chain.proceed(chain.request())
                return response.newBuilder().body(
                    ProgressResponseBody(response.body!!, downloadListener)
                ).build()
            }
        })
        val call = okHttpBuilder.build().newCall(requestBuilder.build())
        callList.add(call)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isDownloading = false
                uiHandler.post { downloadListener.onFailure() }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.byteStream()?.use {
                    try {
                        File(filePath).writeBytes(it.readBytes())
                        isDownloading = false
                        uiHandler.post { downloadListener.onSuccess() }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        isDownloading = false
                        uiHandler.post {
                            if(call.isCanceled()){
                                downloadListener.onCancel()
                            }else{
                                downloadListener.onFailure()
                            }
                        }
                    }
                }
            }
        })
    }

    /**
     * 取消下载
     */
    fun cancel(){
        callList.forEach { call ->
            if(!call.isCanceled()){
                call.cancel()
            }
        }
    }

    /**
     * 是否正在下载中
     */
    fun isDownloading() : Boolean{
        return this.isDownloading
    }

    /**
     * 获取默认OkHttpClient.Builder
     * @return Builder
     */
    private fun getOkHttpDefaultBuilder(): OkHttpClient.Builder {
        //默认信任所有的证书
        val trustManager: X509TrustManager = object : X509TrustManager {
            override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) = Unit
            override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) = Unit
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        var sslContext: SSLContext? = null
        try {
            sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val sslSocketFactory = sslContext?.socketFactory
        val unSafeHostnameVerifier = HostnameVerifier { _, _ -> true }
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(15000, TimeUnit.MILLISECONDS)
        builder.readTimeout(30000, TimeUnit.MILLISECONDS)
        builder.writeTimeout(30000, TimeUnit.MILLISECONDS)
        sslSocketFactory?.let { builder.sslSocketFactory(sslSocketFactory, trustManager) }
        builder.hostnameVerifier(unSafeHostnameVerifier)
        return builder
    }
}