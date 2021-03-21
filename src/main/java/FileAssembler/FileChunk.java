package FileAssembler;

import User.User;

import java.io.Serializable;

public class FileChunk implements Serializable {
    int userId;
    int size;
    int position;
    String filename;
    byte[] buffer;

    public FileChunk(int userId, int size, int position, String filename, byte[] buffer) {
        this.userId = userId;
        this.size = size;
        this.position = position;
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
}
