package ch.cyberduck.core.transfer.symlink;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferItem;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DownloadSymlinkResolverTest extends AbstractTestCase {

    @Test
    public void testNoSymbolicLink() throws Exception {
        DownloadSymlinkResolver resolver = new DownloadSymlinkResolver(Collections.<TransferItem>emptyList());
        Path p = new Path("a", Path.FILE_TYPE);
        assertFalse(resolver.resolve(p));
    }

    @Test
    public void testResolve() throws Exception {
        final ArrayList<TransferItem> files = new ArrayList<TransferItem>();
        files.add(new TransferItem(new Path("/a", Path.DIRECTORY_TYPE)));
        DownloadSymlinkResolver resolver = new DownloadSymlinkResolver(files);
        Path p = new Path("/a/b", Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE);
        p.setSymlinkTarget(new Path("/a/c", Path.FILE_TYPE));
        assertTrue(resolver.resolve(p));
        p.setSymlinkTarget(new Path("/b/c", Path.FILE_TYPE));
        assertFalse(resolver.resolve(p));
    }

    @Test
    public void testResolveRoot() throws Exception {
        final ArrayList<TransferItem> files = new ArrayList<TransferItem>();
        files.add(new TransferItem(new Path("/a", Path.DIRECTORY_TYPE)));
        DownloadSymlinkResolver resolver = new DownloadSymlinkResolver(files);
        Path p = new Path("/b", Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE);
        p.setSymlinkTarget(new Path("/a", Path.DIRECTORY_TYPE));
        assertTrue(resolver.resolve(p));
    }

    @Test
    public void testRelativize() throws Exception {
        DownloadSymlinkResolver r = new DownloadSymlinkResolver(Collections.<TransferItem>emptyList());
        assertEquals("d", r.relativize("/a/b/c", "/a/b/d"));
        assertEquals("../boot-screens/syslinux.cfg", r.relativize("/ubuntu/dists/precise/main/installer-i386/current/images/netboot/ubuntu-installer/i386/pxelinux.cfg/syslinux.cfg",
                "/ubuntu/dists/precise/main/installer-i386/current/images/netboot/ubuntu-installer/i386/boot-screens/syslinux.cfg"));
    }
}