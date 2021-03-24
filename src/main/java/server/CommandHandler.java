package server;

import command.Command;
import fileassembler.FileChunk;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.ObjectOutputStream;
import java.util.List;

public class CommandHandler extends SimpleChannelInboundHandler<Command> {

    private StateMachine serverState;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        serverState = new StateMachine();
        StateMachinesPool.add(serverState);
        serverState.setCommandChannel(ctx.channel());

        serverState.setPhase(StateMachine.Phase.CONNECT);

        Command cmd = new Command("OK");
        ByteBuf out = ctx.alloc().buffer(51);
        ByteBufOutputStream bbos = new ByteBufOutputStream(out);
        ObjectOutputStream oos = new ObjectOutputStream(bbos);
        oos.writeObject(cmd);
        oos.flush();
        ChannelFuture f = ctx.writeAndFlush(out);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client " + ctx.channel().remoteAddress().toString() + " disconnected");
        serverState.setPhase(StateMachine.Phase.DISCONNECT);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) {
        List<String> answer;

        answer = serverState.parseCommand(command.getCommand());
        ctx.writeAndFlush(new Command(answer));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
