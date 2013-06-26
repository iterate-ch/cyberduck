package ch.cyberduck.core;

import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.ui.growl.GrowlFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

/**
 * @version $Id$
 */
public class ConnectionCheckService {
    private static final Logger log = Logger.getLogger(ConnectionCheckService.class);

    private LoginController prompt;

    private HostKeyController key;

    public ConnectionCheckService(final LoginController prompt, final HostKeyController key) {
        this.prompt = prompt;
        this.key = key;
    }

    /**
     * Assert that the connection to the remote host is still alive.
     * Open connection if needed.
     *
     * @param session Session
     * @throws BackgroundException If opening connection fails
     */
    public void check(final Session session) throws BackgroundException {
        if(!session.isConnected()) {
            if(StringUtils.isBlank(session.getHost().getHostname())) {
                if(StringUtils.isBlank(session.getHost().getProtocol().getDefaultHostname())) {
                    throw new ConnectionCanceledException();
                }
                // If hostname is missing update with default
                session.getHost().setHostname(session.getHost().getProtocol().getDefaultHostname());
            }
            this.connect(session);
        }
        else {
            // The session is still supposed to be connected
            try {
                // Send a 'no operation command' to make sure the session is alive
                session.noop();
            }
            catch(BackgroundException e) {
                log.warn(String.format("No operation command failed for session %s. Attempt to reopen connection", session));
                // Try to reconnect once more
                this.connect(session);
            }
        }
    }

    private void connect(final Session session) throws BackgroundException {
        if(session.isConnected()) {
            // Close the underlying socket first
            session.interrupt();
        }

        final Host host = session.getHost();
        session.message(MessageFormat.format(Locale.localizedString("Opening {0} connection to {1}", "Status"),
                host.getProtocol().getName(), host.getHostname()));

        // Configuring proxy if any
        ProxyFactory.get().configure(host);

        final Resolver resolver = new Resolver(
                HostnameConfiguratorFactory.get(host.getProtocol()).lookup(host.getHostname()));

        session.message(MessageFormat.format(Locale.localizedString("Resolving {0}", "Status"),
                host.getHostname()));

        // Try to resolve the hostname first
        try {
            resolver.resolve();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        // The IP address could successfully be determined

        session.open(key);

        GrowlFactory.get().notify("Connection opened", host.getHostname());

        session.message(MessageFormat.format(Locale.localizedString("{0} connection opened", "Status"),
                host.getProtocol().getName()));

        // Update last accessed timestamp
        host.setTimestamp(new Date());

        LoginService login = new LoginService(prompt);
        login.login(session);

        final HistoryCollection history = HistoryCollection.defaultCollection();
        history.add(new Host(host.getAsDictionary()));
    }
}