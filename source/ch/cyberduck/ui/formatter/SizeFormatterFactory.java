package ch.cyberduck.ui.formatter;

import ch.cyberduck.core.Preferences;

/**
 * @version $Id$
 */
public class SizeFormatterFactory {

    public static SizeFormatter instance() {
        return SizeFormatterFactory.instance(Preferences.instance().getBoolean("browser.filesize.decimal"));
    }

    public static SizeFormatter instance(final boolean decimal) {
        if(decimal) {
            return new DecimalSizeFormatter();
        }
        // Default is binary sizes
        return new BinarySizeFormatter();
    }
}