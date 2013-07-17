package ch.cyberduck.ui.action;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ftp.FTPSession;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @version $Id:$
 */
public class DeleteWorkerTest extends AbstractTestCase {

    @Test
    public void testCompile() throws Exception {
        final FTPSession session = new FTPSession(new Host("t")) {
            @Override
            public void delete(final List<Path> files, final LoginController prompt) throws BackgroundException {
                assertEquals(new Path("/t/a", Path.FILE_TYPE), files.get(0));
                assertEquals(new Path("/t/d/b", Path.FILE_TYPE), files.get(1));
                assertEquals(new Path("/t/d", Path.FILE_TYPE), files.get(2));
                assertEquals(new Path("/t", Path.FILE_TYPE), files.get(3));
            }

            @Override
            public AttributedList<Path> list(final Path file) throws BackgroundException {
                if(file.equals(new Path("/t", Path.DIRECTORY_TYPE))) {
                    return new AttributedList<Path>(Arrays.asList(
                            new Path("/t/a", Path.FILE_TYPE),
                            new Path("/t/d", Path.DIRECTORY_TYPE)
                    ));
                }
                if(file.equals(new Path("/t/d", Path.DIRECTORY_TYPE))) {
                    return new AttributedList<Path>(Arrays.asList(
                            new Path("/t/d/b", Path.FILE_TYPE)
                    ));
                }
                fail();
                return null;
            }
        };
        final DeleteWorker worker = new DeleteWorker(session, new DisabledLoginController(), Collections.singletonList(new Path("/t", Path.DIRECTORY_TYPE))) {
            @Override
            public void cleanup(final Boolean result) {
                //
            }
        };
        worker.run();
    }
}
