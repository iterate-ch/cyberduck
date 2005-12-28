package ch.cyberduck.ui.cocoa;

/**
 * @version $Id$
 */
public interface CDSheetCallback {

    /**
     * Use default option; 'OK'
     */
    int DEFAULT_OPTION = 1;
    /**
     * Use alternate option
     */
    int ALTERNATE_OPTION = -1;
    /**
     * Cancel proposed operation
     */
    int CANCEL_OPTION = 0;
    /**
     *
     */
    int SKIP_OPTION = 2;

    /**
     *
     * @param returncode
     */
    public void callback(int returncode);

}
