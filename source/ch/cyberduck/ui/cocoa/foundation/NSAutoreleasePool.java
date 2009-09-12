package ch.cyberduck.ui.cocoa.foundation;

import org.rococoa.ReleaseInFinalize;
import org.rococoa.Rococoa;

/**
 * @version $Id$
 */
public
@ReleaseInFinalize(false)
abstract class NSAutoreleasePool extends org.rococoa.cocoa.foundation.NSAutoreleasePool {

    public static NSAutoreleasePool push() {
        return Rococoa.create("NSAutoreleasePool", NSAutoreleasePool.class);
    }
}
