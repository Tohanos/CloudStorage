package server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        ByteBuf in = (ByteBuf) msg;
        List<String> command = new ArrayList<>();
        try {
            while (in.isReadable()) {
                StringBuilder sb = new StringBuilder(in.readCharSequence(in.readableBytes(), Charset.defaultCharset()));
                System.out.println(sb);
                command = Arrays.asList(sb.toString().replaceAll("[^A-Za-z0-9]", "").split(" ").clone());
                if (command != null) {
                    if ("auth".equals(command.get(0))) {



                    }
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
