package ch.cyberduck.core.transfer.symlink;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.transfer.TransferItem;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UploadSymlinkResolverTest {

    @Test
    public void testNoSymbolicLink() throws Exception {
        UploadSymlinkResolver resolver = new UploadSymlinkResolver(null, Collections.<TransferItem>emptyList());
        assertFalse(resolver.resolve(new NullLocal("a")));
    }

    @Test
    public void testResolve() throws Exception {
        final ArrayList<TransferItem> files = new ArrayList<TransferItem>();
        final Path a = new Path("/a", EnumSet.of(Path.Type.directory));
        files.add(new TransferItem(a, new NullLocal(System.getProperty("java.io.tmpdir"), "a") {
            @Override
            public boolean isFile() {
                return false;
            }

            @Override
            public boolean isDirectory() {
                return true;
            }
        }));
        UploadSymlinkResolver resolver = new UploadSymlinkResolver(new Symlink() {
            @Override
            public void symlink(final Path file, final String target) throws BackgroundException {
                //
            }
        }, files);
        assertTrue(resolver.resolve(new NullLocal(System.getProperty("java.io.tmpdir"), "a" + File.separator + "b") {
            @Override
            public boolean isSymbolicLink() {
                return true;
            }

            @Override
            public boolean isFile() {
                return true;
            }

            @Override
            public Local getSymlinkTarget() throws LocalAccessDeniedException {
                return new NullLocal(System.getProperty("java.io.tmpdir"),  "a" + File.separator + "c");
            }
        }));
        assertFalse(resolver.resolve(new NullLocal("/a/b") {
            public boolean isSymbolicLink() {
                return true;
            }

            @Override
            public boolean isFile() {
                return true;
            }

            @Override
            public Local getSymlinkTarget() throws LocalAccessDeniedException {
                return new NullLocal("/b/c");
            }
        }));
    }
}