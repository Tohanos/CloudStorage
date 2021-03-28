package server;

import fileassembler.FileChunk;
import fileassembler.FileSplitter;
import fileassembler.MachineType;
import io.netty.buffer.ByteBuf;
import user.User;
import io.netty.channel.Channel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StateMachine{
    enum State {
        IDLE,
        WORK,
        RECIEVING,
        RECEIVING_COMPLETE,
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

    public List<String> parseCommand(List<String> commands) throws IOException {
        if (commands.size() == 0) return null;

        List<String> answer = new ArrayList<>();

        while (currentPhase != Phase.DONE) {

            switch (currentState) {
                case IDLE -> {
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
                    switch (commands.get(0)) {
                        case "exit":
                            currentPhase = Phase.DISCONNECT;
                            break;
                        case "upload":
                            currentState = State.RECIEVING;
                            currentTime = System.currentTimeMillis();
                            answer.add("NEXT");
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
                                File file = new File(currentDir + File.separator + commands.get(1));
                                if (file.exists()) {
                                    currentDir = currentDir + commands.get(1);
                                }
//                                commands.set(0, "ls");
                            }
                            answer.add("DONE");
                            currentPhase = Phase.DONE;
                            break;
                        case "ls":
                            try {
                                File file = new File(currentDir + File.separator);
                                String[] fileNames = file.list();
                                ArrayList<String> names = new ArrayList<>();
                                if (fileNames != null) {
                                    names.addAll(Arrays.asList(fileNames));
                                }
                                names.add("..");
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
                    if (currentPhase == Phase.INCOMING_COMMAND) {
                        currentPhase = Phase.NEXT;
                    }

                    if (currentPhase == Phase.NEXT) {
                        answer.add("NEXT");
                        currentPhase = Phase.DONE;
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
                case RECEIVING_COMPLETE -> {
                    currentPhase = Phase.DONE;
                }

                case TRANSMITTING -> {
                    if (currentPhase == Phase.INCOMING_COMMAND) {
                        if (commands.get(0).equals("NEXT")) {
                            currentPhase = Phase.NEXT;
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
                                currentPhase = Phase.DONE;
                                currentState = State.WORK;
                                splitter = null;
                            }
                        }
                        currentPhase = Phase.DONE;
                    }
                    if (currentPhase == Phase.DONE) {
                        answer.add("OK");
                    }
                }
            }


            if (currentPhase == Phase.DISCONNECT) {
                answer.add("DISCONNECT");

            }
        }

        return answer;
    }

    private void sendFileChunk (FileChunk chunk) {
        ByteBuf buf = dataChannel.alloc().buffer(chunkSize);
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
