package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.test.NullLocal;
import ch.cyberduck.core.test.NullSession;

import org.junit.Test;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class ComparisonServiceFilterTest extends AbstractTestCase {

    @Test
    public void testCompareEqualResultFile() throws Exception {
        final AtomicBoolean found = new AtomicBoolean();
        final AtomicBoolean attr = new AtomicBoolean();
        ComparisonServiceFilter s = new ComparisonServiceFilter(new NullSession(new Host("t")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type == Find.class) {
                    return (T) new Find() {
                        @Override
                        public boolean find(final Path file) throws BackgroundException {
                            found.set(true);
                            return true;
                        }

                        @Override
                        public Find withCache(Cache<Path> cache) {
                            return this;
                        }
                    };
                }
                if(type == Attributes.class) {
                    return (T) new Attributes() {
                        @Override
                        public PathAttributes find(final Path file) throws BackgroundException {
                            attr.set(true);
                            return new PathAttributes() {
                                @Override
                                public String getChecksum() {
                                    return "a";
                                }
                            };
                        }

                        @Override
                        public Attributes withCache(Cache<Path> cache) {
                            return this;
                        }
                    };
                }
                return super.getFeature(type);
            }
        }, TimeZone.getDefault(), new DisabledProgressListener());
        assertEquals(Comparison.equal, s.compare(new Path("t", EnumSet.of(Path.Type.file)), new NullLocal("t") {
            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes("/t") {
                    @Override
                    public String getChecksum() {
                        return "a";
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
        ComparisonServiceFilter s = new ComparisonServiceFilter(new NullSession(new Host("t")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type == Find.class) {
                    return (T) new Find() {
                        @Override
                        public boolean find(final Path file) throws BackgroundException {
                            found.set(true);
                            return true;
                        }

                        @Override
                        public Find withCache(Cache<Path> cache) {
                            return this;
                        }
                    };
                }
                return super.getFeature(type);
            }
        }, TimeZone.getDefault(), new DisabledProgressListener());
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
        ComparisonServiceFilter s = new ComparisonServiceFilter(new NullSession(new Host("t")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type == Find.class) {
                    return (T) new Find() {
                        @Override
                        public boolean find(final Path file) throws BackgroundException {
                            found.set(true);
                            return false;
                        }

                        @Override
                        public Find withCache(Cache<Path> cache) {
                            return this;
                        }
                    };
                }
                return super.getFeature(type);
            }
        }, TimeZone.getDefault(), new DisabledProgressListener());
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
        ComparisonServiceFilter s = new ComparisonServiceFilter(new NullSession(new Host("t")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type == Find.class) {
                    return (T) new Find() {
                        @Override
                        public boolean find(final Path file) throws BackgroundException {
                            found.set(true);
                            return true;
                        }

                        @Override
                        public Find withCache(Cache<Path> cache) {
                            return this;
                        }
                    };
                }
                return super.getFeature(type);
            }
        }, TimeZone.getDefault(), new DisabledProgressListener());
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
        ComparisonServiceFilter s = new ComparisonServiceFilter(new NullSession(new Host("t")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type == Find.class) {
                    return (T) new Find() {
                        @Override
                        public boolean find(final Path file) throws BackgroundException {
                            found.set(true);
                            return true;
                        }

                        @Override
                        public Find withCache(Cache<Path> cache) {
                            return this;
                        }
                    };
                }
                if(type == Attributes.class) {
                    return (T) new Attributes() {
                        @Override
                        public PathAttributes find(final Path file) throws BackgroundException {
                            attr.set(true);
                            return new PathAttributes() {
                                @Override
                                public String getChecksum() {
                                    return "b";
                                }

                                @Override
                                public long getSize() {
                                    return 2L;
                                }

                                @Override
                                public long getModificationDate() {
                                    final Calendar c = Calendar.getInstance(TimeZone.getDefault());
                                    c.set(Calendar.HOUR_OF_DAY, 0);
                                    return c.getTimeInMillis();
                                }
                            };
                        }

                        @Override
                        public Attributes withCache(Cache<Path> cache) {
                            return this;
                        }
                    };
                }
                return super.getFeature(type);
            }
        }, TimeZone.getDefault(), new DisabledProgressListener());
        assertEquals(Comparison.local, s.compare(new Path("t", EnumSet.of(Path.Type.file)), new NullLocal("t") {
            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes("t") {
                    @Override
                    public String getChecksum() {
                        return "a";
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
