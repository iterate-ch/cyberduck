package ch.cyberduck.core;

/**
 * @version $Id:$
 */
public class DisabledLoginController extends AbstractLoginController {

    @Override
    public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason,
                       final boolean enableKeychain, final boolean enablePublicKey, final boolean enableAnonymous) throws LoginCanceledException {
        throw new LoginCanceledException();
    }
}
