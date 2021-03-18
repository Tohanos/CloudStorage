package server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler extends ChannelInboundHandlerAdapter {

    private StateMachine serverState;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        serverState = new StateMachine(ctx.channel());

        serverState.setPhase(StateMachine.Phase.CONNECT);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        ByteBuf in = (ByteBuf) msg;
        List<String> command;
        List<String> answer;

        //System.out.println(port);

        try {
            while (in.isReadable()) {
                StringBuilder sb = new StringBuilder(in.readCharSequence(in.readableBytes(), Charset.defaultCharset()));
                System.out.println(sb);
                command = Arrays.asList(sb.toString().replace("\r\n", "").split(" ").clone());
                if (command.get(0).length() > 0) {
                    answer = serverState.parseCommand(command, ctx.channel());
                    ByteBuf out = ctx.alloc().buffer(51);
                    for (String s : answer) {
                        out.writeCharSequence(s.subSequence(0, s.length()), Charset.defaultCharset());
                        out.writeChar(' ');
                    }
                    ChannelFuture f = ctx.writeAndFlush(out);
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
