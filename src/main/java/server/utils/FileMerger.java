package server.utils;

import user.User;
import user.UserManagement;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileMerger {

    public static void assemble(FileChunk chunk, String rootDir) throws IOException {
        User user = UserManagement.getUser(chunk.getUserId());
        String path = rootDir + File.separator + chunk.getFilename();
        RandomAccessFile file = new RandomAccessFile(path, "rw");
        file.seek(chunk.getPosition());
        file.write(chunk.getBuffer(), 0, chunk.getSize());
        file.close();
    }
}
