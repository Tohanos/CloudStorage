package server;

import io.netty.channel.Channel;

public class StateMachine {
    enum State {
        IDLE,
        RECIEVING,
        TRANSMITTING
    }

    private State currentState;
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

    public Channel getChannel() {
        return channel;
    }
}
