package ch.cyberduck.core.exception;

import ch.cyberduck.core.ConnectionCanceledException;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.SocketException;

/**
 * @version $Id$
 */
public class DefaultIOExceptionMappingService extends AbstractIOExceptionMappingService<IOException> {
    private static Logger log = Logger.getLogger(DefaultIOExceptionMappingService.class);

    public BackgroundException map(final IOException failure, final Path directory) {
        return this.map("Connection failed", failure, directory);
    }

    @Override
    public BackgroundException map(final IOException failure) {
        if(failure instanceof SocketException) {
            if(failure.getMessage().equals("Software caused connection abort")) {
                // Do not report as failed if socket opening interrupted
                log.warn("Supressed socket exception:" + failure.getMessage());
                return new ConnectionCanceledException(failure);
            }
            if(failure.getMessage().equals("Socket closed")) {
                // Do not report as failed if socket opening interrupted
                log.warn("Supressed socket exception:" + failure.getMessage());
                return new ConnectionCanceledException(failure);
            }
        }
        if(failure instanceof SSLHandshakeException) {
            log.warn(String.format("Ignore certificate failure %s and drop connection", failure.getMessage()));
            // Server certificate not accepted
            return new ConnectionCanceledException(failure);
        }
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, failure.getMessage());
        return this.wrap(failure, buffer);
    }
}