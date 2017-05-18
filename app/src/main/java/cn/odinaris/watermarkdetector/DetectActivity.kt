package cn.odinaris.watermarkdetector

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import cn.odinaris.watermarkdetector.utils.WatermarkUtils
import kotlinx.android.synthetic.main.activity_detect.*

class DetectActivity : AppCompatActivity() {
    val RESULT_ONE_OK = 1
    val RESULT_TWO_OK = 2
    val bmpList = ArrayList<Bitmap>()
    var bmpPaths = ArrayList<String>()
    var flag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detect)
        btn_select_1.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, RESULT_ONE_OK)
        }
        btn_select_2.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, RESULT_TWO_OK)
        }
        btn_detect.setOnClickListener {
            if(btn_select_1.visibility==View.GONE && btn_select_2.visibility==View.GONE){
                val intent = Intent(this, DetectResultActivity::class.java)
                intent.putExtra("bmpPath1",bmpPaths[0])
                intent.putExtra("bmpPath2",bmpPaths[1])
                startActivity(intent)
                finish()
                System.gc()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == RESULT_ONE_OK && data!=null) {
            val file = WatermarkUtils.switchUri2File(this,data.data)
            //Bitmap解码配置
            val options = BitmapFactory.Options()
            options.inMutable = true
            options.inSampleSize = 1
            options.inScaled = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bmp = BitmapFactory.decodeFile(file.absolutePath,options)
            bmpList.add(bmp)
            bmpPaths.add(0,file.absolutePath)
            iv_watermark_1.setImageBitmap(bmp)
            btn_select_1.visibility = View.GONE
            iv_watermark_1.visibility = View.VISIBLE
        }else if(resultCode == Activity.RESULT_OK && requestCode == RESULT_TWO_OK && data!=null){
            val file = WatermarkUtils.switchUri2File(this,data.data)
            //Bitmap解码配置
            val options = BitmapFactory.Options()
            options.inMutable = true
            options.inSampleSize = 1
            options.inScaled = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bmp = BitmapFactory.decodeFile(file.absolutePath,options)
            bmpList.add(bmp)
            bmpPaths.add(1,file.absolutePath)
            iv_watermark_2.setImageBitmap(bmp)
            btn_select_2.visibility = View.GONE
            iv_watermark_2.visibility = View.VISIBLE
        }
    }

}
