package test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;

public class InsertSource {
    String srcname = null;
    String usrname = null;
    String srcid = null;
    String ip = null;
    Socket so = null;

    public InsertSource(Socket socket, String srcname, String usrname, String srcid, String ip) {
        this.so = socket;
        this.srcname = srcname;
        this.usrname = usrname;
        this.srcid = srcid;
        this.ip = ip;
    }

    public void insert() throws SQLException, IOException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(Conn.url, Conn.user, Conn.password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Statement stmt = conn.createStatement();
        String sql = "INSERT into source (srcname, usrname, srcid, ip) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, srcname);
        ps.setString(2, usrname);
        ps.setString(3, srcid);
        ps.setString(4, ip);
        ps.executeUpdate();
        conn.close();

        OutputStream os = so.getOutputStream();// 字节输出流
        PrintWriter pw = new PrintWriter(os);// 字符输出流
        BufferedWriter bw = new BufferedWriter(pw);// 缓冲输出流

        bw.write("OK");
        bw.flush();
        bw.close();
        pw.close();
        os.close();
    }
}
