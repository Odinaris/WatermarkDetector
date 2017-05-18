package cn.odinaris.watermarkdetector.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.collections.ArrayList


object WatermarkUtils {
    private val M3 = arrayOf(intArrayOf(0,1,0), intArrayOf(1,2,1), intArrayOf(2,2,0))//参考矩阵，三进制
    private val B = 3//进制数
    private val authenticateBitstream = "000111222"
    private class ExtractResult{
        var pixel = IntArray(2)
        var isTampered:Boolean = false//是否被篡改
        var authenInfo = ""//提取的认证信息
    }

    /**
     *  Generate double watermarks
     *  @param bmp      The cover image
     *  @return         The bitmap of double watermarks
     */
    fun generateWatermark(bmp : Bitmap) : ArrayList<Bitmap>{
        //初始化水印图象w1,w2
        val w1 = bmp.copy(Bitmap.Config.ARGB_8888,true)
        val w2 = bmp.copy(Bitmap.Config.ARGB_8888,true)
        val bmpList = ArrayList<Bitmap>()
        var flag = 1//设置标志，用于交替嵌入认证信息和失真信息
        for(j in 0..bmp.height-1){
            for(i in 0..bmp.width-1 step 2){
                val pixels = embed(bmp,i,j,1,2)
                if(flag==1){
                    w1.setPixel(i,j,pixels[0])
                    w1.setPixel(i+1,j,pixels[1])
                    w2.setPixel(i,j,pixels[2])
                    w2.setPixel(i+1,j,pixels[3])
                    flag = 0
                }else if (flag == 0){
                    w2.setPixel(i,j,pixels[0])
                    w2.setPixel(i+1,j,pixels[1])
                    w1.setPixel(i,j,pixels[2])
                    w1.setPixel(i+1,j,pixels[3])
                    flag = 1
                }
                if(true){
                    Log.e("w1像素点坐标及其rgb值：","x = "+i.toString()+"," +
                            " y = "+j.toString()+",r = "+Color.red(w1.getPixel(i,j)).toString()
                            +",g = "+Color.green(w1.getPixel(i,j)).toString()
                            +",b = "+Color.blue(w1.getPixel(i,j)).toString())
                    Log.e("w1像素点坐标及其rgb值：","x = "+(i+1).toString()+"," +
                            " y = "+j.toString()+",r = "+Color.red(w1.getPixel(i+1,j)).toString()
                            +",g = "+Color.green(w1.getPixel(i+1,j)).toString()
                            +",b = "+Color.blue(w1.getPixel(i+1,j)).toString())
                    Log.e("w2像素点坐标及其rgb值：","x = "+i.toString()+"," +
                            " y = "+j.toString()+",r = "+Color.red(w2.getPixel(i,j)).toString()
                            +",g = "+Color.green(w2.getPixel(i,j)).toString()
                            +",b = "+Color.blue(w2.getPixel(i,j)).toString())
                    Log.e("w2像素点坐标及其rgb值：","x = "+(i+1).toString()+"," +
                            " y = "+j.toString()+",r = "+Color.red(w2.getPixel(i+1,j)).toString()
                            +",g = "+Color.green(w2.getPixel(i+1,j)).toString()
                            +",b = "+Color.blue(w2.getPixel(i+1,j)).toString())
                }
            }
        }
        bmpList.add(w1)
        bmpList.add(w2)
        return bmpList
    }

    /**
     * Embed the authentication info and the distortion info in per pixel
     *
     * @param bmp   The cover image
     * @param i     The x coordinate (0...width-1) of the pixel
     * @param j     The y coordinate (0...height-1) of the pixel
     * @param d1    The first secret bit
     * @param d2    The second secret bit
     * @return      The double bitmaps of watermark
     */
    private fun embed(bmp: Bitmap, i: Int, j: Int, d1: Int, d2: Int) : IntArray{
        //获得当前位置的三通道像素
        val w1_pixel= intArrayOf(bmp.getPixel(i,j),bmp.getPixel(i+1,j))
        val w2_pixel= intArrayOf(bmp.getPixel(i,j),bmp.getPixel(i+1,j))
        //将两张水印图中单通道像素保存
        var red_pixel = intArrayOf(Color.red(w1_pixel[0]),Color.red(w1_pixel[1]),
                Color.red(w2_pixel[0]),Color.red(w2_pixel[1]))
        var green_pixel = intArrayOf(Color.green(w1_pixel[0]),Color.green(w1_pixel[1]),
                Color.green(w2_pixel[0]),Color.green(w2_pixel[1]))
        var blue_pixel = intArrayOf(Color.blue(w1_pixel[0]),Color.blue(w1_pixel[1]),
                Color.blue(w2_pixel[0]),Color.blue(w2_pixel[1]))
        red_pixel = embedSingleChannel(red_pixel, d1, d2)
        green_pixel = embedSingleChannel(green_pixel, d1, d2)
        blue_pixel = embedSingleChannel(blue_pixel, d1, d2)
        val pixels = intArrayOf(
                Color.argb(0xff, red_pixel[0], green_pixel[0], blue_pixel[0]),
                Color.argb(0xff, red_pixel[1], green_pixel[1], blue_pixel[1]),
                Color.argb(0xff, red_pixel[2], green_pixel[2], blue_pixel[2]),
                Color.argb(0xff, red_pixel[3], green_pixel[3], blue_pixel[3]))
        return pixels
    }

