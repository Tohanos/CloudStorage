package command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command implements Serializable {

    List<String> args;

    public Command(String command) {
        args = Arrays.asList(command.replace("\r\n", "").split(" ").clone());
    }

    public Command(List<String> args) {
        this.args = args;
    }

    public List<String> getCommand() {
        return args;
    }
}
