/**
 * @Author elvin
 * @Date Created by elvin on 2020/9/12.
 * @Description :
 */
package org.buukle.io;

/*
 * BIO :
 *
 *      每一次客户端连接,都会在linux内核 指定区域创建一个文件描述符,并指向一个 "文件"
 *
 *      每个文件描述符(对应一个客户端连接 ,socket) 一旦开始被线程处理,便必须等该连接释放线程才能切换(否则中断后,数据丢失了)
 *
 *      在java中,每接到一个连接,便copy主线程(java进程) 一份作为子线程 去处理客户端的连接来解决阻塞的问题,这使 java web 服务端能够以多线程的形式处理多个客户端的连接;
 *
 * NIO : (通过对连接的不同阶段添加状态位 , 将连接的处理进行分解,并允许同一线程断点接续处理,避免每个连接都需要新线程去处理, 多连接的情况下,线程切换成本较高)
 *
 *      问题 : 每个文件描述符被访问的时候,线程需要能够切换,回来还能继续执行;
 *
 *      方案 : 通过添加并维护客户端连接 (文件描述符) 的状态位 ,使同一线程在处理不同连接时,能够根据上次切换前的处理进度继续处理;
 *
 *          状态位
 *
 *              服务端接收客户端连接事件	        SelectionKey.OP_ACCEPT(16)
 *              客户端连接服务端事件	            SelectionKey.OP_CONNECT(8)
 *              读事件	                        SelectionKey.OP_READ(1)
 *              写事件	                        SelectionKey.OP_WRITE(4)
 *
 * 优化 :(为状态为发生变化的文件描述符 开辟一块新的区域[epoll存储区域,本质上是链表], 主线程只需要关注这块空间即可,借助硬件事件驱动连接的文件描述符进入这块区域中)
 *
 *      selector(epoll)
 *
 *      问题 : 大量不活跃连接的情况下,一个线程遍历所有的连接(文件描述符)会造成很多无效的检查
 *
 *      方案 : 为了避免这种情况,专门开辟一块区域[epoll存储区域],用于存储活跃的(即文件描述符状态位发生变化的)连接, 主线程只需要关注这块空间,真循环中每次select [epoll存储区域]中的文件描述符就好了.
 *
 *      关键 : 怎么把发生变化的文件描述符放进[epoll存储区域]去呢?
 *
 *      最终方案 : 不同的连接(文件描述符) 数据通过网卡到达之后,会将数据缓存到网卡的内存中,同时产生一个内核硬中断,内核线程会产生一个中断事件,通过中断事件将这个文件描述符放到[epoll存储区域]
 *
 *      |那么，这个准备就绪list链表是怎么维护的呢？当我们执行epoll_ctl时，除了把socket放到epoll文件系统里file对象对应的红黑树上之外，还会给内核中断处理程序注册一个回调函数，
 *      |告诉内核，如果这个句柄的中断到了，就把它放到准备就绪list链表里。所以，当一个socket上有数据到了，内核在把网卡上的数据copy到内核中后就来把socket插入到准备就绪链表里了。
 *      |作者：舒小贱
 *      |链接：https://www.jianshu.com/p/4d8568c0ef0c
 *
 * selector 两种工作方式
 *
                selector.selectNow(); // selector 是非阻塞的,可以看成是子线程在select一直在收集变化的文件描述符 , 主线程可以干点别的事情,例如 redis
                selector.select();    // selector 是阻塞的,只有连接变化才往下执行,可以看成是主线程在收集变化的文件描述符 ,例如 nginx
 *
 *
 *
 */