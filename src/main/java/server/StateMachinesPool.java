package server;

import User.User;
import io.netty.channel.Channel;

import java.util.LinkedList;
import java.util.List;

public class StateMachinesPool {
    private static List<StateMachine> pool;

    public static void add() {
        if (pool == null) {
            pool = new LinkedList<>();
        }
        pool.add(new StateMachine());
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

    public static StateMachine getStateMachine(User user) {
        for (StateMachine stateMachine : pool) {
            if (stateMachine.getUser().equals(user)) {
                return stateMachine;
            }
        }
        return null;
    }

}
