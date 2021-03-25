package server;

import user.User;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

public class StateMachine{
    enum State {
        IDLE,
        RECIEVING,
        TRANSMITTING
    }

    enum Phase {
        CONNECT,
        AUTHORIZE,
        ACCEPT,
        DECLINE,
        WORK,
        DISCONNECT,
        DONE
    }

    private State currentState;

    private User user;

    private Phase currentPhase;
    private Channel commandChannel;
    private Channel dataChannel;

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

    public List<String> parseCommand(List<String> commands) {
        if (commands.size() == 0) return null;

        List<String> answer = new ArrayList<>();

        while (currentPhase != Phase.DONE) {

            switch (currentState) {
                case IDLE -> {
                    if (currentPhase == Phase.CONNECT) {
                        if (commands.get(0).equals("auth")) currentPhase = Phase.AUTHORIZE;
                    }
                    if (currentPhase == Phase.AUTHORIZE) {
                        user = null;
                        currentPhase = Phase.DECLINE;
                        if (commands.size() > 2) {
                            if (UserManagement.exists(commands.get(1))) {            //В пуле команд под индексом 1 идёт имя пользователя
                                user = UserManagement.getUser(commands.get(1));
                                if (user.getPassword().equals(commands.get(2))) {    //В пуле команд под индексом 2 идёт введённый пароль
                                    currentPhase = Phase.ACCEPT;
                                }
                            }
                        }
                    }
                    if (currentPhase == Phase.ACCEPT) {
                        if (user != null) {
                            UsersPool.add(commandChannel, user);
                            answer.add(String.valueOf(user.getUserId()));
                            currentPhase = Phase.WORK;
                        } else {
                            currentPhase = Phase.DONE;
                        }
                    }
                    if (currentPhase == Phase.DECLINE) {
                        answer.add("DECLINE");
                        currentPhase = Phase.DONE;
                    }
                    if (currentPhase == Phase.WORK) {
                        switch (commands.get(0)) {
                            case "exit":
                                currentPhase = Phase.DISCONNECT;
                                break;
                            case "upload":
                                currentState = State.RECIEVING;
                                break;
                            case "download":
                                currentState = State.TRANSMITTING;
                                break;
                        }

                    }
                    if (currentPhase == Phase.DONE) {
                        //answer.add("OK");
                        //commands.clear();
                    }

                }
                case RECIEVING -> {
                    if (currentPhase == Phase.DONE) {
                        answer.add("OK");
                        //commands.clear();

                    }
                }
                case TRANSMITTING -> {
                    if (currentPhase == Phase.DONE) {
                        answer.add("OK");
                        //commands.clear();
                    }
                }
            }


            if (currentPhase == Phase.DISCONNECT) {
                answer.add("DISCONNECT");

            }
        }

        return answer;
    }
}
