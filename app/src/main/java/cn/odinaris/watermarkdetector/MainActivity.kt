package cn.odinaris.watermarkdetector

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.R.attr.data



class MainActivity : AppCompatActivity() {
    val RESULT_GENERATE_OK = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cv_generate.setOnClickListener {
            val intent = Intent()
            /* 开启Pictures画面Type设定为image */
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            /* 取得相片后返回本画面 */
            startActivityForResult(intent, RESULT_GENERATE_OK)
        }
        cv_detect.setOnClickListener {
            startActivity(Intent(this,DetectActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == RESULT_GENERATE_OK && data!=null) {
            val i = Intent(this, GenerateActivity::class.java)
            i.putExtra("imageUri", data.data.toString())
            startActivity(i)
        }
    }
}
