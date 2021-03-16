package com.lemon.util;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class JDBCUtils {

    /**
     * 获取到数据库连接对象
     * @return
     */
    public static Connection getConnection() {
        //定义数据库连接
        //Oracle：jdbc:oracle:thin:@localhost:1521:DBName
        //SqlServer：jdbc:microsoft:sqlserver://localhost:1433; DatabaseName=DBName
        //MySql：jdbc:mysql://localhost:3306/DBName
        String url="jdbc:mysql://test.lemonban.com:3306/future?useUnicode=true&characterEncoding=utf-8";
        String user="auto_test";
        String password="auto_test";
        //定义数据库连接对象
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user,password);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void main(String[] args) {
        //1、插入数据
        //String sqlStr ="insert into member VALUES (10001010,'lemon','25D55AD283AA400AF464C76D713C07AD','13310102021',1,1000.0,'2020-11-11 16:39:36');";
        //update(sqlStr);
        //2、更改数据
        //String sql = "update member set reg_name='lemon' where mobile_phone=13310102021;";
        //update(sql);
        //3、删除数据
        //...
        //查询
        //4、查询所有的结果集
        //String sql = "select * from member limit 10";
        //queryAll(sql);
        //5、查询一条结果集
        //String sql = "select * from member where mobile_phone=13310102021";
        //System.out.println(queryOne(sql));
        //6、查询单个数据
        //String sql="select count(*) from member where mobile_phone=13310102021";
        //System.out.println(querySingle(sql));

        System.out.println(querySingle("select count(*) from member where mobile_phone=13323994545"));


    }

    /**
     * 修改,删除和插入方法封装
     * @param sql 要执行的sql语句
     */
    public static void update(String sql){
        //1、获取到数据库连接对象
        Connection conn = getConnection();
        //2、数据库操作
        QueryRunner queryRunner = new QueryRunner();
        try {
            queryRunner.update(conn,sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        //3、关闭数据库连接
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * 查询所有的结果集
     * @param sql 要执行的sql语句
     * @return
     */
    public static List<Map<String,Object>> queryAll(String sql){
        //1、获取到数据库连接对象
        Connection conn = getConnection();
        //2、数据库操作
        QueryRunner queryRunner = new QueryRunner();
        try {
            //第一个参数：数据库连接对象 ，第二个参数：执行sql语句 第三个参数：接收查询结果
            List<Map<String,Object>> result = queryRunner.query(conn,sql,new MapListHandler());
            return result;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        //3、关闭数据库连接
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * 查询结果集中的第一条数据
     * @param sql 要执行的sql语句
     * @return 结果
     */
    public static Map<String,Object> queryOne(String sql){
        //1、获取到数据库连接对象
        Connection conn = getConnection();
        //2、数据库操作
        QueryRunner queryRunner = new QueryRunner();
        try {
            //第一个参数：数据库连接对象 ，第二个参数：执行sql语句 第三个参数：接收查询结果
            Map<String,Object> result = queryRunner.query(conn,sql,new MapHandler());
            return result;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        //3、关闭数据库连接
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     * 查询结果集中的单个数据
     * @param sql 要执行的sql语句
     * @return 结果
     */
    public static Object querySingle(String sql){
        //1、获取到数据库连接对象
        Connection conn = getConnection();
        //2、数据库操作
        QueryRunner queryRunner = new QueryRunner();
        try {
            //第一个参数：数据库连接对象 ，第二个参数：执行sql语句 第三个参数：接收查询结果
            Object result = queryRunner.query(conn,sql,new ScalarHandler<Object>());
            return result;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        //3、关闭数据库连接
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}