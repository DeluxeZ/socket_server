package test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;

public class InsertSource {
	//定义参数
    String srcname = null;//资源名
    String usrname = null;//设备名
    String srcid = null;//资源识别号
    String ip = null;//设备IP
    Socket so = null;//定义socket

    public InsertSource(Socket socket, String srcname, String usrname, String srcid, String ip) {
		//初始化
        this.so = socket;
        this.srcname = srcname;
        this.usrname = usrname;
        this.srcid = srcid;
        this.ip = ip;
    }

    public void insert() throws SQLException, IOException {
		//建立数据库连接
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(Conn.url, Conn.user, Conn.password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
		//执行插入语句
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
		//资源释放
        bw.write("OK");
        bw.flush();
        bw.close();
        pw.close();
        os.close();
    }
}
