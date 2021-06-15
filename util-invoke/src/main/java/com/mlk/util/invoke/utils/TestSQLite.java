package com.mlk.util.invoke.utils;

import java.sql.*;


/**
 * @author malikai
 * @date 2021年06月10日 15:45
 */
public class TestSQLite {

    public static void main(String[] args) {
        Connection conn = null;
        try {
            //连接SQLite的JDBC

            Class.forName("org.sqlite.JDBC");

            //建立一个数据库名zieckey.db的连接，如果不存在就在当前目录下创建之

            conn = DriverManager.getConnection("jdbc:sqlite:salary.db", "admin", "123");


            Statement stat = conn.createStatement();

            //创建一个表，两列
            stat.executeUpdate("create table salary(name varchar(20), salary int);");

            //插入数据
            stat.executeUpdate("insert into salary values('ZhangSan',8000);");

            stat.executeUpdate("insert into salary values('LiSi',7800);");
            stat.executeUpdate("insert into salary values('WangWu',5800);");
            stat.executeUpdate("insert into salary values('ZhaoLiu',9100);");
            //查询数据
            ResultSet rs = stat.executeQuery("select * from salary;");
            //将查询到的数据打印出来
            while (rs.next()) {

                System.out.print("name = " + rs.getString("name") + " ");

                System.out.println("salary = " + rs.getString("salary"));

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }
}
