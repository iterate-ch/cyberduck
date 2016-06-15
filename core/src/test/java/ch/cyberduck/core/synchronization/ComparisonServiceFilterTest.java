package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
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

public class ComparisonServiceFilterTest {

    @Test
    public void testCompareEqualResultFile() throws Exception {
        final AtomicBoolean found = new AtomicBoolean();
        final AtomicBoolean attr = new AtomicBoolean();
        final Attributes attributes = new Attributes() {
            @Override
            public PathAttributes find(final Path file) throws BackgroundException {
                attr.set(true);
                return new PathAttributes() {
                    @Override
                    public Checksum getChecksum() {
                        return new Checksum(HashAlgorithm.md5, "a");
                    }
                };
            }

            @Override
            public Attributes withCache(PathCache cache) {
                return this;
            }
        };
        final Find find = new Find() {
            @Override
            public boolean find(final Path file) throws BackgroundException {
                found.set(true);
                return true;
            }

            @Override
            public Find withCache(PathCache cache) {
                return this;
            }
        };
        ComparisonServiceFilter s = new ComparisonServiceFilter(new NullSession(new Host(new TestProtocol())) {
        }, TimeZone.getDefault(), new DisabledProgressListener()).withFinder(find).withAttributes(attributes);
        assertEquals(Comparison.equal, s.compare(new Path("t", EnumSet.of(Path.Type.file)), new NullLocal("t") {
            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes("/t") {
                    @Override
                    public Checksum getChecksum() {
                        return new Checksum(HashAlgorithm.md5, "a");
                    }
                };
            }

            @Override
            public boolean exists() {
                return true;
            }
        }));
        assertTrue(found.get());
        assertTrue(attr.get());
    }

    @Test
    public void testCompareEqualResultDirectory() throws Exception {
        final AtomicBoolean found = new AtomicBoolean();
        final Find find = new Find() {
            @Override
            public boolean find(final Path file) throws BackgroundException {
                found.set(true);
                return true;
            }

            @Override
            public Find withCache(PathCache cache) {
                return this;
            }
        };
        ComparisonServiceFilter s = new ComparisonServiceFilter(new NullSession(new Host(new TestProtocol())) {
        }, TimeZone.getDefault(), new DisabledProgressListener()).withFinder(find);
        assertEquals(Comparison.equal, s.compare(new Path("t", EnumSet.of(Path.Type.directory)), new NullLocal("t") {
            @Override
            public boolean exists() {
                return true;
            }
        }));
        assertTrue(found.get());
    }

    @Test
    public void testCompareLocalOnlyDirectory() throws Exception {
        final AtomicBoolean found = new AtomicBoolean();
        final Find find = new Find() {
            @Override
            public boolean find(final Path file) throws BackgroundException {
                found.set(true);
                return false;
            }

            @Override
            public Find withCache(PathCache cache) {
                return this;
            }
        };
        ComparisonServiceFilter s = new ComparisonServiceFilter(new NullSession(new Host(new TestProtocol())) {
        }, TimeZone.getDefault(), new DisabledProgressListener()).withFinder(find);
        assertEquals(Comparison.local, s.compare(new Path("t", EnumSet.of(Path.Type.directory)), new NullLocal("t") {
            @Override
            public boolean exists() {
                return true;
            }
        }));
        assertTrue(found.get());
    }

    @Test
    public void testCompareRemoteOnlyDirectory() throws Exception {
        final AtomicBoolean found = new AtomicBoolean();
        final Find find = new Find() {
            @Override
            public boolean find(final Path file) throws BackgroundException {
                found.set(true);
                return true;
            }

            @Override
            public Find withCache(PathCache cache) {
                return this;
            }
        };
        ComparisonServiceFilter s = new ComparisonServiceFilter(new NullSession(new Host(new TestProtocol())) {
        }, TimeZone.getDefault(), new DisabledProgressListener()).withFinder(find);
        assertEquals(Comparison.remote, s.compare(new Path("t", EnumSet.of(Path.Type.directory)), new NullLocal("t") {
            @Override
            public boolean exists() {
                return false;
            }
        }));
        assertTrue(found.get());
    }

    @Test
    public void testCompareLocalResult() throws Exception {
        final AtomicBoolean found = new AtomicBoolean();
        final AtomicBoolean attr = new AtomicBoolean();
        final Find find = new Find() {
            @Override
            public boolean find(final Path file) throws BackgroundException {
                found.set(true);
                return true;
            }

            @Override
            public Find withCache(PathCache cache) {
                return this;
            }
        };
        final Attributes attributes = new Attributes() {
            @Override
            public PathAttributes find(final Path file) throws BackgroundException {
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

            @Override
            public Attributes withCache(PathCache cache) {
                return this;
            }
        };
        ComparisonServiceFilter s = new ComparisonServiceFilter(new NullSession(new Host(new TestProtocol())) {
        }, TimeZone.getDefault(), new DisabledProgressListener()).withFinder(find).withAttributes(attributes);
        assertEquals(Comparison.local, s.compare(new Path("t", EnumSet.of(Path.Type.file)), new NullLocal("t") {
            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes("t") {
                    @Override
                    public Checksum getChecksum() {
                        return new Checksum(HashAlgorithm.md5, "a");
                    }

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
        }));
        assertTrue(found.get());
        assertTrue(attr.get());
    }
}
