package server.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileSplitter {
    private String filename;
    private int size;
    private int currentPos;
    private int userId;

    public FileSplitter(String filename, int size, int userId) {
        this.filename = filename;
        this.size = size;
        this.userId = userId;
        currentPos = 0;
    }

    public FileChunk getNext() throws IOException {
        RandomAccessFile file = null;
        boolean last = false;
        byte[] buffer = new byte[size];
        try {
            file = new RandomAccessFile(filename, "r");
            long length = file.length();
            file.seek(currentPos);
            if (currentPos + size >= length) {
                last = true;
                size = (int) length - currentPos;
                file.read(buffer, 0, size);
            } else {
                file.read(buffer, 0, size);
            }
            file.close();
        } catch (FileNotFoundException e) {
            return null;
        }
        FileChunk chunk = new FileChunk(userId, size, currentPos, last, filename, buffer);
        currentPos += size;
        return chunk;
    }
}

