package ch.cyberduck.core.transfer.symlink;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;

import org.junit.BeforeClass;
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

    @Test
    public void testResolveRoot() throws Exception {
        final ArrayList<Path> files = new ArrayList<Path>();
        files.add(new NullPath("/a", Path.DIRECTORY_TYPE));
        DownloadSymlinkResolver resolver = new DownloadSymlinkResolver(files);
        NullPath p = new NullPath("/b", Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE);
        p.setSymlinkTarget("/a");
        assertTrue(resolver.resolve(p));
    }

    @Test
    public void testRelativize() throws Exception {
        DownloadSymlinkResolver r = new DownloadSymlinkResolver(Collections.<Path>emptyList());
        assertEquals("d", r.relativize("/a/b/c", "/a/b/d"));
        assertEquals("../boot-screens/syslinux.cfg", r.relativize("/ubuntu/dists/precise/main/installer-i386/current/images/netboot/ubuntu-installer/i386/pxelinux.cfg/syslinux.cfg",
                "/ubuntu/dists/precise/main/installer-i386/current/images/netboot/ubuntu-installer/i386/boot-screens/syslinux.cfg"));
    }
}