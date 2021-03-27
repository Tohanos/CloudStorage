package server;

import fileassembler.FileChunk;
import fileassembler.FileSplitter;
import io.netty.buffer.ByteBuf;
import user.User;
import io.netty.channel.Channel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StateMachine{
    enum State {
        IDLE,
        WORK,
        RECIEVING,
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
        NEXT,
        DISCONNECT,
        DONE
    }

    private State currentState;

    private User user;

    private Phase currentPhase;
    private Channel commandChannel;
    private Channel dataChannel;
    private String currentDir = "server";
    private int chunkSize = 256;
    private String fileName;
    private FileSplitter splitter;

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

    public Channel getCommandChannel() {
        return commandChannel;
    }

    public User getUser() {
        return user;
    }

    public State getState() {
        return currentState;
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
                            File file = new File("server" + File.separator + user.getRootDir());
                            file.mkdir();
                            UsersPool.add(commandChannel, user);
                            currentDir = user.getRootDir();
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
                            UsersPool.add(commandChannel, user);
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
                            break;
                        case "download":
                            currentState = State.TRANSMITTING;
                            if (commands.size() > 1) fileName = commands.get(1);
                            answer.add("READY");
                            currentPhase = Phase.READY;
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
                                File file = new File(currentDir + commands.get(1));
                                if (file.exists()) {
                                    currentDir = currentDir + commands.get(1);
                                }
                                commands.set(0, "ls");
                            }
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
                    if (currentPhase == Phase.DONE) {
                        answer.add("OK");
                        currentState = State.WORK;
                    }
                    if (currentPhase == Phase.NEXT) {
                        answer.add("NEXT");
                    }
                }
                case TRANSMITTING -> {
                    if (currentPhase == Phase.DONE) {
                        answer.add("OK");
                        currentState = State.WORK;
                    }
                    if (currentPhase == Phase.READY) {
                        if (commands.get(0).equals("START")) {
                            File file = new File(currentDir + File.separator + fileName);
                            int size = 0;
                            if (file.exists()) {
                                splitter = new FileSplitter(currentDir + File.separator + fileName,
                                        chunkSize,
                                        user.getUserId());
                                sendFileChunk(splitter.getNext());
                            }

                        }
                    }
                    if (currentPhase == Phase.NEXT) {
                        answer.add("NEXT");
                    }
                }
            }


            if (currentPhase == Phase.DISCONNECT) {
                answer.add("DISCONNECT");
                UsersPool.remove(commandChannel);

            }
        }

        return answer;
    }

    private void sendFileChunk (FileChunk chunk) {
        ByteBuf buf = dataChannel.alloc().buffer(143 + chunkSize);
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
