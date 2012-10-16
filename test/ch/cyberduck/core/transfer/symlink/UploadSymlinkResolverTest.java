package ch.cyberduck.core.transfer.symlink;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
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
        UploadSymlinkResolver resolver = new UploadSymlinkResolver(Collections.<Path>emptyList());
        NullPath p = new NullPath("a", Path.FILE_TYPE);
        assertFalse(resolver.resolve(p));
    }

    @Test
    public void testResolve() throws Exception {
        final ArrayList<Path> files = new ArrayList<Path>();
        files.add(new NullPath("/a", Path.DIRECTORY_TYPE));
        UploadSymlinkResolver resolver = new UploadSymlinkResolver(files);
        assertTrue(resolver.resolve(new NullPath("/a/b", Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE) {
            @Override
            public Session getSession() {
                return new NullSession(new Host("t")) {
                    @Override
                    public boolean isCreateSymlinkSupported() {
                        return true;
                    }
                };
            }

            @Override
            public Local getLocal() {
                return new NullLocal(null, "a/b") {
                    @Override
                    public Attributes attributes() {
                        return new NullAttributes() {
                            @Override
                            public boolean isSymbolicLink() {
                                return true;
                            }
                        };
                    }

                    @Override
                    public AbstractPath getSymlinkTarget() {
                        return new NullLocal(null, "a/c");
                    }
                };
            }
        }));
        assertFalse(resolver.resolve(new NullPath("/a/b", Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE) {
            @Override
            public Session getSession() {
                return new NullSession(new Host("t")) {
                    @Override
                    public boolean isCreateSymlinkSupported() {
                        return true;
                    }
                };
            }

            @Override
            public Local getLocal() {
                return new NullLocal(null, "a/b") {
                    @Override
                    public Attributes attributes() {
                        return new NullAttributes() {
                            @Override
                            public boolean isSymbolicLink() {
                                return true;
                            }
                        };
                    }

                    @Override
                    public AbstractPath getSymlinkTarget() {
                        return new NullLocal(null, "b/c");
                    }
                };
            }
        }));
    }
}
