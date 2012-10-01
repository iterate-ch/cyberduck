package ch.cyberduck.core;

/**
 * @version $Id:$
 */
public class PreferencesUseragentProvider implements UseragentProvider {

    private final String ua = Preferences.instance().getProperty("application.name") + "/"
            + Preferences.instance().getProperty("application.version")
            + " (" + System.getProperty("os.name") + "/" + System.getProperty("os.version") + ")"
            + " (" + System.getProperty("os.arch") + ")";

    @Override
    public String get() {
        return ua;
    }
}
