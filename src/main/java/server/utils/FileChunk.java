package server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.io.Serializable;

public class FileChunk implements Serializable {
    char[] header = {67, 72}; //CH
    int userId;         //ИД пользователя
    int size;           //размер отрезка
    int position;       //стартовая позиция отрезка в файле
    boolean last;       //флаг последнего отрезка в файле
    String filename;    //имя файла
    byte[] buffer;      //сам отрезок

    public FileChunk(int userId, int size, int position, boolean last, String filename, byte[] buffer) {
        this.userId = userId;
        this.size = size;
        this.position = position;
        this.last = last;
        this.filename = filename;
        this.buffer = buffer;
    }

    /***
     * Отправка отрезка по @datachannel
     * @param dataChannel - канал передачи отрезка
     * @param chunkSize - полный размер отрезка включая заголовок
     */
    public void sendFileChunk (Channel dataChannel, int chunkSize) {
        ByteBuf buf = dataChannel.alloc().buffer(chunkSize);
        byte[] bytes = {67, 72};    //идентификатор
        buf.writeBytes(bytes);
        buf.writeInt(getUserId());
        buf.writeInt(getSize());
        buf.writeInt(getPosition());
        buf.writeBoolean(isLast());
        buf.writeShort((short)getFilename().length());
        buf.writeBytes(getFilename().getBytes());
        buf.writeBytes(getBuffer());
        dataChannel.writeAndFlush(buf);
    }

    public int getUserId() {
        return userId;
    }

    public int getSize() {
        return size;
    }

    public int getPosition() {
        return position;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public boolean isLast() {
        return last;
    }
}
