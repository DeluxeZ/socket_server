package test;

import java.sql.*;

public class FindIf {
    public static String findif(String srcname, String usrname, String srcid, String ip) throws SQLException {
        Connection conn = null;
		//建立数据库连接
        conn = DriverManager.getConnection(Conn.url, Conn.user, Conn.password);
        Statement stmt = null;
        stmt = conn.createStatement();
        try {
            Class.forName(Conn.driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String info="301";
		//执行SQL查询语句
        String sql;
        sql = "SELECT srcname FROM source where srcid = '" + srcid + "'";
        ResultSet rs = null;
		//存储查询结果
        rs = stmt.executeQuery(sql);
        if (rs.next()){
            if (rs.getString("srcname").equals(srcname)){
				//有重名时返回302
                info = "302";
            }
        }
        if (info.equals("301")) {
			//无重名时插入该数据并返回301
            sql = "INSERT INTO source (srcname,usrname,available,srcid,info,ip) VALUES ('" + srcname + "','" + usrname + "','1','" + srcid + "',null,'" + ip + "')";
            stmt.executeUpdate(sql);
        }
		//关闭连接
        conn.close();
        return info;
    }
}
