package cn.vi1zen.vupdateapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import cn.vi1zen.vupdateapp_library.interfaces.DownloadListener
import cn.vi1zen.vupdateapp_library.utils.AppUpdate
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var updateDialog:AlertDialog
    private val appUpdate by lazy { AppUpdate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView.setOnClickListener {
            updateDialog = AlertDialog.Builder(this)
                .setTitle("版本更新")
                .setMessage("发现新版本，是否更新？")
                .setPositiveButton("确认更新",null)
                .setNegativeButton("取消",null)
                .create()
            updateDialog.setOnShowListener {
                val positiveButton = updateDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton?.setOnClickListener {
                    positiveButton.visibility = View.GONE
                    updateApp()
                }
                val negativeButton = updateDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                negativeButton?.setOnClickListener {
                    if(appUpdate.isDownloading()){
                        negativeButton.visibility = View.GONE
                        appUpdate.cancel()
                    }else{
                        updateDialog.dismiss()
                    }
                }
            }
            updateDialog.show()
        }
    }

    private fun updateApp(){
        appUpdate.url("https://cdn.zuhao.com/static/down/app/android/app-zuhao-release.apk")
            .start(object : DownloadListener{

                override fun onProgress(totalBytesRead: Long, contentLength: Long, progress: Int) {
                    updateDialog.setMessage("下载进度$progress")
                }

                override fun onSuccess() {
                    appUpdate.install()
                }

                override fun onFailure() {
                    updateDialog.setMessage("安装包下载失败")
                }

                override fun onCancel() {
                    Toast.makeText(this@MainActivity,"下载已取消",Toast.LENGTH_SHORT).show()
                }
            })
    }
}