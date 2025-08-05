package ch.cyberduck.core.irods;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.TransferStatus;

import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.io.IRODSDataObjectOutputStream;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;


public class IRODSTouchFeature implements Touch {

    private final IRODSSession session;

    public IRODSTouchFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        try {

            // Open and immediately close the file to create/truncate it
            final IRODSConnection conn = session.getClient();
            try(IRODSDataObjectOutputStream out = new IRODSDataObjectOutputStream(conn.getRcComm(), file.getAbsolute(),
                    true /* truncate if exists */, false /* don't append */)) {
                // File is created or truncated by opening the stream
            }

            return file;
        }
        catch(IOException | IRODSException e) {
            throw new IRODSExceptionMappingService().map("Cannot create {0}", e, file);
        }
    }
}
