package cn.odinaris.watermarkdetector

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_watermark.*
import cn.odinaris.watermarkdetector.utils.WatermarkUtils
import java.io.File


class WatermarkActivity : AppCompatActivity() {

    var watermarks =  ArrayList<Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watermark)
        val imgPath = intent.getStringExtra("imgFilePath")
        val imgName = intent.getStringExtra("imgName")
        //Bitmap解码配置
        val options = BitmapFactory.Options()
        options.inMutable = true
        options.inSampleSize = 1
        options.inScaled = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bmp = BitmapFactory.decodeFile(imgPath+"/"+imgName,options)
        val waterTask = WatermarkAsyncTask()
        waterTask.execute(bmp)
        btn_save.setOnClickListener {
            val imgPaths = WatermarkUtils.saveWatermarkImages(watermarks, imgName, imgPath)
            Toast.makeText(this,"图像保存成功",Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val uri1 = Uri.fromFile(File(imgPaths[0]))
            val uri2 = Uri.fromFile(File(imgPaths[1]))
            intent.data = uri1
            applicationContext.sendBroadcast(intent)//通知图库更新
            intent.data = uri2
            applicationContext.sendBroadcast(intent)
            Log.e("Watermarks_1_PathName",imgPaths[0])
            Log.e("Watermarks_2_PathName",imgPaths[1])
        }
    }

    inner class WatermarkAsyncTask: AsyncTask<Bitmap,Int,ArrayList<Bitmap>>(){
        override fun doInBackground(vararg bmp: Bitmap): ArrayList<Bitmap> {
            watermarks =  WatermarkUtils.generateWatermark(bmp[0])
            return watermarks
        }

        override fun onPostExecute(result: ArrayList<Bitmap>) {
            super.onPostExecute(result)
            iv_watermark_1.setImageBitmap(result[0])
            iv_watermark_2.setImageBitmap(result[1])
            pb_processing.visibility = GONE
            ll_watermark_box_1.visibility = VISIBLE
            ll_watermark_box_2.visibility = VISIBLE

        }

        override fun onPreExecute() {
            ll_watermark_box_1.visibility = GONE
            ll_watermark_box_2.visibility = GONE
            pb_processing.visibility = VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        System.gc()
        finish()
    }

}
