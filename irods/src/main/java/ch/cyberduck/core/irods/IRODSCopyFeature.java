package ch.cyberduck.core.irods;

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;

import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.vfs.CollectionEntry;
import org.irods.irods4j.high_level.vfs.IRODSCollectionIterator;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.high_level.vfs.ObjectStatus;
import org.irods.irods4j.low_level.api.IRODSApi.RcComm;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.util.EnumSet;

public class IRODSCopyFeature implements Copy {

    private final IRODSSession session;

    public IRODSCopyFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Path copy(final Path source, final Path target, final ch.cyberduck.core.transfer.TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        try {
            final IRODSConnection conn = session.getClient();
            final String from = source.getAbsolute();
            final String to = target.getAbsolute();
            if(source.isFile()) {
                IRODSFilesystem.copyDataObject(conn.getRcComm(), from, to);

                if(listener != null && status.getLength() > 0) {
                    listener.sent(status.getLength());
                }
            }
            if(source.isDirectory()) {
                this.copyDirectoryRecursively(conn.getRcComm(), from, to);
            }
            return target;
        }
        catch(IOException | IRODSException e) {
            throw new IRODSExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    public static void copyDirectoryRecursively(RcComm rcComm, String source, String target) throws IOException, IRODSException {
        // First, create the root of the target directory
        if(!IRODSFilesystem.exists(rcComm, target)) {
            IRODSFilesystem.createCollection(rcComm, target);
        }

        // Recursively iterate through the source collection
        for(CollectionEntry entry : new IRODSCollectionIterator(rcComm, source)) {
            String relative = entry.path().substring(source.length()); // relative path from source
            String targetPath = target + relative;

            ObjectStatus status = entry.status();

            if(status.getType() == ObjectStatus.ObjectType.COLLECTION) {
                // Create directory in target
                IRODSFilesystem.createCollection(rcComm, targetPath);
            }
            else if(status.getType() == ObjectStatus.ObjectType.DATA_OBJECT) {
                // Copy file
                IRODSFilesystem.copyDataObject(rcComm, entry.path(), targetPath);
            }
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }
}
