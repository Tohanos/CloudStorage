package fileassembler;

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
        RandomAccessFile file = new RandomAccessFile(filename, "r");
        byte buffer[] = new byte[size];
        long length = file.length();
        int lastSize = 0;
        boolean last = false;
        if (currentPos + size >= length) {
            last = true;
            lastSize = (int) length - currentPos;
            file.read(buffer, currentPos, lastSize);
            currentPos = -1;
        } else {
            file.read(buffer, currentPos, size);
            currentPos += size;
        }
        file.close();
        return new FileChunk(userId, size, currentPos, last, filename, buffer);
    }
}

