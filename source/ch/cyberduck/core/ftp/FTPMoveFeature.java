package ch.cyberduck.core.ftp;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Move;

import java.io.IOException;

/**
 * @version $Id:$
 */
public class FTPMoveFeature implements Move {

    private FTPSession session;

    public FTPMoveFeature(final FTPSession session) {
        this.session = session;
    }

    @Override
    public boolean isSupported(final Path file) {
        return true;
    }

    @Override
    public void move(final Path file, final Path renamed) throws BackgroundException {
        try {
            if(!session.getClient().rename(file.getAbsolute(), renamed.getAbsolute())) {
                throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }
}
