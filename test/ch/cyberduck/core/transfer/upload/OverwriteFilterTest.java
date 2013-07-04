package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.*;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class OverwriteFilterTest extends AbstractTestCase {

    @Test
    public void testAcceptNoLocal() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        // Local file does not exist
        assertFalse(f.accept(new NullSession(new Host("h")), new NullPath("a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public boolean exists() {
                        return false;
                    }
                };
            }
        }));
        assertFalse(f.accept(new NullSession(new Host("h")), new NullPath("a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public boolean exists() {
                        return false;
                    }
                };
            }
        }));
    }

    @Test
    public void testAcceptRemoteExists() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        assertTrue(f.accept(new NullSession(new Host("h")), new NullPath("a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }
        }));
        assertFalse(f.accept(new NullSession(new Host("h")), new NullPath("a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }

            @Override
            public Path getParent() {
                return new NullPath("/", Path.DIRECTORY_TYPE) {
                    @Override
                    public AttributedList<Path> list() {
                        return new AttributedList<Path>(Collections.<Path>singletonList(new NullPath("a", Path.DIRECTORY_TYPE)));
                    }
                };
            }
        }));
    }

    @Test
    public void testSize() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        assertEquals(1L, f.prepare(new NullSession(new Host("h")), new NullPath("/t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "/t") {
                    @Override
                    public Attributes attributes() {
                        return new NullAttributes() {
                            @Override
                            public long getSize() {
                                return 1L;
                            }
                        };
                    }
                };
            }
        }).getLength(), 0L);
    }

    @Test
    public void testPermissionsNoChange() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final NullPath file = new NullPath("/t", Path.FILE_TYPE);
        file.setLocal(new NullLocal(null, "a"));
        assertFalse(f.prepare(new NullSession(new Host("h")), file).isComplete());
        assertEquals(Acl.EMPTY, file.attributes().getAcl());
        assertEquals(Permission.EMPTY, file.attributes().getPermission());
    }

    @Test
    public void testPermissionsExistsNoChange() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final NullPath file = new NullPath("/t", Path.FILE_TYPE) {
            @Override
            public boolean exists() {
                return true;
            }
        };
        file.setLocal(new NullLocal(null, "a"));
        assertFalse(f.prepare(new NullSession(new Host("h")), file).isComplete());
        assertEquals(Acl.EMPTY, file.attributes().getAcl());
        assertEquals(Permission.EMPTY, file.attributes().getPermission());
    }
}