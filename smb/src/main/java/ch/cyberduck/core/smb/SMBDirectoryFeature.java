package ch.cyberduck.core.smb;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

public class SMBDirectoryFeature implements Directory<Integer> {

    private final SMBSession session;
    
    public SMBDirectoryFeature(SMBSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(Path folder, TransferStatus status) throws BackgroundException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mkdir'");
    }

    @Override
    public Directory<Integer> withWriter(Write<Integer> writer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withWriter'");
    }
    
    @Override
    public boolean isSupported(final Path workdir, final String name) {
        return false;
    }

}
