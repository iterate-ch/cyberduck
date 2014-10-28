package ch.cyberduck.core.local;

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.local.features.Touch;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class LocalTouchFactory extends Factory<Touch> {

    public LocalTouchFactory() {
        super("factory.touch.class");
    }

    public static Touch get() {
        return new LocalTouchFactory().create();
    }
}
