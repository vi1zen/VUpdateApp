package cn.vi1zen.vupdateapp_library.interfaces

import android.content.Intent




/**
 * @author <a href="mailto:wenzhen@snqu.com">vi1zen</a>
 * @version 1.0
 * @description
 * @time 2020/7/23 11:35
 * @update
 */
interface ActivityResultCallback {

    fun onActivityResult(requestCode:Int,resultCode: Int, data: Intent?)
}