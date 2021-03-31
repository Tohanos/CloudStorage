package server;

import user.UserManagement;
import utils.FileChunk;
import utils.FileSplitter;
import utils.MachineType;
import io.netty.buffer.ByteBuf;
import user.User;
import io.netty.channel.Channel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***
 * Здесь выполняется вся логика сервера
 */
public class StateMachine{
    enum State {
        IDLE,
        WORK,
        RECIEVING,
        RECEIVING_NEXT,
        RECEIVING_COMPLETE,
        RECEIVING_ERROR,
        TRANSMITTING
    }

    enum Phase {
        INCOMING_COMMAND,
        CONNECT,
        CREATE_USER,
        AUTHORIZE,
        ACCEPT,
        DECLINE,
        READY,
        BUSY,
        NEXT,
        DISCONNECT,
        ERROR,
        DONE
    }

    private State currentState;

    private User user;

    private Phase currentPhase;
    private Channel commandChannel;
    private Channel dataChannel;
    private String currentDir = "server";
    private int chunkSize = Server.CHUNK_SIZE;
    private int bytesToReceive = 0;
    private String fileName;
    private String fileNameWithPath = "";
    private FileSplitter splitter;
    private MachineType machineType = MachineType.SERVER;


    private long currentTime;

    private final long TIME_TO_WAIT = 3000;

    public StateMachine() {
        currentState = State.IDLE;
    }

    public void setCommandChannel(Channel commandChannel) {
        this.commandChannel = commandChannel;
    }

