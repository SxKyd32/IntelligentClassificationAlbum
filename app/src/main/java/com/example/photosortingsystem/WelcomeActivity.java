package com.example.photosortingsystem;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.photosortingsystem.utils.MyDatabaseOperator;
import com.example.photosortingsystem.utils.SystemDatabseOperator;

import org.tensorflow.demo.TensorFlowImageClassifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


import static com.example.photosortingsystem.utils.ImageDealer.do_tensorflow;
import static com.example.photosortingsystem.utils.ImageDealer.insertImageIntoDB;



/**
 * 进入系统显示欢迎界面，此时处理图片并进行分类（可选择是否后台分类）
 */
public class WelcomeActivity extends AppCompatActivity {
    //获得许可
    private static final int PERMISSION_REQUEST_STORAGE = 200;
    private static final String TAG = WelcomeActivity.class.getSimpleName();
    //扫描图像并保存
    private List<String> stillInDeviceImages;
    private List<String> notBeClassifiedImages;

    private ContentValues value;
    private MyDatabaseOperator myoperator;

    private TensorFlowImageClassifier classifier;

    private int i = 0;
    private int size = 0;
    private TextView textView = null;
    private TextView textViewTitle = null;
    private ProgressBar pbar;
    private final String[] actions =  {
            "全部进入APP时处理", "全部后台处理", "根据图片数量决定"};



    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //扫描图像
                case 0x1:
                    Log.d("MESSAGE", "0x1111");
                    if (textView != null)
                        textView.setText("\n正在扫描图片 ");
                    break;
                //扫描图像完成
                case 0x2:
                    do_afterScanImage();
                    break;
                //用tf对图像进行分类
                case 0x3:
                    textView.setText("正在准备...");
                    break;
                case 0x23:
                    i++;
                    if (textView != null)
                        textView.setText("\n正在处理图片 " + i + "/" + size);
                    break;
                case 0x24:
                    do_finishThisActivity();
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /**
         * UI操作
         */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /**
         * 绑定UI组件
         */
        setContentView(R.layout.activity_welcome);
        textView = (TextView) findViewById(R.id.work_process);
        textViewTitle = (TextView) findViewById(R.id.app_title);
        pbar = (ProgressBar) findViewById(R.id.progressBar);
        pbar.setVisibility(pbar.GONE);
        setAppName();



