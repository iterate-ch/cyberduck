package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class TimestampComparisonServiceTest extends AbstractTestCase {

    @Test
    public void testCompareEqual() throws Exception {
        ComparisonService s = new TimestampComparisonService(TimeZone.getDefault());
        final long timestmap = Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
        assertEquals(Comparison.equal, s.compare(new Path("t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public Attributes attributes() {
                        return new PathAttributes(Path.FILE_TYPE) {
                            @Override
                            public long getModificationDate() {
                                return timestmap;
                            }
                        };
                    }
                };
            }

            @Override
            public PathAttributes attributes() {
                return new PathAttributes(Path.FILE_TYPE) {
                    @Override
                    public long getModificationDate() {
                        return timestmap;
                    }
                };
            }
        }));
        assertEquals(Comparison.equal, s.compare(new Path("t", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public Attributes attributes() {
                        return new PathAttributes(Path.FILE_TYPE) {
                            @Override
                            public long getModificationDate() {
                                return timestmap;
                            }
                        };
                    }
                };
            }

            @Override
            public PathAttributes attributes() {
                return new PathAttributes(Path.FILE_TYPE) {
                    @Override
                    public long getModificationDate() {
                        return timestmap;
                    }
                };
            }
        }));
    }

    @Test
    public void testCompareLocal() throws Exception {
        ComparisonService s = new TimestampComparisonService(TimeZone.getDefault());
        final long timestmap = Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
        assertEquals(Comparison.local, s.compare(new Path("t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public Attributes attributes() {
                        return new PathAttributes(Path.FILE_TYPE) {
                            @Override
                            public long getModificationDate() {
                                return Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
                            }
                        };
                    }
                };
            }

            @Override
            public PathAttributes attributes() {
                return new PathAttributes(Path.FILE_TYPE) {
                    @Override
                    public long getModificationDate() {
                        final Calendar c = Calendar.getInstance(TimeZone.getDefault());
                        c.set(Calendar.HOUR_OF_DAY, 0);
                        return c.getTimeInMillis();
                    }
                };
            }
        }));
    }
}