    public void setDataChannel(Channel dataChannel) {
        this.dataChannel = dataChannel;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setState(State state) {
        this.currentState = state;
    }

    public void setPhase(Phase phase) {
        this.currentPhase = phase;
    }

    public int getBytesToReceive() {
        return bytesToReceive;
    }

    public Channel getCommandChannel() {
        return commandChannel;
    }

    public User getUser() {
        return user;
    }

    public State getState() {
        return currentState;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public String getCurrentDir() {
        return currentDir;
    }

    public String getFileNameWithPath() {
        return fileNameWithPath;
    }

    public Channel getDataChannel() {
        return dataChannel;
    }

    /***
     * Обработка команд клиента - реализация конечного автомата
     * @param commands
     * @return
     * @throws IOException
     */
    public List<String> parseCommand(List<String> commands) throws IOException {
        if (commands.size() == 0) return null;

        List<String> answer = new ArrayList<>();

        while (currentPhase != Phase.DONE) {

            switch (currentState) {
                case IDLE -> {
                    System.out.println("Current state - idle");
                    if (currentPhase == Phase.CONNECT || currentPhase == Phase.INCOMING_COMMAND) {
                        if (commands.get(0).equals("auth")) currentPhase = Phase.AUTHORIZE;
                        if (commands.get(0).equals("create")) currentPhase = Phase.CREATE_USER;
                    }
                    if (currentPhase == Phase.CREATE_USER) {
                        user = UserManagement.createNewUser(commands.get(1), commands.get(2), commands.get(1));
                        if (user == null) {
                            answer.add("EXISTS");
                            currentPhase = Phase.DONE;
                        } else {
                            currentDir = currentDir + File.separator + user.getRootDir();
                            File file = new File(currentDir);
                            file.mkdir();

                            currentPhase = Phase.ACCEPT;
                        }

                    }

                    if (currentPhase == Phase.AUTHORIZE) {
                        user = null;
                        currentPhase = Phase.DECLINE;
                        if (commands.size() > 2) {
                            if (UserManagement.exists(commands.get(1))) {            //В пуле команд под индексом 1 идёт имя пользователя
                                user = UserManagement.getUser(commands.get(1));
                                if (user.getPassword().equals(commands.get(2))) {    //В пуле команд под индексом 2 идёт введённый пароль
//                                    if (StateMachinesPool.getStateMachine(user.getUserId()) == null) {
                                        currentPhase = Phase.ACCEPT;
//                                    } else {
//                                        answer.add("EXIST");
//                                        currentPhase = Phase.DONE;
//                                    }
                                }
                            }
                        }
                    }
                    if (currentPhase == Phase.ACCEPT) {
                        currentPhase = Phase.DONE;
                        if (user != null) {
                            answer.add(String.valueOf(user.getUserId()));
                            currentDir = currentDir + File.separator + user.getRootDir();
                            currentState = State.WORK;
                        }
                    }
                    if (currentPhase == Phase.DECLINE) {
                        answer.add("DECLINE");
                        currentPhase = Phase.DONE;
                    }

                    if (currentPhase == Phase.DONE) {
                        //answer.add("OK");
                    }

                }
                case WORK -> {
                    System.out.println("Current state - work");
                    switch (commands.get(0)) {
                        case "exit":
                            currentPhase = Phase.DISCONNECT;
                            break;
                        case "upload":
                            if (commands.size() > 1) {
                                fileNameWithPath = currentDir + File.separator + fileName;
                                currentState = State.RECIEVING;
                                currentTime = System.currentTimeMillis();
                                answer.add("READY");
                                currentPhase = Phase.DONE;
                            }
                            break;
                        case "uploaddone":
                            answer.add("OK");
                            currentPhase = Phase.DONE;
                            break;
                        case "download":
                            currentState = State.TRANSMITTING;
                            if (commands.size() > 1) fileName = commands.get(1);
                            answer.add("READY");
                            currentPhase = Phase.DONE;
                            break;
                        case "mkdir":
                            if (commands.size() > 1) {
                                File file = new File(currentDir + File.separator + commands.get(1));
                                file.mkdir();
                            }
                            commands.set(0, "ls");
                            break;
                        case "rm":
                            if (commands.size() > 1) {
                                File file = new File(currentDir + File.separator + commands.get(1));
                                file.delete();
                            }
                            answer.add("DONE");
                            currentPhase = Phase.DONE;
                            break;
                        case "cd":
                            if (commands.size() > 1) {
                                if (commands.get(1).equals("..")) {
                                    File file = new File(currentDir);
                                    if (!currentDir.equals("server" + File.separator + user.getRootDir())) {
                                        currentDir = file.getParent();
                                    }
                                } else {
                                    File file = new File(currentDir + File.separator + commands.get(1));
                                    if (file.exists()) {
                                        currentDir = currentDir + File.separator + commands.get(1);
                                    }
                                }
                            }
                            answer.add("DONE");
                            currentPhase = Phase.DONE;
                            break;
                        case "ls":
                            try {
                                File file = new File(currentDir + File.separator);
                                String[] fileNames = file.list();
                                ArrayList<String> names = new ArrayList<>();
                                names.add("..");
                                if (fileNames != null) {
                                    names.addAll(Arrays.asList(fileNames));
                                }
                                answer = names;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            currentPhase = Phase.DONE;
                            break;
                        case "chunksize":
                            currentPhase = Phase.DONE;
                            answer.add(String.valueOf(chunkSize));
                            break;
                    }
                    if (currentPhase == Phase.DONE) {
                        //answer.add("OK");
                    }


                }

                case RECIEVING -> {
                    System.out.println("Current state - recieving");
                    if (currentPhase == Phase.INCOMING_COMMAND) {
                        if (commands.get(0).equals("uploaddone")) {
                            answer.add("DONE");
                            currentPhase = Phase.DONE;
                            currentState = State.WORK;
                        } else {
//                            currentPhase = Phase.NEXT;

                        }
                    }

                    if (currentPhase == Phase.BUSY) {
                        if (System.currentTimeMillis() - currentTime > TIME_TO_WAIT ) {
                            currentPhase = Phase.ERROR;
                        }
                    }
                    if (currentPhase == Phase.READY) {
                        answer.add("OK");
                        currentPhase = Phase.DONE;
                        currentState = State.WORK;
                    }

                    if (currentPhase == Phase.ERROR) {
                        answer.add("ERROR");
                        currentPhase = Phase.DONE;
                        currentState = State.WORK;
                    }

                }

                case RECEIVING_NEXT -> {
                    System.out.println("Current state - recieving_next");
                    if (currentPhase == Phase.INCOMING_COMMAND) {
                        if (commands.get(0).equals("uploaddone")) {
                            answer.add("DONE");
                            currentPhase = Phase.DONE;
                            currentState = State.WORK;
                        } else {
                            answer.add("NEXT");
                            currentPhase = Phase.DONE;
                            currentState = State.RECIEVING;
                        }
                    }
                }

                case RECEIVING_COMPLETE -> {
                    System.out.println("Current state - recieving_complete");
                    currentState = State.WORK;
                    answer.add("DONE");
                    currentPhase = Phase.DONE;
                }

                case RECEIVING_ERROR -> {
                    System.out.println("Current state - recieving_error");
                    currentState = State.WORK;
                    answer.add("ERROR");
                    currentPhase = Phase.DONE;
                }

                case TRANSMITTING -> {
                    System.out.println("Current state - transmitting");
                    if (currentPhase == Phase.INCOMING_COMMAND) {
                        if (commands.get(0).equals("NEXT")) {
                            currentPhase = Phase.NEXT;
                        } else {
                            answer.add("ERROR");
                            currentState = State.WORK;
                        }
                    }
                    if (currentPhase == Phase.NEXT) {
                        File file = new File(currentDir + File.separator + fileName);
                        FileChunk chunk;
                        if (file.exists()) {
                            if (splitter == null)
                                splitter = new FileSplitter(currentDir + File.separator + fileName,
                                    chunkSize,
                                    user.getUserId());
                            chunk = splitter.getNext();
                            sendFileChunk(chunk);
                            if (chunk.isLast()) {
                                currentState = State.WORK;
                                splitter = null;
                                fileName = "";
                            }
                            answer.add("DONE");
                        }
                        currentPhase = Phase.DONE;
                    }
                    if (currentPhase == Phase.DONE) {

                    }
                }
            }


            if (currentPhase == Phase.DISCONNECT) {
                answer.add("DISCONNECT");

            }
        }

        return answer;
    }

    /***
     * Отправка файлового отрезка клиенту
     * @param chunk
     */
    private void sendFileChunk (FileChunk chunk) {
        ByteBuf buf = dataChannel.alloc().buffer(chunkSize);
        byte[] bytes = {67, 72};
        buf.writeBytes(bytes);
        buf.writeInt(chunk.getUserId());
        buf.writeInt(chunk.getSize());
        buf.writeInt(chunk.getPosition());
        buf.writeBoolean(chunk.isLast());
        buf.writeShort((short)chunk.getFilename().length());
        buf.writeBytes(chunk.getFilename().getBytes());
        buf.writeBytes(chunk.getBuffer());
        dataChannel.writeAndFlush(buf);
    }
}
