package ch.cyberduck.core;

import java.io.IOException;

/**
 * @version $Id:$
 */
public class DefaultHostKeyController implements HostKeyController {

    @Override
    public boolean verify(final String hostname, final int port,
                          final String serverHostKeyAlgorithm, final byte[] serverHostKey) throws IOException, ConnectionCanceledException {
        return true;
    }
}