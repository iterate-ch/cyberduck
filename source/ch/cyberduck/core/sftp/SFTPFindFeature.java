package ch.cyberduck.core.sftp;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

import java.io.IOException;

import ch.ethz.ssh2.SFTPException;

/**
 * @version $Id:$
 */
public class SFTPFindFeature implements Find {

    private SFTPSession session;

    public SFTPFindFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        try {
            try {
                return session.sftp().canonicalPath(file.getAbsolute()) != null;
            }
            catch(SFTPException e) {
                throw new SFTPExceptionMappingService().map(e);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
        catch(NotfoundException e) {
            return false;
        }
    }
}
