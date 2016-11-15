package ch.cyberduck.core.notification;

import org.junit.Test;

public class NotificationCenterTest {

    @Test
    public void testNotify() throws Exception {
        final NotificationService n = new NotificationCenter();
        n.notify("title", "test");
    }
}