    /**
     * Embed the authentication info and the distortion info in single channel per pixel
     *
     * @param pixel     the single channel pixels,
     * which pixel[0] and pixel[1] stands for the adjacent pixels of watermark 1,
     * pixel[2] and pixel[3] stands for the adjacent pixels of watermark 2.
     * @param d1    The first secret bit
     * @param d2    The second secret bit
     * @return      The modified single channel pixels
     * **/
    private fun embedSingleChannel(pixel: IntArray, d1: Int, d2: Int): IntArray {
        val newPixel = IntArray(4)
        for(p1 in pixel[0]-1..pixel[0]+1){
            for(p2 in pixel[1]-1..pixel[1]+1){
                val i1 = p1 % B
                val i2 = p2 % B
                val i3 = (p2 + 1) % B
                if(p1 in 0..255 && p2 in 0..255){
                    if (M3[i1][i2] == d1 && M3[i1][i3] == d2) {
                        val s1 = p1 - pixel[0] + 1
                        val s2 = p2 - pixel[1] + 1
                        for(q1 in pixel[2]-1..pixel[2]+1){
                            for(q2 in pixel[3]-1..pixel[3]+1){
                                val j1 = q1 % B
                                val j2 = q2 % B
                                val j3 = (q2 + 1) % B
                                if (M3[j1][j2] == s1 && M3[j1][j3] == s2) {
                                    newPixel[0] = p1
                                    newPixel[1] = p2
                                    newPixel[2] = q1
                                    newPixel[3] = q2
                                    return newPixel
                                }
                            }
                        }
                    }
                }
                else{ Log.e("越界异常","p1 = "+p1.toString()+", p2 = "+p2.toString()) }
            }
        }
        return newPixel
    }

    //图片完整性认证，检测是否被篡改，若未被篡改，根据双水印图片恢复原始图片，若被篡改，标记篡改位置
    fun authenticateIntegrity(bmpList: ArrayList<Bitmap>) : Bitmap{
        //认证信息
        val d1 = 1
        val d2 = 2
        val newBmp = bmpList[0].copy(Bitmap.Config.ARGB_8888,true)
        var flag = 1
        for(j in 0..bmpList[0].height-1){
            for(i in 0..bmpList[0].width-1 step 2){
                val results = extract(bmpList, i, j, d1, d2, flag)
                if(results.isTampered) {
                    Log.e("篡改位置","x=$i,y=$j")
                    newBmp.setPixel(i,j,0)
                    newBmp.setPixel(i+1,j,0)
                }else{
                    newBmp.setPixel(i,j,results.pixel[0])
                    newBmp.setPixel(i+1,j,results.pixel[1])
                }
                if(flag == 1) flag = 0 else flag = 1
            }
        }
        return newBmp
    }

    private fun extract(bmpList: ArrayList<Bitmap>, i: Int, j: Int, d1: Int, d2: Int, flag: Int): ExtractResult {
        val result = ExtractResult()
        val pixels = intArrayOf(bmpList[0].getPixel(i,j),bmpList[1].getPixel(i,j),
                bmpList[0].getPixel(i+1,j),bmpList[1].getPixel(i+1,j))
        //分别计算失真信息核认证信息模运算结果，并赋值
        val modResult = ModResult(pixels,flag)
        result.isTampered = isTampered(modResult, d1, d2)
        //如果图像没有被篡改，则提取认证信息
        if(!result.isTampered){
            result.authenInfo =
                    d1.toString() + d2.toString() +
                    d1.toString() + d2.toString() +
                    d1.toString() + d2.toString()
            result.pixel = recover(pixels,flag)
        }
        return result
    }

