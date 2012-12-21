package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.MD5ChecksumCompute;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 */
public class LocalAttributes extends Attributes {
    private static final Logger log = Logger.getLogger(LocalAttributes.class);

    protected String path;

    public LocalAttributes(String path) {
        this.path = path;
    }

    @Override
    public long getModificationDate() {
        final File file = new File(path);
        if(file.exists()) {
            return file.lastModified();
        }
        return -1;
    }

    /**
     * @return The modification date instead.
     */
    @Override
    public long getCreationDate() {
        return this.getModificationDate();
    }

    /**
     * @return The modification date instead.
     */
    @Override
    public long getAccessedDate() {
        return this.getModificationDate();
    }

    /**
     * This is only returning the correct result if the file already exists.
     *
     * @return File type
     * @see ch.cyberduck.core.local.Local#exists()
     */
    @Override
    public int getType() {
        final int t = this.isFile() ? AbstractPath.FILE_TYPE : AbstractPath.DIRECTORY_TYPE;
        if(this.isSymbolicLink()) {
            return t | AbstractPath.SYMBOLIC_LINK_TYPE;
        }
        return t;
    }

    @Override
    public long getSize() {
        if(this.isDirectory()) {
            return -1;
        }
        final File file = new File(path);
        if(file.exists()) {
            return new File(path).length();
        }
        return -1;
    }

    @Override
    public Permission getPermission() {
        return new LocalPermission();
    }

    @Override
    public boolean isVolume() {
        return null == new File(path).getParent();
    }

    /**
     * This is only returning the correct result if the file already exists.
     *
     * @see ch.cyberduck.core.local.Local#exists()
     */
    @Override
    public boolean isDirectory() {
        return new File(path).isDirectory();
    }

    /**
     * This is only returning the correct result if the file already exists.
     *
     * @see ch.cyberduck.core.local.Local#exists()
     */
    @Override
    public boolean isFile() {
        return new File(path).isFile();
    }

    /**
     * Checks whether a given file is a symbolic link.
     * <p/>
     * <p>It doesn't really test for symbolic links but whether the
     * canonical and absolute paths of the file are identical - this
     * may lead to false positives on some platforms.</p>
     *
     * @return true if the file is a symbolic link.
     */
    @Override
    public boolean isSymbolicLink() {
        final File f = new File(path);
        if(!f.exists()) {
            return false;
        }
        // For a link that actually points to something (either a file or a directory),
        // the absolute path is the path through the link, whereas the canonical path
        // is the path the link references.
        try {
            return !f.getAbsolutePath().equals(f.getCanonicalPath());
        }
        catch(IOException e) {
            return false;
        }
    }

    /**
     * Calculate the MD5 sum as Hex-encoded string
     *
     * @return Null if failure
     */
    @Override
    public String getChecksum() {
        if(this.isFile()) {
            try {
                return new MD5ChecksumCompute().compute(new FileInputStream(path));
            }
            catch(FileNotFoundException e) {
                log.error(String.format("Error computing checksum for path %s", path), e);
                return null;
            }
        }
        return null;
    }

    private final class LocalPermission extends Permission {
        @Override
        public boolean isReadable() {
            return new File(path).canRead();
        }

        @Override
        public boolean isWritable() {
            return new File(path).canWrite();
        }

        @Override
        public boolean isExecutable() {
            return true;
        }

        @Override
        public String toString() {
            return Locale.localizedString("Unknown");
        }
    }
}
