package server;

import io.netty.channel.Channel;

import java.util.LinkedList;
import java.util.List;

public class StateMachinesPool {
    private static List<StateMachine> pool;

    public static void add(StateMachine stateMachine) {
        if (pool == null) {
            pool = new LinkedList<>();
        }
        pool.add(stateMachine);
    }

    public static void remove(Channel channel) {
        for (StateMachine stateMachine : pool) {
            if (stateMachine.getCommandChannel() == channel) {
                remove(stateMachine);
            }
        }
    }

    public static void remove(StateMachine stateMachine) {
        pool.remove(stateMachine);
    }

    public static StateMachine.State getState(Channel commandChannel) {
        for (StateMachine stateMachine : pool) {
            if (stateMachine.getCommandChannel() == commandChannel) return stateMachine.getState();
        }
        return null;
    }

    public static StateMachine getStateMachine (Channel dataChannel) {
        for (StateMachine stateMachine : pool) {
            if (stateMachine.getDataChannel() == dataChannel) return stateMachine;
        }
        return null;
    }

    public static StateMachine getStateMachine(int userId) {
        for (StateMachine stateMachine : pool) {
            if (stateMachine.getUser().getUserId() == userId) {
                return stateMachine;
            }
        }
        return null;
    }

}
