package ch.cyberduck.core.smb;

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Map;

import com.hierynomus.mssmb2.SMBApiException;

public class SMBDeleteFeature implements Delete {

    private final SMBSession session;

    public SMBDeleteFeature(SMBSession session) {
        this.session = session;
    }

    @Override
    public void delete(Map<Path, TransferStatus> files, PasswordCallback prompt, Callback callback) throws BackgroundException {
        for(Path file : files.keySet()) {
            callback.delete(file);

            try {
                if(file.isFile() || file.isSymbolicLink()) {
                    session.share.rm(file.getAbsolute());
                }
                else if(file.isDirectory()) {
                    session.share.rmdir(file.getAbsolute(), true);
                }
            }
            catch(SMBApiException e) {
                throw new SmbExceptionMappingService().map(e);
            }

        }
    }

    @Override
    public boolean isRecursive() {
        return true;
    }

}
