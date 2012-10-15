package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.NullAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.NullPathAttributes;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.local.Local;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class SizeComparisonServiceTest extends AbstractTestCase {
    @Test
    public void testCompare() throws Exception {
        ComparisonService s = new SizeComparisonService();
        assertEquals(Comparison.EQUAL, s.compare(new NullPath("t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
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

            @Override
            public PathAttributes attributes() {
                return new NullPathAttributes() {
                    @Override
                    public long getSize() {
                        return 1L;
                    }
                };
            }
        }));
        assertEquals(Comparison.UNEQUAL, s.compare(new NullPath("t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
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

            @Override
            public PathAttributes attributes() {
                return new NullPathAttributes() {
                    @Override
                    public long getSize() {
                        return 2L;
                    }
                };
            }
        }));
    }
}
