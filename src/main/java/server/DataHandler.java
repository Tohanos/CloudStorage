package server;

import fileassembler.FileChunk;
import fileassembler.FileMerger;
import io.netty.channel.ChannelInboundHandlerAdapter;
import user.User;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

public class DataHandler extends ChannelInboundHandlerAdapter {

    private ByteBuf buf;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buf = ctx.alloc().buffer(Server.CHUNK_SIZE);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        buf.release();
        buf = null;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf m = (ByteBuf) msg;
        buf.writeBytes(m);
        m.release();

        if (buf.readableBytes() >= Server.CHUNK_SIZE) {

            try {
                byte[] header = new byte[2];
                int userId = 0;
                int size = 0;
                int position = 0;
                boolean last = false;
                short fileNameSize = 0;
                int chunkSize = Server.CHUNK_SIZE;

                header[0] = buf.readByte();
                header[1] = buf.readByte();
                if (header[0] == 67 && header[1] == 72) {
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
                            StateMachinesPool.getStateMachine(userId).setDataChannel(ctx.channel());
                            buf.clear();
                        } else {
                            FileMerger.assemble(fileChunk, StateMachinesPool.getStateMachine(userId).getCurrentDir());
                            if (fileChunk.isLast()) {
                                StateMachinesPool.getStateMachine(userId).setState(StateMachine.State.RECEIVING_COMPLETE);
                                buf.clear();
                            } else {
                                buf.clear();
                            }
                        }
                    }
                } else {
                    //StateMachinesPool.getStateMachine(userId).setState(StateMachine.State.RECEIVING_ERROR);
                    buf.clear();
                }

            } finally {

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
