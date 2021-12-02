package pro.glideim.sdk.socket;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

public class Netty {

    private static Bootstrap bootstrap;

    public static void init() {
        bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                        ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                        ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                        ch.pipeline().addLast(new HeartbeatHandler());
                    }
                });
    }

    public static void connect() {
        ChannelFuture channelFuture = bootstrap.connect("", 8080)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        Channel channel = future.channel();

                    } else {

                    }
                });
        try {
            channelFuture.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
