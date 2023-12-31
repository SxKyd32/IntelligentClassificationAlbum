package com.example.photosortingsystem;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.photosortingsystem.adapter.HorizontalScrollViewAdapter;
import com.example.photosortingsystem.entity.Scan;
import com.example.photosortingsystem.utils.MyDatabaseOperator;
import com.example.photosortingsystem.view.MyHorizontalScrollView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.senab.photoview.PhotoViewAttacher;

import static com.example.photosortingsystem.utils.ImagesScaner.getAlbumPhotos;
import static com.example.photosortingsystem.utils.ImagesScaner.getMediaImageInfo;


/**
 * 照片详情页，可以左右滚动
 */
public class PhotoDetailActivity extends AppCompatActivity /*implements View.OnClickListener */{

    //初始化几个textview， 可以点击并且出发事件
    private TextView txt_back;
    //    private TextView txt_share;
    private TextView txt_love;
    private TextView txt_delete;

    //自定义的布局， 实现下面缩略图，上面大图
    private MyHorizontalScrollView mHorizontalScrollView;
    //适配器
    private HorizontalScrollViewAdapter mAdapter;
    private ImageView mImg;
    //照片数组。照片在drawable文件夹中，名字为a.png ...
    private List<Map> mDatas ;

    private PhotoViewAttacher mAttacher;
    //which image
    int position_now = -1;
    //image url
    String url = null;
    String image = null;
    //has been init
    boolean init = false;
    private String type = null;

    private int position_tmp;

    private String url_detect = "https://api-cn.faceplusplus.com/facepp/v3/detect";     //face++人脸识别api
    private String api_key = "WQMnizoArxDI0w8jbpxlP1XGCj3BZ65a";                        //api key
    private String api_secret = "Luydc6vpPPYk6C4GyliH8889rlImPd4G";                     //api密码



    private Handler myHandler = new Handler()
    {
        @Override
        //重写handleMessage方法,根据msg中what的值判断是否执行后续操作
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0x21:

                    Glide
                            .with(PhotoDetailActivity.this)
                            .load((String) mDatas.get(position_tmp).get("_data"))
                            .error(R.drawable.error)
                            .thumbnail(0.1f)
                            .into(mImg);

                    break;

                case 0x22:

                    break;
            }
        }
    };

    public PhotoDetailActivity() {

    }

    // 重写创建活动的方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fg_detail);
//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

/*        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#11000000")));
        getSupportActionBar().setSplitBackgroundDrawable(new ColorDrawable(Color.parseColor("#11000000")));
         绑定textview按钮
        bindViews();*/
        // get photo list
        initPhoto();
        if (type != null)
            mDatas = getAlbumPhotos(this, this.type);
            // 下面设置下面缩略图上面大图。
        else
            mDatas = getMediaImageInfo(this.getBaseContext());
        mImg = (ImageView) findViewById(R.id.id_content);
