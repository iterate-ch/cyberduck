package ch.cyberduck.core.irods;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.high_level.vfs.IRODSFilesystemException;

import java.io.IOException;

public class IRODSDirectoryFeature implements Directory<Void> {

    private final IRODSSession session;

    public IRODSDirectoryFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            final IRODSConnection conn = session.getClient();
            String path = folder.getAbsolute();
            boolean created = IRODSFilesystem.createCollection(conn.getRcComm(), path);
            if(!created) {
                throw new IOException("Failed to create collection: " + path);
            }
            return folder;
        }
        catch(IOException | IRODSFilesystemException e) {
            throw new IRODSExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }
}
