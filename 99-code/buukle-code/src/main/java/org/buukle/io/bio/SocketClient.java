/**
 * Copyright (C), 2015-2020  http:// www.buukle.top
 * FileName: SocketClient
 * Author:   elvin
 * Date:     2020/9/12 15:05
 * Description: 客户端
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package org.buukle.io.bio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @description 〈客户端〉
 * @author elvin
 * @create 2020/9/12
 * @since 1.0.0
 */
public class SocketClient {

    public static void main(String[] args) throws IOException {
        // 要连接的服务端IP地址和端口
        String host = "127.0.0.1";
        int port = 18080;
        // 与服务端建立连接
        Socket socket = new Socket(host, port);
        // 建立连接后获得输出流
        OutputStream outputStream = socket.getOutputStream();
        String message="你好!";
        socket.getOutputStream().write(message.getBytes("UTF-8"));
        outputStream.close();
        socket.close();
    }

}