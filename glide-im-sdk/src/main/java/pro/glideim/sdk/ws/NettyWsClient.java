package pro.glideim.sdk.ws;

import java.net.URI;
import java.net.URISyntaxException;

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

    private NioEventLoopGroup eventExecutors;
    private Bootstrap bootstrap;
    private WsInboundChHandler channelInboundHandler;
    private Channel channel;
    private URI uri;

    public NettyWsClient(String wsUrl) {
        uri = null;
        try {
            uri = new URI(wsUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }
        channelInboundHandler = new WsInboundChHandler(uri);
    }

    public Single<Boolean> connect() {
        if (getState() != WsClient.STATE_CLOSED) {
            return Single.just(true);
        }
        channelInboundHandler.onStateChanged(WsClient.STATE_CONNECTING);
        return Single.create(emitter -> {
            ChannelFuture connect = connect2().sync();
            if (connect.isDone() && !connect.isSuccess()) {
                emitter.onError(connect.cause());
                return;
            }
            ChannelPromise sync = channelInboundHandler.handshakeFuture.sync();
            if (sync.isSuccess()) {
                this.channel = connect.channel();
                emitter.onSuccess(true);
                channelInboundHandler.onStateChanged(WsClient.STATE_OPENED);
            } else {
                channelInboundHandler.onStateChanged(WsClient.STATE_CLOSED);
                emitter.onError(sync.cause());
            }
        });
    }

    private ChannelFuture connect2() {
        eventExecutors = new NioEventLoopGroup();
        bootstrap = new Bootstrap();

        if (channelInboundHandler != null) {
            channelInboundHandler = channelInboundHandler.copy();
        }

        bootstrap.option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(this);
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
        this.channelInboundHandler.connStateListener.add(listener);
    }

    @Override
    public void removeStateListener(ConnStateListener listener) {
        this.channelInboundHandler.connStateListener.remove(listener);
    }

    @Override
    public void setMessageListener(MessageListener listener) {
        this.channelInboundHandler.messageListener = listener;
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
