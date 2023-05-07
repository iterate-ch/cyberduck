package ch.cyberduck.core.smb;

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
        // TODO implement these values
        final PathAttributes attributes = new PathAttributes();

        attributes.setModificationDate(13370000);
        attributes.setCreationDate(1337000);
        attributes.setSize(1337);
        attributes.setETag("SMB ETag");
        attributes.setDisplayname("SMB Displayname");
        attributes.setLockId("SMB LockId");

        return attributes;
    }
    
}
