package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.PathAttributes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class ChecksumComparisonServiceTest extends AbstractTestCase {

    @Test
    public void testCompare() throws Exception {
        ComparisonService s = new ChecksumComparisonService();
        assertEquals(Comparison.equal, s.compare(new PathAttributes() {
                                                     @Override
                                                     public String getChecksum() {
                                                         return "a";
                                                     }
                                                 }, new LocalAttributes("/t") {
                                                     @Override
                                                     public String getChecksum() {
                                                         return "a";
                                                     }
                                                 }
        ));

        assertEquals(Comparison.notequal, s.compare(new PathAttributes() {
                                                        @Override
                                                        public String getChecksum() {
                                                            return "b";
                                                        }
                                                    }, new LocalAttributes("/t") {
                                                        @Override
                                                        public String getChecksum() {
                                                            return "a";
                                                        }
                                                    }
        ));
    }

    @Test
    public void testDirectory() throws Exception {
        ComparisonService s = new ChecksumComparisonService();
        assertEquals(Comparison.notequal, s.compare(new PathAttributes(),
                new LocalAttributes("/t")));
    }
}