    private fun recover(pixels: IntArray, flag: Int): IntArray {
        val newPixel = IntArray(2)
        if(flag==1){
            val mod_q1_red = Color.red(pixels[1]) % B
            val mod_q2_red = Color.red(pixels[3]) % B
            val mod_q3_red = (Color.red(pixels[3])+1) % B
            val mod_q1_green = Color.green(pixels[1]) % B
            val mod_q2_green = Color.green(pixels[3]) % B
            val mod_q3_green = (Color.green(pixels[3])+1) % B
            val mod_q1_blue = Color.blue(pixels[1]) % B
            val mod_q2_blue = Color.blue(pixels[3]) % B
            val mod_q3_blue = (Color.blue(pixels[3])+1) % B
            val d_1_red = M3[ mod_q1_red ][ mod_q2_red ]
            val d_2_red = M3[ mod_q1_red ][ mod_q3_red]
            val d_1_green = M3[ mod_q1_green ][ mod_q2_green ]
            val d_2_green = M3[ mod_q1_green ][ mod_q3_green]
            val d_1_blue = M3[ mod_q1_blue ][ mod_q2_blue ]
            val d_2_blue = M3[ mod_q1_blue ][ mod_q3_blue]
            val p_1_red = Color.red(pixels[0])-d_1_red+1
            val p_1_green = Color.green(pixels[0])-d_1_green+1
            val p_1_blue = Color.blue(pixels[0])-d_1_blue+1
            val p_2_red = Color.red(pixels[2])-d_2_red+1
            val p_2_green = Color.green(pixels[2])-d_2_green+1
            val p_2_blue = Color.blue(pixels[2])-d_2_blue+1
            newPixel[0] = Color.argb(0xff, p_1_red, p_1_green, p_1_blue)
            newPixel[1] = Color.argb(0xff, p_2_red, p_2_green, p_2_blue)
        }
        else{
            val mod_q1_red = Color.red(pixels[0]) % B
            val mod_q2_red = Color.red(pixels[2]) % B
            val mod_q3_red = (Color.red(pixels[2])+1) % B
            val mod_q1_green = Color.green(pixels[0]) % B
            val mod_q2_green = Color.green(pixels[2]) % B
            val mod_q3_green = (Color.green(pixels[2])+1) % B
            val mod_q1_blue = Color.blue(pixels[0]) % B
            val mod_q2_blue = Color.blue(pixels[2]) % B
            val mod_q3_blue = (Color.blue(pixels[2])+1) % B
            val d_1_red = M3[ mod_q1_red ][ mod_q2_red ]
            val d_2_red = M3[ mod_q1_red ][ mod_q3_red]
            val d_1_green = M3[ mod_q1_green ][ mod_q2_green ]
            val d_2_green = M3[ mod_q1_green ][ mod_q3_green]
            val d_1_blue = M3[ mod_q1_blue ][ mod_q2_blue ]
            val d_2_blue = M3[ mod_q1_blue ][ mod_q3_blue]
            val p_1_red = Color.red(pixels[1])-d_1_red+1
            val p_1_green = Color.green(pixels[1])-d_1_green+1
            val p_1_blue = Color.blue(pixels[1])-d_1_blue+1
            val p_2_red = Color.red(pixels[3])-d_2_red+1
            val p_2_green = Color.green(pixels[3])-d_2_green+1
            val p_2_blue = Color.blue(pixels[3])-d_2_blue+1
            newPixel[0] = Color.argb(0xff, p_1_red, p_1_green, p_1_blue)
            newPixel[1] = Color.argb(0xff, p_2_red, p_2_green, p_2_blue)
        }
        return newPixel
    }

    //判断当前像素任一通道是否被篡改
    private fun isTampered(modResult: Array<IntArray>, d1: Int, d2: Int): Boolean {
        val r_flag = M3[modResult[0][0]][modResult[0][1]] == d1
                && M3[modResult[0][0]][modResult[0][2]] == d2
        val g_flag = M3[modResult[1][0]][modResult[1][1]] == d1
                && M3[modResult[1][0]][modResult[1][2]] == d2
        val b_flag = M3[modResult[2][0]][modResult[2][1]] == d1
                && M3[modResult[2][0]][modResult[2][2]] == d2
        return !(r_flag && g_flag && b_flag)
    }

