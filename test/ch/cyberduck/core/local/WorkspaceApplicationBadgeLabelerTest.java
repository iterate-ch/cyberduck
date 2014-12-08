package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.test.Depends;

import org.junit.Test;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class WorkspaceApplicationBadgeLabelerTest extends AbstractTestCase{

    @Test
    public void testBadge() throws Exception {
        new WorkspaceApplicationBadgeLabeler().badge("1");
    }
}
