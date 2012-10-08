package ch.cyberduck.core;

/**
 * @version $Id:$
 */
public class DefaultCredentials extends Credentials {

    public DefaultCredentials() {
        super();
    }

    public DefaultCredentials(final String user, final String password) {
        super(user, password);
    }

    public DefaultCredentials(final String user, final String password, final boolean shouldBeAddedToKeychain) {
        super(user, password, shouldBeAddedToKeychain);
    }

    @Override
    public String getUsernamePlaceholder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPasswordPlaceholder() {
        throw new UnsupportedOperationException();
    }
}
