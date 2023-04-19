package ch.cyberduck.core.smb;

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.shared.AppendWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.output.ProxyOutputStream;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.share.File;

public class SMBWriteFeature extends AppendWriteFeature<Void> {
    private final SMBSession session;

    public SMBWriteFeature(SMBSession session) {
        this.session = session;
    }

    @Override
    public StatusOutputStream<Void> write(Path file, TransferStatus status, ConnectionCallback callback)
            throws BackgroundException {
        try {
            Set<SMB2ShareAccess> shareAccessSet = new HashSet<>();
            shareAccessSet.add(SMB2ShareAccess.FILE_SHARE_READ);
            shareAccessSet.add(SMB2ShareAccess.FILE_SHARE_WRITE);
            shareAccessSet.add(SMB2ShareAccess.FILE_SHARE_DELETE);

            Set<FileAttributes> fileAttributes = new HashSet<>();
            fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);
            Set<SMB2CreateOptions> createOptions = new HashSet<>();
            SMB2CreateDisposition smb2CreateDisposition = SMB2CreateDisposition.FILE_OPEN_IF;

            Set<AccessMask> accessMask = new HashSet<>();
            accessMask.add(AccessMask.MAXIMUM_ALLOWED);

            createOptions.add(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE);


            File fileEntry = session.share.openFile(file.getAbsolute(), accessMask, fileAttributes,
                    shareAccessSet, smb2CreateDisposition, createOptions);

            return new VoidStatusOutputStream(new SMBOutputStream(fileEntry.getOutputStream(), fileEntry));
        }
        catch(SMBApiException e) {
            throw new SMBExceptionMappingService().map("Upload {0} failed", e, file);
        }


    }

    private static final class SMBOutputStream extends ProxyOutputStream {

        private File file;
        private long fileSize;

        public SMBOutputStream(OutputStream stream, File file) {
            super(stream);
            this.file = file;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                file.flush();
                file.setLength(fileSize);
                file.close();
            }
        }

        @Override
        protected void afterWrite(int n) throws IOException {
            fileSize += n;
            super.afterWrite(n);
        }

    }

}
