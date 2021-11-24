package server.message;

public class MissingPostMessage extends AbstractMessage {
    public MissingPostMessage() {
        super("Missing POST parameters!");
    }
}
