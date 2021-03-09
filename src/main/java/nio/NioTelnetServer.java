package nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class NioTelnetServer {
    private final ByteBuffer buffer = ByteBuffer.allocate(512);

    public static final String LS_COMMAND = "\tls          view all files from current directory\r\n";
    public static final String MKDIR_COMMAND = "\tmkdir       view all files from current directory\r\n";
    public static final String TOUCH_COMMAND = "\ttouch          create new file\r\n";
    public static final String CD_COMMAND = "\tcd       change directory\r\n";
    public static final String RM_COMMAND = "\trm          remove directory/file\r\n";
    public static final String COPY_COMMAND = "\tcopy       copy files\r\n";
    public static final String CAT_COMMAND = "\tcat       view file content\r\n";

    private ArrayList<String> currentPath;

    public NioTelnetServer() throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open(); // открыли
        server.bind(new InetSocketAddress(1234));
        server.configureBlocking(false); // ВАЖНО
        Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started");
        currentPath = new ArrayList<>();
        currentPath.add("server");
        while (server.isOpen()) {
            selector.select();
            var selectionKeys = selector.selectedKeys();
            var iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                var key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key, selector);
                } else if (key.isReadable()) {
                    handleRead(key, selector);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        int readBytes = channel.read(buffer);
        if (readBytes < 0) {
            channel.close();
            return;
        } else if (readBytes == 0) {
            return;
        }

        buffer.flip();
        StringBuilder sb = new StringBuilder();
        while (buffer.hasRemaining()) {
            sb.append((char) buffer.get());
        }
        buffer.clear();

        if (key.isValid()) {
            String command = sb.toString()
                    .replace("\n", "")
                    .replace("\r", "");
            String args[] = command.split(" ");
            if ("--help".equals(command)) {
                sendMessage(LS_COMMAND, selector);
                sendMessage(MKDIR_COMMAND, selector);
                sendMessage(TOUCH_COMMAND, selector);
                sendMessage(CD_COMMAND, selector);
                sendMessage(RM_COMMAND, selector);
                sendMessage(COPY_COMMAND, selector);
                sendMessage(CAT_COMMAND, selector);
            } else if ("ls".equals(command)) {
                sendMessage(getFilesList().concat("\r\n"), selector);
            } else if ("exit".equals(command)) {
                System.out.println("Client logged out. IP: " + channel.getRemoteAddress());
                channel.close();
                return;
            } else if ("touch".equals(args[0])) {
                if (args.length > 1) {
                    if (!createFile(args[1])) {
                        System.out.println("Cannot create file " + args[1]);
                        sendMessage("Cannot create file " + args[1] + "\r\n", selector);
                    }
                }
            } else if ("mkdir".equals(args[0])) {
                if (args.length > 1) {
                    if (!createDir(args[1])) {
                        System.out.println("Cannot create directory " + args[1]);
                        sendMessage("Cannot create directory " + args[1] + "\r\n", selector);
                    }
                }
            } else if ("cd".equals(args[0])) {
                if (args.length > 1) {
                    if (!changeDir(args[1])) {
                        System.out.println("Cannot change directory to " + args[1]);
                        sendMessage("Cannot change directory to " + args[1] + "\r\n", selector);
                    }
                }
            } else if ("rm".equals(args[0])) {
                if (args.length > 1) {
                    if (!removeFile(args[1])) {
                        System.out.println("Cannot remove file or directory " + args[1]);
                        sendMessage("Cannot remove file or directory " + args[1] + "\r\n", selector);
                    }
                }
            } else if ("copy".equals(args[0])) {
                if (args.length > 2) {
                    if (!copyFile(args[1], args[2])) {
                        System.out.println("Cannot copy file from " + args[1] + " to " + args[2]);
                        sendMessage("Cannot copy file from " + args[1] + " to " + args[2] + "\r\n", selector);
                    }
                }
            } else if ("cat".equals(args[0])) {
                if (args.length > 1) {
                    ArrayList<String> res = catFile(args[1]);
                    if (res != null) {
                        for (String s : res) {
                            sendMessage(s + "\r\n", selector);
                        }
                    } else {
                        System.out.println("Cannot show contents from " + args[1]);
                        sendMessage("Cannot show contents from " + args[1] + "\r\n", selector);
                    }
                }
            }
        }
        sendName(channel);
    }

    private void sendName(SocketChannel channel) throws IOException {
        channel.write(
                ByteBuffer.wrap(channel
                        .getRemoteAddress().toString()
                        .concat(" ")
                        .concat(getCurrentDir())
                        .concat(">: ")
                        .getBytes(StandardCharsets.UTF_8)
                )
        );
    }

    private String getFilesList() {
        return String.join("\t", new File(getCurrentDir()).list());
    }

    private boolean createFile(String filename) throws IOException {
        System.out.println("Creating file " + filename);
        Path path = Path.of(getCurrentDir(), filename);
        if (!Files.exists(path)) {
            Files.createFile(path);
            return true;
        }
        return false;
    }

    private boolean createDir(String dirname) throws IOException {
        System.out.println("Creating directory " + dirname);
        Path path = Path.of(getCurrentDir(), dirname);
        Files.createDirectories(path);
        return true;
    }

    private boolean changeDir(String dirname) {
        System.out.println("Changing directory to " + dirname);
        if ("..".equals(dirname)) {
            currentPath.remove(currentPath.size() - 1);
            return true;
        }
        if (new File(getCurrentDir() + dirname).exists()) {
            currentPath.add(dirname);
            return true;
        }
        return false;
    }

    private boolean removeFile (String filename) throws IOException {
        System.out.println("Removing file " + filename);
        Path path = Path.of(getCurrentDir(), filename);
        if (Files.exists(path)) {
            Files.delete(path);
            return true;
        }
        return false;
    }

    private boolean copyFile (String from, String to) throws IOException {
        System.out.println("Copying file " + from + " to file " + to);
        Path pathFrom = Path.of(getCurrentDir(), from);
        Path pathTo = Path.of(getCurrentDir(), to);
        if (Files.exists(pathFrom)) {
            Files.copy(pathFrom, pathTo, StandardCopyOption.REPLACE_EXISTING);
            return true;
        }
        return false;
    }

    private ArrayList<String> catFile (String filename) throws IOException {
        System.out.println("Showing file " + filename);
        ArrayList<String> res = new ArrayList<>();
        Path path = Path.of(getCurrentDir(), filename);
        if (Files.exists(path)) {
            Files.newBufferedReader(path).lines().forEach(a -> res.add(a));
            return res;
        }
        return null;
    }

    private String getCurrentDir() {
        StringBuilder sb = new StringBuilder();
        for (String s : currentPath) {
            sb.append(s);
            sb.append(File.separator);
        }
        return sb.toString();
    }

    private void sendMessage(String message, Selector selector) throws IOException {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                ((SocketChannel) key.channel())
                        .write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        System.out.println("Client accepted. IP: " + channel.getRemoteAddress());
        channel.register(selector, SelectionKey.OP_READ, "some attach");
        channel.write(ByteBuffer.wrap("Hello user!\r\n".getBytes(StandardCharsets.UTF_8)));
        channel.write(ByteBuffer.wrap("Enter --help for support info\r\n".getBytes(StandardCharsets.UTF_8)));
    }

    public static void main(String[] args) throws IOException {
        new NioTelnetServer();
    }
}