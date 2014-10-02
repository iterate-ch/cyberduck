package ch.cyberduck.ui.action;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ftp.FTPSession;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class CalculateSizeWorkerTest extends AbstractTestCase {

    @Test
    public void testRun() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        final Path a = new Path("a", EnumSet.of(Path.Type.file));
        a.attributes().setSize(1L);
        files.add(a);
        final Path b = new Path("a", EnumSet.of(Path.Type.file));
        b.attributes().setSize(3L);
        files.add(b);
        assertEquals(4L, new CalculateSizeWorker(new FTPSession(new Host("h")), files,
                new DisabledProgressListener()) {
            int i = 0;

            @Override
            public void cleanup(final Long result) {
                assertEquals(4L, result, 0L);
            }

            @Override
            protected void update(final long size) {
                if(0 == i) {
                    assertEquals(1L, size, 0L);
                }
                if(1 == i) {
                    assertEquals(4L, size, 0L);
                }
                i++;
            }
        }.run(), 0L);
    }
}
