package ch.cyberduck.ui.cocoa;

/**
 * @version $Id$
 */
public interface CDSheetCallback {

    /**
     * Use default option; 'OK'
     */
    public int DEFAULT_OPTION = 1;
    /**
     * Use alternate option
     */
    public int ALTERNATE_OPTION = -1;
    /**
     * Cancel proposed operation
     */
    public int CANCEL_OPTION = 0;
    /**
     *
     */
    public int SKIP_OPTION = 2;

    /**
     *
     * @param returncode
     */
    public abstract  void callback(final int returncode);

}
