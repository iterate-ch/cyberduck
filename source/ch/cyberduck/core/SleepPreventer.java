package ch.cyberduck.core;

/**
 * @version $Id:$
 */
public interface SleepPreventer {

    /**
     * @return Reference to lock to use for release
     */
    String lock();

    /**
     * @param id Reference
     */
    void release(String id);
}
