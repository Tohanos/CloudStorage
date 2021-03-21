package server;

import User.User;
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
        DISCONNECT
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
        List<String> answer = new ArrayList<>();

        switch (currentState) {
            case IDLE -> {

            }
        }

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
                answer.add("OK");
                currentPhase = Phase.WORK;
            } else {
                currentPhase = Phase.CONNECT;
            }
        }
        if (currentPhase == Phase.DECLINE) {
            answer.add("DECLINE");
            currentPhase = Phase.CONNECT;
        }

        if (currentPhase == Phase.WORK) {

        }

        if (currentPhase == Phase.DISCONNECT) {
            answer.add("DISCONNECT");

        }
        return answer;
    }
}
