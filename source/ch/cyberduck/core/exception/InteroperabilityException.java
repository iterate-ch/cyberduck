package ch.cyberduck.core.exception;

import ch.cyberduck.core.LocaleFactory;

/**
 * Interoperability failure was detected
 *
 * @version $Id$
 */
public class InteroperabilityException extends BackgroundException {
    private static final long serialVersionUID = 4426127443925394476L;

    public InteroperabilityException(final String detail, final Throwable cause) {
        super(LocaleFactory.localizedString("Interoperability failure", "Error"), detail, cause);
    }
}
