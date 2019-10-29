package ch.cyberduck.core.notification;

import org.junit.Test;

public class NotificationCenterTest {

    @Test
    public void testNotify() {
        final NotificationService n = new NotificationCenter();
        n.notify(null, null, "title", "test");
    }
}
