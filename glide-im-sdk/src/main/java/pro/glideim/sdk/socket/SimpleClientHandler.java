package pro.glideim.sdk.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.time.LocalDateTime;
public class SimpleClientHandler extends SimpleChannelInboundHandler<Object> {

    private Channel outboundChannel;
    private final ChannelHandlerContext channel;
    private static final ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;


    public SimpleClientHandler(ChannelHandlerContext channel, WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
        this.channel = channel;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("handlerAdded");
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("channelActive");
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("WebSocket Client connect failed!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead0");
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                System.out.println("WebSocket Client connected!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                System.out.println("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);

            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.getStatus()
                    + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            // resposnse(ctx, frame);

            channel.writeAndFlush(textFrame.text());
            System.out.println("WebSocket Client received message: " + textFrame.text());
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        }
    }

    private void response(ChannelHandlerContext ctx, final WebSocketFrame msg) {
        String content = msg.toString();
        clients.writeAndFlush(new TextWebSocketFrame("[msg]" + LocalDateTime.now() + "text:" + content));

        final Channel inboundChannel = ctx.channel();

        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass());
//                .handler(new SocketHandlerInitializer(inboundChannel));

        ChannelFuture f = b.connect("127.0.0.1", 5688);
        outboundChannel = f.channel();
        msg.retain();

        ChannelFuture channelFuture = f.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("isSuccess:true");
                    outboundChannel.writeAndFlush("2222222222");
                } else {
                    System.out.println("isSuccess：false");
                    inboundChannel.close();
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

}