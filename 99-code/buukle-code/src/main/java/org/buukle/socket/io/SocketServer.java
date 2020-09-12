/**
 * Copyright (C), 2015-2020  http:// www.buukle.top
 * FileName: SocketServer
 * Author:   elvin
 * Date:     2020/9/12 15:05
 * Description: 服务端
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package org.buukle.socket.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @description 〈服务端〉
 * @author elvin
 * @create 2020/9/12
 * @since 1.0.0
 */
public class SocketServer {


    public static void main(String[] args) throws IOException {

        int port = 18080;
        ServerSocket server = new ServerSocket(port);
        Socket socket = server.accept();
        InputStream inputStream = socket.getInputStream();

        byte[] bytes = new byte[1024];
        int len;
        StringBuilder sb = new StringBuilder();
        while ((len = inputStream.read(bytes)) != -1) {
            sb.append(new String(bytes, 0, len,"UTF-8"));
        }
        System.out.println("get message from client: " + sb);
        inputStream.close();
        socket.close();
        server.close();
    }
}