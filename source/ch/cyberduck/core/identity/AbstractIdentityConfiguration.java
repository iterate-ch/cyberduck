package ch.cyberduck.core.identity;

import ch.cyberduck.core.threading.BackgroundException;

/**
 * @version $Id$
 */
public abstract class AbstractIdentityConfiguration implements IdentityConfiguration {

    @Override
    public void deleteUser(final String username) throws BackgroundException {
        //
    }

    @Override
    public void createUser(final String username, final String policy) throws BackgroundException {
        //
    }
}
