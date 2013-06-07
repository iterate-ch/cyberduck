package ch.cyberduck.core.cf;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.*;

import org.junit.Test;

import java.io.IOException;

/**
 * @version $Id:$
 */
public class ContainerListServiceTest extends AbstractTestCase {

    @Test(expected = IOException.class)
    public void testList() throws Exception {
        new ContainerListService().list(new CFSession(new Host(Protocol.CLOUDFILES, Protocol.CLOUDFILES.getDefaultHostname())));
    }
}
