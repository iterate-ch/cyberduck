package ch.cyberduck.core;

import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.io.IOException;

/**
 * @version $Id$
 */
public class DefaultHostKeyController implements HostKeyController {

    @Override
    public boolean verify(final String hostname, final int port,
                          final String serverHostKeyAlgorithm, final byte[] serverHostKey) throws IOException, ConnectionCanceledException {
        return true;
    }
}