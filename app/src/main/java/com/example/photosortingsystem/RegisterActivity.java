package com.example.photosortingsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.photosortingsystem.entity.User;
import com.example.photosortingsystem.utils.JDBCUtils;

import java.sql.SQLException;


/**
 * 注册界面
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    Button reg, reset;
    EditText user, pwd;
    String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        reg = (Button)findViewById(R.id.btn_reg_r);
        reset = (Button)findViewById(R.id.btn_reset);

        user = (EditText)findViewById(R.id.et_username_r);
        pwd = (EditText)findViewById(R.id.et_password_r);

        reg.setOnClickListener(this);
        reset.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //注册按钮
            case R.id.btn_reg_r:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int status = 0;
                        try {
                            username = user.getText().toString();
                            password = pwd.getText().toString();
                            status = JDBCUtils.insertData(username, password);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if (1 == status) {  //插入成功
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterActivity.this,"注册成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {    //插入失败
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterActivity.this,"注册失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();


                break;
            case R.id.btn_reset:
                    user.setText("");
                    pwd.setText("");
                break;
        }
    }
}