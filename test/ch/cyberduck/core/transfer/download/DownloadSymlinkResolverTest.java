package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class DownloadSymlinkResolverTest extends AbstractTestCase {

    @Test
    public void testNoSymbolicLink() throws Exception {
        DownloadSymlinkResolver resolver = new DownloadSymlinkResolver(Collections.<Path>emptyList());
        NullPath p = new NullPath("a", Path.FILE_TYPE);
        assertFalse(resolver.resolve(p));
    }

    @Test
    public void testResolve() throws Exception {
        final ArrayList<Path> files = new ArrayList<Path>();
        files.add(new NullPath("/a", Path.DIRECTORY_TYPE));
        DownloadSymlinkResolver resolver = new DownloadSymlinkResolver(files);
        NullPath p = new NullPath("/a/b", Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE);
        p.setSymlinkTarget("/a/c");
        assertTrue(resolver.resolve(p));
        p.setSymlinkTarget("/b/c");
        assertFalse(resolver.resolve(p));
    }
}