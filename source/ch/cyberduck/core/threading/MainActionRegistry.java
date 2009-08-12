package ch.cyberduck.core.threading;

/**
 * @version $Id:$
 */
public class MainActionRegistry extends AbstractActionRegistry<MainAction> {

    private static MainActionRegistry instance = null;

    private static final Object lock = new Object();

    public static MainActionRegistry instance() {
        synchronized(lock) {
            if(null == instance) {
                instance = new MainActionRegistry();
            }
            return instance;
        }
    }
}
