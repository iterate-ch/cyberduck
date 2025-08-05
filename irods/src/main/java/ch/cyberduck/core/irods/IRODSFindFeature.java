package ch.cyberduck.core.irods;

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;

import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;

public class IRODSFindFeature implements Find {

    private final IRODSSession session;

    public IRODSFindFeature(IRODSSession session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            final IRODSConnection conn = session.getClient();
            return IRODSFilesystem.exists(conn.getRcComm(), file.getAbsolute());
        }
        catch(IOException | IRODSException e) {
            throw new IRODSExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }

    }
}
