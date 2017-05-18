package cn.odinaris.watermarkdetector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.odinaris.watermarkdetector.utils.WatermarkUtils
import kotlinx.android.synthetic.main.activity_result_detect.*

class DetectResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_detect)
        val bmpPath1 = intent.getStringExtra("bmpPath1")
        val bmpPath2 = intent.getStringExtra("bmpPath2")
        val options = BitmapFactory.Options()
        options.inMutable = true
        options.inSampleSize = 1
        options.inScaled = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bmp1 = BitmapFactory.decodeFile(bmpPath1,options)
        val bmp2 = BitmapFactory.decodeFile(bmpPath2,options)
        val bmpList = ArrayList<Bitmap>()
        bmpList.add(0,bmp1)
        bmpList.add(1,bmp2)
        val recoverTask = RecoverAsyncTask()
        recoverTask.execute(bmpList)
    }

    inner class RecoverAsyncTask: AsyncTask<ArrayList<Bitmap>, Int, Bitmap>(){
        override fun doInBackground(vararg bmpList: ArrayList<Bitmap>): Bitmap {
            val result =  WatermarkUtils.authenticateIntegrity(bmpList[0])
            return result
        }

        override fun onPostExecute(result: Bitmap) {
            super.onPostExecute(result)
            pb_loading.visibility = View.GONE
            iv_detect_result.setImageBitmap(result)
            iv_detect_result.visibility = View.VISIBLE

        }

        override fun onPreExecute() {
            pb_loading.visibility = View.VISIBLE
            iv_detect_result.visibility = View.GONE
            btn_save.visibility = View.VISIBLE

        }
    }
}
