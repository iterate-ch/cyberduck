package ch.cyberduck.core.exception;

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;

/**
 * @version $Id$
 */
public class LoginFailureException extends BackgroundException {
    private static final long serialVersionUID = -7628228280711158915L;

    public LoginFailureException(final String detail) {
        super(detail, null);
    }

    public LoginFailureException(final String detail, final Exception cause) {
        super(detail, cause);
    }

    @Override
    public String getMessage() {
        return Locale.localizedString("Login failed", "Credentials");
    }
}
