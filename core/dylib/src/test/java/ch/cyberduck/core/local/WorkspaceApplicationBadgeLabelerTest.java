package ch.cyberduck.core.local;

import org.junit.Test;

/**
 * @version $Id$
 */
public class WorkspaceApplicationBadgeLabelerTest {

    @Test
    public void testBadge() throws Exception {
        new WorkspaceApplicationBadgeLabeler().badge("1");
    }
}
