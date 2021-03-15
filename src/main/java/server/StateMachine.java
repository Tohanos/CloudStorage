package server;

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

    private Phase currentPhase;
    private Channel channel;

    public StateMachine(Channel channel) {
        this.channel = channel;
        currentState = State.IDLE;
    }

    public void setState(State state) {
        this.currentState = state;
    }

    public State getState() {
        return currentState;
    }

    public void setPhase(Phase phase) {
        this.currentPhase = phase;
    }

    public Channel getChannel() {
        return channel;
    }

    public List<String> parseCommand(List<String> commands, Channel channel) {
        List<String> answer = new ArrayList<>();
        User user = null;

        switch (currentState) {
            case IDLE -> {

            }
        }

        if (currentPhase == Phase.CONNECT) {
            if (commands.get(0).equals("auth")) currentPhase = Phase.AUTHORIZE;
        }
        if (currentPhase == Phase.AUTHORIZE) {
            currentPhase = Phase.DECLINE;
            if (commands.size() > 2) {
                if (UserManagement.exists(commands.get(1))) {                                           //В пуле команд под индексом 1 идёт имя пользователя
                    user = UserManagement.getUser(commands.get(1));
                    if (user.getPassword().equals(commands.get(2))) {    //В пуле команд под индексом 2 идёт введённый пароль
                        currentPhase = Phase.ACCEPT;
                    }
                }
            }
        }
        if (currentPhase == Phase.ACCEPT) {
            if (user != null) {
                UsersPool.add(channel, user);
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