        if (Build.VERSION.SDK_INT >= 23) {
            /**
             * 判断权限是否已获取
             */
            // check permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                // require permission for wr
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                }, PERMISSION_REQUEST_STORAGE);
            }
            else {  //执行
                prepareForApplication();
            }
        }
        else {
            prepareForApplication();
        }
    }

    /**
     * do it after require permission
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode,grantResults);
    }

    /**
     * if have permission will do this, or show a toast
     * @param requestCode
     * @param grantResults
     */
    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pbar.setVisibility(pbar.VISIBLE);
                myHandler.sendEmptyMessage(0x3);
                prepareForApplication();
            } else {
                Toast.makeText(WelcomeActivity.this,
                        "对不起，不能访问存储卡我不能继续工作！",
                        Toast.LENGTH_LONG).show();
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        WelcomeActivity.this.finish();
                    }
                };
                timer.schedule(task, 1000 * 2);
            }
        }
    }

    /**
     * open another thread to scan images and judge whether the image had
     * been deleted in other places or add some new image but has not been classified by tf
     */
    private void prepareForApplication() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                do_prepare(WelcomeActivity.this);
                Looper.loop();
            }
        }).start();
    }


    /**
     * when open application, this function will scan images in device and check them with the db
     * of this application to confirm whether the image has been classified by tf or whether the
     * images had been delete in other places, and do something to keep application running with
     * no error
     * @param ctx
     */
    private void do_prepare(Context ctx) {
        //标记在数据库中但不在设备中的图像
        stillInDeviceImages = new ArrayList<>();
        //保存未被tf分类的图像
        notBeClassifiedImages = new ArrayList<>();
        //获取本机所有图片
        List<Map> imagesInDevice = SystemDatabseOperator.getExternalImageInfo(ctx);
        //数据库的运算准备
        MyDatabaseOperator operator = new MyDatabaseOperator(ctx, Config.DB_NAME, Config.dbversion);

        String url;
        List<Map> findResult;
        for (Map<String, String> imageInfo : imagesInDevice) {
            myHandler.sendEmptyMessage(0x1);
            url = imageInfo.get("_data");
            //检查是否已被分类
            findResult = operator.search("TFInformation", "url = '" + url + "'");
            if (findResult.size() == 0) {
                //未被分类
                notBeClassifiedImages.add(url);
                Log.d("TFInformation", "no");
            }
            else {
                //已被分类
                stillInDeviceImages.add(url);
                Log.d("TFInformation", "yes");
            }
        }

        size = notBeClassifiedImages.size();
        //从数据库删除本地已被删除的图片
        //todo 这个功能不生效
        List<Map> imagesInDB = operator.search("AlbumPhotos");
        for (Map<String, String> imageInfo : imagesInDB) {
            url = imageInfo.get("url");
            //测试是否被删除
            findResult = operator.search("AlbumPhotos", "url = '" + url + "'");
            if (findResult.size() == 0) {
                //已删除，在db中删除
                operator.erase("AlbumPhotos", "url = ?", new String[] { "'" + url + "'"});
                operator.erase("TFInformation", "url = ?", new String[] { "'" + url + "'"});
            }
            else {
                //没有删除，不进行操作
            }
        }

        //从数据库删除已被删除的相册
        String album_name;
        List<Map> typeInAlbum = operator.search("Album");
        for (Map<String, String> albumInfo : typeInAlbum) {
            album_name = albumInfo.get("album_name");
            findResult = operator.search("AlbumPhotos", "album_name = '" + album_name + "'");
            if (findResult.size() == 0) {
                //已删除，在db中删除
                operator.erase("Album", "album_name = ?", new String[] { "'" + album_name + "'"});
            }
            else {
                //没有删除，不进行操作
            }
        }
        operator.close();
        myHandler.sendEmptyMessage(0x2);
        //myHandler.sendEmptyMessage(0x24);
    }

    /**
     * confirm what level you like to use or do something by level
     */
    private void do_afterScanImage() {
        Log.d("MESSAGE", "0x1");
        final MyDatabaseOperator operator = new MyDatabaseOperator(WelcomeActivity.this, Config.DB_NAME, Config.dbversion);
        List<Map> findResult = operator.search("Settings");

        try {
            //非首次打开应用
            String tmp = (String) findResult.get(0).get("notFirstIn");
            int level = Integer.parseInt((String) findResult.get(0).get("updateTime"));
            Log.d("LEVEL", "" + level);
            do_byLevel(level);
        } catch (Exception e) {
            //首次打开应用
            AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
            builder.setTitle("选择图片处理的时间");
            builder.setIcon(R.drawable.things);
            builder.setItems(actions, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    final MyDatabaseOperator operator = new MyDatabaseOperator(WelcomeActivity.this, Config.DB_NAME, Config.dbversion);
                    ContentValues values = new ContentValues();
                    values.put("notFirstIn", "true");
                    values.put("updateTime", which);
                    operator.insert("Settings", values);
                    operator.close();
                    Toast.makeText(WelcomeActivity.this,  actions[which], Toast.LENGTH_SHORT).show();
                    do_byLevel(which);
                }
            });
            builder.show();
        }
        operator.close();
    }

    /**
     * when to classify images? you have three choices:
     * 1. when open app && when all images have been classified -> goto MainActivity
     * 2. when open app, just only scan picture and clear app'db, classifying images in background
     * 3. if image is not too much, goto MainActivity until new images have been classified, or do
     * it as choice 2
     * @param level 1, 2 or 3
     */
    private void do_byLevel(int level) {
        if (level == 0) {
            classifyNewImages();
        }
        else if (level == 1) {
            Config.needToBeClassified = notBeClassifiedImages;
            myHandler.sendEmptyMessage(0x24);
        }
        else if (level == 2) {
            if (notBeClassifiedImages.size() <= Config.imageNumber) {
                classifyNewImages();
            }
            else {
                Config.needToBeClassified = new ArrayList<>();
                Config.needToBeClassified.addAll(notBeClassifiedImages.subList(Config.imageNumber, notBeClassifiedImages.size()));
                notBeClassifiedImages = notBeClassifiedImages.subList(0, Config.imageNumber);
                classifyNewImages();
            }
        }
    }

    /**
     * when handler get the message that 'tf is end', the do this function to update UI,
     * goto MainActivity and finish this activity
     */
    private void do_finishThisActivity() {
        pbar.setVisibility(pbar.GONE);
        textView.setText("正在进入...");
        //setAppName();
        final Intent it = new Intent(getApplication(), MainActivity.class); //你要转向的Activity
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(it);
                WelcomeActivity.this.finish();
            }
        };
        timer.schedule(task, 1000 * 2);
    }
    private void setAppName() {
        textViewTitle.setText("欢迎");
        textViewTitle.setTextSize(32);
        textViewTitle.setTextColor(Color.rgb(140, 21, 119));
    }

    /**
     * for every image will be classified, this function will classify them
     */
    private void classifyNewImages() {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
                Looper.prepare();
                // init tensorflow
                if (classifier == null) {
                    // get permission
                    classifier = new TensorFlowImageClassifier();
                    try {
                        classifier.initializeTensorFlow(
                                getAssets(), Config.MODEL_FILE, Config.LABEL_FILE,
                                Config.NUM_CLASSES, Config.INPUT_SIZE, Config.IMAGE_MEAN,
                                Config.IMAGE_STD, Config.INPUT_NAME, Config.OUTPUT_NAME);
                    } catch (final IOException e) {
                        ;
                    }
                }
                Bitmap bitmap;
                value = new ContentValues();
                myoperator = new MyDatabaseOperator(WelcomeActivity.this, Config.DB_NAME, Config.dbversion);
                for (String image : notBeClassifiedImages) {
                    myHandler.sendEmptyMessage(0x23);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                    bitmap = BitmapFactory.decodeFile(image, options);
                    insertImageIntoDB(image, do_tensorflow(bitmap, classifier), myoperator, value);
                }
                myoperator.close();
                myHandler.sendEmptyMessage(0x24);
                Looper.loop();
            }
        }).start();
    }
}