//        mAttacher = new PhotoViewAttacher(mImg);
        Glide.with(PhotoDetailActivity.this).load((String) mDatas.get(position_now).get("_data")).into(mImg);

        mHorizontalScrollView = (MyHorizontalScrollView) findViewById(R.id.id_horizontalScrollView);
        mAdapter = new HorizontalScrollViewAdapter(this, mDatas);
        //添加滚动回调
        mHorizontalScrollView
                .setCurrentImageChangeListener(new MyHorizontalScrollView.CurrentImageChangeListener()
                {
                    @Override
                    public void onCurrentImgChanged(int position,
                                                    View viewIndicator)
                    {
                        if (!init) {
                            position = position_now;
                            init = true;
                        }
                        Log.d("PhotoDetail: ", "Image change to: " + position);
                        try {
                            position_tmp = position;
                            myHandler.sendEmptyMessage(0x21);
                        } catch (Exception e) {
                            ;
                        }

                        viewIndicator.setBackgroundColor(Color.parseColor("#AA024DA4"));
                    }
                });
        //添加点击回调
        mHorizontalScrollView.setOnItemClickListener(new MyHorizontalScrollView.OnItemClickListener()
        {

            @Override
            public void onClick(View view, int position)
            {
                mImg.setImageURI(Uri.fromFile(new File((String)mDatas.get(position).get("_data"))));

                view.setBackgroundColor(Color.parseColor("#AA024DA4"));
            }
        });
        //设置适配器
        mHorizontalScrollView.initDatas(mAdapter, position_now);
    }
    private void initPhoto() {
        Intent intent = getIntent();
        try {
            position_now = intent.getIntExtra("position", -1);
            url = intent.getStringExtra("url");
            type = intent.getStringExtra("type");

        } catch (Exception e) {
            Log.d("ERROR: ", "" + e);
        }
        image = url;
        Log.d("Test-----------------: ", "" + position_now + " " + url);
    }
    //UI组件初始化与事件绑定
    private void bindViews() {
        // 返回删除等按钮
/*        txt_back = (TextView) findViewById(R.id.back);
        txt_share = (TextView) findViewById(R.id.share);
        txt_love = (TextView) findViewById(R.id.love);
        txt_delete = (TextView) findViewById(R.id.delete);*/
        // 设置监听
/*        txt_back.setOnClickListener(this);
        txt_share.setOnClickListener(this);
        txt_love.setOnClickListener(this);
        txt_delete.setOnClickListener(this);*/
    }
    // 恢复点击状态为未点击状态
    private void setSelect() {
/*        txt_back.setSelected(false);
        txt_share.setSelected(false);
        txt_love.setSelected(false);
        txt_delete.setSelected(false);*/
    }
    /**
     * 生成动作栏上的菜单项目
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_for_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }
    /**
     * 监听菜单栏目的动作，当按下不同的按钮执行相应的动作
     *
     * @param item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                // 返回
                this.finish();
                break;

            case R.id.action_about:
                //goto PhotoInfoActivity
                Intent intent = new Intent(this, PhotoInfoActivity.class);
                int position;
                if (!init) {
                    position = position_now;
                } else {
                    position = mHorizontalScrollView.getmShowIndex();
                }
                //send args
                intent.putExtra("position", position);
                intent.putExtra("url", (String)mDatas.get(position).get("_data"));
                startActivity(intent);
                break;

            case R.id.action_scan:
                //进行人脸识别
                scanFaces();
                break;

            /*case R.id.action_delete:
                //删除图片
                deleteImage();
                //goto PhotoInfoActivity
                Intent intent1 = new Intent(this, MainActivity.class);
                startActivity(intent1);
                break;*/

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //图片人脸识别
    private void scanFaces() {
        //创建http网络请求
        OkHttpClient okHttpClient = new OkHttpClient();

        //创建一个RequestBody，用add添加键值对
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.parse("multipart/form-data"))
                .addFormDataPart("api_key", api_key)
                .addFormDataPart("api_secret", api_secret)
                .addFormDataPart("return_attributes", "age,gender,beauty");
                File file = new File(image);
            builder.addFormDataPart("image_file", file.getName() ,RequestBody.create(MediaType.parse("image"),file));

        RequestBody requestBody = builder.build();

        //创建Request对象，设置url地址为face++的人脸识别url地址，将RequestBody作为post方法的参数传入
        Request request = new Request.Builder().url(url_detect).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        e.printStackTrace();
                        Toast.makeText(PhotoDetailActivity.this, "请求网络数据失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String result = null;
                        try {
                            result = response.body().string();
                            Log.d("scanFace", result);
                            //这里开始解析json
                            Scan scan = new Gson().fromJson(result, new TypeToken<Scan>() {
                            }.getType());
                            List<Scan.Face> faces = scan.faces;
                            if (faces == null || faces.size() <= 0) {
                                Toast.makeText(PhotoDetailActivity.this, "没有找到人物哦", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            int index = 1;
                            for (Scan.Face face : faces) {
                                //获取年龄
                                Scan.Face.Attributes.Age age = face.attributes.age;
                                //获取性别
                                Scan.Face.Attributes.Gender gender = face.attributes.gender;
                                //获取颜值
                                Scan.Face.Attributes.Beauty beauty = face.attributes.beauty;

                                if (gender.value.equals("Male")) {
                                    Toast.makeText(PhotoDetailActivity.this, "人脸" + index + ": 性别:" + "男" + " 年龄:" + age.value +
                                            " 颜值:" + beauty.male_score + "\n", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(PhotoDetailActivity.this, "人脸" + index + ": 性别:" + "女" + " 年龄:" + age.value +
                                            " 颜值:" + beauty.female_score + "\n", Toast.LENGTH_LONG).show();
                                }
                                index++;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


    //删除图片
    /*private void deleteImage() {
        //todo 只从db删除，不从本地删除
        //ContentValues value = new ContentValues();
        MyDatabaseOperator myOperator = new MyDatabaseOperator(PhotoDetailActivity.this,
                Config.DB_NAME, Config.dbversion);
        Log.d("deleteImage", "okk");
        myOperator.erase("AlbumPhotos", "url = ?", new String[] { "'" + image + "'"});
        myOperator.erase("TFInformation", "url = ?", new String[] { "'" + image + "'"});
        myOperator.close();

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = PhotoDetailActivity.this.getContentResolver();
        String where = MediaStore.Images.Media.DATA + "='" + image + "'";
        mContentResolver.delete(uri, where, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ;
        } else {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }*/



    // 下面按钮（返回删除等）的点击动作
/*    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // 返回
            case R.id.back:
                setSelect();
                txt_back.setSelected(true);
                System.out.println("1");
                PhotoDetailActivity.this.finish(); // 结束当前的activity， 返回上一个界面
                break;
*//*            case R.id.share: // 分享
                setSelect();
                txt_share.setSelected(true);
                System.out.println("2");

                break;*//*
            case R.id.love: // 喜爱
                setSelect();
                txt_love.setSelected(true);
                System.out.println("3");

                break;
*//*            case R.id.delete: // 删除
                setSelect();
                txt_delete.setSelected(true);

                txt_delete.setSelected(false);
                System.out.println("4");

                break;*//*
        }
    }*/

}