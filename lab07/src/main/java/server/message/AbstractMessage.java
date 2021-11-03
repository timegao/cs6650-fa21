package server.message;

import lombok.Data;

@Data
public class AbstractMessage {
    private final String message;

    public AbstractMessage(String message) {
        this.message = message;
    }
}
