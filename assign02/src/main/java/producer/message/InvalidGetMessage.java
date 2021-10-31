package producer.message;

public class InvalidGetMessage extends AbstractMessage {
    public InvalidGetMessage() {
        super("Invalid GET parameters!");
    }
}
