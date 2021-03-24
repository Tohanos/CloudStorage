package server;

import fileassembler.FileChunk;
import fileassembler.FileMerger;
import user.User;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.ObjectOutputStream;

public class DataHandler extends SimpleChannelInboundHandler<FileChunk> {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        FileChunk chunk = new FileChunk(0, 2, 0, true,"", "AA".getBytes());
        ByteBuf out = ctx.alloc().buffer(51);
        ByteBufOutputStream bbos = new ByteBufOutputStream(out);
        ObjectOutputStream oos = new ObjectOutputStream(bbos);
        oos.writeObject(chunk);
        oos.flush();
        ChannelFuture f = ctx.writeAndFlush(out);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FileChunk fileChunk) throws Exception {
        User user = UserManagement.getUser(fileChunk.getUserId());
        if (user != null) {
            if (fileChunk.getSize() == 0) {
                StateMachinesPool.getStateMachine(user).setDataChannel(channelHandlerContext.channel());
            }
            FileMerger.assemble(fileChunk);
            if (fileChunk.isLast()) {
                StateMachinesPool.getStateMachine(user).setPhase(StateMachine.Phase.DONE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }




}
