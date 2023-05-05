package ch.cyberduck.core.smb;

import java.util.HashSet;
import java.util.Set;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.share.DiskEntry;

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete.Callback;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

public class SMBMoveFeature implements Move {

    private final SMBSession session;

    public SMBMoveFeature(SMBSession session) {
        this.session = session;
    }

	@Override
    public Path move(Path source, Path target, TransferStatus status, Callback delete, ConnectionCallback prompt)
            throws BackgroundException {
        
        // TODO rename is working, implement move

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

        if (source.isDirectory()) {
            createOptions.add(SMB2CreateOptions.FILE_DIRECTORY_FILE);
        }
        else if (source.isFile()) {
            createOptions.add(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE);
        }
        else {
            throw new IllegalArgumentException("Path '" + source.getAbsolute() + "' can't be resolved to file nor directory");
        }

        try (DiskEntry file = session.share.open(source.getAbsolute(), accessMask, fileAttributes, shareAccessSet,
                smb2CreateDisposition, createOptions)) {
            file.rename(target.getAbsolute());
        }

        return target;

    }
    
}
