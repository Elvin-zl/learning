package org.buukle.io.nio;

import org.buukle.io.nio.util.ClientUtil;

public class NioSocketClient2 {

    public static String USERNAME  = "715426640@qq.com";
    public static String TARGET  = "736060369@qq.com";

    public static void main(String[] args) {
        ClientUtil.execute(USERNAME, TARGET);
    }
}
