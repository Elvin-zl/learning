package org.buukle.io.nio.util;

import org.buukle.io.nio.NioSocketServer;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ChatThread extends Thread {

    private Selector selector;
    private SocketChannel socket;
    private String username;
    private String target;

    public ChatThread(Selector selector, SocketChannel socket, String username, String target) {
        super();
        this.selector = selector;
        this.socket = socket;
        this.username = username;
        this.target = target;
    }

    @Override
    public void run() {
        try {
            // 等待连接建立
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            String s="";
            s += this.username;
            s += NioSocketServer.SOURCE_SPLIT;
            s += this.target;
            s += NioSocketServer.TARGET_SPLIT;
            s += input;
            if(input == null || input.equals("") || input.length() == 0){
                continue;
            }
            try {
                // 用户已输入，注册写事件，将输入的消息发送给客户端
                socket.register(selector, SelectionKey.OP_WRITE, ByteBuffer.wrap(s.getBytes()));
                // 唤醒之前因为监听OP_READ而阻塞的select()
                selector.wakeup();
                System.out.println("我对 : " + this.target + " 说: " + input);
                System.out.println("=========================================");
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        }
    }
}
