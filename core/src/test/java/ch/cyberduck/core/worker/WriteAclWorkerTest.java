package ch.cyberduck.core.worker;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.shared.DefaultAclFeature;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class WriteAclWorkerTest {

    @Test
    public void testRunNoFiles() throws Exception {
        final WriteAclWorker worker = new WriteAclWorker(new AclOverwrite(Collections.emptyMap(), Collections.emptyMap()), true, new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean result) {
                //
            }
        };
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(Class<T> type) {
                if(type.equals(AclPermission.class)) {
                    return (T) new DefaultAclFeature() {
                        @Override
                        public Acl getPermission(final Path file) throws BackgroundException {
                            fail();
                            return null;
                        }

                        @Override
                        public void setPermission(final Path file, final Acl acl) throws BackgroundException {
                            fail();
                        }

                        @Override
                        public List<Acl.User> getAvailableAclUsers() {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return super.getFeature(type);
            }
        });
    }

    @Test
    public void testRunEmpty() throws Exception {
        final Acl acl = new Acl();
        final Path t = new Path("/t", EnumSet.of(Path.Type.file));

        final Map<Path, List<Acl.UserAndRole>> original = new HashMap<>();
        original.put(t, Collections.emptyList());

        final WriteAclWorker worker = new WriteAclWorker(new AclOverwrite(original, Collections.emptyMap()), true, new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean result) {
                //
            }
        };
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(Class<T> type) {
                if(type.equals(AclPermission.class)) {
                    return (T) new DefaultAclFeature() {
                        @Override
                        public Acl getPermission(final Path file) throws BackgroundException {
                            fail();
                            return null;
                        }

                        @Override
                        public void setPermission(final Path file, final Acl acl) throws BackgroundException {
                            assertEquals(Acl.EMPTY, acl);
                        }

                        @Override
                        public List<Acl.User> getAvailableAclUsers() {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return super.getFeature(type);
            }
        });
    }

    @Test
    public void testRunNew() throws Exception {
        final Acl acl = new Acl(new Acl.EmailUser(), new Acl.Role("r"));
        final Path t = new Path("/t", EnumSet.of(Path.Type.file));
        final AtomicBoolean set = new AtomicBoolean();

        final Map<Path, List<Acl.UserAndRole>> original = new HashMap<>();
        original.put(t, Collections.emptyList());
        final Map<Acl.User, Acl.Role> overwrite = new HashMap<>();
        overwrite.put(new Acl.EmailUser(), new Acl.Role("r"));

        final WriteAclWorker worker = new WriteAclWorker(new AclOverwrite(original, overwrite), true, new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean result) {
                //
            }
        };
        worker.run(new NullSession(new Host(new TestProtocol())) {
                       @Override
                       @SuppressWarnings("unchecked")
                       public <T> T getFeature(Class<T> type) {
                           if(type.equals(AclPermission.class)) {
                               return (T) new DefaultAclFeature() {
                                   @Override
                                   public Acl getPermission(final Path file) throws BackgroundException {
                                       fail();
                                       return null;
                                   }

                                   @Override
                                   public void setPermission(final Path file, final Acl n) throws BackgroundException {
                                       assertEquals(acl, n);
                                       set.set(true);
                                   }

                                   @Override
                                   public List<Acl.User> getAvailableAclUsers() {
                                       throw new UnsupportedOperationException();
                                   }

                                   @Override
                                   public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
                                       throw new UnsupportedOperationException();
                                   }
                               };
                           }
                           return super.getFeature(type);
                       }
                   }
        );
        assertTrue(set.get()

        );
    }
}
