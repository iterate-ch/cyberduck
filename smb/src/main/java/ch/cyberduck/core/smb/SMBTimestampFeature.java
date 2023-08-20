package ch.cyberduck.core.smb;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultTimestampFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.HashSet;
import java.util.Set;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.share.File;

public class SMBTimestampFeature extends DefaultTimestampFeature {

    private final SMBSession session;

    public SMBTimestampFeature(SMBSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(Path file, TransferStatus status) throws BackgroundException {
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

        try (File fileEntry = session.share.openFile(SMBUtils.convertedAbsolutePath(file), accessMask, fileAttributes, shareAccessSet, smb2CreateDisposition, createOptions)) {
            FileTime creationTime = fileEntry.getFileInformation().getBasicInformation().getCreationTime();
            FileTime time = FileTime.ofEpochMillis(status.getTimestamp());

            FileBasicInformation fileBasicInformation = new FileBasicInformation(creationTime, time, time, time, FileAttributes.FILE_ATTRIBUTE_NORMAL.getValue());
            fileEntry.setFileInformation(fileBasicInformation);
        }
        catch(SMBApiException e) {
            throw new SMBExceptionMappingService().map("Cannot change timestamp of {0}", e, file);
        }
    }

}
