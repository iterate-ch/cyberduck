package ch.cyberduck.core.smb;

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.smbj.common.SMBRuntimeException;

public class SMBAttributesFinderFeature implements AttributesFinder {

    private final SMBSession session;

    public SMBAttributesFinderFeature(SMBSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(Path file, ListProgressListener listener) throws BackgroundException {
        final PathAttributes attributes = new PathAttributes();

        try {

            FileAllInformation fileInformation = session.share.getFileInformation(SMBUtils.convertedAbsolutePath(file));
            if(file.isDirectory() && !fileInformation.getStandardInformation().isDirectory()) {
                throw new NotfoundException("Path found but type is not directory");
            }
            else if(file.isFile() && fileInformation.getStandardInformation().isDirectory()) {
                throw new NotfoundException("Path found but type is not file");
            }

            attributes.setAccessedDate(fileInformation.getBasicInformation().getLastAccessTime().toEpochMillis());
            attributes.setModificationDate(fileInformation.getBasicInformation().getLastWriteTime().toEpochMillis());
            attributes.setCreationDate(fileInformation.getBasicInformation().getCreationTime().toEpochMillis());
            attributes.setSize(fileInformation.getStandardInformation().getEndOfFile());
            attributes.setDisplayname(file.getName());

            return attributes;
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }
}
