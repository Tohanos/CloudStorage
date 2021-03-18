package server;

public class FileChunk {
    User user;
    int size;
    long position;
    String filename;
    Byte[] buffer;

    public FileChunk(User user, int size, long position, String filename, Byte[] buffer) {
        this.user = user;
        this.size = size;
        this.position = position;
        this.filename = filename;
        this.buffer = buffer;
    }

    public User getUser() {
        return user;
    }

    public int getSize() {
        return size;
    }

    public long getPosition() {
        return position;
    }

    public String getFilename() {
        return filename;
    }

    public Byte[] getBuffer() {
        return buffer;
    }
}
