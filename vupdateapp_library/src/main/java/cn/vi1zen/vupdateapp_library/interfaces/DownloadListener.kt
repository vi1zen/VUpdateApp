package cn.vi1zen.vupdateapp_library.interfaces

/**
 * @author <a href="mailto:wenzhen@snqu.com">vi1zen</a>
 * @version 1.0
 * @description
 * @time 2020/7/22 16:13
 * @update
 */
interface DownloadListener {

    /**
     * 下载进度
     */
    fun onProgress(totalBytesRead:Long,contentLength:Long,progress:Int)

    /**
     * 下载成功
     */
    fun onSuccess()

    /**
     * 下载失败
     */
    fun onFailure()

    /**
     * 下载取消
     */
    fun onCancel()
}