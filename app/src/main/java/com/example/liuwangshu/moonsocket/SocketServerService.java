package com.example.liuwangshu.moonsocket;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServerService extends Service {
    private boolean isServiceDestroyed = false;

    @Override
    public void onCreate() {
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class TcpServer implements Runnable {
        @Override
        public void run() {
            ServerSocket serverSocket;
            try {
                //监听8688端口
                serverSocket = new ServerSocket(8688);
            } catch (IOException e) {

                return;
            }
            while (!isServiceDestroyed) {
                try {
                    // 接受客户端请求，并且阻塞直到接收到消息
                    final Socket client = serverSocket.accept();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void responseClient(Socket client) throws IOException {
        // 用于接收客户端消息
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        // 用于向客户端发送消息
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
        out.println("您好，我是服务端");
        while (!isServiceDestroyed) {
            String str = in.readLine();
            Log.i("moon", "收到客户端发来的信息" + str);
            if (TextUtils.isEmpty(str)) {
                //客户端断开了连接
                Log.i("moon", "客户端断开连接");
                break;
            }
            String message = "收到了客户端的信息为：" + str;
            // 从客户端收到的消息加工再发送给客户端
            out.println(message);
        }
        out.close();
        in.close();
        client.close();
    }

    @Override
    public void onDestroy() {
        isServiceDestroyed = true;
        super.onDestroy();
    }
}
