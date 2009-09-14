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
    public final int OTHER_OPTION = -1;
    /**
     * Cancel proposed operation
     */
    public final int ALTERNATE_OPTION = 0;

    /**
     * Called after the sheet has been dismissed by the user. The return codes are defined in
     * <code>ch.cyberduck.ui.cooca.CDSheetCallback</code>
     *
     * @param returncode
     */
    public abstract void callback(final int returncode);

}
