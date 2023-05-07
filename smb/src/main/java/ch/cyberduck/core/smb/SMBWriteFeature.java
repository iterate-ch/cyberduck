package ch.cyberduck.core.smb;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.share.File;

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.shared.AppendWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;

public class SMBWriteFeature extends AppendWriteFeature<Void> {

    private final SMBSession session;

    public SMBWriteFeature(SMBSession session) {
        this.session = session;
    }

    @Override
    public StatusOutputStream<Void> write(Path file, TransferStatus status, ConnectionCallback callback)
            throws BackgroundException {

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

    public final class SMBOutputStream extends OutputStream {

        private OutputStream stream;
        private File file;

        public SMBOutputStream(OutputStream stream, File file) {
            this.stream = stream;
            this.file = file;
        }

        @Override
        public void write(int b) throws IOException {
            stream.write(b);
        }

        @Override
        public void close() throws IOException {
            stream.flush();
            super.close();
            file.close();
        }

    }

}
