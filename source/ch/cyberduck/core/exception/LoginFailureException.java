package ch.cyberduck.core.exception;

import java.io.IOException;

/**
 * @version $Id:$
 */
public class LoginFailureException extends IOException {
    private static final long serialVersionUID = -7628228280711158915L;

    public LoginFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
