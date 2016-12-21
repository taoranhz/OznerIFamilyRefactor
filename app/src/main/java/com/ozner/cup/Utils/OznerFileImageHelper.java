package com.ozner.cup.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ozner_67 on 2016/12/20.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class OznerFileImageHelper {
    public static class ScaleBitmap {
        public Bitmap bitmap;
        public String bmpPath;
    }

    //

    //获取图片缩略图并保存本地
    public static String getSmallBitmapPath(Context context, String filePath) {
//        ScaleBitmap result = new ScaleBitmap();
        String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
        if (!filename.startsWith("oznercache")) {
            filename = "oznercache" + filename;
            if (context != null) {
                File tempParentDir = new File(Environment.getExternalStorageDirectory().getPath() + "/OznerCache/");
                if (!tempParentDir.exists()) {
                    try {
                        tempParentDir.mkdir();
                    } catch (Exception ex) {
                        ex.printStackTrace();
//                        if (LogUtilsLC.APP_DBG)
//                            Log.e("tag", "创建HoYoImage_Ex：" + ex.getMessage());
                    }
                }
                File tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/OznerCache/" + filename);
                final BitmapFactory.Options options = new BitmapFactory.Options();
                String imgPath = "";
                if (tempFile.exists()) {
                    options.inJustDecodeBounds = false;
                    try {
//                        result.bitmap = BitmapFactory.decodeFile(tempFile.getPath(), options);
//                        result.bmpPath = tempFile.getPath();
                        return tempFile.getPath();
                    } catch (OutOfMemoryError ex) {
                        ex.printStackTrace();
//                        if (LogUtilsLC.APP_DBG)
//                            Log.e("tag", "getSmallBitmap:" + ex.getMessage());
                        return null;
                    }
                } else {
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(filePath, options);
                    Bitmap tempbitmap;
                    if (options.outHeight * options.outWidth > 960 * 720) {
                        options.inSampleSize = getScaleValue(options, 800, 600);
                        options.inJustDecodeBounds = false;
                        options.inScaled = true;
                        tempbitmap = BitmapFactory.decodeFile(filePath, options);

                        try {
                            tempFile.createNewFile();
                            FileOutputStream fout = new FileOutputStream(tempFile);
                            tempbitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                            fout.flush();
                            fout.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
//                            if (LogUtilsLC.APP_DBG)
//                                Log.e("tag", "getSmallBitmap_createNewFile：" + ex.getMessage());
                        }
                        imgPath = tempFile.getPath();
                    } else {
                        tempbitmap = BitmapFactory.decodeFile(filePath);
                        imgPath = filePath;
                    }
//                    result.bitmap = tempbitmap;
//                    result.bmpPath = imgPath;
                    return imgPath;
                }
            }
        } else {
//            result.bitmap = BitmapFactory.decodeFile(filePath);
//            result.bmpPath = filePath;
            return filePath;
        }
        return null;
    }

    //计算图片缩放比例
    public static int getScaleValue(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int scaleVal = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            scaleVal = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return scaleVal;
    }

    //获取图片要保存的uri
    public static Uri getOutPutPicFileUri() {
        return Uri.fromFile(getOutPutPicFile());
    }

    //获取图片要保存的路径
    public static File getOutPutPicFile() {
        File mediaStorageDir = null;
        try {
            mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//            if (LogUtilsLC.APP_DBG)
//                Log.e("tag", "Successfully created mediaStorageDir: " + mediaStorageDir);

        } catch (Exception e) {
            e.printStackTrace();
//            if (LogUtilsLC.APP_DBG)
//                Log.e("tag", "Error in Creating mediaStorageDir: " + mediaStorageDir);
        }

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
//                if (LogUtilsLC.APP_DBG)
//                    Log.e("tag",
//                            "failed to create directory, check if you have the WRITE_EXTERNAL_STORAGE permission");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }


    /**
     * 2.  * 以最省内存的方式读取本地资源的图片
     * 3.  * @param context
     * 4.  * @param resId
     * 5.  * @return
     * 6.
     */
    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    //从资源文件获取drawable对象
    public static Drawable readBitDrawable(Context context, int resId) {
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapDrawable.createFromStream(is, "");
    }
}
