package resilientbtree;

import java.io.IOException;

/**
 * Created by sohaib on 07/12/16.
 */
public class RBTDeserializationException extends IOException {
    public RBTDeserializationException(String message) {
        super(message);
    }
}
