package ch.cyberduck.core.smb;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.share.File;

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

public class SMBReadFeature implements Read {
    private static final Logger logger = LogManager.getLogger(SMBReadFeature.class);

    private final SMBSession session;

    public SMBReadFeature(SMBSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(Path file, TransferStatus status, ConnectionCallback callback) throws BackgroundException {
        try {
            Set<SMB2ShareAccess> shareAccessSet = new HashSet<>();
            shareAccessSet.add(SMB2ShareAccess.FILE_SHARE_READ);

            Set<FileAttributes> fileAttributes = new HashSet<>();
            fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);
            Set<SMB2CreateOptions> createOptions = new HashSet<>();
            SMB2CreateDisposition smb2CreateDisposition = SMB2CreateDisposition.FILE_OPEN_IF;

            Set<AccessMask> accessMask = new HashSet<>();
            accessMask.add(AccessMask.FILE_READ_DATA);

            createOptions.add(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE);

            File fileEntry = session.share.openFile(file.getAbsolute(), accessMask, fileAttributes, shareAccessSet, smb2CreateDisposition, createOptions);

            InputStream stream = fileEntry.getInputStream();

            if(status.isAppend()) {
                try {
                    long skipped = stream.skip(status.getOffset());
                    if(skipped != status.getOffset()) {
                        logger.log(Level.WARN, "Could not skip %d bytes in file %s.", status.getOffset(), file);
                    }
                }
                catch(IOException e) {
                    fileEntry.close();
                    throw new BackgroundException(e);
                }
            }

            return new SMBInputStream(stream, fileEntry);
        } catch(SMBApiException e) {
            throw new SMBExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    private final class SMBInputStream extends ProxyInputStream {

        private final InputStream stream;
        private final File file;


        public SMBInputStream(InputStream stream, File file) {
            super(stream);
            this.stream = stream;
            this.file = file;
        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }

        @Override
        public void close() throws IOException {
            super.close();
            file.close();
        }

    }

}
