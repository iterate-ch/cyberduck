package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;

import org.junit.Test;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultComparePathFilterTest {

    @Test
    public void testCompareEqualResultFile() throws Exception {
        final AtomicBoolean found = new AtomicBoolean();
        final AtomicBoolean attr = new AtomicBoolean();
        final AttributesFinder attributes = new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) {
                attr.set(true);
                return new PathAttributes() {
                    @Override
                    public Checksum getChecksum() {
                        return new Checksum(HashAlgorithm.md5, "a");
                    }
                };
            }
        };
        final Find find = new Find() {
            @Override
            public boolean find(final Path file, final ListProgressListener listener) {
                found.set(true);
                return true;
            }
        };
        ComparePathFilter s = new DefaultComparePathFilter(new NullSession(new Host(new TestProtocol()))) {
            @Override
            protected Checksum checksum(final HashAlgorithm algorithm, final Local local) {
                return new Checksum(HashAlgorithm.md5, "a");
            }
        }.withFinder(find).withAttributes(attributes);
        final String path = new AlphanumericRandomStringService().random();
        assertEquals(Comparison.equal, s.compare(new Path(path, EnumSet.of(Path.Type.file)), new NullLocal(path) {
            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes(path);
            }

            @Override
            public boolean exists() {
                return true;
            }
        }, new DisabledProgressListener()));
        assertTrue(found.get());
        assertTrue(attr.get());
    }

    @Test
    public void testCompareEqualResultDirectory() throws Exception {
        final AtomicBoolean found = new AtomicBoolean();
        final Find find = new Find() {
            @Override
            public boolean find(final Path file, final ListProgressListener listener) {
                found.set(true);
                return true;
            }
        };
        ComparePathFilter s = new DefaultComparePathFilter(new NullSession(new Host(new TestProtocol())) {
        }).withFinder(find);
        assertEquals(Comparison.equal, s.compare(new Path("t", EnumSet.of(Path.Type.directory)), new NullLocal("t") {
            @Override
            public boolean exists() {
                return true;
            }
        }, new DisabledProgressListener()));
        assertTrue(found.get());
    }

    @Test
    public void testCompareLocalOnlyDirectory() throws Exception {
        final AtomicBoolean found = new AtomicBoolean();
        final Find find = new Find() {
            @Override
            public boolean find(final Path file, final ListProgressListener listener) {
                found.set(true);
                return false;
            }
        };
        ComparePathFilter s = new DefaultComparePathFilter(new NullSession(new Host(new TestProtocol())) {
        }).withFinder(find);
        assertEquals(Comparison.local, s.compare(new Path("t", EnumSet.of(Path.Type.directory)), new NullLocal("t") {
            @Override
            public boolean exists() {
                return true;
            }
        }, new DisabledProgressListener()));
        assertTrue(found.get());
    }

    @Test
    public void testCompareRemoteOnlyDirectory() throws Exception {
        final AtomicBoolean found = new AtomicBoolean();
        final Find find = new Find() {
            @Override
            public boolean find(final Path file, final ListProgressListener listener) {
                found.set(true);
                return true;
            }
        };
        ComparePathFilter s = new DefaultComparePathFilter(new NullSession(new Host(new TestProtocol())) {
        }).withFinder(find);
        assertEquals(Comparison.remote, s.compare(new Path("t", EnumSet.of(Path.Type.directory)), new NullLocal("t") {
            @Override
            public boolean exists() {
                return false;
            }
        }, new DisabledProgressListener()));
        assertTrue(found.get());
    }

    @Test
    public void testCompareLocalResult() throws Exception {
        final AtomicBoolean found = new AtomicBoolean();
        final AtomicBoolean attr = new AtomicBoolean();
        final Find find = new Find() {
            @Override
            public boolean find(final Path file, final ListProgressListener listener) {
                found.set(true);
                return true;
            }
        };
        final AttributesFinder attributes = new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) {
                attr.set(true);
                return new PathAttributes() {
                    @Override
                    public Checksum getChecksum() {
                        return new Checksum(HashAlgorithm.md5, "b");
                    }

                    @Override
                    public long getSize() {
                        return 2L;
                    }

                    @Override
                    public long getModificationDate() {
                        final Calendar c = Calendar.getInstance(TimeZone.getDefault());
                        c.set(Calendar.HOUR_OF_DAY, 0);
                        c.set(Calendar.MINUTE, 0);
                        c.set(Calendar.SECOND, 0);
                        c.set(Calendar.MILLISECOND, 0);
                        return c.getTimeInMillis();
                    }
                };
            }
        };
        ComparePathFilter s = new DefaultComparePathFilter(new NullSession(new Host(new TestProtocol()))) {
            @Override
            protected Checksum checksum(final HashAlgorithm algorithm, final Local local) {
                return new Checksum(HashAlgorithm.md5, "a");
            }
        }.withFinder(find).withAttributes(attributes);
        assertEquals(Comparison.local, s.compare(new Path("t", EnumSet.of(Path.Type.file)), new NullLocal("t") {
            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes("t") {

                    @Override
                    public long getSize() {
                        return 1L;
                    }

                    @Override
                    public long getModificationDate() {
                        return Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
                    }
                };
            }

            @Override
            public boolean exists() {
                return true;
            }
        }, new DisabledProgressListener()));
        assertTrue(found.get());
        assertTrue(attr.get());
    }
}
