package ch.cyberduck.core;

import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;

import java.text.MessageFormat;

/**
 * @version $Id:$
 */
public class LoginService {

    private LoginController prompt;

    public LoginService(final LoginController prompt) {
        this.prompt = prompt;
    }

    /**
     * Attempts to login using the credentials provided from the login controller. Repeat failed
     * login attempts until canceled by the user.
     *
     * @param session Session
     */
    public void login(final Session session) throws BackgroundException {
        session.prompt(prompt);
        session.warn(prompt);

        session.message(MessageFormat.format(Locale.localizedString("Authenticating as {0}", "Status"),
                session.getHost().getCredentials().getUsername()));
        try {
            session.login(prompt);
            session.message(Locale.localizedString("Login successful", "Credentials"));
            prompt.success(session.getHost());
        }
        catch(LoginFailureException e) {
            session.message(Locale.localizedString("Login failed", "Credentials"));
            prompt.fail(session.getHost().getProtocol(), session.getHost().getCredentials(), e.getDetail());
            this.login(session);
        }
    }
}
