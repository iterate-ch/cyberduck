package ch.cyberduck.core.ftp;

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;

/**
 * @version $Id$
 */
public final class FTPProtocol extends AbstractProtocol {

    @Override
    public String getIdentifier() {
        return this.getScheme().name();
    }

    @Override
    public String getDescription() {
        return LocaleFactory.localizedString("FTP (File Transfer Protocol)");
    }

    @Override
    public Scheme getScheme() {
        return Scheme.ftp;
    }

    @Override
    public boolean isUTCTimezone() {
        return false;
    }

    @Override
    public boolean isEncodingConfigurable() {
        return true;
    }

    @Override
    public FTPSession createSession(final Host host) {
        return new FTPSession(host);
    }
}
