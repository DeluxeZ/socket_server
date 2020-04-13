package test;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
	//用向量组存储列表内容
    static ArrayList<String> logged = new ArrayList<>();
    static ArrayList<ServerThread> serverThreads = new ArrayList<>();
    static int count = 0;//列表内资源数

    public static void main(String[] args) {
        try {
            //创建一个服务器端的Socket,即ServerSocket，指定绑定的端口
            ServerSocket ss = new ServerSocket(8899);

            System.out.println("服务器即将启动，等待客户端的连接...");
            //循环侦听等待客户端的连接
            while (true) {
                //调用accept方法开始监听，等待客户端的连接
                Socket so = ss.accept();
                ServerThread serverThread = new ServerThread(so);
                serverThread.start();
                serverThreads.add(serverThread);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ServerThread extends Thread {
        private Socket socket;
		//定义所需资源，输入输出流与缓存
        InputStream is = null;
        OutputStream os = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        PrintWriter pw = null;
        BufferedWriter bw = null;

        public ServerThread(Socket socket) {
            this.socket = socket;
            try {
                is = socket.getInputStream();
                os = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isr = new InputStreamReader(is);// 将字节输入流包装成字符输入流
            br = new BufferedReader(isr);// 加上缓冲流，提高效率
            pw = new PrintWriter(os);// 字符输出流
            bw = new BufferedWriter(pw);// 缓冲输
        }

        @Override
        public void run() {
            try {
                handleSocket();
            } catch (Exception e) {
                System.out.println("客户端断开连接");
                serverThreads.remove(serverThreads.indexOf(this));
            }
        }

        public void handleSocket() throws Exception {
            String[] arr;
            String info = "";
            while ((info = br.readLine()) != null) {
				//监听客户端发送的信息，并存储到arr中
                System.out.println("我是服务器，客户端说：" + info);
                arr = info.split("\\s+");
                String status = arr[0];
                String usrname = arr[1];
				//定义数据库连接
                Connection conn = DriverManager.getConnection(Conn.url, Conn.user, Conn.password);
                Statement stmt = conn.createStatement();

                System.out.println(status + " " + usrname + " ");
                if (status.equals("100")) {
					//操作码为100时，收到登录请求
                    logged.add(usrname);
                    Class.forName(Conn.driver);
                    String passwrd = arr[2];
                    System.out.println(passwrd);
                    String sql;
                    String pass = "";
					//向数据库查询设备名、密码
                    sql = "SELECT passwrd FROM client where usrname = '" + usrname + "'";
                    ResultSet rs = stmt.executeQuery(sql);
                    while (rs.next()) {
                        pass = rs.getString("passwrd");
                    }
                    System.out.println(pass);
                    //获取一个输出流，向客户端输出信息,响应客户端的请求
                    boolean pa = true;
                    if (pass.equals(passwrd)) {
						//若验证成功，返回101
                        System.out.println(pass);
                        bw.write("101");
                        sql = " UPDATE client SET status = '1' WHERE usrname = '" + usrname + "'";
                        stmt.executeUpdate(sql);
                        System.out.println("我是服务器，客户端通过认证\n");
                    } else {
						//否则返回102
                        bw.write("102");
                        System.out.println("我是服务器，客户端认证失败\n");
                        serverThreads.remove(logged.indexOf(usrname));
                        logged.remove(logged.indexOf(usrname));
                        pa = false;
                    }
                    bw.newLine();
                    bw.flush();
                    if (!pa) {
                        break;
                    }
                } else if (status.equals("300")) {
					//操作码为300时，收到资源声明请求
                    String srcname = arr[2];
                    String srcid = arr[3];
                    String ip = arr[4];
					//调用Findif判断是否有重名资源，并执行相应操作
                    String result = FindIf.findif(srcname, usrname, srcid, ip);
                    System.out.println(result);
					//返回执行结果
                    bw.write(result);
                    bw.newLine();
                    bw.flush();
                } else if (status.equals("200")) {
					//操作码为200时，收到下载资源请求
                    String srcname = arr[2];
                    String sendName = arr[3];
                    String sendIP = arr[4];
                    String receiveIP = arr[5];
                    System.out.println(srcname + " " + sendName + " " + sendIP + " " + receiveIP);
					//查看所申请资源是否可用
                    int index = logged.indexOf(sendName);
                    if (index == -1) {
						//若不可用，返回202设备不在线
                        bw.write("202");
                        bw.newLine();
                        bw.flush();
                    } else {
						//否则调用serverThread进行传输操作
                        serverThreads.get(index).getSource(receiveIP, srcname);
                    }
                } else if (status.equals("400")) {
					//操作码为400时，收到资源刷新请求
					//调用Refrresh刷新资源
                    String Adi = Refresh.refresh(usrname);
                    System.out.println(Adi);
					//返回401与操作结果
                    bw.write("401/" + Adi);
                    bw.newLine();
                    bw.flush();

                } else if (status.equals("500")) {
					//操作码为500时，收到登出请求
					//连接数据库
                    conn = DriverManager.getConnection(Conn.url, Conn.user, Conn.password);
                    stmt = conn.createStatement();
                    Class.forName(Conn.driver);
                    String sql;
					//修改设备状态为离线
                    sql = " UPDATE client SET status = '0' WHERE usrname = '" + usrname + "'";
                    stmt.executeUpdate(sql);
					//返回501
                    bw.write("501");
                    bw.newLine();
                    bw.flush();
                } else if (status.equals("600")) {
					//操作码为600时，收到注册请求
                    String password = arr[2];
                    System.out.println("请求注册设备：\n 用户名:" + usrname + "\n 密码:" + password);
                    Scanner sc = new Scanner(System.in);
                    while (true) {
						//服务端确定是否允许注册
                        System.out.println("请确认是否允许注册，（1代表允许，0代表拒绝）：");
                        String isSign = sc.nextLine();
                        if (isSign.equals("0")) {
							//若拒绝注册，返回603
                            System.out.println(isSign);
                            bw.write("603");
                            bw.newLine();
                            bw.flush();
                            break;
                        } else if (isSign.equals("1")) {
							//若允许注册，查询是否有重名用户
                            int sign = 1;
                            String sql = "SELECT passwrd from client where usrname = '"+usrname+"'";
                            PreparedStatement ps1 = conn.prepareStatement(sql);
                            ResultSet rs1 = ps1.executeQuery();
                            while (rs1.next()) {
                                String pass = rs1.getString("passwrd");
                                if (pass.equals(password)){
									//若有重名用户，返回602
                                    bw.write("602");
                                    bw.newLine();
                                    bw.flush();
                                    sign = 0;
                                }
                            }
                            if (sign==1) {
								//否则允许注册，将用户信息插入用户表
                                sql = "INSERT into client(usrname, passwrd) values(?,?)";
                                PreparedStatement ps = conn.prepareStatement(sql);
                                ps.setString(1, usrname);
                                ps.setString(2, password);
                                int rs = ps.executeUpdate();
                                if (rs == 0) {
                                    bw.write("602");
                                    bw.newLine();
                                    bw.flush();
                                } else {
                                    bw.write("601");
                                    bw.newLine();
                                    bw.flush();
                                }
                            }
                            break;
                        } else {
                            System.out.println("输入不合法，请重新输入：");
                        }
                    }
                }
            }
        }

        public void getSource(String receiveIP, String srcname) throws Exception {
            bw.write("203/" + receiveIP + "/" + srcname);
            bw.newLine();
            bw.flush();
        }
    }
}
