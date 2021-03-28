package server;

import fileassembler.FileChunk;
import fileassembler.FileMerger;
import io.netty.channel.ChannelInboundHandlerAdapter;
import user.User;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.nio.charset.Charset;

public class DataHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        try {
            ByteBuf buf = (ByteBuf) msg;
            int userId = 0;
            int size = 0;
            int position = 0;
            boolean last = false;
            short fileNameSize = 0;
            int chunkSize = Server.CHUNK_SIZE;

            if (buf.readableBytes() >= chunkSize) {
                userId = buf.readInt();
                size = buf.readInt();
                position = buf.readInt();
                last = buf.readBoolean();
                fileNameSize = buf.readShort();
                String fileName = "";
                fileName = buf.readCharSequence(fileNameSize, Charset.defaultCharset()).toString();
                byte[] body = new byte[size];
                buf.readBytes(body, 0, size);
                FileChunk fileChunk = new FileChunk(userId, size, position, last, fileName, body);
                User user = UserManagement.getUser(userId);
                if (user != null) {
                    if (fileChunk.getSize() == 0) {
                        StateMachinesPool.getStateMachine(userId).setDataChannel(channelHandlerContext.channel());
                    } else {
                        FileMerger.assemble(fileChunk,
                                StateMachinesPool.getStateMachine(userId).getCurrentDir() +
                                File.separator +
                                fileName);
                        if (fileChunk.isLast()) {
                            StateMachinesPool.getStateMachine(userId).setState(StateMachine.State.RECEIVING_COMPLETE);
                        } else {
                            StateMachinesPool.getStateMachine(userId).setPhase(StateMachine.Phase.NEXT);
                        }
                    }
                }
                buf.release();
            }
        } finally {
            //ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }




}
