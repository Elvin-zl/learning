package org.buukle.socket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class NioSocketServer {

    public static String SOURCE_SPLIT = "@!SOURCE_SPLIT!@";
    public static String TARGET_SPLIT = "@!TARGET_SPLIT!@";
    public static String SERVER_TARGET = "SERVER@SERVER.COM";

    public static void main(String[] args) {

        // 会话中心 map
        ConcurrentHashMap<String ,SocketChannel> clientMap = new ConcurrentHashMap<>(8);
        // 阻塞消息 map
        ConcurrentHashMap<String , ArrayList<String>> blockMessageMap = new ConcurrentHashMap<>(8);
        try {
            // 服务初始化
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            // 设置为非阻塞
            serverSocket.configureBlocking(false);
            // 绑定端口
            serverSocket.bind(new InetSocketAddress("localhost", 9999));
            // 注册OP_ACCEPT事件（即监听该事件，如果有客户端发来连接请求，则该键在select()后被选中）
            Selector selector = Selector.open();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            Calendar ca = Calendar.getInstance();
            System.out.println("服务端开启了");
            System.out.println("=========================================================");
            // 轮询服务
            while (true) {
                // 选择准备好的事件
                selector.select();
                // 已选择的键集
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                // 处理已选择键集事件
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    // 处理掉后将键移除，避免重复消费(因为下次选择后，还在已选择键集中)
                    it.remove();
                    // 处理连接请求
                    if (key.isAcceptable()) {
                        // 处理请求
                        SocketChannel socket = serverSocket.accept();
                        socket.configureBlocking(false);
                        // 注册read，监听客户端发送的消息
                        socket.register(selector, SelectionKey.OP_READ);
                        // keys为所有键，除掉 serverSocket 注册的键就是已连接socketChannel的数量
                        String message = "登陆成功... ....";
                        // 向客户端发送消息
                        socket.write(ByteBuffer.wrap(message.getBytes()));
                        InetSocketAddress address = (InetSocketAddress) socket.getRemoteAddress();

                        // 输出客户端地址
                        System.out.println(ca.getTime() + "\t" + address.getHostString() + ":" + address.getPort() + "\t");
                        System.out.println("客戶端已连接");
                        System.out.println("=========================================================");
                    }
               
                    if (key.isReadable()) {
                        // 源 socket
                        SocketChannel resourceSocket = (SocketChannel) key.channel();
                        InetSocketAddress address = (InetSocketAddress) resourceSocket.getRemoteAddress();
                        ByteBuffer bf = ByteBuffer.allocate(1024 * 4);
                        int len = 0;
                        byte[] res = new byte[1024 * 4];
                        try {
                            String message = "";
                            while ((len = resourceSocket.read(bf)) != 0) {
                                bf.flip();
                                bf.get(res, 0, len);
                                message += new String(res, 0, len);
                                bf.clear();
                            }
                            String[] allMessage = message.split(SOURCE_SPLIT);
                            String resource = allMessage[0];
                            clientMap.put(resource,resourceSocket);
                            System.out.println("\t" + ca.getTime() + "\t" + address.getHostString() + ":" + address.getPort() + "\t");

                            String log = "来源 : " + allMessage[0] + '\t';
                            String targetAndMessage = allMessage[1];
                            String[] splitTarget = targetAndMessage.split(TARGET_SPLIT);
                            String target = splitTarget[0];
                            // 上线消息
                            if(target.equals(SERVER_TARGET)){
                                // 处理客户端积压的离线消息
                                ArrayList<String> strings = blockMessageMap.get(resource);
                                Iterator<String> iterator = strings.iterator();
                                // 遍历发送积压消息,移出本地缓存
                                while (iterator.hasNext()){
                                    String msg = iterator.next();
                                    resourceSocket.write(ByteBuffer.wrap(msg.getBytes()));
                                    resourceSocket.write(ByteBuffer.wrap("\t".getBytes()));
                                    iterator.remove();
                                }
                            }
                            log += "目标 : " + target + "\t";
                            log += "信息 : " + (splitTarget.length > 1 ? splitTarget[1] : "空!");
                            System.out.println(log);
                            // 目标 socket
                            SocketChannel targetSocket = clientMap.get(target);

                            // 该用户不在线
                            if(targetSocket == null ){
                                // 缓存该消息
                                if(splitTarget.length > 1){
                                    cacheMessage(blockMessageMap,target,message);
                                    resourceSocket.write(ByteBuffer.wrap("用户已经离线,下次上线时会收到该消息!".getBytes()));
                                }else{
                                    resourceSocket.write(ByteBuffer.wrap("系统不会发送空白消息!".getBytes()));
                                }
                            }
                            // 用户下线了
                            else if(targetSocket != null && splitTarget.length > 1){
                                try{
                                    targetSocket.write(ByteBuffer.wrap(message.getBytes()));
                                }catch (Exception e){
                                    // 缓存该消息
                                    cacheMessage(blockMessageMap,target,message);
                                    resourceSocket.write(ByteBuffer.wrap("用户已经离线,下次上线时会收到该消息!".getBytes()));
                                }
                            }
                            System.out.println("=========================================================");
                        } catch (IOException e) {
                            // 客户端关闭了
                            resourceSocket.close();
                            System.out.println("当前客戶端失去连接!");
                            System.out.println("=========================================================");
                        } catch (Exception e){
                            System.out.println("处理客戶端数据时出现异常!");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("服务器异常!");
            System.out.println("=========================================================");
        }
    }

    private static void cacheMessage(ConcurrentHashMap<String, ArrayList<String>> blockMessageMap, String target, String message) {
        ArrayList<String> msgList = blockMessageMap.get(target);
        if(msgList == null){
            msgList = new ArrayList<>();
            blockMessageMap.put(target,msgList);
        }
        msgList.add(message);
    }
}
