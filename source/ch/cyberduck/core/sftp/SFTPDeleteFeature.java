package ch.cyberduck.core.sftp;

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

/**
 * @version $Id:$
 */
public class SFTPDeleteFeature implements Delete {

    private SFTPSession session;

    public SFTPDeleteFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public void delete(final List<Path> files) throws BackgroundException {
        for(Path file : files) {
            session.message(MessageFormat.format(LocaleFactory.localizedString("Deleting {0}", "Status"),
                    file.getName()));
            try {
                if(file.attributes().isFile() || file.attributes().isSymbolicLink()) {
                    session.sftp().rm(file.getAbsolute());
                }
                else if(file.attributes().isDirectory()) {
                    session.sftp().rmdir(file.getAbsolute());
                }
            }
            catch(IOException e) {
                throw new SFTPExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }
}
