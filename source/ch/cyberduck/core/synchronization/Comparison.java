package ch.cyberduck.core.synchronization;

/**
 *
 */
public enum Comparison {
    /**
     * Remote file is newer or local file does not exist
     */
    REMOTE_NEWER,
    /**
     * Local file is newer or remote file does not exist
     */
    LOCAL_NEWER,
    /**
     * Files are identical or directories
     */
    EQUAL,
    /**
     * Files differ in size
     */
    UNEQUAL;
}
