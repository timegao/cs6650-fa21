package server.message;

public class InvalidPostMessage extends AbstractMessage {
    public InvalidPostMessage() {
        super("Invalid POST parameters!");
    }
}
