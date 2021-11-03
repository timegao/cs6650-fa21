package server.message;

public class MissingGetMessage extends AbstractMessage {
    public MissingGetMessage() {
        super("Missing GET parameters!");
    }
}
