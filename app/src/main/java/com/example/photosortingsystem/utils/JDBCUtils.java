package com.example.photosortingsystem.utils;

import com.example.photosortingsystem.entity.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;


/**
 * MySQL数据库连接方法类
 */
public class JDBCUtils {

    private static final String DATA_BASE_TABLE_NAME = "users"; //数据库表名

    private static String driver = "com.mysql.jdbc.Driver";     //MySQL驱动

    private static String url = "jdbc:mysql://192.168.31.66:3306/test";   //MySQL数据库
    /*private static String url = "jdbc:mysql://10.0.2.2/test";   //MySQL数据库（虚拟机调试用）*/
    private static String user = "root";        //用户名
    private static String password = "root";    //密码

    private static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return conn;
    }

    public static Map<String,String> login(User user) {     //登录时查数据库表
        HashMap<String, String> map = new HashMap<>();
        //建立连接
        Connection conn = getConnection();
        try {
            Statement st = conn.createStatement();

            String sql = "select * from " + DATA_BASE_TABLE_NAME + " where username ='" + user.getUsername() + "' and password ='" + user.getPassword() + "'";

            ResultSet res = st.executeQuery(sql);
            if (null == res) {
                return null;
            } else {
                int cnt = res.getMetaData().getColumnCount();
                res.next();
                for (int i = 1; i <= cnt; ++i) {
                    String field = res.getMetaData().getColumnName(i);
                    map.put(field, res.getString(field));
                }
                conn.close();
                st.close();
                res.close();
                return map;
            }
            } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int insertData(String username, String password) throws SQLException {    //注册时向数据库表插入内容
        //建立连接
        Connection conn = getConnection();
        //向users表插入数据
        String sql = "insert into " + DATA_BASE_TABLE_NAME + " (username,password) values (?,?);";
        PreparedStatement ps = conn.prepareStatement(sql);
        //通过setString赋值
        ps.setString(1, username);
        ps.setString(2, password);
        //返回状态码，判断是否插入成功
        int insertCode = ps.executeUpdate();
        conn.close();
        ps.close();
        return insertCode;
    }

}
