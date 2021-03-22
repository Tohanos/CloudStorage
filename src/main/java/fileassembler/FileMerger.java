package fileassembler;

import user.User;
import server.UserManagement;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileMerger {

    public static void assemble(FileChunk chunk) throws IOException {
        User user = UserManagement.getUser(chunk.getUserId());
        RandomAccessFile file = new RandomAccessFile(user.getRootDir() + chunk.getFilename(), "rw");
        file.write(chunk.getBuffer(), chunk.getPosition(), chunk.getSize());
        file.close();
    }




}
