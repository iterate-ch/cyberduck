package ch.cyberduck.core;

/**
 * @version $Id$
 */
public class TransferOptions {

    public static final TransferOptions DEFAULT 
            = new TransferOptions();

    /**
     * Resume requested using user interface
     */
    public boolean resumeRequested = false;

    /**
     * Reload requested using user interface
     */
    public boolean reloadRequested = false;

    /**
     * Close session after transfer
     */
    public boolean closeSession = true;
}
