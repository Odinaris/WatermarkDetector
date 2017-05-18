package cn.odinaris.watermarkdetector

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_generate.*
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import cn.odinaris.watermarkdetector.utils.WatermarkUtils


class GenerateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate)
        val imgUri = Uri.parse(intent.getStringExtra("imageUri"))
        val file = WatermarkUtils.switchUri2File(this,imgUri)
        //Bitmap解码配置
        val options = BitmapFactory.Options()
        options.inMutable = true
        options.inSampleSize = 1
        options.inScaled = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(file.absolutePath,options)
        if(isSatisfiedSize(options)){
            options.inJustDecodeBounds = false
            bmp = BitmapFactory.decodeFile(file.absolutePath,options)
            Log.e("Bitmap_Info","rowBytes"+bmp.rowBytes+"byteCount"+bmp.byteCount+"width"+bmp.width+"height"+bmp.height)
            iv_cover.setImageBitmap(bmp)
        }else{
            Toast.makeText(this,"图片过大!"+"width="+options.outWidth+"height="+options.outHeight,Toast.LENGTH_SHORT).show()
        }
        btn_generate.setOnClickListener {
            if(bmp!=null){
                val intent = Intent(this,WatermarkActivity::class.java)
                intent.putExtra("imgFilePath",file.parent)
                intent.putExtra("imgName",file.name)
                startActivity(intent)
            }
        }
    }

    //设定实验图像阈值，超过大小进行提示，不作处理
    private fun isSatisfiedSize(options: BitmapFactory.Options): Boolean {
        return options.outWidth<1024 && options.outHeight<1024
    }

    override fun onDestroy() {
        super.onDestroy()
        System.gc()

    }
}
