package server;

import command.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.ObjectOutputStream;
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

//        Command cmd = new Command("OK");
//        ByteBuf out = ctx.alloc().buffer(51);
//        ByteBufOutputStream bbos = new ByteBufOutputStream(out);
//        ObjectOutputStream oos = new ObjectOutputStream(bbos);
//        oos.writeObject(cmd);
//        oos.flush();
//        ChannelFuture f = ctx.writeAndFlush(out);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client " + ctx.channel().remoteAddress().toString() + " disconnected");
        serverState.setPhase(StateMachine.Phase.DISCONNECT);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        List<String> answer;

        ByteBuf buf = (ByteBuf) msg;

        Command command = new Command(buf.readCharSequence(buf.readableBytes(), Charset.defaultCharset()).toString().replaceAll("[^0-9a-zA-Z ]+", ""));

        System.out.println("Incoming command " + command.getCommand().toString());

        answer = serverState.parseCommand(command.getCommand());

        if (answer != null) {

            buf.writeCharSequence(answer.toString(), Charset.defaultCharset());

            ctx.writeAndFlush(buf);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
