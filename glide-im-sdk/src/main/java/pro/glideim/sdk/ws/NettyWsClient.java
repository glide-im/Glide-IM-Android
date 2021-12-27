package pro.glideim.sdk.ws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.reactivex.Single;
import pro.glideim.sdk.http.RetrofitManager;
import pro.glideim.sdk.im.ConnStateListener;

public class NettyWsClient extends ChannelInitializer<NioSocketChannel> implements WsClient {

    private static Bootstrap bootstrap;
    private final NioEventLoopGroup eventExecutors;
    private WsInboundChHandler channelInboundHandler;
    private Channel channel;
    private final List<ConnStateListener> connStateListener = new ArrayList<>();

    private MessageListener messageListener;

    public NettyWsClient() {
        eventExecutors = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(this);
    }

    public Single<Boolean> connect(String url) {
        return Single.create(emitter -> {
            ChannelFuture connect = connect2(url).sync();
            if (connect.isDone() && !connect.isSuccess()) {
                emitter.onError(connect.cause());
                return;
            }
            ChannelPromise sync = channelInboundHandler.handshakeFuture.sync();
            if (sync.isSuccess()) {
                this.channel = connect.channel();
                emitter.onSuccess(true);
            } else {
                emitter.onError(sync.cause());
            }
        });
    }

    public ChannelFuture connect2(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        channelInboundHandler = new WsInboundChHandler(uri);
        channelInboundHandler.connStateListener = connStateListener;
        channelInboundHandler.messageListener = messageListener;
        return bootstrap.connect(uri.getHost(), uri.getPort());
    }

    @Override
    public void disconnect() {
        if (channel.isOpen()) {
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            eventExecutors.shutdownGracefully();
        }
    }

    @Override
    public int getState() {
        return channelInboundHandler.connectionState;
    }

    @Override
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    @Override
    public boolean write(Object msg) {
        if (!channel.isActive()) {
            return false;
        }
        String s = RetrofitManager.toJson(msg);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(s);
        ChannelFuture channelFuture = null;
        try {
            channelFuture = channel.writeAndFlush(textWebSocketFrame).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return channelFuture.isSuccess();
    }

    @Override
    public void addStateListener(ConnStateListener listener) {
        this.connStateListener.add(listener);
        channelInboundHandler.connStateListener = connStateListener;
    }



    @Override
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {

//                        ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
//                        ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
        ch.pipeline().addLast(new HttpClientCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024 * 10));
        ch.pipeline().addLast(WebSocketClientCompressionHandler.INSTANCE);
//                        ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
//                        ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
        ch.pipeline().addLast(channelInboundHandler);
    }
}
