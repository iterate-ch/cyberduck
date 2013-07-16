package ch.cyberduck.core.ftp;

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.i18n.Locale;

/**
 * @version $Id:$
 */
public final class FTPTLSProtocol extends AbstractProtocol {
    @Override
    public String getIdentifier() {
        return this.getScheme().name();
    }

    @Override
    public Type getType() {
        return Type.ftp;
    }

    @Override
    public String getName() {
        return "FTP-SSL";
    }

    @Override
    public String getDescription() {
        return Locale.localizedString("FTP-SSL (Explicit AUTH TLS)");
    }

    @Override
    public Scheme getScheme() {
        return Scheme.ftps;
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "ftp");
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
