package server;

import user.UserManagement;
import server.utils.FileChunk;
import server.utils.FileMerger;
import io.netty.channel.ChannelInboundHandlerAdapter;
import user.User;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.Charset;

public class DataHandler extends ChannelInboundHandlerAdapter {

    private ByteBuf buf;            //буфер для сбора прилетевших байтов

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buf = ctx.alloc().buffer(Server.CHUNK_SIZE);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        buf.release();      //релизим буфер при выходе для СМ
        buf = null;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf m = (ByteBuf) msg;      //Собственно ByteBuf
        buf.writeBytes(m);              //копируем (этот код - кусок из туториала)
        m.release();                    //сбрасываем буфер обработчика

        if (buf.readableBytes() >= Server.CHUNK_SIZE) { //если накопилось байтов размером с отрезок + служебная информация

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
                                StateMachinesPool.getStateMachine(userId).setState(StateMachine.State.RECEIVING_NEXT);
                                buf.clear();
                            }
                        }
                    }
                } else {
                    //StateMachinesPool.getStateMachine(userId).setState(StateMachine.State.RECEIVING_ERROR);
                    buf.clear();    //чистим буфер
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