    private fun ModResult(pixels: IntArray, flag: Int): Array<IntArray> {
        val modResult = arrayOf(IntArray(3),IntArray(3),IntArray(3),IntArray(3),IntArray(3),IntArray(3))
        if(flag == 1){
//            val red_p1 = Color.red(pixels[0])
//            val red_p2 = Color.red(pixels[2])
//            val red_q1 = Color.red(pixels[1])
//            val red_q2 = Color.red(pixels[3])
            modResult[0][0] = Color.red(pixels[0]) % B
            modResult[0][1] = Color.red(pixels[2]) % B
            modResult[0][2] = (Color.red(pixels[2]) + 1) % B

            modResult[1][0] = Color.green(pixels[0]) % B
            modResult[1][1] = Color.green(pixels[2]) % B
            modResult[1][2] = (Color.green(pixels[2]) + 1) % B

            modResult[2][0] = Color.blue(pixels[0]) % B
            modResult[2][1] = Color.blue(pixels[2]) % B
            modResult[2][2] = (Color.blue(pixels[2]) + 1) % B

            modResult[3][0] = Color.red(pixels[1]) % B
            modResult[3][1] = Color.red(pixels[3]) % B
            modResult[3][2] = (Color.red(pixels[3]) + 1) % B

            modResult[4][0] = Color.green(pixels[1]) % B
            modResult[4][1] = Color.green(pixels[3]) % B
            modResult[4][2] = (Color.green(pixels[3]) + 1) % B

            modResult[5][0] = Color.blue(pixels[1]) % B
            modResult[5][1] = Color.blue(pixels[3]) % B
            modResult[5][2] = (Color.blue(pixels[3]) + 1) % B
        }
        else{
            modResult[3][0] = Color.red(pixels[0]) % B
            modResult[3][1] = Color.red(pixels[2]) % B
            modResult[3][2] = (Color.red(pixels[2]) + 1) % B

            modResult[4][0] = Color.green(pixels[0]) % B
            modResult[4][1] = Color.green(pixels[2]) % B
            modResult[4][2] = (Color.green(pixels[2]) + 1) % B

            modResult[5][0] = Color.blue(pixels[0]) % B
            modResult[5][1] = Color.blue(pixels[2]) % B
            modResult[5][2] = (Color.blue(pixels[2]) + 1) % B

            modResult[0][0] = Color.red(pixels[1]) % B
            modResult[0][1] = Color.red(pixels[3]) % B
            modResult[0][2] = (Color.red(pixels[3]) + 1) % B

            modResult[1][0] = Color.green(pixels[1]) % B
            modResult[1][1] = Color.green(pixels[3]) % B
            modResult[1][2] = (Color.green(pixels[3]) + 1) % B

            modResult[2][0] = Color.blue(pixels[1]) % B
            modResult[2][1] = Color.blue(pixels[3]) % B
            modResult[2][2] = (Color.blue(pixels[3]) + 1) % B
        }
        return modResult
    }

    //保存水印图片
    fun saveWatermarkImages(bmpList: ArrayList<Bitmap>,  imgName: String, filePath: String) : ArrayList<String> {
        val path = android.os.Environment.getExternalStorageDirectory().path + "/Pictures/ Watermarks/"
        val imgPaths = ArrayList<String>()
        if (!File(path).exists()) { File(path).mkdir() }
        val file1 = File(filePath + "/" + imgName + "_watermark_1.png")
        val file2 = File(filePath + "/" + imgName + "_watermark_2.png")
        val out1: FileOutputStream
        val out2: FileOutputStream
        try {
            out1 = FileOutputStream(file1)
            out2 = FileOutputStream(file2)
            if (bmpList[0].compress(Bitmap.CompressFormat.PNG, 100, out1)) {
                out1.flush()
                out1.close()
            }
            if (bmpList[1].compress(Bitmap.CompressFormat.PNG, 100, out2)) {
                out2.flush()
                out2.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        imgPaths.add(file1.absolutePath)
        imgPaths.add(file2.absolutePath)
        return imgPaths
    }

    //将Uri指向的图片文件转换到file格式
    fun switchUri2File(activity : Activity, uri : Uri) : File{
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val actualImageCursor = activity.managedQuery(uri, projection, null, null, null)
        val actualImageColumnIndex = actualImageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        actualImageCursor.moveToFirst()
        val img_path = actualImageCursor.getString(actualImageColumnIndex)
        return File(img_path)
    }
}