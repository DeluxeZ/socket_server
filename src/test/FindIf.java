package test;

import java.sql.*;

public class FindIf {
    public static String findif(String srcname, String usrname, String srcid, String ip) throws SQLException {
        Connection conn = null;
        conn = DriverManager.getConnection(Conn.url, Conn.user, Conn.password);
        Statement stmt = null;
        stmt = conn.createStatement();
        try {
            Class.forName(Conn.driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String info="301";
        String sql;
        sql = "SELECT srcname FROM source where srcid = '" + srcid + "'";
        ResultSet rs = null;
        rs = stmt.executeQuery(sql);
        if (rs.next()){
            if (rs.getString("srcname").equals(srcname)){
                info = "302";
            }
        }
        if (info.equals("301")) {
            sql = "INSERT INTO source (srcname,usrname,available,srcid,info,ip) VALUES ('" + srcname + "','" + usrname + "','1','" + srcid + "',null,'" + ip + "')";
            stmt.executeUpdate(sql);
        }
        conn.close();
        return info;
    }
}
