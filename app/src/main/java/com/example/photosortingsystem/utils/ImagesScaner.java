package com.example.photosortingsystem.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.photosortingsystem.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 图片扫描类
 */
public class ImagesScaner {
    /**
     * 异步扫描SD卡多媒体文件,不阻塞当前线程
     */
    /*public static void scanMediaAsync(Context ctx, File file) {
        Intent scanner = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanner.setData(Uri.fromFile(file));
        ctx.sendBroadcast(scanner);
    }*/


    public static List<Map> getAlbumPhotos(Context ctx, String name) {
        List<Map> result = new ArrayList<>();
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(ctx, "Album.db", null, Config.dbversion);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = null;
        cursor = db.query("AlbumPhotos", null, "album_name = '" + name + "'", null, null, null, null);
        if (cursor.moveToFirst()) {
            Map<String, String> tmp;
            do {
                tmp = new HashMap<>();
                String album_name = cursor.getString(cursor.getColumnIndex("album_name"));
                String url = cursor.getString(cursor.getColumnIndex("url"));
                tmp.put("album_name", album_name);
                tmp.put("url", url);
                tmp.put("_data", url);
                result.add(tmp);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return result;
    }
    public static List<Map<String, String>> getAlbumInfo(Context ctx) {
        List<Map<String, String>> result = new ArrayList<>();
        Config.dbHelper = new MyDatabaseHelper(ctx, "Album.db", null, Config.dbversion);
        SQLiteDatabase db = Config.dbHelper.getWritableDatabase();
        Cursor cursor = null;
        cursor = db.query("Album", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            Map<String, String> tmp;
            do {
                tmp = new HashMap<>();;
                String album_name = cursor.getString(cursor.getColumnIndex("album_name"));
                String url = cursor.getString(cursor.getColumnIndex("show_image"));
                tmp.put("album_name", album_name);
                tmp.put("show_image", url);
                result.add(tmp);
                Log.d("ITEM", String.valueOf(tmp));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        Log.d("Album info", "END");
        return result;
    }
    public static List<Map> getMediaImageInfo(Context ctx) {
        //可以手动指定获取的列
        String[] columns = new String[]{
                MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DISPLAY_NAME
                , MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE, MediaStore.Images.Media.DATE_TAKEN};

        ContentResolver contentResolver = ctx.getContentResolver();
        //外部存储的SD卡的访问Uri
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        //  MediaStore.Images.Media.INTERNAL_CONTENT_URI
        Cursor cursor = contentResolver.query(uri, null, null, null, null);//获取全部列
        //  Cursor cursor = contentResolver.query(uri, columns, null, null, null);//获取指定列
        if (cursor != null) {
            Map<String, String> item = null;
            List<Map> result = new ArrayList<>();
            while (cursor.moveToNext()) {
                String[] columnNames = cursor.getColumnNames();
                item = new HashMap<>();
                for (String colnmnName : columnNames) {
                    int columnIndex = cursor.getColumnIndex(colnmnName);
                    String columnValue = cursor.getString(columnIndex);
                    item.put(colnmnName, columnValue);
                }
                result.add(item);
            }
            Log.d("debug", "getMediaImageInfo() size=" + result.size() + ", result=" + result);
            cursor.close();
            return result;
        }
        cursor.close();
        return null;
    }
    public static Map<String, String> getInformation(Context ctx, String url) {
        String[] columns = new String[]{
                MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DISPLAY_NAME
                , MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE, MediaStore.Images.Media.DATE_TAKEN};
        ContentResolver contentResolver = ctx.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Log.d("getInformation", url);
        Cursor cursor = contentResolver.query(uri, null, MediaStore.Images.Media.DATA+"='" + url + "'", null, null);//获取全部列
        Map<String, String> result = null;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String[] columnNames = cursor.getColumnNames();
                result = new HashMap<>();
                for (String colnmnName : columnNames) {
                    int columnIndex = cursor.getColumnIndex(colnmnName);
                    String columnValue = cursor.getString(columnIndex);
                    result.put(colnmnName, columnValue);
                }
            }
            Log.d("debug", "getMediaImageInfo() size=" + result.size() + ", result=" + result);
            cursor.close();
        }

        return result;
    }

    public static ArrayList<HashMap<String,String>> getAllPictures(Context context) {
        ArrayList<HashMap<String,String>> picturemaps = new ArrayList<>();
        HashMap<String,String> picturemap = null;
        ContentResolver cr = context.getContentResolver();
        //先得到缩略图的URL和对应的图片id
        Cursor cursor = cr.query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Images.Thumbnails.IMAGE_ID,
                        MediaStore.Images.Thumbnails.DATA
                },
                null,
                null,
                null);
        if (cursor.moveToFirst()) {
            do {
                picturemap = new HashMap<>();
                picturemap.put("image_id_path",cursor.getInt(0)+"");
                picturemap.put("thumbnail_path",cursor.getString(1));
                picturemaps.add(picturemap);

            } while (cursor.moveToNext());
            cursor.close();
        }
        //再得到正常图片的path
        for (int i = 0;i<picturemaps.size();i++) {

            picturemap = picturemaps.get(i);
            String media_id = picturemap.get("image_id_path");
            cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Images.Media.DATA
                    },
                    MediaStore.Images.Media._ID+"="+media_id,
                    null,
                    null
            );
            if (cursor.moveToFirst()) {
                do {
                    picturemap.put("image_id",cursor.getString(0));
                    picturemaps.set(i,picturemap);
                } while (cursor.moveToNext());
                cursor.close();
            }
            Log.d("PPPP", String.valueOf(picturemap));
        }

        return picturemaps;
    }
    //更新数据库，添加新图像
    public static void updateGallery(Context con, String filename)
    {
        MediaScannerConnection.scanFile(con,
            new String[] { filename }, null,
            new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                    Config.workdone = true;
                }
            });
    }
    //获取文件名的缩略图
    public static Bitmap getBitmap(Context ctx, String fileName) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //选择条件
        String whereClause = MediaStore.Images.Media.DATA + " = '" + fileName + "'";
        //结果收集
        ContentResolver cr = ctx.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,new String[] { MediaStore.Images.Media._ID }, whereClause,null, null);
        if (cursor == null || cursor.getCount() == 0) {
            if(cursor != null)
                cursor.close();
            Log.d("THUM", "null");
            return null;
        }
        cursor.moveToFirst();
        //图像表中的图像ID
        String videoId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        cursor.close();
        if (videoId == null) {
            Log.d("THUM", "null");
            return null;
        }
        long videoIdLong = Long.parseLong(videoId);
        //通过imageId获取缩略图表中的bitmap类型缩略图
        bitmap = MediaStore.Images.Thumbnails.getThumbnail(cr, videoIdLong, MediaStore.Images.Thumbnails.MINI_KIND, options);
        if (bitmap == null) {
            Log.d("THUM", "null");
        } else {
            Log.d("THUM","" + bitmap.getHeight() + " " + bitmap.getWidth());
        }
        return bitmap;
    }
}
