package ch.cyberduck.core.smb;

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Map;

import com.hierynomus.smbj.common.SMBRuntimeException;

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
                    session.share.rm(SMBUtils.convertedAbsolutePath(file));
                }
                else if(file.isDirectory()) {
                    session.share.rmdir(SMBUtils.convertedAbsolutePath(file), true);
                }
            }
            catch(SMBRuntimeException e) {
                throw new SMBExceptionMappingService().map("Cannot delete {0}", e, file);
            }

        }
    }

    @Override
    public boolean isRecursive() {
        return true;
    }

}
