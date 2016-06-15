package ch.cyberduck.core.transfer.symlink;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferItem;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class DownloadSymlinkResolverTest {

    @Test
    public void testNotSupported() throws Exception {
        DownloadSymlinkResolver resolver = new DownloadSymlinkResolver(null, Collections.<TransferItem>singletonList(
                new TransferItem(new Path("/", EnumSet.of(Path.Type.directory)), new Local(System.getProperty("java.io.tmpdir")))
        ));
        Path p = new Path("/a", EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        assertFalse(resolver.resolve(p));
    }

    @Test
    public void testResolve() throws Exception {
        final ArrayList<TransferItem> files = new ArrayList<TransferItem>();
        files.add(new TransferItem(new Path("/a", EnumSet.of(Path.Type.directory))));
        DownloadSymlinkResolver resolver = new DownloadSymlinkResolver(files);
        Path p = new Path("/a/b", EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        p.setSymlinkTarget(new Path("/a/c", EnumSet.of(Path.Type.file)));
        assertTrue(resolver.resolve(p));
        p.setSymlinkTarget(new Path("/b/c", EnumSet.of(Path.Type.file)));
        assertFalse(resolver.resolve(p));
    }

    @Test
    public void testResolveRoot() throws Exception {
        final ArrayList<TransferItem> files = new ArrayList<TransferItem>();
        files.add(new TransferItem(new Path("/a", EnumSet.of(Path.Type.directory))));
        DownloadSymlinkResolver resolver = new DownloadSymlinkResolver(files);
        Path p = new Path("/b", EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        p.setSymlinkTarget(new Path("/a", EnumSet.of(Path.Type.directory)));
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