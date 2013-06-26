package ch.cyberduck.core;

/**
 * @version $Id$
 */
public class DisabledLoginController extends AbstractLoginController {

    @Override
    public void warn(final String title, final String message, final String continueButton, final String disconnectButton, final String preference) throws LoginCanceledException {
        throw new LoginCanceledException();
    }

    @Override
    public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason,
                       final boolean enableKeychain, final boolean enablePublicKey, final boolean enableAnonymous) throws LoginCanceledException {
        throw new LoginCanceledException();
    }
}
