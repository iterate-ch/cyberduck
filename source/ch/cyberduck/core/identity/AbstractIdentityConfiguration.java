package ch.cyberduck.core.identity;

/**
 * @version $Id:$
 */
public abstract class AbstractIdentityConfiguration implements IdentityConfiguration {

    @Override
    public void deleteUser(final String username) {
        //
    }

    @Override
    public void createUser(final String username, final String policy) {
        //
    }
}
