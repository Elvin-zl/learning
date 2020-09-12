package org.buukle.socket.nio;

import org.buukle.socket.nio.util.ClientUtil;

public class NioSocketClient1 {

    public static String USERNAME = "736060369@qq.com";
    public static String TARGET = "715426640@qq.com";

    public static void main(String[] args) {
        ClientUtil.execute(USERNAME, TARGET);
    }
}
