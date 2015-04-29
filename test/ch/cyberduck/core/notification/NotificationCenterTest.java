package ch.cyberduck.core.notification;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @version $Id$
 */
public class NotificationCenterTest extends AbstractTestCase {

    @Test
    @Ignore
    public void testNotify() throws Exception {
        final NotificationService n = new NotificationCenter();
        n.notify("title", "test");
    }
}