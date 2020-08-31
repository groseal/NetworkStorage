
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class Network {
    private static Network ourInstance = new Network();

    public static Network getInstance() {
        return ourInstance;
    }

    private Network() {
    }

    private Channel currentChannel;//канал передачи

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public void start(CountDownLatch countDownLatch) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();//настройка клиента
            clientBootstrap.group(group)//ссылка на пул потоков
                    .channel(NioSocketChannel.class)//работа через NioSocketChannel
                    .remoteAddress(new InetSocketAddress("localhost", 8189))//адрес соединения
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast();
                            currentChannel = socketChannel;//ссылка на socketChannel
                        }
                    });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();//создается подключение
            countDownLatch.countDown();//подключение открывается
            channelFuture.channel().closeFuture().sync();//ожидание завершения подключения
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();//завершает пул потоков
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        currentChannel.close();
    }//метод остановки сети
}