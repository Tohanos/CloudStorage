package fileassembler;

import java.io.Serializable;

public class FileChunk implements Serializable {
    char[] header = {67, 72}; //CH
    int userId;
    int size;
    int position;
    boolean last;
    String filename;
    byte[] buffer;

    public FileChunk(int userId, int size, int position, boolean last, String filename, byte[] buffer) {
        this.userId = userId;
        this.size = size;
        this.position = position;
        this.last = last;
        this.filename = filename;
        this.buffer = buffer;
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
