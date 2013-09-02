package ch.cyberduck.core.sftp;

import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Move;

import java.io.IOException;
import java.util.Collections;

/**
 * @version $Id$
 */
public class SFTPMoveFeature implements Move {

    private SFTPSession session;

    public SFTPMoveFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public boolean isSupported(final Path file) {
        return true;
    }

    @Override
    public void move(final Path file, final Path renamed) throws BackgroundException {
        try {
            if(new SFTPFindFeature(session).find(renamed)) {
                new SFTPDeleteFeature(session).delete(Collections.singletonList(renamed), new DisabledLoginController());
            }
            session.sftp().mv(file.getAbsolute(), renamed.getAbsolute());
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }
}
