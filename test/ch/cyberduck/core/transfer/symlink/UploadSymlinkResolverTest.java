package ch.cyberduck.core.transfer.symlink;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.local.Local;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class UploadSymlinkResolverTest extends AbstractTestCase {

    @Test
    public void testNoSymbolicLink() throws Exception {
        UploadSymlinkResolver resolver = new UploadSymlinkResolver(null, Collections.<Path>emptyList());
        Path p = new Path("a", Path.FILE_TYPE);
        p.setLocal(new NullLocal(null, "a"));
        assertFalse(resolver.resolve(p));
    }

    @Test
    public void testResolve() throws Exception {
        final ArrayList<Path> files = new ArrayList<Path>();
        final Path a = new Path("/a", Path.DIRECTORY_TYPE);
        a.setLocal(new NullLocal(null, "a"));
        files.add(a);
        UploadSymlinkResolver resolver = new UploadSymlinkResolver(new Symlink() {
            @Override
            public void symlink(final Path file, final String target) throws BackgroundException {
                //
            }
        }, files);
        assertTrue(resolver.resolve(new Path("/a/b", Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "a/b") {
                    @Override
                    public Attributes attributes() {
                        return new PathAttributes(Path.FILE_TYPE) {
                            @Override
                            public boolean isSymbolicLink() {
                                return true;
                            }
                        };
                    }

                    @Override
                    public Local getSymlinkTarget() {
                        return new NullLocal(null, "a/c");
                    }
                };
            }
        }));
        final Path ab = new Path("/a/b", Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "a/b") {
                    @Override
                    public Attributes attributes() {
                        return new PathAttributes(Path.FILE_TYPE) {
                            @Override
                            public boolean isSymbolicLink() {
                                return true;
                            }
                        };
                    }

                    @Override
                    public Local getSymlinkTarget() {
                        return new NullLocal(null, "b/c");
                    }
                };
            }
        };
        ab.setLocal(new NullLocal(null, "a"));
        assertFalse(resolver.resolve(ab));
    }
}
