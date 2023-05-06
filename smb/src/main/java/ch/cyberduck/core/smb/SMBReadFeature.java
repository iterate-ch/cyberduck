package ch.cyberduck.core.smb;

import java.io.InputStream;

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

public class SMBReadFeature implements Read {

    private final SMBSession session;

    public SMBReadFeature(SMBSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(Path file, TransferStatus status, ConnectionCallback callback) throws BackgroundException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'read'");
    }

    
}
