package ch.cyberduck.core.local;

import org.junit.Test;

public class WorkspaceApplicationBadgeLabelerTest {

    @Test
    public void testBadge() throws Exception {
        new WorkspaceApplicationBadgeLabeler().badge("1");
    }
}
