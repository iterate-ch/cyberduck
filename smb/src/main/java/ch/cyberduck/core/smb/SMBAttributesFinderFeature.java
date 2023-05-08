package ch.cyberduck.core.smb;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VoidAttributesAdapter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;

public class SMBAttributesFinderFeature extends VoidAttributesAdapter implements AttributesFinder {

    private final SMBSession session;

    public SMBAttributesFinderFeature(SMBSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(Path file, ListProgressListener listener) throws BackgroundException {
        final PathAttributes attributes = new PathAttributes();

        FileAllInformation fileInformation = session.share.getFileInformation(file.getAbsolute());

        attributes.setModificationDate(fileInformation.getBasicInformation().getLastWriteTime().toEpochMillis());
        attributes.setCreationDate(fileInformation.getBasicInformation().getCreationTime().toEpochMillis());
        attributes.setSize(fileInformation.getStandardInformation().getEndOfFile());
        attributes.setDisplayname(fileInformation.getNameInformation().substring(1));

        return attributes;
    }
    
}
