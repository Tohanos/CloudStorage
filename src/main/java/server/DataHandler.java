package server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.nio.charset.Charset;

public class DataHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        ByteBuf in = (ByteBuf) msg;
        String command = "";
        try {
            while (in.isReadable()) {
                StringBuilder sb = new StringBuilder(in.readCharSequence(in.readableBytes(), Charset.defaultCharset()));
                System.out.println(sb);
                command = sb.toString().replaceAll("[^A-Za-z0-9]", "");
                String chk = "exit";
                if ("exit".equals(command)) {
                    return;
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
