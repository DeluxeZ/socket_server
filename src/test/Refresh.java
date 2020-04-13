package test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Refresh {
    private static String usrname;

    public Refresh(String usrname) {
        this.usrname = usrname;
    }

    public static String refresh(String usrname) throws SQLException, IOException {
		//建立数据库连接
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(Conn.url, Conn.user, Conn.password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
		//执行SQL查询语句，获取列表内容
        Statement stmt = conn.createStatement();
        String sql;
        sql = "SELECT srcname, usrname, available, info, ip FROM source";

        ResultSet rs = stmt.executeQuery(sql);
        String Adi = "";
        while (rs.next()) {
            //存储查询结果，单行数据内部用“，”隔开，多行数据间用“/”隔开
            Adi = Adi + rs.getString("srcname") + ","
                    + rs.getString("usrname") + ","
                    + rs.getInt("available") + ","
                    + rs.getString("info") + ","
                    + rs.getString("ip") + "/";
        }

        return Adi;
    }
}
