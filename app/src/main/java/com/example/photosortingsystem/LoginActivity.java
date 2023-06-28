package com.example.photosortingsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.photosortingsystem.entity.User;
import com.example.photosortingsystem.utils.JDBCUtils;

import java.util.Map;


/**
 * 登录界面
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button login, reg;
    EditText user, pwd;
    String username, password;
    User u;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (Button)findViewById(R.id.btn_login);
        reg = (Button)findViewById(R.id.btn_reg);

        user = (EditText)findViewById(R.id.et_username);
        pwd = (EditText)findViewById(R.id.et_password);

        login.setOnClickListener(this);
        reg.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                username = user.getText().toString().trim();
                password = pwd.getText().toString().trim();

                u = new User(username,password);
                checkLogin(u);
                /*this.finish();*/
                break;
            case R.id.btn_reg:
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
                break;
        }
    }

    //Android4.0以后不支持在主线程进行耗时操作，因此，如果设备是4.0版本以上的，要新开一条线程操作数据库
    class DBThread implements Runnable {
        private User user;
        private Context context;

        public void setUser(User user) {
            this.user = user;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            Map<String, String> result = JDBCUtils.login(user);
            if (result != null && result.size() > 0) {
                Log.d("msg", "yes!");   //账号密码与数据库匹配，登录成功
                //Toast.makeText(getApplicationContext(),"登录成功！", Toast.LENGTH_SHORT).show();
                /*Intent intent = new Intent(this, PhotoActivity.class);
                intent.putExtra("user", u);
                context.startActivity(intent);*/
                startActivity(new Intent(getApplicationContext(),WelcomeActivity.class));
            } else {
                Log.d("msg", "no!");    //账号密码与数据库不匹配，登录失败
            }
        }
    }

    private void checkLogin(User u) {
        DBThread dt = new DBThread();
        dt.setUser(u);
        dt.setContext(this);
        Thread thread = new Thread(dt);
        thread.start();
    }
}