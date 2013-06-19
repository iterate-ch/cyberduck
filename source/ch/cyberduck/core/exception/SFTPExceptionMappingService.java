package ch.cyberduck.core.exception;

import ch.cyberduck.core.threading.BackgroundException;

import java.io.IOException;
import java.net.SocketException;

/**
 * @version $Id$
 */
public class SFTPExceptionMappingService extends AbstractIOExceptionMappingService<IOException> {

    @Override
    public BackgroundException map(final IOException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        if(e.getMessage().equals("Unexpected end of sftp stream.")) {
            return this.wrap(new SocketException(), buffer);
        }
        return this.wrap(e, buffer);
    }
}
