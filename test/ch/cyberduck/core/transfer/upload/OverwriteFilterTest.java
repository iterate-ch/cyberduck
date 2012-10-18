package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.local.Local;
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
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        // Local file does not exist
        assertFalse(f.accept(new NullPath("a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t");
            }
        }));
        assertFalse(f.accept(new NullPath("a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t");
            }
        }));
        assertFalse(f.accept(new NullPath("a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t");
            }

            @Override
            public boolean exists() {
                return true;
            }
        }));
    }

    @Test
    public void testSize() throws Exception {
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        assertEquals(1L, f.prepare(new NullPath("/t", Path.FILE_TYPE) {
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
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final NullPath file = new NullPath("/t", Path.FILE_TYPE);
        file.setLocal(new NullLocal(null, "a"));
        assertFalse(f.prepare(file).isComplete());
        Preferences.instance().setProperty("queue.upload.changePermissions", false);
        assertEquals(Acl.EMPTY, file.attributes().getAcl());
        assertEquals(Permission.EMPTY, file.attributes().getPermission());
    }

    @Test
    public void testPermissionsExistsNoChange() throws Exception {
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
        final NullPath file = new NullPath("/t", Path.FILE_TYPE) {
            @Override
            public boolean exists() {
                return true;
            }
        };
        file.setLocal(new NullLocal(null, "a"));
        assertFalse(f.prepare(file).isComplete());
        Preferences.instance().setProperty("queue.upload.changePermissions", true);
        assertEquals(Acl.EMPTY, file.attributes().getAcl());
        assertEquals(Permission.EMPTY, file.attributes().getPermission());
    }

    @Test
    public void testPermissionsExists() throws Exception {
        OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver());
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

            @Override
            public boolean exists() {
                return true;
            }
        };
        file.setLocal(new NullLocal(null, "a"));
        assertFalse(f.prepare(file).isComplete());
        Preferences.instance().setProperty("queue.upload.changePermissions", true);
        Preferences.instance().setProperty("queue.upload.permissions.useDefault", false);
        assertEquals(acl, file.attributes().getAcl());
        assertEquals(permission, file.attributes().getPermission());
    }
}