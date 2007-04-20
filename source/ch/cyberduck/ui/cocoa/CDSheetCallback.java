package ch.cyberduck.ui.cocoa;

/**
 * @version $Id$
 */
public interface CDSheetCallback {

    /**
     * Use default option; 'OK'
     */
    public final int DEFAULT_OPTION = 1;
    /**
     * Use alternate option
     */
    public final int ALTERNATE_OPTION = -1;
    /**
     * Cancel proposed operation
     */
    public final int CANCEL_OPTION = 0;

    /**
     *
     * @param returncode
     */
    public abstract void callback(final int returncode);

}
