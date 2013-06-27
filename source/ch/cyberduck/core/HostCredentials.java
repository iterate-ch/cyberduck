package ch.cyberduck.core;

/**
 * @version $Id:$
 */
public final class HostCredentials extends Credentials {

    private Host host;

    public HostCredentials(final Host host) {
        super(Preferences.instance().getProperty("connection.login.name"), null);
        this.host = host;
    }

    @Override
    public String getUsernamePlaceholder() {
        return host.getProtocol().getUsernamePlaceholder();
    }

    @Override
    public String getPasswordPlaceholder() {
        return host.getProtocol().getPasswordPlaceholder();
    }
}
