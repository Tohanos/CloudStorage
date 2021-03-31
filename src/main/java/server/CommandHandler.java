package server;

import command.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class CommandHandler extends ChannelInboundHandlerAdapter {

    private StateMachine serverState;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        serverState = new StateMachine();
        StateMachinesPool.add(serverState);
        serverState.setCommandChannel(ctx.channel());
        serverState.setPhase(StateMachine.Phase.CONNECT);

        System.out.println("Client connected: " + ctx.channel().remoteAddress());

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client " + ctx.channel().remoteAddress().toString() + " disconnected");
        serverState.setPhase(StateMachine.Phase.DISCONNECT);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        List<String> answer;

        ByteBuf buf = (ByteBuf) msg;

        Command command = new Command(buf.readCharSequence(buf.readableBytes(), Charset.defaultCharset()).toString().replaceAll("[^0-9a-zA-Z._ ]+", ""));

        System.out.println("Incoming command " + command.getCommand().toString());

        serverState.setPhase(StateMachine.Phase.INCOMING_COMMAND);

        answer = serverState.parseCommand(command.getCommand());

        if (answer != null) {
            CharSequence cs = answer.toString().replaceAll("[\\[\\],]+", "");
            buf.writeCharSequence(answer.toString().replaceAll("[\\[\\],]+", ""),
                    Charset.defaultCharset());
            ChannelFuture f = ctx.writeAndFlush(msg);
        } else {
            buf.release();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
