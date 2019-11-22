package com.example.myapplication;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

class StorageUtil extends Application {

    private static StorageUtil me;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public StorageUtil(){
        me=this;
    }
    //public static Context Context() {
    //    return me;
    //}
    public static ContentResolver ContentResolver() {
        return me.getContentResolver();
    }

    static boolean checkSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    static FileFilter videoFilter = new FileFilter() {

        @Override
        public boolean accept(File file) {
            String filename = file.getName();

            return filename.endsWith("MP4") || filename.endsWith("3GP") ||
                    filename.endsWith("mp4") || filename.endsWith("3gp");
        }
    };

    // 過濾所列的檔案類型
    static FileFilter imageFilter = new FileFilter() {

        @Override
        public boolean accept(File file) {
            String filename = file.getName();

            return filename.endsWith("JPG") || filename.endsWith("BMP") ||
                    filename.endsWith("PNG") || filename.endsWith("JPEG") ||
                    filename.endsWith("jpg") || filename.endsWith("bmp") ||
                    filename.endsWith("png") || filename.endsWith("jpeg");
        }
    };

    static FileFilter musicFilter = new FileFilter() {

        @Override
        public boolean accept(File file) {
            String filename = file.getName();

            return filename.endsWith("mp3") || filename.endsWith("MP3") ||
                   filename.endsWith("3gp");
        }
    };

    static String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = null;
        try {
            cursor = ContentResolver().query(contentURI, null, null, null, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    //=====================================================================
    // 將bitmap儲存
    //=====================================================================
    public static void savePic(Bitmap b, String strFileName) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(strFileName);
            b.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //=====================================================================
    // 取得拍照的檔名
    //=====================================================================
    public static String getPhotoDefaultName() {
        return "IMG_" + simpleDateFormat.format(System.currentTimeMillis()) + ".jpg";
    }

}
