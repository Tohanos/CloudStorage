package server;

import command.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class CommandHandler extends ChannelInboundHandlerAdapter {

    private StateMachine serverState;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        serverState = new StateMachine();
        StateMachinesPool.add(serverState);
        serverState.setCommandChannel(ctx.channel());

        serverState.setPhase(StateMachine.Phase.CONNECT);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client " + ctx.channel().remoteAddress().toString() + " disconnected");
        serverState.setPhase(StateMachine.Phase.DISCONNECT);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        List<String> answer;

        System.out.println(msg.getClass().getName());
        if (msg instanceof Command) {
            answer = serverState.parseCommand(((Command) msg).getCommand());
            ctx.writeAndFlush(new Command(answer));
        } else {
            System.out.println("Not a command message");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
