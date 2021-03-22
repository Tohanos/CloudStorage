package server;

import user.User;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UsersPool {
    private static Map<Channel, User> userMap;

    public static void add (Channel channel, User user) {
        if (userMap == null) {
            userMap = new ConcurrentHashMap<>();
        }
        userMap.put(channel, user);
    }

    public static void remove (Channel channel) {
        if (userMap != null) {
            userMap.remove(channel);
        }
    }

    public static User getUser (Channel channel) {
        if (userMap != null && !userMap.isEmpty()) return userMap.get(channel);
        return null;
    }

}
