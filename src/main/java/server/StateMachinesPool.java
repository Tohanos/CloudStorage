package server;

import io.netty.channel.Channel;

import java.util.LinkedList;
import java.util.List;

public class StateMachinesPool {
    private static List<StateMachine> pool;

    public static void add(Channel channel) {
        if (pool == null) {
            pool = new LinkedList<>();
        }
        pool.add(new StateMachine(channel));
    }

    public static void remove(StateMachine stateMachine) {
        pool.remove(stateMachine);
    }

    public static StateMachine.State getState(Channel channel) {
        for (StateMachine stateMachine : pool) {
            if (stateMachine.getChannel() == channel) return stateMachine.getState();
        }
        return null;
    }
}
