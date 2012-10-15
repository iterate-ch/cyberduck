package ch.cyberduck.core.formatter;

import ch.cyberduck.core.Preferences;

/**
 * @version $Id$
 */
public final class SizeFormatterFactory {

    private SizeFormatterFactory() {
        //
    }

    public static SizeFormatter instance() {
        return SizeFormatterFactory.instance(Preferences.instance().getBoolean("browser.filesize.decimal"));
    }

    public static SizeFormatter instance(final boolean decimal) {
        if(decimal) {
            return new ch.cyberduck.core.formatter.DecimalSizeFormatter();
        }
        // Default is binary sizes
        return new ch.cyberduck.core.formatter.BinarySizeFormatter();
    }
}