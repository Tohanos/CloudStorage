package FileAssembler;

import User.User;

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
        file.read(buffer, currentPos, size);
        currentPos += size;
        file.close();
        return new FileChunk(userId, size, currentPos, filename, buffer);
    }
}

