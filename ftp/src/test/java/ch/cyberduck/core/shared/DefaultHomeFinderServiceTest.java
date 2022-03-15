package ch.cyberduck.core.shared;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.ftp.AbstractFTPTest;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class DefaultHomeFinderServiceTest extends AbstractFTPTest {

    @Test
    public void testFind() throws Exception {
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DefaultHomeFinderService(session).find());
    }

    @Test
    public void testFindDefaultDirectory() throws Exception {
        session.getHost().setDefaultPath("/test.d");
        assertEquals(new Path("/test.d", EnumSet.of(Path.Type.directory)), new DefaultHomeFinderService(session).find());
    }
}
