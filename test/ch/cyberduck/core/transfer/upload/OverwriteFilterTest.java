package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.*;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
public class OverwriteFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        // Local file does not exist
        assertFalse(f.accept(new NullSession(new Host("h")), new NullPath("a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t");
            }
        }, new TransferStatus()));
        assertFalse(f.accept(new NullSession(new Host("h")), new NullPath("a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t");
            }
        }, new TransferStatus()));
        assertFalse(f.accept(new NullSession(new Host("h")), new NullPath("a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t");
            }

            @Override
            public boolean exists() {
                return true;
            }
        }, new TransferStatus()));
    }

    @Test
    public void testSize() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final TransferStatus status = new TransferStatus();
        f.prepare(new NullSession(new Host("h")), new NullPath("/t", Path.FILE_TYPE) {
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
        }, status);
        assertEquals(1L, status.getLength(), 0L);
    }

    @Test
    public void testPermissionsNoChange() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final NullPath file = new NullPath("/t", Path.FILE_TYPE);
        file.setLocal(new NullLocal(null, "a"));
        final TransferStatus status = new TransferStatus();
        f.prepare(new NullSession(new Host("h")), file, status);
        assertFalse(status.isComplete());
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
        final TransferStatus status = new TransferStatus();
        f.prepare(new NullSession(new Host("h")), file, status);
        assertFalse(status.isComplete());
        assertEquals(Acl.EMPTY, file.attributes().getAcl());
        assertEquals(Permission.EMPTY, file.attributes().getPermission());
    }

    @Test
    public void testPermissionsExists() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final Acl acl = new Acl(new Acl.UserAndRole(new Acl.User("t") {
            @Override
            public String getPlaceholder() {
                return null;
            }
        }, new Acl.Role("t")));
        final Permission permission = new Permission(777);
        final NullPath file = new NullPath("/t", Path.FILE_TYPE) {
            @Override
            public Session getSession() {
                return new NullSession(new Host("t")) {
                    @Override
                    public boolean isAclSupported() {
                        return true;
                    }

                    @Override
                    public boolean isUnixPermissionsSupported() {
                        return true;
                    }
                };
            }

            @Override
            public void readAcl() {
                attributes().setAcl(acl);
            }

            @Override
            public void readUnixPermission() {
                attributes().setPermission(permission);
            }
        };
        file.setLocal(new NullLocal(null, "a"));
        final TransferStatus status = new TransferStatus();
        status.setOverride(true);
        f.prepare(new NullSession(new Host("h")), file, status);
        assertFalse(status.isComplete());
        assertEquals(acl, file.attributes().getAcl());
        assertEquals(permission, file.attributes().getPermission());
    }
}