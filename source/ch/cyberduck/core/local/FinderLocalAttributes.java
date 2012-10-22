package ch.cyberduck.core.local;

import ch.cyberduck.core.Permission;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.NSDate;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSFileManager;
import ch.cyberduck.ui.cocoa.foundation.NSNumber;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;
import org.rococoa.Rococoa;

/**
 * Extending attributes with <code>NSFileManager</code>.
 *
 * @see ch.cyberduck.ui.cocoa.foundation.NSFileManager
 */
public class FinderLocalAttributes extends LocalAttributes {
    private static final Logger log = Logger.getLogger(FinderLocalAttributes.class);

    public FinderLocalAttributes(String path) {
        super(path);
    }

    /**
     * @return Null if no such file.
     */
    private NSDictionary getNativeAttributes() {
//            if(!exists()) {
//                return null;
//            }
        // If flag is true and path is a symbolic link, the attributes of the linked-to file are returned;
        // if the link points to a nonexistent file, this method returns null. If flag is false,
        // the attributes of the symbolic link are returned.
        return NSFileManager.defaultManager().attributesOfItemAtPath_error(
                path, null);
    }

    /**
     * @param name File manager attribute name
     * @return Null if no such file or attribute.
     */
    private NSObject getNativeAttribute(final String name) {
        NSDictionary dict = this.getNativeAttributes();
        if(null == dict) {
            log.error(String.format("No file at %s", path));
            return null;
        }
        // Returns an entry’s value given its key, or null if no value is associated with key.
        return dict.objectForKey(name);
    }

    @Override
    public long getSize() {
        if(this.isDirectory()) {
            return -1;
        }
        NSObject size = this.getNativeAttribute(NSFileManager.NSFileSize);
        if(null == size) {
            return -1;
        }
        // Refer to #5503 and http://code.google.com/p/rococoa/issues/detail?id=3
        return (long) Rococoa.cast(size, NSNumber.class).doubleValue();
    }

    @Override
    public Permission getPermission() {
        try {
            NSObject object = this.getNativeAttribute(NSFileManager.NSFilePosixPermissions);
            if(null == object) {
                return Permission.EMPTY;
            }
            String posixString = Integer.toOctalString(Rococoa.cast(object, NSNumber.class).intValue());
            return new FinderLocalPermission(Integer.parseInt(posixString.substring(posixString.length() - 3)));
        }
        catch(NumberFormatException e) {
            log.error(e.getMessage());
        }
        return Permission.EMPTY;
    }

    /**
     * Read <code>NSFileCreationDate</code>.
     *
     * @return Milliseconds since 1970
     */
    @Override
    public long getCreationDate() {
        NSObject object = this.getNativeAttribute(NSFileManager.NSFileCreationDate);
        if(null == object) {
            return -1;
        }
        return (long) (Rococoa.cast(object, NSDate.class).timeIntervalSince1970() * 1000);
    }

    @Override
    public String getOwner() {
        NSObject object = this.getNativeAttribute(NSFileManager.NSFileOwnerAccountName);
        if(null == object) {
            return super.getOwner();
        }
        return object.toString();
    }

    @Override
    public String getGroup() {
        NSObject object = this.getNativeAttribute(NSFileManager.NSFileGroupOwnerAccountName);
        if(null == object) {
            return super.getGroup();
        }
        return object.toString();
    }

    /**
     * @return The value for the key NSFileSystemFileNumber, or 0 if the receiver doesn’t have an entry for the key
     */
    public long getInode() {
        NSObject object = this.getNativeAttribute(NSFileManager.NSFileSystemFileNumber);
        if(null == object) {
            return -1;
        }
        NSNumber number = Rococoa.cast(object, NSNumber.class);
        return number.longValue();
    }

    @Override
    public boolean isBundle() {
        return NSWorkspace.sharedWorkspace().isFilePackageAtPath(path);
    }

    @Override
    public boolean isSymbolicLink() {
        return NSFileManager.defaultManager().destinationOfSymbolicLinkAtPath_error(path, null) != null;
    }

    /**
     * Executable, readable and writable flags based on <code>NSFileManager</code>.
     */
    private final class FinderLocalPermission extends Permission {
        public FinderLocalPermission(final int octal) {
            super(octal);
        }

        @Override
        public boolean isExecutable() {
            return NSFileManager.defaultManager().isExecutableFileAtPath(path);
        }

        @Override
        public boolean isReadable() {
            return NSFileManager.defaultManager().isReadableFileAtPath(path);
        }

        @Override
        public boolean isWritable() {
            return NSFileManager.defaultManager().isWritableFileAtPath(path);
        }
    }
}
