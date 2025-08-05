package ch.cyberduck.core.irods;

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;

public class IRODSMoveFeature implements Move {

    private final IRODSSession session;
    private final Delete delete;

    public IRODSMoveFeature(IRODSSession session) {
        this.session = session;
        this.delete = new IRODSDeleteFeature(session);
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        try {
            final IRODSConnection conn = session.getClient();
            if(!IRODSFilesystem.exists(conn.getRcComm(), file.getAbsolute())) {
                throw new NotfoundException(String.format("%s doesn't exist", file.getAbsolute()));
            }
            if(status.isExists()) {
                delete.delete(Collections.singletonMap(renamed, status), connectionCallback, callback);
            }
            IRODSFilesystem.rename(conn.getRcComm(), file.getAbsolute(), renamed.getAbsolute());
            return renamed;
        }
        catch(IOException | IRODSException e) {
            throw new IRODSExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }

}
