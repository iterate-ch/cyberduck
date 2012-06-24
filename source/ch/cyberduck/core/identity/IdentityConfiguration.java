package ch.cyberduck.core.identity;

import ch.cyberduck.core.Credentials;

/**
 * @version $Id:$
 */
public interface IdentityConfiguration {

    /**
     * Remove user
     *
     * @param username Username
     */
    void deleteUser(final String username);

    /**
     * Verify user exsits and get credentials from keychain
     *
     * @param username Username
     * @return Access credentials for user
     */
    Credentials getUserCredentials(String username);

    /**
     * Create new user and create access credentials
     *
     * @param username Username
     */
    void createUser(String username);
}
