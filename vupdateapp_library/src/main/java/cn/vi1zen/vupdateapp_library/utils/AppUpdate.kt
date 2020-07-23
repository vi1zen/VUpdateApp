package cn.vi1zen.vupdateapp_library.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import cn.vi1zen.vupdateapp_library.interfaces.DownloadListener
import java.io.File

/**
 * @author <a href="mailto:wenzhen@snqu.com">vi1zen</a>
 * @version 1.0
 * @description
 * @time 2020/7/23 10:26
 * @update
 */
class AppUpdate(private val context: Context) {

    private val downloadUtil = DownloadUtil.instance()
    private lateinit var url: String
    private var apkPath:String = ""
    private val headerMap = hashMapOf<String,String>()

    /**
     * 设置请求Url
     *
     * @param url
     * @return
     */
    fun url(url: String): AppUpdate {
        this.url = url
        return this
    }

    /**
     * 下载文件保存的路径
     *
     * @param apkPath
     * @return
     */
    fun apkPath(apkPath: String): AppUpdate {
        this.apkPath = apkPath
        return this
    }

    /**
     * 设置请求头
     *
     * @param headersMap
     * @return
     */
    fun headers(headersMap: Map<String, String>): AppUpdate {
        this.headerMap.clear()
        this.headerMap.putAll(headersMap)
        return this
    }

    /**
     * 开始更新
     */
    fun start(downloadListener: DownloadListener){
        if(url.isEmpty()){
            throw NullPointerException("url cant be empty")
        }
        if(apkPath.isEmpty()){
            apkPath = context.filesDir.absolutePath + "/update.apk"
        }
        downloadUtil.cancel()
        downloadUtil
            .url(url)
            .downloadPath(apkPath)
            .headers(headerMap)
            .start(downloadListener)
    }

    /**
     * 安装APK
     */
    fun install(){
        val file = File(apkPath)
        if(!file.exists()){
            Log.e("AppUpdate","apkFile not exists")
            return
        }
        val uri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Uri.fromFile(file)
        } else {
            val authority: String = context.packageName + ".updateFileProvider"
            FileProvider.getUriForFile(context, authority, file)
        }
        val intent = Intent(Intent.ACTION_VIEW)
        val type = "application/vnd.android.package-archive"
        intent.setDataAndType(uri, type)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun isDownloading() = downloadUtil.isDownloading()

    /**
     * 取消更新
     */
    fun cancel(){
        downloadUtil.cancel()
    }
}