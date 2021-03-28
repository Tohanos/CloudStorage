package fileassembler;

import user.User;
import server.UserManagement;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileMerger {

    public static void assemble(FileChunk chunk, String rootDir) throws IOException {
        User user = UserManagement.getUser(chunk.getUserId());
        String path = rootDir + File.separator + chunk.getFilename();
        RandomAccessFile file = new RandomAccessFile(path, "rw");
        file.write(chunk.getBuffer(), chunk.getPosition(), chunk.getSize());
        file.close();
    }
}
