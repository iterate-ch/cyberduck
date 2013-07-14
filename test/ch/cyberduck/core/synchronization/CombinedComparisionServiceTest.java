package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.local.Local;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class CombinedComparisionServiceTest extends AbstractTestCase {

    @Test
    public void testCompare() throws Exception {
        ComparisonService s = new CombinedComparisionService(new FTPSession(new Host("t")) {
            @Override
            public boolean exists(final Path path) throws BackgroundException {
                return true;
            }
        });
        assertEquals(Comparison.EQUAL, s.compare(new Path("t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public Attributes attributes() {
                        return new PathAttributes(Path.FILE_TYPE) {
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
                };
            }

            @Override
            public PathAttributes attributes() {
                return new PathAttributes(Path.FILE_TYPE) {
                    @Override
                    public String getChecksum() {
                        return "a";
                    }
                };
            }
        }));
        assertEquals(Comparison.LOCAL_NEWER, s.compare(new Path("t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public Attributes attributes() {
                        return new PathAttributes(Path.FILE_TYPE) {
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
                                return Calendar.getInstance().getTimeInMillis();
                            }
                        };
                    }

                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }

            @Override
            public PathAttributes attributes() {
                return new PathAttributes(Path.FILE_TYPE) {
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
        }));
    }
}
