package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

/**
 * @version $Id$
 */
public class WorkspaceApplicationBadgeLabelerTest extends AbstractTestCase{

    @Test
    public void testBadge() throws Exception {
        new WorkspaceApplicationBadgeLabeler().badge("1");
    }
}
